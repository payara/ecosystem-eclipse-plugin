/******************************************************************************
 * Copyright (c) 2018 Oracle
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

import org.eclipse.payara.tools.sdk.GlassFishIdeException;
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * GlassFish server stop DAS command entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass
@RunnerRestClass(runner = RunnerRestStopDAS.class)
public class CommandStopDAS extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for version command. */
    private static final String COMMAND = "stop-domain";

    /** Error message for administration command execution exception . */
    private static final String ERROR_MESSAGE = "DAS stop failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Stops running DAS server.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @return Stop DAS task response.
     * @throws GlassFishIdeException When error occurred during administration command execution.
     */
    public static ResultString stopDAS(final GlassFishServer server) throws GlassFishIdeException {
        Command command = new CommandStopDAS();
        Future<ResultString> future = ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(ERROR_MESSAGE, ie);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server version command entity.
     */
    public CommandStopDAS() {
        super(COMMAND);
    }

}
