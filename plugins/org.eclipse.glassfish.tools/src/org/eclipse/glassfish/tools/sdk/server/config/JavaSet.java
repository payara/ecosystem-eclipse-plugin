/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.server.config;

/**
 * Abstract Container of GlassFish Java features configuration.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public abstract class JavaSet {
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Highest Java specification version implemented. */
    private final String version;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of container of GlassFish Java features
     * configuration.
     * <p/>
     * @param version  Highest JavaEE specification version implemented.
     */
    public JavaSet(final String version) {
        this.version = version;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get highest JavaEE specification version implemented.
     * <p/>
     * @return Highest JavaEE specification version implemented.
     */
    public String getVersion() {
        return version;
    }

}
