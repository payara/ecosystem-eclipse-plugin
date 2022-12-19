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

package fish.payara.eclipse.tools.server.sdk.admin;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.utils.Utils;

/**
 * GlassFish Server <code>undeploy</code> Admin Command Execution using HTTP interface.
 * <p/>
 * Contains common code for commands that are called with <code>DEFAULT=&lt;target&gt;</code> query
 * string. Individual child classes are not needed at this stage. Class implements GlassFish server
 * administration functionality trough HTTP interface.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpUndeploy extends RunnerHttp {

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
    private static String query(Command command) {
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
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpUndeploy(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
