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

import org.eclipse.glassfish.tools.sdk.server.parser.TreeParser.Path;

/**
 * <code>javadocs</code> library configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ConfigReaderJavadocs extends ConfigReader {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>link</code> XML element reader. */
    private final LinkReader linkReader = new LinkReader();

    /** <code>lookup</code> XML element reader. */
    private final LookupReader lookupReader = new LookupReader();

    ////////////////////////////////////////////////////////////////////////////
    // XML reader methods                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Provide paths to listen on.
     * <p/>
     * Sets readers for internal <code>javadocs</code> elements.
     * <p/>
     * @return Paths that the reader listens to.
     */
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new Path("/server/library/javadocs/file", pathReader));
        paths.add(new Path("/server/library/javadocs/fileset", filesetReader));
        paths.add(new Path("/server/library/javadocs/link", linkReader));
        paths.add(new Path("/server/library/javadocs/lookup", lookupReader));
        return paths;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get links retrieved from XML elements.
     * <p/>
     * @return Links sets retrieved from XML elements.
     */
    List<String> getLinks() {
        return linkReader.getLinks();
    }
    
    /**
     * Get lookups retrieved from XML elements.
     * <p/>
     * @return Links sets retrieved from XML elements.
     */
    List<String> getLookups() {
        return lookupReader.getLookups();
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
        linkReader.reset();
        lookupReader.reset();
    }

}
