/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.parser;

import java.util.List;
import java.util.Map;

/**
 * Common library configuration XML elements reader.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class ConfigReader implements XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>fileset</code> XML element reader. */
    final FilesetReader filesetReader = new FilesetReader();

    /** <code>file</code> XML element reader. */
    final PathReader pathReader
            = new PathReader("/server/library/classpath");

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get file sets retrieved from XML elements.
     * <p/>
     * @return File sets retrieved from XML elements.
     */
    Map<String, List<String>> getFilesets() {
        return filesetReader.getFilesets();
    }
    
    /**
     * Get paths retrieved from XML elements.
     * <p/>
     * @return Paths sets retrieved from XML elements.
     */
    List<String> getPaths() {
        return pathReader.getPaths();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    void reset() {
        filesetReader.reset();
        pathReader.reset();
    }
}
