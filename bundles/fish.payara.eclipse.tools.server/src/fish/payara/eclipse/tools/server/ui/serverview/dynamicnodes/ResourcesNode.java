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

package fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes;

import static fish.payara.eclipse.tools.server.PayaraServerPlugin.logError;
import static fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes.NodeTypes.RESOURCES;
import static fish.payara.eclipse.tools.server.utils.NodesUtils.getResourceData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.serverview.ResourceDesc;
import fish.payara.eclipse.tools.server.utils.NodesUtils;

/**
 * This node shows is the root node below which the dynamically retrieved "managed resources" (such
 * as JDBC datasources, mail resources, etc) reside.
 *
 * <p>
 * The following depicts this element in the "Servers" views:
 * </p>
 * <p>
 *
 * <pre>
 * Payara 5 [domain1]
 * |- GlassFish Management
 *     |-Resources *
 *     |-Deployed Applications
 *     |-Deployed Web Services
 * |- [WTP managed application]
 * </pre>
 * </p>
 *
 * <p>
 * Payara / GlassFish is dynamically queried for this list, hence it can only be retrieved for a
 * running server. </>
 *
 */
public class ResourcesNode extends TreeNode {

    private PayaraServer server;
    private ResourcesNode[] children;
    private boolean containerNode;
    private ResourceDesc resDescriptor;
    private Map<String, String> map;

    public ResourcesNode(PayaraServer server) {
        this("Resources", RESOURCES, server, null);
        containerNode = true;
    }

    public ResourcesNode(String name, String type, PayaraServer server, ResourceDesc resDescriptor) {
        super(name, type, null);

        this.server = server;
        this.resDescriptor = resDescriptor;

        String[] childTypes = NodeTypes.getChildTypes(type);

        if (childTypes != null) {
            for (String childtype : childTypes) {

                ResourcesNode n = new ResourcesNode(childtype, childtype, server, null);

                if (NodeTypes.getChildTypes(childtype) != null) {
                    n.setContainerNode();
                }

                addChild(n);
            }
        }
    }

    public PayaraServer getServer() {
        return server;
    }

    public void setContainerNode() {
        containerNode = true;
    }

    public boolean isContainerNode() {
        return containerNode;
    }

    public ResourceDesc getResource() {
        return resDescriptor;
    }

    @Override
    public Object[] getChildren() {

        // If a container node or a node that does shows a resource, return std
        // child

        if (containerNode || resDescriptor != null) {
            return childModules.toArray();
        }

        ArrayList<ResourcesNode> list = new ArrayList<>();

        if (children == null) {

            try {
                if (server == null) {
                    children = list.toArray(new ResourcesNode[list.size()]);
                    return children;
                }

                try {
                    List<ResourceDesc> resourcesList = NodesUtils.getResources(server, type);

                    for (ResourceDesc resource : resourcesList) {
                        list.add(new ResourcesNode(resource.getName(), type, server, resource));
                    }

                } catch (Exception ex) {
                    logError("get GlassFish Resources is failing=", ex); //$NON-NLS-1$
                }
            } catch (Exception e) {
            }

            children = list.toArray(new ResourcesNode[list.size()]);

        }

        return children;
    }

    public void refresh() {
        children = null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {

        List<IPropertyDescriptor> properties = new ArrayList<>();

        try {
            if (resDescriptor != null) {

                map = getResourceData(server, resDescriptor.getName());

                Set<String> s = map.keySet();

                for (String prop : s) {
                    String realvalue = prop.substring(prop.lastIndexOf(".") + 1, prop.length());
                    properties.add(new TextPropertyDescriptor(prop, realvalue));
                }
            }

            return properties.toArray(new IPropertyDescriptor[0]);

        } catch (Exception ex) {
            logError("get GlassFish Resources is failing=", ex); //$NON-NLS-1$
        }

        return null;
    }

    @Override
    public Object getPropertyValue(Object id) {

        if ((resDescriptor == null) || (map == null)) {
            return null;
        }

        return map.get(id);
    }

}
