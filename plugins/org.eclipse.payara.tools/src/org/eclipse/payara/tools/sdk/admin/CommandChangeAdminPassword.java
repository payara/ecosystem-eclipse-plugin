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
 * GlassFish server change administrator's password administration command entity.
 * <p/>
 *
 * @author Tomas Kraus
 */
@RunnerHttpClass(runner = RunnerAsadminChangeAdminPassword.class)
@RunnerRestClass(runner = RunnerAsadminChangeAdminPassword.class)
public class CommandChangeAdminPassword extends CommandJava {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for change administrator's password command. */
    private static final String COMMAND = "change-admin-password";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * GlassFish administrator's new password to be set. Value of <code>null</code> or empty
     * <code>String</code> means no password.
     */
    final String password;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server administration command entity with specified server
     * command, Java SE home and class path.
     * <p/>
     *
     * @param javaHome Java SE home used to select JRE for GlassFish server.
     * @param password GlassFish administrator's new password to be set.
     */
    public CommandChangeAdminPassword(final String javaHome,
            final String password) {
        super(COMMAND, javaHome);
        this.password = password;
    }

}
