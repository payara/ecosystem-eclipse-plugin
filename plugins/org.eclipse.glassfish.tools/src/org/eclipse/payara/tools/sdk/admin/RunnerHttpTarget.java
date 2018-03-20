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
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * GlassFish instance and cluster administration command with <code>DEFAULT=&lt;target&gt;</code>
 * query execution using HTTP interface.
 * <p/>
 * Contains common code for commands that are called with <code>DEFAULT=&lt;target&gt;</code> query
 * string. Individual child classes are not needed at this stage. Class implements GlassFish server
 * administration functionality trough HTTP interface.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpTarget extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Start/Stop command <code>DEFAULT</code> parameter's name. */
    private static final String DEFAULT_PARAM = "DEFAULT";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds enable/disable query string for given command.
     * <p/>
     * <code>QUERY :: "DEFAULT" '=' &lt;target&gt;</code>
     * <p/>
     *
     * @param command GlassFish Server Administration Command Entity. <code>CommandDisable</code>
     * instance is expected.
     * @return Enable/Disable query string for given command.
     */
    static String query(Command command) {
        String target;
        if (command instanceof CommandTarget) {
            target = Utils.sanitizeName(((CommandTarget) command).target);
        } else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        if (target == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder(
                    DEFAULT_PARAM.length() + 1 + target.length());
            sb.append(DEFAULT_PARAM);
            sb.append(PARAM_ASSIGN_VALUE);
            sb.append(target);
            return sb.toString();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpTarget(final GlassFishServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
