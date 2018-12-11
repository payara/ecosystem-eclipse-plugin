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

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Command that creates JDBC resource on server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpCreateJDBCResource.class)
@RunnerRestClass(runner = RunnerRestCreateJDBCResource.class)
public class CommandCreateJDBCResource extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create JDBC resource command. */
    private static final String COMMAND = "create-jdbc-resource";

    /** Error message for administration command execution exception . */
    private static final String ERROR_MESSAGE = "Create JDBC resource failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create JDBC resource.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param connectionPoolId Connection pool unique name (and ID).
     * @param jndiName The JNDI name of this JDBC resource.
     * @param target Helps specify the target to which you are deploying.
     * @param properties Optional properties for configuring the pool.
     * @return Create JDBC connection pool task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString createJDBCResource(
            final PayaraServer server, final String connectionPoolId,
            final String jndiName, final String target,
            final Map<String, String> properties) throws PayaraIdeException {
        Command command = new CommandCreateJDBCResource(connectionPoolId,
                jndiName, target, properties);
        Future<ResultString> future = ServerAdmin
                .<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new PayaraIdeException(ERROR_MESSAGE, ie);
        }
    }

    /**
     * Create JDBC resource.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param connectionPoolId Connection pool unique name (and ID).
     * @param jndiName The JNDI name of this JDBC resource.
     * @param target Helps specify the target to which you are deploying.
     * @param properties Optional properties for configuring the pool.
     * @param timeout Administration command execution timeout [ms].
     * @return Create JDBC resource task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString createJDBCResource(
            final PayaraServer server, final String connectionPoolId,
            final String jndiName, final String target,
            final Map<String, String> properties, final long timeout)
            throws PayaraIdeException {
        Command command = new CommandCreateJDBCResource(connectionPoolId,
                jndiName, target, properties);
        Future<ResultString> future = ServerAdmin
                .<ResultString>exec(server, command);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new PayaraIdeException(ERROR_MESSAGE, ie);
        } catch (TimeoutException te) {
            throw new PayaraIdeException(
                    ERROR_MESSAGE + " in " + timeout + "ms", te);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Connection pool unique name (and ID). */
    final String connectionPoolId;

    /** The JNDI name of this JDBC resource. */
    final String jndiName;

    /**
     * Helps specify the target to which you are deploying.
     * <p/>
     * Valid values are:<br/>
     * <table>
     * <tr>
     * <td><b>server</b></td>
     * <td>Deploys the component to the default server instance. This is the default value.</td>
     * </tr>
     * <tr>
     * <td><b>domain</b></td>
     * <td>Deploys the component to the domain.</td>
     * </tr>
     * <tr>
     * <td><b>cluster_name</b></td>
     * <td>Deploys the component to every server instance in the cluster.</td>
     * </tr>
     * <tr>
     * <td><b>instance_name</b></td>
     * <td>Deploys the component to a particular sever instance.</td>
     * </tr>
     * </table>
     */
    final String target;

    /** Optional properties for configuring the resource. */
    final Map<String, String> properties;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server create JDBC resource command entity.
     * <p/>
     *
     * @param connectionPoolId Connection pool unique name (and ID).
     * @param jndiName The JNDI name of this JDBC resource.
     * @param target Specify the target to which you are deploying.
     * @param properties Optional properties for configuring the resource.
     */
    public CommandCreateJDBCResource(final String connectionPoolId,
            final String jndiName, final String target,
            final Map<String, String> properties) {
        super(COMMAND);
        this.connectionPoolId = connectionPoolId;
        this.jndiName = jndiName;
        this.target = target;
        this.properties = properties;
    }

}
