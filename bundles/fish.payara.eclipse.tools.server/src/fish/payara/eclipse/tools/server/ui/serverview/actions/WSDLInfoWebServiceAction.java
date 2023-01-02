/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.ui.serverview.actions;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.PayaraServerPlugin;
import fish.payara.eclipse.tools.server.deploying.PayaraServerBehaviour;
import fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes.DeployedWebServicesNode;
import fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes.WebServiceNode;
import fish.payara.eclipse.tools.server.utils.Utils;

public class WSDLInfoWebServiceAction extends Action {
    ISelection selection;

    public WSDLInfoWebServiceAction(ISelection selection) {
        setText("Show WDSL content in Browser");

        this.selection = selection;
    }

    @Override
    public void runWithEvent(Event event) {
        if (selection instanceof TreeSelection) {
            TreeSelection ts = (TreeSelection) selection;
            Object obj = ts.getFirstElement();
            if (obj instanceof WebServiceNode) {
                final WebServiceNode module = (WebServiceNode) obj;
                final DeployedWebServicesNode target = (DeployedWebServicesNode) module
                        .getParent();

                try {
                    final PayaraServerBehaviour be = target
                            .getServer().getServerBehaviourAdapter();

                    IWorkbenchBrowserSupport browserSupport = PlatformUI
                            .getWorkbench().getBrowserSupport();
                    IWebBrowser browser = browserSupport
                            .createBrowser(
                                    IWorkbenchBrowserSupport.LOCATION_BAR
                                            | IWorkbenchBrowserSupport.NAVIGATION_BAR,
                                    null, null, null);
                    PayaraServer server = be.getPayaraServerDelegate();
                    String host = server.getServer().getHost();
                    int port = server.getPort();

                    String url = Utils.getHttpListenerProtocol(host, port) + "://" + host + ":" + port + "/" + module.getWSInfo().getName();
                    browser.openURL(new URL(url));

                } catch (Exception e) {
                    PayaraServerPlugin.logMessage("Error opening browser: "
                            + e.getMessage());
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
