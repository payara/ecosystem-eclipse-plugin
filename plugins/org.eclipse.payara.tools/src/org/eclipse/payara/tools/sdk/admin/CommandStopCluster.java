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

import static org.eclipse.payara.tools.sdk.admin.ServerAdmin.exec;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * GlassFish Server Stop Cluster Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpTarget.class)
@RunnerRestClass(runner = RunnerRestStopCluster.class)
public class CommandStopCluster extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for stop-cluster command. */
    private static final String COMMAND = "stop-cluster";

    /** Error message for administration command execution exception . */
    private static final String ERROR_MESSAGE = "Cluster stop failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Stops cluster.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param target Cluster name.
     * @return Stop cluster task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString stopCluster(PayaraServer server, String target) throws PayaraIdeException {
        Future<ResultString> future = exec(server, new CommandStopCluster(target));
        
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException| CancellationException ie) {
            throw new PayaraIdeException(ERROR_MESSAGE, ie);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server stop-cluster command entity.
     * <p/>
     *
     * @param target Target GlassFish cluster.
     */
    public CommandStopCluster(String target) {
        super(COMMAND, target);
    }

}
