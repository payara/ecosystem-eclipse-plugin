/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.config;

import java.util.List;
import java.util.Map;

/**
 * Library content set for library content for GlassFish features configuration.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class FileSet {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Paths retrieved from XML elements. */
    private final List<String> paths;

    /** Links retrieved from XML elements. */
    private final List<String> links;

    /** File sets retrieved from XML elements. */
    private final Map<String, List<String>> filesets;

    /** Links retrieved from XML elements. */
    private final List<String> lookups;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Library content for GlassFish libraries
     * configuration.
     * <p/>
     * @param paths    Paths retrieved from XML elements.
     * @param links    Links retrieved from XML elements.
     * @param filesets File sets retrieved from XML elements.
     * @param lookups  Lookups retrieved from XML elements.
     */
    public FileSet(final List<String> paths, final List<String> links,
            final Map<String, List<String>> filesets,
            final List<String> lookups) {
        this.paths = paths;
        this.links = links;
        this.filesets = filesets;
        this.lookups = lookups;
    }

    /**
     * Creates an instance of Library content for GlassFish libraries
     * configuration.
     * <p/>
     * Content of links and lookups is set to <code>null</code>.
     * <p/>
     * @param paths    Paths retrieved from XML elements.
     * @param filesets File sets retrieved from XML elements.
     */
    public FileSet(final List<String> paths,
            final Map<String, List<String>> filesets) {
        this(paths, null, filesets, null);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get paths retrieved from XML elements.
     * <p/>
     * @return Paths sets retrieved from XML elements.
     */
    public List<String> getPaths() {
        return paths;
    }

    /**
     * Get links retrieved from XML elements.
     * <p/>
     * @return Links sets retrieved from XML elements.
     */
    public List<String> getLinks() {
        return links;
    }

    /**
     * Get file sets retrieved from XML elements.
     * <p/>
     * @return File sets retrieved from XML elements.
     */
    public Map<String, List<String>> getFilesets() {
        return filesets;
    }
    
    /**
     * Get lookups retrieved from XML elements.
     * <p/>
     * @return Links sets retrieved from XML elements.
     */
    public List<String> getLookups() {
        return lookups;
    }

}
