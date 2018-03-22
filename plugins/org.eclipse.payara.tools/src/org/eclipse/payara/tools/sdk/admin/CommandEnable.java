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
 * GlassFish Server Enable (a deployed application) Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpEnableDisable.class)
@RunnerRestClass(runner = RunnerRestEnable.class)
public class CommandEnable extends CommandTargetName {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for enable command. */
    private static final String COMMAND = "enable";

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server enable command entity.
     * <p/>
     *
     * @param name Name of module to enable.
     * @param target Target GlassFish instance.
     */
    public CommandEnable(String name, String target) {
        super(COMMAND, name, target);
    }

}
