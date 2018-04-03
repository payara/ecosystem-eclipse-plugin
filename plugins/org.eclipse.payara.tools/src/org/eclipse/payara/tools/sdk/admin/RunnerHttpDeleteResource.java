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

import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerHttpDeleteResource extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerHttpDeleteResource.class);

    /** Deploy command <code>DEFAULT</code> parameter name. */
    private static final String DEFAULT_PARAM = "DEFAULT";

    /**
     * Creates query string from command object properties.
     * <p/>
     *
     * @param command GlassFish server administration command entity.
     * @return Query string from command object properties.
     */
    private static String query(CommandDeleteResource command) {
        StringBuilder query = new StringBuilder(128);
        query.append(DEFAULT_PARAM);
        query.append('=');
        query.append(command.name);
        if (null != command.target) {
            query.append(PARAM_SEPARATOR);
            query.append("target=");
            query.append(command.target);
        }
        return query.toString();
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
    public RunnerHttpDeleteResource(final PayaraServer server,
            final Command command) {
        super(server, command, query((CommandDeleteResource) command));
    }
}
