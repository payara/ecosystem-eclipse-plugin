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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

public class MigrateHandler extends AbstractHandler {
	
	public static final String PAYARA_TRANSFORMER = "fish.payara.transformer";
	public static final String PAYARA_TRANSFORMER_MAVEN = "fish.payara.transformer.maven";
	public static final String PAYARA_TRANSFORMER_VERSION = "0.2.15";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
    	IStructuredSelection selection = getSelection(event);
        if (selection != null && !selection.isEmpty()) {
            Object firstElement = selection.getFirstElement();
            IPath resourcePath = null;
            IPath projectPath = null;
            String name = "";
            boolean isFile = false;
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
            	isFile = true;
            }
            if (resourcePath != null && projectPath != null && !"".equals(name)) {
                String srcPath = resourcePath.toOSString();
                String srcProjectPath = projectPath.toOSString();
                String destinationPath = chooseDestinationPath(srcProjectPath, name, isFile);
                if ("".equals(destinationPath)) return null;
				int exitCode = runMvnCommand(srcProjectPath, srcPath, destinationPath);
	            if (exitCode == 0) {
	                MessageDialog.openInformation(shell, "Success", "Project " + destinationPath + " created successfully.");
	            } else {
	                MessageDialog.openError(shell, "Error", "Maven command failed with exit code " + exitCode + ".");
	            }
            }
        }
        return null;
    }
    
    private String chooseDestinationPath(String srcPath, String name, boolean isFile) {
    	Shell shell = new Shell();
    	DirectoryDialog dialog = new DirectoryDialog(shell);
    	dialog.setText("Choose a " + (isFile ? "New File" : "") + " destination Folder");
    	dialog.setMessage("Please select a Directory:");
    	dialog.setFilterPath(srcPath);
    	String selectedDirectory = dialog.open();
    	if (selectedDirectory != null) {
    		if (isFile) {
    			String targetDir = selectedDirectory + "/jakartaee10/";
    			try {
					final Path path = Paths.get(targetDir);
					if (!Files.exists(path)) {
						Files.createDirectories(path);
					}
					return targetDir + name;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
    		}
    	    return selectedDirectory + "/" + name + "-JakartaEE10";
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
    
    private int runMvnCommand(String srcProjectPath, String srcPath, String targetPath) {
    	Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        
        MessageConsole console = new MessageConsole("Maven Command Console", null);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});
        
        MessageConsoleStream consoleStream = console.newMessageStream();
        
        try {
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
            MessageDialog.openError(shell, "Error", "An error occurred while running the Maven command: " + e.getMessage());
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
