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

package fish.payara.eclipse.tools.server.ui.serverview;

import static fish.payara.eclipse.tools.server.PayaraServerPlugin.EAR_MODULE_IMG;
import static fish.payara.eclipse.tools.server.PayaraServerPlugin.EJB_MODULE_IMG;
import static fish.payara.eclipse.tools.server.PayaraServerPlugin.GF_SERVER_IMG;
import static fish.payara.eclipse.tools.server.PayaraServerPlugin.RESOURCES_IMG;
import static fish.payara.eclipse.tools.server.PayaraServerPlugin.WEBSERVICE_IMG;
import static fish.payara.eclipse.tools.server.PayaraServerPlugin.WEB_MODULE_IMG;

import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import fish.payara.eclipse.tools.server.PayaraServerPlugin;
import fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes.ApplicationNode;
import fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes.DeployedWebServicesNode;
import fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes.ResourcesNode;
import fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes.TreeNode;
import fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes.WebServiceNode;

/**
 * This provides provides the icons and text associated with the dynamic nodes provided by
 * {@link ServerViewDynamicNodeProvider}
 *
 */
public class ServerViewLabelProvider extends LabelProvider implements ITableFontProvider {

    @Override
    public Image getImage(Object element) {
        if (element instanceof ApplicationNode) {
            switch (((ApplicationNode) element).getApplicationInfo().getType()) {
            case "web":
                return PayaraServerPlugin.getImage(WEB_MODULE_IMG);
            case "ejb":
                return PayaraServerPlugin.getImage(EJB_MODULE_IMG);
            case "ear":
                return PayaraServerPlugin.getImage(EAR_MODULE_IMG);
            }
        } else if (element instanceof ResourcesNode) {
            ResourcesNode rn = (ResourcesNode) element;

            if (rn.getResource() == null) {
                return PayaraServerPlugin.getImage(RESOURCES_IMG);
            }

            return PayaraServerPlugin.getImage(GF_SERVER_IMG);
        } else if (element instanceof DeployedWebServicesNode) {
            return PayaraServerPlugin.getImage(WEBSERVICE_IMG);
        } else if (element instanceof WebServiceNode) {
            return PayaraServerPlugin.getImage(WEBSERVICE_IMG);
        }

        return PayaraServerPlugin.getImage(GF_SERVER_IMG);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof TreeNode) {
            TreeNode module = (TreeNode) element;
            String name = module.getName();
            if (name.endsWith("/") && !name.equals("/")) {
                name = name.substring(0, name.length() - 1);
            }

            return name;
        }

        return null;
    }

    @Override
    public Font getFont(Object arg0, int arg1) {
        return null;

    }

}
