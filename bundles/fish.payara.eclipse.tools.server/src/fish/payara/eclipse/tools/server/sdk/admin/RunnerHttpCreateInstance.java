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

import java.util.logging.Level;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.logging.Logger;
import fish.payara.eclipse.tools.server.sdk.utils.Utils;

/**
 * GlassFish instance and cluster administration command with <code>DEFAULT=&lt;target&gt;</code>
 * query execution using HTTP interface.
 * <p/>
 * Contains common code for commands that are called with <code>DEFAULT=&lt;target&gt;</code> query
 * string. Individual child classes are not needed at this stage. Class implements GlassFish server
 * administration functionality trough HTTP interface.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpCreateInstance extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerHttpCreateInstance.class);

    /** Start/Stop command <code>DEFAULT</code> param name. */
    private static final String DEFAULT_PARAM = "DEFAULT";

    /** Start/Stop command <code>node</code> param name. */
    private static final String NODE_PARAM = "node";

    /** Start/Stop command <code>cluster</code> param name. */
    private static final String CLUSTER_PARAM = "cluster";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds enable/disable query string for given command.
     * <p/>
     * <code>QUERY :: "DEFAULT" '=' &lt;name&gt; '&' "node" '=' &lt;node&gt;
     *                ['&' "cluster" '=' &lt;cluster&gt; ]</code>
     * <p/>
     *
     * @param command GlassFish Server Admin Command Entity. <code>CommandDisable</code> instance is
     * expected.
     * @return Enable/Disable query string for given command.
     */
    private static String query(Command command) {
        final String METHOD = "query";
        String name;
        String cluster;
        String node;
        if (command instanceof CommandCreateInstance) {
            cluster = Utils.sanitizeName(
                    ((CommandCreateInstance) command).target);
            if (((CommandTargetName) command).name == null
                    || ((CommandCreateInstance) command).node == null) {
                throw new CommandException(LOGGER.excMsg(METHOD, "nullValue"));
            }
            name = Utils.sanitizeName(((CommandCreateInstance) command).name);
            node = Utils.sanitizeName(((CommandCreateInstance) command).node);
        } else {
            throw new CommandException(
                    LOGGER.excMsg(METHOD, "illegalInstance"));
        }
        StringBuilder sb = new StringBuilder(
                DEFAULT_PARAM.length() + 1 + name.length()
                        + 1 + NODE_PARAM.length() + 1 + node.length() + (cluster != null
                                ? 1 + CLUSTER_PARAM.length() + 1 + cluster.length()
                                : 0));
        sb.append(DEFAULT_PARAM).append(PARAM_ASSIGN_VALUE).append(name);
        sb.append(PARAM_SEPARATOR);
        sb.append(NODE_PARAM).append(PARAM_ASSIGN_VALUE).append(node);
        if (cluster != null) {
            sb.append(PARAM_SEPARATOR);
            sb.append(CLUSTER_PARAM).append(PARAM_ASSIGN_VALUE).append(cluster);
        }
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
    public RunnerHttpCreateInstance(final PayaraServer server,
            final Command command) {
        super(server, command, query(command));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Extracts result value from internal <code>Manifest</code> object. Value of <i>message</i>
     * attribute in <code>Manifest</code> object is stored as <i>value</i> into
     * <code>ResultString</code> result object.
     * <p/>
     *
     * @return true if result was extracted correctly. <code>null</code> <i>message</i>value is
     * considered as failure.
     */
    @Override
    protected boolean processResponse() {
        final String METHOD = "processResponse";
        try {
            result.value = manifest.getMainAttributes().getValue("message");
            result.value = result.value.replace("%%%EOL%%%", "\n");
        } catch (IllegalArgumentException iae) {
            LOGGER.log(Level.WARNING, METHOD, "illegalArgument", iae);
        }
        return result.value != null;
    }

}
