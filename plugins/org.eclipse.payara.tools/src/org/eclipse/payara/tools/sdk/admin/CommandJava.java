/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.util.Map;

/**
 * GlassFish server administration command entity with local Java SE support.
 * <p/>
 *
 */
public abstract class CommandJava extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Java SE home used to select JRE for GlassFish server. */
    final String javaHome;
    
    /** Variables to append to the process-environment (if any) */
    final Map<? extends String, ? extends String> environmentVars;

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
     * @param environmentVars variables to append to the process-environment
     */
    protected CommandJava(
            final String command, 
            final String javaHome, 
            final Map<? extends String, ? extends String> environmentVars) {
        super(command);
        this.javaHome = javaHome;
        this.environmentVars = environmentVars;
    }
    
    /**
     * Constructs an instance of GlassFish server administration command entity with specified server
     * command and Java SE home.
     * <p/>
     *
     * @param command Server command represented by this object.
     * @param javaHome Java SE home used to select JRE for GlassFish server.
     */
    protected CommandJava(
            final String command, 
            final String javaHome) {
        this(command, javaHome, new ProcessBuilder().environment() );
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
