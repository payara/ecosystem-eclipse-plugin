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

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Command that retrieves list of components defined on server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpListComponents.class)
@RunnerRestClass(runner = RunnerRestListApplications.class, command = "list-applications")
public class CommandListComponents extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for list components command. */
    private static final String COMMAND = "list-components";

    /** Error message for administration command execution exception . */
    private static final String ERROR_MESSAGE = "List components failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * List components.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param target Target server instance or cluster.
     * @return List components task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultMap<String, List<String>> listComponents(
            final PayaraServer server, final String target)
            throws PayaraIdeException {
        Command command = new CommandListComponents(target);
        Future<ResultMap<String, List<String>>> future = ServerAdmin
                .<ResultMap<String, List<String>>>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new PayaraIdeException(ERROR_MESSAGE, ie);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server list components command entity.
     * <p/>
     *
     * @param target Target GlassFish instance or cluster.
     */
    public CommandListComponents(final String target) {
        super(COMMAND, target);
    }

}
