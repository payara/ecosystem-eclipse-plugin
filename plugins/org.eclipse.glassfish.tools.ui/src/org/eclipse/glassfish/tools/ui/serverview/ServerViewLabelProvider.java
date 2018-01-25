/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.serverview;

import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import org.eclipse.glassfish.tools.ui.GlassfishToolsUIPlugin;

public class ServerViewLabelProvider extends LabelProvider implements ITableFontProvider {
    
    public ServerViewLabelProvider() {
    	return;
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
    public Image getImage(Object element) {
        if (element instanceof ApplicationNode) {
            ApplicationNode module = (ApplicationNode) element;
            if (module.getApplicationInfo().getType().equals("web")) {
                return GlassfishToolsUIPlugin.getInstance().
                		getImageRegistry().get(GlassfishToolsUIPlugin.WEB_MODULE_IMG);
            }
            if (module.getApplicationInfo().getType().equals("ejb")) {
            	return GlassfishToolsUIPlugin.getInstance().
                		getImageRegistry().get(GlassfishToolsUIPlugin.EJB_MODULE_IMG);
            }
            if (module.getApplicationInfo().getType().equals("ear")) {
                return GlassfishToolsUIPlugin.getInstance().
                		getImageRegistry().get(GlassfishToolsUIPlugin.EAR_MODULE_IMG);
            }
        } else if (element instanceof ResourcesNode) {
            ResourcesNode rn = (ResourcesNode) element;
            if (rn.getResource() == null) {
                return GlassfishToolsUIPlugin.getInstance().
                		getImageRegistry().get(GlassfishToolsUIPlugin.RESOURCES_IMG);
            } else {
                return GlassfishToolsUIPlugin.getInstance().
                		getImageRegistry().get(GlassfishToolsUIPlugin.GF_SERVER_IMG);
            }
        } else if (element instanceof DeployedWebServicesNode) {
            return GlassfishToolsUIPlugin.getInstance().
            		getImageRegistry().get(GlassfishToolsUIPlugin.WEBSERVICE_IMG);
        } else if (element instanceof WebServiceNode) {
            return GlassfishToolsUIPlugin.getInstance().
            		getImageRegistry().get(GlassfishToolsUIPlugin.WEBSERVICE_IMG);
        }

        return GlassfishToolsUIPlugin.getInstance().
        		getImageRegistry().get(GlassfishToolsUIPlugin.GF_SERVER_IMG);
    }

    @Override
    public Font getFont(Object arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }
}
