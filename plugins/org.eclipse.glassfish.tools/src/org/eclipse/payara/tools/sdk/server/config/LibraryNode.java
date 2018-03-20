/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.config;

/**
 * Internal library node element.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class LibraryNode {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Library ID. */
    final String libraryID;
    
    /** Class path file set. */
    final FileSet classpath;

    /** Java doc file set. */
    final FileSet javadocs;

    /** Java sources file set. */
    final FileSet sources;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of internal library node element.
     * <p/>
     * @param libraryID Library ID.
     * @param classpath Class path file set
     * @param javadocs  Java doc file set.
     * @param sources   Java sources file set.
     */
    public LibraryNode(final String libraryID, final FileSet classpath,
            final FileSet javadocs, final FileSet sources) {
        this.libraryID = libraryID;
        this.classpath = classpath;
        this.javadocs = javadocs;
        this.sources = sources;
    }
    
}
