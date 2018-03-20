/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.sdk.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads configuration of http listeners from domain.xml. For each http listener returns one
 * {@link HttpData} object that contains name of listener, port number and information whether this
 * listener is secured.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class HttpListenerReader extends TargetConfigReader implements XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(HttpListenerReader.class);

    public static final String DEFAULT_PATH = "/domain/configs/config/http-service/http-listener";

    private String path;

    private Map<String, HttpData> result;

    public HttpListenerReader(String targetConfigName) {
        this(DEFAULT_PATH, targetConfigName);
    }

    public HttpListenerReader(String path, String targetConfigName) {
        super(targetConfigName);
        this.path = path;
        this.result = new HashMap<>();
    }

    @Override
    public void readAttributes(String qname, Attributes attributes) throws SAXException {
        final String METHOD = "readAttributes";
        // <http-listener
        // id="http-listener-1" port="8080" xpowered-by="true"
        // enabled="true" address="0.0.0.0" security-enabled="false"
        // family="inet" default-virtual-server="server"
        // server-name="" blocking-enabled="false" acceptor-threads="1">
        if (readData) {
            try {
                String id = attributes.getValue("id");
                if (id != null && id.length() > 0) {
                    int port = Integer.parseInt(attributes.getValue("port"));
                    boolean secure = Boolean.TRUE.toString().equals(attributes.getValue("security-enabled"));
                    boolean enabled = !Boolean.FALSE.toString().equals(attributes.getValue("enabled"));
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
