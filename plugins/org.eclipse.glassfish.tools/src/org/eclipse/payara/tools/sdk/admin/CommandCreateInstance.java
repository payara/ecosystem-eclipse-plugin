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
 * GlassFish Server Create Instance Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpCreateInstance.class)
@RunnerRestClass(runner=RunnerRestCreateInstance.class)
public class CommandCreateInstance extends CommandTargetName {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create-instance command. */
    private static final String COMMAND = "create-instance";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Target GlassFish node where instance will be created. */
    final String node;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server create-instance command entity.
     * <p/>
     * @param name   Name of instance to create.
     * @param target Target GlassFish cluster or <code>null</code> for
     *               standalone instance.
     * @param node   Target GlassFish node where instance will be created.
     */
    public CommandCreateInstance(String name, String target, String node) {
        super(COMMAND, name, target);
        this.node = node;
    }

}
