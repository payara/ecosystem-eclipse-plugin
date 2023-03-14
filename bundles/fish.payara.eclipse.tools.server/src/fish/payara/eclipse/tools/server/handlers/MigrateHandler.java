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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
	 public static final String PAYARA_TRANSFORMER_VERSION = "0.2.10";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
    	
    	IStructuredSelection selection = getSelection(event);
        if (selection != null && !selection.isEmpty()) {
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof IResource) {
                IResource resource = (IResource) firstElement;
                IPath path = resource.getLocation();
                if (path != null) {
                    String folderPath = path.toOSString();
                    MessageDialog.openInformation(shell, "Folder Path", folderPath);
                    runMvnCommand(folderPath, folderPath + "-jakartaEE10");
                }
            }
        }
    	
        return null;
    }
    
    private IStructuredSelection getSelection(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection) selection;
        }
        return null;
    }
    
    private void runMvnCommand(String srcPath, String targetPath) {
    	Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        
    	// Create a new message console
        MessageConsole console = new MessageConsole("Maven Command Console", null);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});
        
        // Get the console stream
        MessageConsoleStream consoleStream = console.newMessageStream();
        
        // Run the Maven command
        try {
        	List<String> command = new ArrayList<>();
        	command.add("mvn.cmd");
        	command.add("package");
        	command.add(getTransformCommand());
        	command.add("-DselectedSource=" + srcPath);
        	command.add("-DselectedTarget=" + targetPath);
        	ProcessBuilder builder = new ProcessBuilder(command);
        	builder.environment().put("PATH", System.getenv("PATH"));
        	builder.directory(new File(srcPath));
        	builder.redirectErrorStream(true);
        	Process process = builder.start();
            
            // Display the output in the console
        	BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        	String line = null;
            while ((line = reader.readLine()) != null) {
                consoleStream.println(line);
            }
            
            // Wait for the process to finish
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                MessageDialog.openInformation(shell, "Success", "Maven command completed successfully.");
            } else {
                MessageDialog.openError(shell, "Error", "Maven command failed with exit code " + exitCode + ".");
            }
            
        } catch (IOException | InterruptedException e) {
            MessageDialog.openError(shell, "Error", "An error occurred while running the Maven command: " + e.getMessage());
        }
    }
    
    private String getTransformCommand() {
        return String.format("%s:%s:%s:run",
                PAYARA_TRANSFORMER, PAYARA_TRANSFORMER_MAVEN, PAYARA_TRANSFORMER_VERSION);
    }

}
