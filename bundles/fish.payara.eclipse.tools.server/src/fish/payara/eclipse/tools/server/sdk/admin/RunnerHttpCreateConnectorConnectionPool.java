/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.sdk.admin;

import fish.payara.eclipse.tools.server.PayaraServer;

/**
 * GlassFish server create EIS connection pool administration command execution using HTTP
 * interface.
 * <p/>
 * Contains code for create EIS connection pool command. Class implements GlassFish server
 * administration functionality trough HTTP interface.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpCreateConnectorConnectionPool extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create JDBC connection pool command <code>poolname</code> parameter name.
     */
    private static final String POOL_NAME_PARAM = "poolname";

    /**
     * Create JDBC connection pool command <code>raname</code> parameter name.
     */
    private static final String RA_NAME_PARAM = "raname";

    /**
     * Create JDBC connection pool command <code>connectiondefinition</code> parameter name.
     */
    private static final String CONNECTION_DEFINITION_PARAM = "connectiondefinition";

    /**
     * Create JDBC connection pool command <code>property</code> parameter name.
     */
    private static final String PROPERTY_PARAM = "property";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds create JDBC connection pool query string for given command.
     * <p/>
     * <code>QUERY :: "poolname" '=' &lt;poolname&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "restype" '=' &lt;restype&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "raname" '=' &lt;raName&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "connectiondefinition" '=' &lt;connectionDefinition&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "properties" '=' &lt;pname&gt; '=' &lt;pvalue&gt;
     * { ':' &lt;pname&gt; '=' &lt;pvalue&gt;} ]</code>
     * <p/>
     *
     * @param command GlassFish server administration command entity.
     * <code>CommandCreateAdminObject</code> instance is expected.
     * @return Create JDBC connection pool query string for given command.
     */
    private static String query(final Command command) {
        String poolName;
        String raName;
        String connectionDefinition;
        if (command instanceof CommandCreateConnectorConnectionPool) {
            poolName = ((CommandCreateConnectorConnectionPool) command).poolName;
            raName = ((CommandCreateConnectorConnectionPool) command).raName;
            connectionDefinition = ((CommandCreateConnectorConnectionPool) command).connectionDefinition;
        } else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        boolean isRaname = raName != null && raName.length() > 0;
        boolean isConnectionDefinition = connectionDefinition != null
                && connectionDefinition.length() > 0;
        // Calculate StringBuilder initial length to avoid resizing
        StringBuilder sb = new StringBuilder(
                POOL_NAME_PARAM.length() + 1 + poolName.length()
                        + (isRaname
                                ? RA_NAME_PARAM.length() + 1 + raName.length()
                                : 0)
                        + (isConnectionDefinition
                                ? CONNECTION_DEFINITION_PARAM.length()
                                        + 1 + connectionDefinition.length()
                                : 0)
                        + queryPropertiesLength(
                                ((CommandCreateConnectorConnectionPool) command).properties, PROPERTY_PARAM));
        if (isRaname) {
            sb.append(PARAM_SEPARATOR).append(RA_NAME_PARAM);
            sb.append(PARAM_ASSIGN_VALUE).append(raName);
        }
        if (isConnectionDefinition) {
            sb.append(PARAM_SEPARATOR).append(RA_NAME_PARAM);
            sb.append(PARAM_ASSIGN_VALUE).append(connectionDefinition);
        }
        queryPropertiesAppend(sb,
                ((CommandCreateConnectorConnectionPool) command).properties,
                PROPERTY_PARAM, true);
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
    public RunnerHttpCreateConnectorConnectionPool(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
