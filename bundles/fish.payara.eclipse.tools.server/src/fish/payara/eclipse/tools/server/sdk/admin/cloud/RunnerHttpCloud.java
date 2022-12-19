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

package fish.payara.eclipse.tools.server.sdk.admin.cloud;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.admin.Command;
import fish.payara.eclipse.tools.server.sdk.admin.RunnerHttp;

/**
 * GlassFish cloud administration command execution using HTTP interface.
 * <p/>
 * HTTP interface is not supported with cloud commands.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpCloud extends RunnerHttp {

    /**
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     * This constructor prototype is called from factory class and should remain public in all child
     * classes.
     * <p/>
     *
     * @param server GlassFish cloud entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpCloud(final PayaraServer server,
            final CommandCloud command) {
        super(server, command, "/command/cloud/", null);
        throw new UnsupportedOperationException(
                "HTTP interface does not support cloud commands.");
    }

    /**
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     * @param query Query string for this command.
     */
    RunnerHttpCloud(final PayaraServer server, final Command command,
            final String query) {
        super(server, command, "/command/cloud/", query);
        throw new UnsupportedOperationException(
                "HTTP interface does not support cloud commands.");
    }

    /**
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     * @param path Path which builds URL we speak to.
     * @param query Query string for this command.
     */
    RunnerHttpCloud(final PayaraServer server, final Command command,
            final String path, final String query) {
        super(server, command, path, query);
        throw new UnsupportedOperationException(
                "HTTP interface does not support cloud commands.");
    }

}
