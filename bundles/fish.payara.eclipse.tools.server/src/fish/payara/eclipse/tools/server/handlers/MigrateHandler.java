/******************************************************************************
 * Copyright (c) 2023 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

public class MigrateHandler extends AbstractHandler {
	
	public static final String PAYARA_TRANSFORMER = "fish.payara.transformer";
	public static final String PAYARA_TRANSFORMER_MAVEN = "fish.payara.transformer.maven";
	public static final String PAYARA_TRANSFORMER_VERSION = "0.2.14";
	
	private IPath resourcePath = null;
	private IPath projectPath = null;
	private String name = "";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
    	Shell shell = activeWorkbenchWindow.getShell();
    	IStructuredSelection selection = getSelection(event);
        if (selection != null && !selection.isEmpty()) {
            Object firstElement = selection.getFirstElement();
            
            final boolean isFile = checkIfFile(firstElement);
            if (resourcePath != null && projectPath != null && !"".equals(name)) {
                String srcPath = resourcePath.toOSString();
                String srcProjectPath = projectPath.toOSString();
                String destinationPath = chooseDestinationPath(srcProjectPath, name, isFile);
                if ("".equals(destinationPath)) return null;
				
	            Job job = new Job("Running Maven Command") {
	                @Override
	                protected IStatus run(IProgressMonitor monitor) {
	                	Display.getDefault().asyncExec(() -> {
	                        try {
	                            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	                            IConsoleView consoleView = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
	                            MessageConsole console = new MessageConsole("Maven Command Console", null);
	                            ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
	                            consoleView.display(console);
	                            
	                            int exitCode = runMvnCommand(console, page, srcProjectPath, srcPath, destinationPath, shell);
	    	                    if (exitCode == 0) {
	    	                        Display.getDefault().asyncExec(() -> {
	    	                            MessageDialog.openInformation(shell, "Success", (isFile ? "File " : "Project ") + destinationPath + " created successfully.");
	    	                        });
	    	                    } else {
	    	                        Display.getDefault().asyncExec(() -> {
	    	                            MessageDialog.openError(shell, "Error", "Maven command failed with exit code " + exitCode + ".");
	    	                        });
	    	                    }
	                        } catch (PartInitException e) {
	                            e.printStackTrace();
	                        }
	                    });
	                    return Status.OK_STATUS;
	                }
	            };
	            job.schedule();
            }
        }
        return null;
    }
    
    private boolean checkIfFile(Object firstElement) {
    	if (firstElement instanceof IResource) {
        	IResource resource = (IResource) firstElement;
            resourcePath = resource.getLocation();
            projectPath = resourcePath;
            name = resource.getName();
        } else if (firstElement instanceof IJavaProject) {
        	IJavaProject javaProject = (IJavaProject) firstElement;
            IProject project = javaProject.getProject();
			resourcePath = project.getLocation();
			projectPath = resourcePath;
            name = project.getName();
        } else if (firstElement instanceof ICompilationUnit) {
        	ICompilationUnit file = (ICompilationUnit) firstElement;
        	resourcePath = file.getResource().getLocation();
        	projectPath = file.getJavaProject().getProject().getLocation();
        	name = file.getElementName();
        	return true;
        }
		return false;
	}

	private String chooseDestinationPath(String srcPath, String name, boolean isFile) {
    	Shell shell = new Shell();
    	DirectoryDialog dialog = new DirectoryDialog(shell);
    	dialog.setText("Choose a " + (isFile ? "New File" : "") + " destination Folder");
    	dialog.setMessage("Please select a Directory:");
    	dialog.setFilterPath(srcPath);
    	String selectedDirectory = dialog.open();
    	if (selectedDirectory != null) {
			return selectedDirectory + "/" + name;
    	}
    	return "";
    }

    private IStructuredSelection getSelection(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection) selection;
        }
        return null;
    }
    
    private int runMvnCommand(MessageConsole console, IWorkbenchPage page, String srcProjectPath, String srcPath, String targetPath, Shell shell) {
        try {
            MessageConsoleStream consoleStream = console.newMessageStream();

            List<String> command = new ArrayList<>();
            command.add(getMvnCommand());
            command.add(getTransformCommand());
            command.add("-DselectedSource=" + srcPath);
            command.add("-DselectedTarget=" + targetPath);
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.environment().put("PATH", System.getenv("PATH"));
            builder.directory(new File(srcProjectPath));
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                consoleStream.println(line);
            }

            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            MessageDialog.openError(shell, "Error",
                    "An error occurred while running the Maven command: " + e.getMessage());
            return -1;
        }
    }

    private String getTransformCommand() {
        return String.format("%s:%s:%s:run",
                PAYARA_TRANSFORMER, PAYARA_TRANSFORMER_MAVEN, PAYARA_TRANSFORMER_VERSION);
    }
    
    private String getMvnCommand() {
    	String osName = System.getProperty("os.name");
    	if (osName.contains("Windows")) {
    	    return "mvn.cmd";
    	} else {
    		return "mvn";
    	}
    }

}
