/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.util.Map;

/**
 * Command that creates a pool of connections to an enterprise information system (EIS).
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpCreateConnectorConnectionPool.class)
@RunnerRestClass(runner = RunnerRestCreateConnectorPool.class)
public class CommandCreateConnectorConnectionPool extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create EIS connection pool command. */
    private static final String COMMAND = "create-connector-connection-pool";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Connection pool unique name (and ID). */
    final String poolName;

    /** The name of the resource adapter. */
    final String raName;

    /** The name of the connection definition. */
    final String connectionDefinition;

    /**
     * Optional properties for configuring the pool.
     * <p/>
     * <table>
     * <tr>
     * <td><b>LazyConnectionEnlistment</b></td>
     * <td><i>Deprecated.</i> Use the equivalent option. Default value is false.</td>
     * </tr>
     * <tr>
     * <td><b>LazyConnectionAssociation</b></td>
     * <td><i>Deprecated.</i> Use the equivalent option. Default value is false.</td>
     * </tr>
     * <tr>
     * <td><b>AssociateWithThread</b></td>
     * <td><i>Deprecated.</i> Use the equivalent option. Default value is false.</td>
     * </tr>
     * <tr>
     * <td><b>MatchConnections</b></td>
     * <td><i>Deprecated.</i> Use the equivalent option. Default value is false.</td>
     * </tr>
     * </table>
     */
    final Map<String, String> properties;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server create EIS connection pool command entity.
     * <p/>
     *
     * @param poolName Connection pool unique name (and ID).
     * @param raName The name of the resource adapter.
     * @param connectionDefinition The name of the connection definition.
     * @param properties Optional properties for configuring the resource.
     */
    public CommandCreateConnectorConnectionPool(final String poolName,
            final String raName, final String connectionDefinition,
            final Map<String, String> properties) {
        super(COMMAND);
        this.poolName = poolName;
        this.raName = raName;
        this.connectionDefinition = connectionDefinition;
        this.properties = properties;
    }

}
