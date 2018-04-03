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

package org.eclipse.payara.tools.ui.serverview;

import static org.eclipse.payara.tools.ui.PayaraToolsUIPlugin.EAR_MODULE_IMG;
import static org.eclipse.payara.tools.ui.PayaraToolsUIPlugin.EJB_MODULE_IMG;
import static org.eclipse.payara.tools.ui.PayaraToolsUIPlugin.GF_SERVER_IMG;
import static org.eclipse.payara.tools.ui.PayaraToolsUIPlugin.RESOURCES_IMG;
import static org.eclipse.payara.tools.ui.PayaraToolsUIPlugin.WEBSERVICE_IMG;
import static org.eclipse.payara.tools.ui.PayaraToolsUIPlugin.WEB_MODULE_IMG;
import static org.eclipse.payara.tools.ui.PayaraToolsUIPlugin.getImg;

import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.ApplicationNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.DeployedWebServicesNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.ResourcesNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.TreeNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.WebServiceNode;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

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
                return getImg(WEB_MODULE_IMG);
            case "ejb":
                return getImg(EJB_MODULE_IMG);
            case "ear":
                return getImg(EAR_MODULE_IMG);
            }
        } else if (element instanceof ResourcesNode) {
            ResourcesNode rn = (ResourcesNode) element;

            if (rn.getResource() == null) {
                return getImg(RESOURCES_IMG);
            }

            return getImg(GF_SERVER_IMG);
        } else if (element instanceof DeployedWebServicesNode) {
            return getImg(WEBSERVICE_IMG);
        } else if (element instanceof WebServiceNode) {
            return getImg(WEBSERVICE_IMG);
        }

        return getImg(GF_SERVER_IMG);
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
