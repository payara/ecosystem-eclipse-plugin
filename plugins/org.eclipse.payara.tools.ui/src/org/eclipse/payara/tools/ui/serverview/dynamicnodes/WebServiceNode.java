/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.serverview.dynamicnodes;

import java.util.ArrayList;

import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.payara.tools.serverview.WSDesc;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 *
 * A deployed web service node in the server view
 *
 * @author Ludovic Champenois
 */
public class WebServiceNode extends TreeNode {

    private PayaraServer server;
    private WSDesc app;

    public WebServiceNode(DeployedWebServicesNode root, PayaraServer server, WSDesc app) {
        super(app.getName(), null, root);
        this.server = server;
        this.app = app;
    }

    public PayaraServer getServer() {
        return this.server;
    }

    public WSDesc getWSInfo() {
        return this.app;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        ArrayList<IPropertyDescriptor> properties = new ArrayList<>();
        
        properties.add(new TextPropertyDescriptor("testurl", "Test URL"));
        properties.add(new TextPropertyDescriptor("name", "name"));
        properties.add(new TextPropertyDescriptor("wsdlurl", "WSDL URL"));

        return properties.toArray(new IPropertyDescriptor[0]);
    }

    @Override

    public Object getPropertyValue(Object id) {
        if (id.equals("testurl")) {
            return app.getTestURL();
        }

        if (id.equals("name")) {
            return app.getName();
        }

        if (id.equals("wsdlurl")) {
            return app.getWsdlUrl();
        }

        return null;
    }

}
