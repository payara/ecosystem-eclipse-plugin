/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.sdk.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reader for jmx connector port number as string from domain.xml.
 * <p/>
 * 
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JmxConnectorReader extends TargetConfigReader implements XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(JmxConnectorReader.class);

    public static final String DEFAULT_PATH = "/domain/configs/config/admin-service/jmx-connector";

    private String path;

    private String result;

    public JmxConnectorReader(String path, String targetConfigName) {
        super(targetConfigName);
        this.path = path;
    }

    public JmxConnectorReader(String targetConfigName) {
        this(DEFAULT_PATH, targetConfigName);
    }

    @Override
    public void readAttributes(String qname, Attributes attributes)
            throws SAXException {
        final String METHOD = "getServerConfig";
        /*
         * <admin-service type="das-and-server" system-jmx-connector-name="system"> <jmx-connector .....
         * port="8686" />
         */
        if (readData) {
            String jmxAttr = attributes.getValue("port");
            try {
                int port = Integer.parseInt(jmxAttr);
                result = "" + port; //$NON-NLS-1$
                LOGGER.log(Level.INFO, METHOD, "port", result);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.SEVERE, METHOD, "error", ex);
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

    public String getResult() {
        return result;
    }
}
