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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Command that retrieves list of JDBC resources defined on server.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpListResources.class)
@RunnerRestClass(runner = RunnerRestListResources.class)
public class CommandListResources extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandListResources.class);

    /**
     * Command string prefix used to construct list JDBC resources HTTP command.
     */
    private static final String COMMAND_PREFIX = "list-";

    /**
     * Command string suffix used to construct list JDBC resources HTTP command.
     */
    private static final String COMMAND_SUFFIX = "s";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add resource to target server.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param cmdSuffix Resource command suffix. Value should not be null.
     * @param target GlassFish server target.
     * @return Add resource task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultList<String> listResources(final PayaraServer server,
            final String cmdSuffix, final String target)
            throws PayaraIdeException {
        final String METHOD = "listResources";
        Command command = new CommandListResources(command(cmdSuffix), target);
        Future<ResultList<String>> future = ServerAdmin.<ResultList<String>>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new PayaraIdeException(
                    LOGGER.excMsg(METHOD, "exception"), ie);
        }
    }

    /**
     * Constructs command string for provided resource command suffix.
     * <p/>
     *
     * @param resourceCmdSuffix Resource command suffix. Value should not be null.
     */
    public static String command(String resourceCmdSuffix) {
        StringBuilder sb = new StringBuilder(COMMAND_PREFIX.length()
                + COMMAND_SUFFIX.length() + resourceCmdSuffix.length());
        sb.append(COMMAND_PREFIX);
        sb.append(resourceCmdSuffix);
        sb.append(COMMAND_SUFFIX);
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server list JDBC resources command entity.
     * <p/>
     * Command string is supplied as an argument.
     * <p/>
     *
     * @param command Server command represented by this object. Use <code>command</code> static method
     * to build this string using resource command suffix.
     * @param target Target GlassFish instance or cluster.
     */
    public CommandListResources(final String command, final String target) {
        super(command, target);
    }
}
