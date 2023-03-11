package fish.payara.eclipse.tools.server.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class MigrateHandler extends AbstractHandler {

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

}
