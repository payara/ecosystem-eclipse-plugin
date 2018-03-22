/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import org.eclipse.payara.tools.sdk.utils.Utils;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * GlassFish Server <code>enable</code> and <code>disable</code> administration command execution
 * using HTTP interface.
 * <p/>
 * Contains common code for enable and disable commands. Individual child classes are not needed at
 * this stage. Class implements GlassFish server administration functionality trough HTTP interface.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpEnableDisable extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Enable/Disable command <code>DEFAULT</code> param name. */
    private static final String DEFAULT_PARAM = "DEFAULT";

    /** Enable/Disable command <code>target</code> param name. */
    private static final String TARGET_PARAM = "target";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds enable/disable query string for given command.
     * <p/>
     * <code>QUERY :: "DEFAULT" '=' &lt;name&gt;
     *                ['&' "target" '=' &lt;target&gt; ]</code>
     * <p/>
     *
     * @param command GlassFish Server Admin Command Entity. <code>CommandDisable</code> instance is
     * expected.
     * @return Enable/Disable query string for given command.
     */
    private static String query(final Command command) {
        String target;
        String name;
        if (command instanceof CommandTargetName) {
            target = Utils.sanitizeName(((CommandTargetName) command).target);
            if (((CommandTargetName) command).name == null) {
                throw new CommandException(CommandException.ILLEGAL_NULL_VALUE);
            }
            name = Utils.sanitizeName(((CommandTargetName) command).name);
        } else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        StringBuilder sb = new StringBuilder(
                DEFAULT_PARAM.length() + 1 + name.length() + (target != null
                        ? 1 + TARGET_PARAM.length() + 1 + target.length()
                        : 0));
        sb.append(DEFAULT_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(name);
        if (target != null) {
            sb.append(PARAM_SEPARATOR);
            sb.append(TARGET_PARAM);
            sb.append(PARAM_ASSIGN_VALUE);
            sb.append(target);
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of admin command executor using HTTP interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish Server Admin Command Entity.
     */
    public RunnerHttpEnableDisable(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
