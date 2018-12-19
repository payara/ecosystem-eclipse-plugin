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
import static org.eclipse.payara.tools.utils.URIHelper.getModuleDeployURI;
import static org.eclipse.payara.tools.utils.URIHelper.showURI;
import static org.eclipse.payara.tools.utils.WtpUtil.load;
import static org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.payara.tools.server.deploying.PayaraServerBehaviour;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;

/**
 * This action opens an assembled module (e.g. a .war archive or exploded folder) that's deployed to a Payara server 
 * in the file browser from the operating system.
 * 
 * <p>
 * Inspecting the actual assembled module is sometimes necessary to resolve and diagnose deployment errors.
 *   
 * @author Arjan Tijms
 *
 */
public class OpenModuleInFileBrowserAction extends Action {

    ISelection selection;

    public OpenModuleInFileBrowserAction(ISelection selection) {
        setText("Open in File Browser");

        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setImageDescriptor(sharedImages.getImageDescriptor(IMG_OBJ_FOLDER));

        this.selection = selection;
    }

    @Override
    public void runWithEvent(Event event) {
        if (selection instanceof TreeSelection) {
            TreeSelection ts = (TreeSelection) selection;
            Object firstElement = ts.getFirstElement();
            if (firstElement instanceof IServerModule) {

                IServerModule module = (IServerModule) firstElement;

                IServer server = module.getServer();
                IModule[] modules = module.getModule();

                if (modules.length > 0) {

                    PayaraServerBehaviour serverBehaviour = load(server, PayaraServerBehaviour.class);
                    
                    try {
                        showURI(getModuleDeployURI(serverBehaviour, modules[0]));
                    } catch (Exception e) {
                        logMessage("Error opening browser: " + e.getMessage());
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
