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

package org.eclipse.payara.tools.sdk.server.config;

import java.net.URL;
import java.util.List;

import org.eclipse.payara.tools.sdk.data.GlassFishConfig;
import org.eclipse.payara.tools.sdk.server.parser.ConfigReaderServer;
import org.eclipse.payara.tools.sdk.server.parser.TreeParser;

/**
 * Payara configuration reader API.
 * <p/>
 * Allows to access Payara server features and libraries configuration XML file using configuration
 * XML file parser.
 * <p/>
 * XML configuration file reader is called only once. Any subsequent configuration values access
 * will return values cached from first attempt.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class GlassFishConfigXMLImpl implements GlassFishConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish configuration XML file. */
    private final URL configFile;

    /** GlassFish configuration XML file reader. */
    private final ConfigReaderServer reader = new ConfigReaderServer();

    /**
     * Stores information whether GlassFish configuration XML file was already read and processed
     */
    private volatile boolean readDone;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Payara configuration API.
     * <p/>
     *
     * @param configFile Payara configuration XML file.
     */
    public GlassFishConfigXMLImpl(final URL configFile) {
        this.configFile = configFile;
        readDone = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Payara libraries configuration.
     * <p/>
     *
     * @return Payara libraries configuration.
     */
    @Override
    public List<LibraryNode> getLibrary() {
        readXml();
        return reader.getLibraries();
    }

    /**
     * Get Payara JavaEE configuration.
     * <p/>
     *
     * @return Payara JavaEE configuration.
     */
    @Override
    public JavaEESet getJavaEE() {
        readXml();
        return reader.getJavaEE();
    }

    /**
     * Get Payara JavaSE configuration.
     * <p/>
     *
     * @return Payara JavaSE configuration.
     */
    @Override
    public JavaSESet getJavaSE() {
        readXml();
        return reader.getJavaSE();
    }

    /**
     * Get Payara tools configuration.
     * <p/>
     *
     * @return Payara tools configuration.
     */
    @Override
    public Tools getTools() {
        readXml();
        return reader.getTools();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Make sure Payara configuration XML file was read and processed.
     */
    private void readXml() {
        if (readDone) {
            return;
        }
        synchronized (reader) {
            if (!readDone) {
                TreeParser.readXml(configFile, reader);
                readDone = true;
            }
        }
    }

}
