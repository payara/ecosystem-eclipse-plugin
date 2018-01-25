/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.server.parser;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.glassfish.tools.sdk.server.config.JavaSESet;
import org.eclipse.glassfish.tools.sdk.server.config.ServerConfigException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Java SE configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ConfigReaderJavaSE extends ConfigReaderJava {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>java</code> XML element name. */
    static final String NODE = "java";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>platform</code> XML element reader. */
    private final JavaSEPlatformReader platformReader;

    /** JavaSE set for GlassFish features configuration read from XML. */
    JavaSESet javaSE;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE configuration XML element reader.
     * <p/>
     * @param pathPrefix Tree parser path prefix to be prepended before
     *        current XML element.
     */
    ConfigReaderJavaSE(final String pathPrefix) {
        super(pathPrefix, NODE);
        platformReader = new JavaSEPlatformReader(path);
        javaSE = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // XML reader methods                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Provide paths to listen on.
     * <p/>
     * Sets readers for <code>java</code> element and it's content.
     * <p/>
     * @return Paths that the reader listens to.
     */
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new TreeParser.Path(path, this));
        paths.add(new TreeParser.Path(
                platformReader.getPath(), platformReader));
        return paths;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tree parser methods                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Process attributes from current XML element.
     * <p/>
     * @param qname      Not used.
     * @param attributes List of XML attributes.
     * @throws SAXException When any problem occurs.
     */
    @Override
    public void readAttributes(final String qname, final Attributes attributes)
            throws SAXException {
        super.readAttributes(qname, attributes);
    }

    /**
     * Finish <code>java</code> element processing.
     * <p/>
     * @param qname Current XML element name.
     * @throws ServerConfigException when more than one <code>java</code>
     *         XML elements were found.
     */
    @Override
    public void endNode(final String qname) throws SAXException {
        if (NODE.equals(qname)) {
            if (javaSE != null) {
                throw new SAXException(
                        "Multiple " + NODE + " XML element is not allowed.");
            }
            javaSE = new JavaSESet(getPlatforms(), getVersion());
            reset();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get platforms retrieved from XML elements.
     * <p/>
     * @return Platforms retrieved from XML elements.
     */
    private List<String> getPlatforms() {
        return platformReader.getPlatforms();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    @Override
    void reset() {
        super.reset();
        platformReader.reset();
    }

}
