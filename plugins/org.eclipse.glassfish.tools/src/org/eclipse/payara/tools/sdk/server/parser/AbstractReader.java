/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.parser;

/**
 * Abstract XML element reader.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public abstract class AbstractReader extends TreeParser.NodeListener {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Tree parser element path. */
    final String path;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE platform check configuration XML element reader.
     * <p/>
     *
     * @param pathPrefix Tree parser path prefix to be prepended before current XML element.
     * @param node XML element name.
     */
    AbstractReader(final String pathPrefix, final String node) {
        StringBuilder sb = new StringBuilder(
                (pathPrefix != null ? pathPrefix.length() : 0)
                        + TreeParser.PATH_SEPARATOR.length() + node.length());
        if (pathPrefix != null) {
            sb.append(pathPrefix);
        }
        sb.append(TreeParser.PATH_SEPARATOR);
        sb.append(node);
        path = sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get current element tree parser path.
     * <p/>
     *
     * @return Current element tree parser path.
     */
    String getPath() {
        return path;
    }

}
