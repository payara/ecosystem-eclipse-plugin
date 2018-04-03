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

import org.eclipse.payara.tools.server.PayaraServer;

/**
 * GlassFish server create administered object administration command execution using HTTP
 * interface.
 * <p/>
 * Contains code for create administered object command. Class implements GlassFish server
 * administration functionality trough HTTP interface.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpCreateAdminObject extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create JDBC connection pool command <code>restype</code> parameter name.
     */
    private static final String RESOURCE_TYPE_PARAM = "restype";

    /**
     * Create JDBC connection pool command <code>jndi_name</code> parameter name.
     */
    private static final String JNDI_NAME_PARAM = "jndi_name";

    /**
     * Create JDBC connection pool command <code>raName</code> parameter name.
     */
    private static final String RA_NAME_PARAM = "raname";

    /**
     * Create JDBC connection pool command <code>property</code> parameter name.
     */
    private static final String PROPERTY_PARAM = "property";

    /**
     * Create JDBC connection pool command <code>enabled</code> parameter name.
     */
    private static final String ENABLED_PARAM = "enabled";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds create JDBC connection pool query string for given command.
     * <p/>
     * <code>QUERY :: "jndi_name" '=' &lt;jndiName&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "restype" '=' &lt;restype&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "raname" '=' &lt;raName&gt; ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ['&' "properties" '=' &lt;pname&gt; '=' &lt;pvalue&gt;
     * { ':' &lt;pname&gt; '=' &lt;pvalue&gt;} ]</code> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * ['&' "enabled" '=' true|false ]<br/>
     * <p/>
     *
     * @param command GlassFish server administration command entity.
     * <code>CommandCreateAdminObject</code> instance is expected.
     * @return Create JDBC connection pool query string for given command.
     */
    private static String query(final Command command) {
        String jndiName;
        String resType;
        String raName;
        boolean enabled;
        if (command instanceof CommandCreateAdminObject) {
            jndiName = ((CommandCreateAdminObject) command).jndiName;
            resType = ((CommandCreateAdminObject) command).resType;
            raName = ((CommandCreateAdminObject) command).raName;
            enabled = ((CommandCreateAdminObject) command).enabled;
        } else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
        boolean isResType = resType != null && resType.length() > 0;
        boolean isRaname = raName != null && raName.length() > 0;
        // Calculate StringBuilder initial length to avoid resizing
        StringBuilder sb = new StringBuilder(
                JNDI_NAME_PARAM.length() + 1 + jndiName.length()
                        + ENABLED_PARAM.length() + 1 + toString(enabled).length()
                        + (isResType
                                ? RESOURCE_TYPE_PARAM.length() + 1 + resType.length()
                                : 0)
                        + (isRaname
                                ? RA_NAME_PARAM.length() + 1 + raName.length()
                                : 0)
                        + queryPropertiesLength(
                                ((CommandCreateAdminObject) command).properties,
                                PROPERTY_PARAM));
        // Build query string
        sb.append(JNDI_NAME_PARAM).append(PARAM_ASSIGN_VALUE);
        sb.append(jndiName);
        sb.append(PARAM_SEPARATOR).append(ENABLED_PARAM);
        sb.append(PARAM_ASSIGN_VALUE).append(toString(enabled));
        if (isResType) {
            sb.append(PARAM_SEPARATOR).append(RESOURCE_TYPE_PARAM);
            sb.append(PARAM_ASSIGN_VALUE).append(resType);
        }
        if (isRaname) {
            sb.append(PARAM_SEPARATOR).append(RA_NAME_PARAM);
            sb.append(PARAM_ASSIGN_VALUE).append(raName);
        }
        queryPropertiesAppend(sb,
                ((CommandCreateAdminObject) command).properties,
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
    public RunnerHttpCreateAdminObject(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
