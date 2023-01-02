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

package fish.payara.eclipse.tools.server.sdk.server.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import fish.payara.eclipse.tools.server.sdk.logging.Logger;
import fish.payara.eclipse.tools.server.sdk.server.parser.TreeParser.Path;

/**
 * Reads configuration of network listeners. For each listener returns one {@link HttpData} object
 * that contains port number, protocol and information whether this protocol is secured.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class NetworkListenerReader extends TargetConfigReader implements
        XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(NetworkListenerReader.class);

    public static final String DEFAULT_PATH = "/domain/configs/config/network-config/network-listeners/network-listener";

    private String path;

    private Map<String, HttpData> result;

    public NetworkListenerReader(String targetConfigName) {
        this(DEFAULT_PATH, targetConfigName);
    }

    public NetworkListenerReader(String path, String targetConfigName) {
        super(targetConfigName);
        this.path = path;
        this.result = new HashMap<>();
    }

    @Override
    public void readAttributes(String qname, Attributes attributes) throws SAXException {
        final String METHOD = "readAttributes";
        /*
         * <network-listeners> <thread-pool max-thread-pool-size="20" min-thread-pool-size="2"
         * thread-pool-id="http-thread-pool" max-queue-size="4096"></thread-pool> <network-listener
         * port="8080" protocol="http-listener-1" transport="tcp" name="http-listener-1"
         * thread-pool="http-thread-pool"></network-listener> <network-listener port="8181" enabled="false"
         * protocol="http-listener-2" transport="tcp" name="http-listener-2"
         * thread-pool="http-thread-pool"></network-listener> <network-listener port="4848"
         * protocol="admin-listener" transport="tcp" name="admin-listener"
         * thread-pool="http-thread-pool"></network-listener> </network-listeners>
         */
        if (readData) {
            try {
                String id = attributes.getValue("name");
                if (id != null && id.length() > 0) {

                    if (attributes.getValue("port").startsWith("$")) { // GlassFish v3.1 : ignore these template entries
                        return;
                    }
                    int port = Integer.parseInt(attributes.getValue("port"));
                    boolean secure = "true".equals(attributes.getValue(
                            "security-enabled"));
                    boolean enabled = !"false".equals(attributes.getValue(
                            "enabled"));
                    LOGGER.log(Level.INFO, METHOD, "port", new Object[] {
                            Integer.toString(port), Boolean.toString(enabled),
                            Boolean.toString(secure) });
                    if (enabled) {
                        HttpData data = new HttpData(id, port, secure);
                        LOGGER.log(Level.INFO, METHOD, "add", data);
                        result.put(id, data);
                    }
                } else {
                    LOGGER.log(Level.INFO, METHOD, "noName");
                }
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.SEVERE, METHOD, "numberFormat", ex);
            }
        }
    }

    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new Path(path, this));
        paths.add(new Path(CONFIG_PATH, new TargetConfigMarker()));
        return paths;
    }

    public Map<String, HttpData> getResult() {
        return result;
    }
}
