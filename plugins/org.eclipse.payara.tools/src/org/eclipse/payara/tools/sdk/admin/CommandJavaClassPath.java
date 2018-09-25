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
 * GlassFish server administration command entity with local Java SE support and class path.
 * <p/>
 *
 */
public abstract class CommandJavaClassPath extends CommandJava {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Class path to be passed to java executable formated as
     * <code>-cp &lt;path1&gt;:&lt;path2&gt;:...:&lt;pathN&gt;</code>.
     */
    final String classPath;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server administration command entity with specified server
     * command, Java SE home and class path.
     * <p/>
     *
     * @param command Server command represented by this object.
     * @param javaHome Java SE home used to select JRE for GlassFish server.
     * @param classPath Java SE class path.
     * @param environmentVars variables to append to the process-environment
     */
    public CommandJavaClassPath(
            final String command, 
            final String javaHome,
            final String classPath, 
            final Map<? extends String, ? extends String> environmentVars) {
        super(command, javaHome, environmentVars);
        this.classPath = classPath;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get class path to be passed to java executable.
     * <p/>
     *
     * @return the classPath Class path to be passed to java executable.
     */
    public String getClassPath() {
        return classPath;
    }

}
