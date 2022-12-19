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

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.serverview.AppDesc;

/**
 * A deployed app node in the server view
 *
 * <p>
 * This node represents each application below the "Deployed Applications" parent node.
 * </p>
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
 *         |- App1 *
 *         |- App2 *
 *     |-Deployed Web Services
 * |- [WTP managed application]
 * </pre>
 * </p>
 *
 * <p>
 * Payara / GlassFish is dynamically queried for this list, hence it can only be retrieved for a
 * running server. </>
 *
 * @author Ludovic Champenois
 *
 */
public class ApplicationNode extends TreeNode {

    DeployedApplicationsNode parent;
    PayaraServer server;
    TreeNode[] modules;
    AppDesc app;

    public ApplicationNode(DeployedApplicationsNode root, PayaraServer server, AppDesc app) {
        super(app.getName(), null, root);
        this.server = server;
        this.app = app;
    }

    public PayaraServer getServer() {
        return this.server;
    }

    public AppDesc getApplicationInfo() {
        return this.app;
    }

    @Override

    public IPropertyDescriptor[] getPropertyDescriptors() {

        ArrayList<IPropertyDescriptor> properties = new ArrayList<>();
        PropertyDescriptor pd;

        pd = new TextPropertyDescriptor("contextroot", "context root");
        pd.setCategory("Payara Applications");
        properties.add(pd);

        pd = new TextPropertyDescriptor("name", "name");
        pd.setCategory("Payara Applications");
        properties.add(pd);

        pd = new TextPropertyDescriptor("path", "path");
        pd.setCategory("Payara Applications");
        properties.add(pd);

        pd = new TextPropertyDescriptor("engine", "engine");
        pd.setCategory("Payara Applications");
        properties.add(pd);

        return properties.toArray(new IPropertyDescriptor[0]);
    }

    @Override

    public Object getPropertyValue(Object id) {

        if (id.equals("contextroot")) {
            return app.getContextRoot();
        }

        if (id.equals("name")) {
            return app.getName();
        }

        if (id.equals("path")) {
            return app.getPath();
        }

        if (id.equals("engine")) {
            return app.getType();
        }

        return null;

    }

}
