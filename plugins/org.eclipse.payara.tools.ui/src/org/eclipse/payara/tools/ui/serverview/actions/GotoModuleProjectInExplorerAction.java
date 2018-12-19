/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.serverview.actions;

import static org.eclipse.payara.tools.PayaraToolsPlugin.logMessage;
import static org.eclipse.ui.ISharedImages.IMG_TOOL_FORWARD;
import static org.eclipse.ui.ISharedImages.IMG_TOOL_FORWARD_DISABLED;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.ui.IServerModule;

/**
 * This action allows the user to navigate from the a module deployed to a server in the servers view
 * to the project from which the module was created in the project explorer. 
 * 
 * @author Arjan Tijms
 *
 */
public class GotoModuleProjectInExplorerAction extends Action {

    private ISelection selection;

    public GotoModuleProjectInExplorerAction(ISelection selection) {
        setText("Goto Project");

        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setImageDescriptor(sharedImages.getImageDescriptor(IMG_TOOL_FORWARD));
        setDisabledImageDescriptor(sharedImages.getImageDescriptor(IMG_TOOL_FORWARD_DISABLED));

        this.selection = selection;
    }

    @Override
    public void runWithEvent(Event event) {
        if (selection instanceof TreeSelection) {
            TreeSelection treeSelection = (TreeSelection) selection;
            Object firstElement = treeSelection.getFirstElement();
            if (firstElement instanceof IServerModule) {

                IServerModule serverModule = (IServerModule) firstElement;
                IModule[] modules = serverModule.getModule();

                // Make sure we have a selection that actually contains modules
                if (modules.length > 0) {
                    
                    // Get the project associated with the selection
                    IProject project = modules[0].getProject();
                    
                    try {
                        
                        // Obtain the Project Explorer
                        ProjectExplorer projectExplorer = (ProjectExplorer) PlatformUI.getWorkbench()
                                  .getActiveWorkbenchWindow()
                                  .getActivePage()
                                  .showView(ProjectExplorer.VIEW_ID);
                        
                        // Give focus to the explorer
                        projectExplorer.setFocus();
                        
                        // Set the selection within the explorer to the project
                        projectExplorer.selectReveal(new StructuredSelection(project));

                        // Open the project node (expand nodes so the project and its direct children are visible)
                        projectExplorer.getCommonViewer().expandToLevel(project, 1);
                    } catch (Exception e) {
                        logMessage("Error navigating to project: " + e.getMessage());
                    }
                }
            }
          
            super.run();
        }
    }

    @Override
    public void run() {
        this.runWithEvent(null);
    }

}
