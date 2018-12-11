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
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * GlassFish server restart DAS command entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpRestartDAS.class)
@RunnerRestClass(command = "restart")
public class CommandRestartDAS extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for version command. */
    private static final String COMMAND = "restart-domain";

    /** Error message for administration command execution exception . */
    private static final String ERROR_MESSAGE = "DAS restart failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Restarts running DAS server.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param debug Specifies whether the domain is restarted with JPDA.
     * @return Restart DAS task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString restartDAS(final PayaraServer server,
            final boolean debug) throws PayaraIdeException {
        Command command = new CommandRestartDAS(debug);
        Future<ResultString> future = ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new PayaraIdeException(ERROR_MESSAGE, ie);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Specifies whether the domain is restarted with JPDA. */
    final boolean debug;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server version command entity.
     * <p/>
     *
     * @param debug Specifies whether the domain is restarted with JPDA.
     */
    public CommandRestartDAS(final boolean debug) {
        super(COMMAND);
        this.debug = debug;
    }

}
