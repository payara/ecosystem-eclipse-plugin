/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.server.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.glassfish.tools.sdk.server.config.FileSet;
import org.eclipse.glassfish.tools.sdk.server.config.JavaEESet;
import org.eclipse.glassfish.tools.sdk.server.config.JavaSESet;
import org.eclipse.glassfish.tools.sdk.server.config.LibraryNode;
import org.eclipse.glassfish.tools.sdk.server.config.ServerConfigException;
import org.eclipse.glassfish.tools.sdk.server.config.Tools;
import org.eclipse.glassfish.tools.sdk.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Whole <code>server</code> configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ConfigReaderServer extends TreeParser.NodeListener implements
        XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>classpath</code> library configuration XML element reader. */
    private ConfigReaderClasspath classpathReader = new ConfigReaderClasspath();

    /** <code>javadocs</code> library configuration XML element reader. */
    private ConfigReaderJavadocs javadocsReader = new ConfigReaderJavadocs();

    /** <code>sources</code> library configuration XML element reader. */
    private ConfigReaderSources sourcesReader = new ConfigReaderSources();

    /** Java SE configuration XML element reader. */
    private ConfigReaderJavaSE javaSEReader = new ConfigReaderJavaSE("/server");

    /** Java EE configuration XML element reader. */
    private ConfigReaderJavaEE javaEEReader = new ConfigReaderJavaEE("/server");

    /** Tools configuration XML element reader. */
    private ConfigReaderTools configReaderTools
            = new ConfigReaderTools("/server");

    /** Libraries read from XML file. */
    private List<LibraryNode> libraries = new LinkedList<>();

    /** Library ID. */
    private String actualLibID;

    ////////////////////////////////////////////////////////////////////////////
    // XML reader methods                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Provide paths to listen on.
     * <p/>
     * Sets readers for <code>server</code> element and it's content.
     * <p/>
     * @return Paths that the reader listens to.
     */
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        ArrayList<Path> paths = new ArrayList<>(14);
        paths.add(new Path("/server/library", this));
        paths.addAll(classpathReader.getPathsToListen());
        paths.addAll(javadocsReader.getPathsToListen());
        paths.addAll(sourcesReader.getPathsToListen());
        paths.addAll(javaSEReader.getPathsToListen());
        paths.addAll(javaEEReader.getPathsToListen());
        paths.addAll(configReaderTools.getPathsToListen());
        return paths;
    }

    /**
     * Process attributes from current XML element.
     * <p/>
     * @param qname      Not used.
     * @param attributes List of XML attributes.
     * @throws SAXException When any problem occurs.
     */
    @Override
    public void readAttributes(final String qname,
    final Attributes attributes) throws SAXException {
        actualLibID = attributes.getValue("id");
    }

    /**
     * Finish <code>javaee</code> element processing.
     * <p/>
     * @param qname Current XML element name.
     * @throws ServerConfigException when more than one <code>javaee</code>
     *         XML elements were found.
     */
    @Override
    public void endNode(final String qname) throws SAXException {
        if ("library".equals(qname)) {
            FileSet classpath = new FileSet(classpathReader.getPaths(),
                    classpathReader.getFilesets());
            FileSet javadocs = new FileSet(javadocsReader.getPaths(),
                    javadocsReader.getLinks(),
                    javadocsReader.getFilesets(),
                    javadocsReader.getLookups());
            FileSet sources = new FileSet(sourcesReader.getPaths(),
                    sourcesReader.getFilesets());
            LibraryNode config = new LibraryNode(actualLibID, classpath,
                    javadocs,
                    sources);
            libraries.add(config);
            actualLibID = null;
            classpathReader.reset();
            javadocsReader.reset();
            sourcesReader.reset();            
        }                        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get libraries read from XML file.
     * <p/>
     * @return Libraries read from XML file.
     */
    public List<LibraryNode> getLibraries() {
        return libraries;
    }

    /**
     * Get JavaEE set for GlassFish features configuration read from XML.
     * <p/>
     * @return JavaEE set for GlassFish features configuration read from XML.
     */
    public JavaEESet getJavaEE() {
        return javaEEReader.javaEE;
    }

    /**
     * Get JavaSE set for GlassFish features configuration read from XML.
     * <p/>
     * @return JavaSE set for GlassFish features configuration read from XML.
     */
    public JavaSESet getJavaSE() {
        return javaSEReader.javaSE;
    }

    /**
     * Get GlassFish tools configuration read from XML.
     * <p/>
     * @return GlassFish tools configuration read from XML.
     */
    public Tools getTools() {
        return configReaderTools.tools;
    }

}
