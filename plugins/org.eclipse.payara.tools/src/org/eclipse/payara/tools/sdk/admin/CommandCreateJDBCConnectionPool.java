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
 * Command that creates JDBC connection pool on server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpCreateJDBCConnectionPool.class)
@RunnerRestClass(runner = RunnerRestCreateJDBCConnectionPool.class)
public class CommandCreateJDBCConnectionPool extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create JDBC connection pool command. */
    private static final String COMMAND = "create-jdbc-connection-pool";

    /** Error message for administration command execution exception . */
    private static final String ERROR_MESSAGE = "Create JDBC connection pool failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * create JDBC connection pool.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param connectionPoolId Connection pool unique name (and ID).
     * @param dataSourceClassName The name of the vendor-supplied JDBC data source resource manager.
     * @param resType Resource type.
     * @param properties Optional properties for configuring the pool.
     * @return Create JDBC connection pool task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString createJDBCConnectionPool(
            final PayaraServer server, final String connectionPoolId,
            final String dataSourceClassName, final String resType,
            final Map<String, String> properties) throws PayaraIdeException {
        Command command = new CommandCreateJDBCConnectionPool(connectionPoolId,
                dataSourceClassName, resType, properties);
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
     * Create JDBC connection pool.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param connectionPoolId Connection pool unique name (and ID).
     * @param dataSourceClassName The name of the vendor-supplied JDBC data source resource manager.
     * @param resType Resource type.
     * @param properties Optional properties for configuring the pool.
     * @param timeout Administration command execution timeout [ms].
     * @return Create JDBC connection pool task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString createJDBCConnectionPool(
            final PayaraServer server, final String connectionPoolId,
            final String dataSourceClassName, final String resType,
            final Map<String, String> properties, final long timeout)
            throws PayaraIdeException {
        Command command = new CommandCreateJDBCConnectionPool(connectionPoolId,
                dataSourceClassName, resType, properties);
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

    /** The name of the vendor-supplied JDBC data source resource manager. */
    final String dataSourceClassName;

    /** Resource type. */
    final String resType;

    /**
     * Optional properties for configuring the pool.
     * <p/>
     * <table>
     * <tr>
     * <td><b>user</b></td>
     * <td>Specifies the user name for connecting to the database.</td>
     * </tr>
     * <tr>
     * <td><b>password</b></td>
     * <td>Specifies the password for connecting to the database.</td>
     * </tr>
     * <tr>
     * <td><b>databaseName</b></td>
     * <td>Specifies the database for this connection pool.</td>
     * </tr>
     * <tr>
     * <td><b>serverName</b></td>
     * <td>Specifies the database server for this connection pool.</td>
     * </tr>
     * <tr>
     * <td><b>port</b></td>
     * <td>Specifies the port on which the database server listens for requests.</td>
     * </tr>
     * <tr>
     * <td><b>networkProtocol</b></td>
     * <td>Specifies the communication protocol.</td>
     * </tr>
     * <tr>
     * <td><b>roleName</b></td>
     * <td>Specifies the initial SQL role name.</td>
     * </tr>
     * <tr>
     * <td><b>datasourceName</b></td>
     * <td>Specifies an underlying XADataSource, or a ConnectionPoolDataSource if connection pooling is
     * done.</td>
     * </tr>
     * <tr>
     * <td><b>description</b></td>
     * <td>Specifies a text description.</td>
     * </tr>
     * <tr>
     * <td><b>url</b></td>
     * <td>Specifies the URL for this connection pool. Although this is not a standard property, it is
     * commonly used.</td>
     * </tr>
     * <tr>
     * <td><b>dynamic-reconfiguration-wait-timeout-in-seconds</b></td>
     * <td>Used to enable dynamic reconfiguration of the connection pool transparently to the
     * applications that are using the pool, so that applications need not be re-enabled for the
     * attribute or property changes to the pool to take effect. Any in-flight transaction's connection
     * requests will be allowed to complete with the old pool configuration as long as the connection
     * requests are within the timeout period, so as to complete the transaction. New connection
     * requests will wait for the pool reconfiguration to complete and connections will be acquired
     * using the modified pool configuration.</td>
     * </tr>
     * <tr>
     * <td><b>LazyConnectionEnlistment</b></td>
     * <td><i>Deprecated.</i> Use the equivalent attribute. The default value is false.</td>
     * </tr>
     * <tr>
     * <td><b>LazyConnectionAssociation</b></td>
     * <td>Deprecated.</i> Use the equivalent attribute. The default value is false.</td>
     * </tr>
     * <tr>
     * <td><b>AssociateWithThread</b></td>
     * <td><i>Deprecated.</i> Use the equivalent attribute. The default value is false.</td>
     * </tr>
     * <tr>
     * <td><b>MatchConnections</b></td>
     * <td><i>Deprecated.</i> Use the equivalent attribute. The default value is true.</td>
     * </tr>
     * <tr>
     * <td><b>Prefer-Validate-Over-Recreate</b></td>
     * <td>Specifies whether pool resizer should validate idle connections before destroying and
     * recreating them. The default value is true.</td>
     * </tr>
     * <tr>
     * <td><b>time-to-keep-queries-in-minutes</b></td>
     * <td>Specifies the number of minutes that will be cached for use in calculating frequently used
     * queries. Takes effect when SQL tracing and monitoring are enabled for the JDBC connection pool.
     * The default value is 5 minutes.</td>
     * </tr>
     * <tr>
     * <td><b>number-of-top-queries-to-report</b></td>
     * <td>Specifies the number of queries to list when reporting the top and most frequently used
     * queries. Takes effect when SQL tracing and monitoring are enabled for the JDBC connection pool.
     * The default value is 10 queries.</td>
     * </tr>
     * </table>
     */
    final Map<String, String> properties;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server create JDBC connection pool command entity.
     * <p/>
     *
     * @param connectionPoolId Connection pool unique name (and ID).
     * @param dataSourceClassName The name of the vendor-supplied JDBC data source resource manager.
     * @param resType Resource type.
     * @param properties Optional properties for configuring the pool.
     */
    public CommandCreateJDBCConnectionPool(final String connectionPoolId,
            final String dataSourceClassName, final String resType,
            final Map<String, String> properties) {
        super(COMMAND);
        this.connectionPoolId = connectionPoolId;
        this.dataSourceClassName = dataSourceClassName;
        this.resType = resType;
        this.properties = properties;
    }

}
