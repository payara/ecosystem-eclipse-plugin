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

/**
 * GlassFish Server Start DAS Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerLocal.class)
@RunnerRestClass(runner = RunnerLocal.class)
public class CommandStartDAS extends CommandJavaClassPath {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * No command string is needed for Start DAS command but we may use it in logs.
     */
    private static final String COMMAND = "start-das";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * JVM options to be passed to java executable. Typically options as as
     * <code>-D&lt;name&gt;=&lt;value&gt;</code> or <code>-X&lt;option&gt</code>.
     */
    final String javaOpts;

    /**
     * GlassFish specific arguments to be passed to bootstrap main method, e.g.
     * <code>--domain domain_name</code>.
     */
    final String glassfishArgs;

    /** GlassFish server domain directory (full path). */
    final String domainDir;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server start DAS command entity.
     *
     * @param javaHome Java SE home used to select JRE for GlassFish server.
     * @param classPath Java SE class path.
     * @param javaOptions JVM options to be passed to java executable.
     * @param glassfishArgs GlassFish specific arguments to be passed to bootstrap main method.
     * @param domainDir GlassFish server domain directory (full path).
     */
    public CommandStartDAS(String javaHome, String classPath, String javaOptions, String glassfishArgs, String domainDir) {
        super(COMMAND, javaHome, classPath);
        this.javaOpts = javaOptions;
        this.glassfishArgs = glassfishArgs;
        this.domainDir = domainDir;
    }

}
