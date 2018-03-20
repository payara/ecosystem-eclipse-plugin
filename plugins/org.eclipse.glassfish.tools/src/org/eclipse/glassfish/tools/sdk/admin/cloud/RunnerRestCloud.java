/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin.cloud;

import org.eclipse.glassfish.tools.sdk.admin.Command;
import org.eclipse.glassfish.tools.sdk.admin.RunnerRest;
import org.eclipse.glassfish.tools.server.GlassFishServer;

/**
 * GlassFish cloud administration command execution using REST interface.
 * <p/>
 * Class implements GlassFish cloud administration functionality trough REST
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
class RunnerRestCloud extends RunnerRest {
    
    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * This constructor prototype is called from factory class and should
     * remain public in all child classes.
     * <p/>
     * @param server  GlassFish cloud entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestCloud(final GlassFishServer server,
            final Command command) {
        super(server, command, "/command/cloud/", null);
    }

    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     * @param query   Query string for this command.
     */
    RunnerRestCloud(final GlassFishServer server, final Command command,
            final String query) {
        super(server, command, "/command/cloud/", query);
    }

    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     * @param path    Path which builds URL we speak to.
     * @param query   Query string for this command.
     */
    RunnerRestCloud(final GlassFishServer server, final Command command,
            final String path, final String query) {
        super(server, command, path, query);
    }

}
