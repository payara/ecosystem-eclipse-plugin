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

package fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes;

import static fish.payara.eclipse.tools.server.utils.NodesUtils.getWebServices;

import java.util.ArrayList;
import java.util.List;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.PayaraServerPlugin;
import fish.payara.eclipse.tools.server.serverview.WSDesc;

/**
 * This node shows is the root node below which the dynamically retrieved web-services reside.
 *
 * <p>
 * The following depicts this element in the "Servers" view:
 * </p>
 * <p>
 *
 * <pre>
 * Payara 5 [domain1]
 * |- GlassFish Management
 *     |-Resources
 *     |-Deployed Applications
 *     |-Deployed Web Services *
 * |- [WTP managed application]
 * </pre>
 * </p>
 *
 * <p>
 * Payara / GlassFish is dynamically queried for this list, hence it can only be retrieved for a
 * running server. </>
 *
 */
public class DeployedWebServicesNode extends TreeNode {

    private PayaraServer server;
    private WebServiceNode[] deployedapps;

    public DeployedWebServicesNode(PayaraServer server) {
        super("Deployed Web Services", null, null);
        this.server = server;
    }

    public PayaraServer getServer() {
        return server;
    }

    @Override
    public Object[] getChildren() {

        ArrayList<WebServiceNode> appsList = new ArrayList<>();

        if (deployedapps == null) {

            try {

                if (server == null) {
                    deployedapps = appsList.toArray(new WebServiceNode[appsList.size()]);
                    return deployedapps;
                }

                try {
                    List<WSDesc> wss = getWebServices(server);

                    for (WSDesc app : wss) {

                        WebServiceNode t = new WebServiceNode(this, server,

                                app);

                        appsList.add(t);

                    }

                } catch (Exception ex) {

                    PayaraServerPlugin.logError("get Applications is failing=", ex); //$NON-NLS-1$

                }

            } catch (Exception e) {

            }

            this.deployedapps = appsList

                    .toArray(new WebServiceNode[appsList.size()]);

        }

        return this.deployedapps;

    }

    public void refresh() {

        this.deployedapps = null;

    }

}
