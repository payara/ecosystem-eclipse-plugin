/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.sdk.GlassFishIdeException;
import org.eclipse.glassfish.tools.sdk.TaskStateListener;

/**
 * GlassFish Server Deploy Command Entity.
 * <p>
 * Holds data for command. Objects of this class are created by API user.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpDeploy.class)
@RunnerRestClass(runner=RunnerRestDeploy.class)
public class CommandDeploy extends CommandTargetName {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for deploy command. */
    private static final String COMMAND = "deploy";

    /** Error message for administration command execution exception .*/
    private static final String ERROR_MESSAGE
            = "Application deployment failed.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Deploy task that deploys application on server.
     * <p/>
     * @param server      GlassFish server entity.
     * @param application File object representing archive or directory
     *                    to be deployed.
     * @param listener    Command execution events listener.
     * @return  Deploy task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString deploy(GlassFishServer server, File application,
            TaskStateListener listener) throws GlassFishIdeException {
        Command command = new CommandDeploy(null, null, application,
                null, null, null);
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command, listener);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(ERROR_MESSAGE, ie);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** File to deploy. */
    final File path;

    /** Deployed application context root. */
    final String contextRoot;

    /** Deployment properties. */
    final Map<String, String> properties;

    /** Deployment libraries. */
    final File[] libraries;

    /** Is this deployment of a directory? */
    final boolean dirDeploy;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server deploy command entity.
     * <p/>
     * @param name        Name of module/cluster/instance to modify.
     * @param target      Target GlassFish instance or cluster where
     *                    <code>name</code> is stored.
     * @param path        File to deploy.
     * @param contextRoot Deployed application context root.
     * @param properties  Deployment properties.
     * @param libraries   Not used in actual deploy command.
     */
    public CommandDeploy(final String name, final String target,
            final File path, final String contextRoot,
            final Map<String,String> properties, final File[] libraries) {
        super(COMMAND, name, target);
        this.path = path;
        this.contextRoot = contextRoot;
        this.properties = properties;
        this.libraries = libraries;
        this.dirDeploy = path.isDirectory();
    }

}
