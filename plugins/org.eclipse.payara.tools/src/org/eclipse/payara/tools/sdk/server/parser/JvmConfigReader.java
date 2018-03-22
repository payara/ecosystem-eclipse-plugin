/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.payara.tools.sdk.server.parser.TreeParser.NodeListener;
import org.eclipse.payara.tools.sdk.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class JvmConfigReader extends NodeListener implements
        XMLReader {

    private static String JVM_OPTIONS_TAG = "jvm-options";

    private String serverName;
    /**
     * Holds all values found in <jvm-options> tags
     */
    private ArrayList<String> optList = new ArrayList<>();
    /**
     * Holds all key-value pairs representing attributes of jvm-config tag. These are used for computing
     * the classpath.
     */
    private HashMap<String, String> propMap = new HashMap<>();
    private boolean isMonitoringEnabled = false;
    private String serverConfigName;
    private boolean readConfig = false;
    private StringBuilder b = new StringBuilder();

    public JvmConfigReader(String serverName) {
        this.serverName = serverName;
    }

    public TreeParser.NodeListener getServerFinder() {
        return new TreeParser.NodeListener() {

            @Override
            public void readAttributes(String qname, Attributes attributes) throws SAXException {
                // <server lb-weight="100" name="server" config-ref="server-config">
                if (serverConfigName == null || serverConfigName.length() == 0) {
                    if (serverName.equals(attributes.getValue("name"))) { // NOI18N
                        serverConfigName = attributes.getValue("config-ref"); // NOI18N
                        // Logger.getLogger("glassfish").finer("DOMAIN.XML: Server profile defined by " + serverConfigName);
                        // // NOI18N
                    }
                }
            }
        };
    }

    public TreeParser.NodeListener getConfigFinder() {
        return new TreeParser.NodeListener() {

            @Override
            public void readAttributes(String qname, Attributes attributes) throws SAXException {
                // <config name="server-config" dynamic-reconfiguration-enabled="true">
                if (serverConfigName != null && serverConfigName.equals(attributes.getValue("name"))) { // NOI18N
                    readConfig = true;
                    // Logger.getLogger("glassfish").finer("DOMAIN.XML: Reading JVM options from server profile " +
                    // serverConfigName); // NOI18N
                }
            }

            @Override
            public void endNode(String qname) throws SAXException {
                if ("config".equals(qname)) {
                    readConfig = false;
                }
            }
        };
    }

    @Override
    public void readAttributes(String qname, Attributes attributes) throws SAXException {
        // <java-config
        // classpath-prefix="CP-PREFIX"
        // classpath-suffix="CP-SUFFIX"
        // debug-enabled="false"
        // debug-options="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009"
        // env-classpath-ignored="false"
        // java-home="${com.sun.aas.javaRoot}"
        // javac-options="-g"
        // native-library-path-prefix="NATIVE-LIB-PREFIX"
        // native-library-path-suffix="NATIVE-LIB-SUFFIX"
        // rmic-options="-iiop -poa -alwaysgenerate -keepgenerated -g"
        // server-classpath="SERVER-CLASSPATH"
        // system-classpath="SYSTEM-CLASSPATH">
        if (readConfig) {
            int attrLen = attributes.getLength();
            for (int i = 0; i < attrLen; i++) {
                String name = attributes.getLocalName(i);
                // seems that sometimes from uknown reasons
                // getLocalName returns empty string...
                if ((name == null) || name.isEmpty()) {
                    name = attributes.getQName(i);
                }
                String value = attributes.getValue(i);
                if (name != null && name.length() > 0 && value != null && value.length() > 0) {
                    propMap.put(name, value);
                }
            }
        }
    }

    @Override
    public void readCData(String qname, char[] ch, int start, int length) throws SAXException {
        // <jvm-options>-client</jvm-options>
        // <jvm-options>-Djava.endorsed.dirs=${com.sun.aas.installRoot}/lib/endorsed</jvm-options>
        if (readConfig && JVM_OPTIONS_TAG.equals(qname)) {
            b.append(ch, start, length);
        }
    }

    @Override
    public void endNode(String qname) throws SAXException {
        if (readConfig && JVM_OPTIONS_TAG.equals(qname)) {
            optList.add(b.toString());
            b.delete(0, b.length());
        }
    }

    public TreeParser.NodeListener getMonitoringFinder() {
        return new TreeParser.NodeListener() {

            @Override
            public void readAttributes(String qname, Attributes attributes) throws SAXException {
                // <monitoring-service [monitoring-enabled="false"]
                if (readConfig) {
                    isMonitoringEnabled = !"false".equals(attributes.getValue("monitoring-enabled"));
                    // if (monitoringAgent.exists()) {
                    // if (!"false".equals(attributes.getValue("monitoring-enabled"))) { // NOI18N
                    // //optList.add("-javaagent:"+Utils.quote(monitoringAgent.getAbsolutePath())+"=unsafe=true,noServer=true");
                    // // NOI18N
                    // isMonitoringEnabled = true;
                    // }
                    // }
                }
            }
        };
    }

    // private NodeListener getOptionsReader() {
    // return new NodeListener() {
    //
    //
    // @Override
    // public void endNode(String qname) throws SAXException {
    // if (readJvmConfig) {
    // optList.add(b.toString());
    // b.delete(0, b.length());
    // }
    // }
    //
    // };
    // }
    //
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new Path("/domain/servers/server", getServerFinder()));
        paths.add(new Path("/domain/configs/config", getConfigFinder()));
        paths.add(new Path("/domain/configs/config/java-config", this));
        paths.add(new Path("/domain/configs/config/monitoring-service", getMonitoringFinder()));
        return paths;
    }

    public List<String> getOptList() {
        return optList;
    }

    public Map<String, String> getPropMap() {
        return propMap;
    }

    public boolean isMonitoringEnabled() {
        return isMonitoringEnabled;
    }
}
