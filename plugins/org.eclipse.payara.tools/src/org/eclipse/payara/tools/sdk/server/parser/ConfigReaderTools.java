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

package org.eclipse.payara.tools.sdk.server.parser;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.payara.tools.sdk.server.config.AsadminTool;
import org.eclipse.payara.tools.sdk.server.config.ServerConfigException;
import org.eclipse.payara.tools.sdk.server.config.Tools;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Tools configuration XML element reader.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ConfigReaderTools extends AbstractReader implements XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>tools</code> XML element name. */
    static final String NODE = "tools";

    /** <code>lib</code> XML element attribute name. */
    private static final String LIB_ATTR = "lib";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Tools library directory (relative under GlassFish home). */
    private String lib;

    /** <code>asadmin</code> XML element reader. */
    private final ToolsAsadminReader toolsAsadminReader;

    /** Tools configuration read from XML. */
    Tools tools;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of <code>tools</code> configuration XML element reader.
     * <p/>
     *
     * @param pathPrefix Tree parser path prefix to be prepended before current XML element.
     */
    ConfigReaderTools(final String pathPrefix) {
        super(pathPrefix, NODE);
        toolsAsadminReader = new ToolsAsadminReader(path);
        lib = null;
        tools = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // XML reader methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Provide paths to listen on.
     * <p/>
     * Sets readers for <code>tools</code> element and it's content.
     * <p/>
     *
     * @return Paths that the reader listens to.
     */
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new TreeParser.Path(path, this));
        paths.add(new TreeParser.Path(
                toolsAsadminReader.getPath(), toolsAsadminReader));
        return paths;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tree parser methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Process attributes from current XML element.
     * <p/>
     *
     * @param qname Not used.
     * @param attributes List of XML attributes.
     * @throws SAXException When any problem occurs.
     */
    @Override
    public void readAttributes(final String qname, final Attributes attributes)
            throws SAXException {
        lib = attributes.getValue(LIB_ATTR);
    }

    /**
     * Finish <code>tools</code> element processing.
     * <p/>
     *
     * @param qname Current XML element name.
     * @throws ServerConfigException when more than one <code>tools</code> XML elements were found.
     */
    @Override
    public void endNode(final String qname) throws SAXException {
        if (NODE.equals(qname)) {
            if (tools != null) {
                throw new SAXException(
                        "Multiple " + NODE + " XML element is not allowed.");
            }
            tools = new Tools(new AsadminTool(getLib(), getJar()));
            reset();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get tools library directory (relative under GlassFish home).
     * <p/>
     *
     * @return Tools library directory (relative under GlassFish home).
     */
    String getLib() {
        return lib;
    }

    /**
     * Get asadmin tool JAR.
     * <p/>
     *
     * @return Asadmin tool JAR.
     */
    String getJar() {
        return toolsAsadminReader.getJar();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    void reset() {
        lib = null;
        toolsAsadminReader.reset();
    }

}
