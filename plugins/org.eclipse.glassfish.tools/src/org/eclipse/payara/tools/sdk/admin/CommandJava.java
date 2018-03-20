/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

/**
 * GlassFish server administration command entity with local Java SE support.
 * <p/>
 *
 * @author Tomas Kraus
 */
public abstract class CommandJava extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Java SE home used to select JRE for GlassFish server. */
    final String javaHome;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server administration command entity with specified server
     * command and Java SE home.
     * <p/>
     *
     * @param command Server command represented by this object.
     * @param javaHome Java SE home used to select JRE for GlassFish server.
     */
    public CommandJava(final String command, final String javaHome) {
        super(command);
        this.javaHome = javaHome;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Java SE home used to select JRE for GlassFish server.
     * <p/>
     *
     * @return Java SE home used to select JRE for GlassFish server.
     */
    public String getJavaHome() {
        return javaHome;
    }

}
