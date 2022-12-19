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

import java.util.HashMap;

public class NodeTypes {

    // Contract provider constants (identify the different containers in V3)
    public static final String EAR_CONTAINER = "ear"; // NOI18N
    public static final String WEB_CONTAINER = "web"; // NOI18N
    public static final String EJB_CONTAINER = "ejb"; // NOI18N
    public static final String APPCLIENT_CONTAINER = "appclient"; // NOI18N
    public static final String CONNECTOR_CONTAINER = "connector"; // NOI18N

    // Resource types
    public static final String JDBC = "JDBC"; // NOI18N
    public static final String JDBC_RESOURCE = "jdbc-resource"; // NOI18N
    public static final String JDBC_CONNECTION_POOL = "jdbc-connection-pool"; // NOI18N
    public static final String SESSION_PRESERVATION_FLAG = "preserveSessionsOn";

    public static final String CONNECTORS = "CONNECTORS"; // NOI18N
    public static final String CONN_RESOURCE = "connector-resource"; // NOI18N
    public static final String CONN_CONNECTION_POOL = "connector-connection-pool"; // NOI18N
    public static final String ADMINOBJECT_RESOURCE = "admin-object"; // NOI18N

    public static final String JAVAMAIL = "JAVAMAIL"; // NOI18N
    public static final String JAVAMAIL_RESOURCE = "javamail-resource"; // NOI18N

    private static HashMap<String, String[]> nodeTree;

    public static final String APPLICATIONS = "APPLICATIONS";
    public static final String EARS = "EARS";
    public static final String WEBAPPS = "WEBAPPS";
    public static final String EJBS = "EJBS";
    public static final String APPCLIENTS = "APPCLIENTS";

    public static final String RESOURCES = "RESOURCES";

    private static final String[] APPLICATIONS_TREE = {
            EARS, WEBAPPS, EJBS, APPCLIENTS };
    private static final String[] RESOURCES_TREE = {
            JDBC, CONNECTORS, JAVAMAIL };
    private static final String[] JDBC_TREE = {
            JDBC_RESOURCE, JDBC_CONNECTION_POOL };
    private static final String[] CONNECTORS_TREE = {
            CONN_RESOURCE, CONN_CONNECTION_POOL, ADMINOBJECT_RESOURCE };
    private static final String[] JAVAMAIL_TREE = {
            JAVAMAIL_RESOURCE };

    static {
        nodeTree = new HashMap<>();
        nodeTree.put(APPLICATIONS, APPLICATIONS_TREE);
        nodeTree.put(RESOURCES, RESOURCES_TREE);
        nodeTree.put(JDBC, JDBC_TREE);
        nodeTree.put(CONNECTORS, CONNECTORS_TREE);
        nodeTree.put(JAVAMAIL, JAVAMAIL_TREE);
    }

    private NodeTypes() {
    }

    /**
     * Returns an array of tree children as strings given a particular parent name.
     *
     * @param type The node from which children types are derived.
     *
     * @return All the children types for the node name passed.
     */
    static String[] getChildTypes(String type) {
        return nodeTree.get(type);
    }

}
