/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.serverview.actions;

import static org.eclipse.payara.tools.utils.Utils.getHttpListenerProtocol;
import static org.eclipse.ui.ISharedImages.IMG_TOOL_FORWARD;
import static org.eclipse.ui.ISharedImages.IMG_TOOL_FORWARD_DISABLED;
import static org.eclipse.ui.IWorkbenchCommandConstants.FILE_PRINT;
import static org.eclipse.ui.browser.IWorkbenchBrowserSupport.LOCATION_BAR;
import static org.eclipse.ui.browser.IWorkbenchBrowserSupport.NAVIGATION_BAR;

import java.net.URI;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.payara.tools.PayaraToolsPlugin;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.payara.tools.server.deploying.PayaraServerBehaviour;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.DeployedApplicationsNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.TreeNode;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class OpenInBrowserAction extends Action {

    ISelection selection;

    public OpenInBrowserAction(ISelection selection) {
        setText("Open in Browser");

        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setImageDescriptor(sharedImages.getImageDescriptor(IMG_TOOL_FORWARD));
        setDisabledImageDescriptor(sharedImages.getImageDescriptor(IMG_TOOL_FORWARD_DISABLED));
        setActionDefinitionId(FILE_PRINT);

        this.selection = selection;
    }

    @Override
    public void runWithEvent(Event event) {
        if (selection instanceof TreeSelection) {
            TreeSelection ts = (TreeSelection) selection;
            Object obj = ts.getFirstElement();
            if (obj instanceof TreeNode) {
                TreeNode module = (TreeNode) obj;
                DeployedApplicationsNode target = (DeployedApplicationsNode) module.getParent();

                try {
                    PayaraServerBehaviour be = target.getServer().getServerBehaviourAdapter();

                    IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
                    IWebBrowser browser = browserSupport.createBrowser(LOCATION_BAR | NAVIGATION_BAR, null, null, null);
                    PayaraServer server = be.getPayaraServerDelegate();
                    String host = server.getServer().getHost();
                    int port = server.getPort();

                    URI uri = new URI(getHttpListenerProtocol(host, port), null, host, port, "/" + module.getName(), null, null); // NOI18N
                    browser.openURL(uri.toURL());

                } catch (Exception e) {
                    PayaraToolsPlugin.logMessage("Error opening browser: " + e.getMessage());
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
