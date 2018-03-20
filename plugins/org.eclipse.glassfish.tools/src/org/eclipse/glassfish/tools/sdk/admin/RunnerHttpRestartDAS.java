/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin;

import org.eclipse.glassfish.tools.server.GlassFishServer;

/**
 * GlassFish restart DAS administration command with 
 * <code></code> query execution using HTTP interface.
 * <p/>
 * Contains code for command that is called with
 * <code>debug=true|false&force=true|false&kill=true|false</code> query string.
 * <p/>
 * Class implements GlassFish server administration functionality trough HTTP
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpRestartDAS extends RunnerHttp {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Restart DAS command <code>debug</code> parameter's name. */
    private static final String DEBUG_PARAM = "debug";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds restart DAS query string for given command.
     * <p/>
     * <code>debug=true|false&force=true|false&kill=true|false</code>
     * <p/>
     * @param command GlassFish Server Administration Command Entity.
     *                <code>CommandRestartDAS</code> instance is expected.
     * @return Restart DAS query string for given command.
     */
    static String query(final Command command) {
        if (command instanceof CommandRestartDAS) {
            boolean debug = ((CommandRestartDAS)command).debug;
            int boolValSize = FALSE_VALUE.length() > TRUE_VALUE.length()
                    ? FALSE_VALUE.length() : TRUE_VALUE.length();
            StringBuilder sb = new StringBuilder(DEBUG_PARAM.length()
                    + boolValSize + 1);
            sb.append(DEBUG_PARAM);
            sb.append(PARAM_ASSIGN_VALUE);
            sb.append(debug ? TRUE_VALUE : FALSE_VALUE);
            return sb.toString();
        }
        else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpRestartDAS(final GlassFishServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
