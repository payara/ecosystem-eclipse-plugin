/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.serverview;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.viewers.BaseContentProvider;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;

@SuppressWarnings("restriction")
public class ServerViewContentProvider extends BaseContentProvider implements ITreeContentProvider {

	static String GLASSFISH_MANAGEMENT = "GlassFish Management"; //$NON-NLS-1$
    public ServerViewContentProvider() {
    	return;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IServer) {
            IServer server = (IServer) parentElement;

            //only active for glassfish 3.1.x server which is started!!!
            boolean is31x = GlassfishToolsPlugin.is31OrAbove(server.getRuntime());
            if ((is31x && (server.getServerState() == IServer.STATE_STARTED))) {
                GlassFishServer ser = (GlassFishServer) server.loadAdapter(GlassFishServer.class, new NullProgressMonitor());

                if (ser != null) {
                    TreeNode root = new TreeNode(GLASSFISH_MANAGEMENT, GLASSFISH_MANAGEMENT, null);
                    //Applications Node
                    DeployedApplicationsNode apps = new DeployedApplicationsNode(ser);
                    //Resources Node
                    DeployedWebServicesNode ws = new DeployedWebServicesNode(ser);

                    ResourcesNode rs = new ResourcesNode("Resources", NodeTypes.RESOURCES, ser, null);
                    rs.setContainerNode();
                    root.addChild(apps);
                    root.addChild(rs);
                    root.addChild(ws);
                    return new Object[]{root};
                }
            }
        }
        if (parentElement instanceof TreeNode) {
            TreeNode root = (TreeNode) parentElement;
            return root.getChildren();
        }
        return null;
    }

    @Override
    public Object[] getElements(Object parentElement) {
        return getChildren(parentElement);
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof DeployedApplicationsNode) {
            return ((DeployedApplicationsNode) element).getServer();
        } else if (element instanceof ApplicationNode) {
            return ((ApplicationNode) element).getParent();
        } else if (element instanceof TreeNode) {
            TreeNode m = (TreeNode) element;
            return m.getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IServer) {
            return true;
        } else if (element instanceof DeployedApplicationsNode) {
            return true;
        } else if (element instanceof ApplicationNode) {
            return true;
        } else if (element instanceof TreeNode) {
            TreeNode m = (TreeNode) element;
            return m.getChildren().length > 0;
        }
        return false;
    }
}
