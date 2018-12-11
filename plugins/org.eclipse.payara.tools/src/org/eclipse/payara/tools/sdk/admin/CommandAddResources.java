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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Command registers resources defined in provided xml file on specified target.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
@RunnerHttpClass(runner = RunnerHttpAddResources.class)
@RunnerRestClass(runner = RunnerRestAddResources.class)
public class CommandAddResources extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandAddResources.class);

    /** Command string for create-cluster command. */
    private static final String COMMAND = "add-resources";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add resource to target server.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param xmlResourceFile File object pointing to XML file containing resources to be added.
     * @param target GlassFish server target.
     * @return Add resource task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString addResource(PayaraServer server, File xmlResourceFile, String target) throws PayaraIdeException {
        String METHOD = "addResource";

        Command command = new CommandAddResources(xmlResourceFile, target);
        Future<ResultString> future = ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException | CancellationException ie) {
            throw new PayaraIdeException(LOGGER.excMsg(METHOD, "exception"), ie);
        }
    }

    /**
     * Add resource to target server.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param xmlResourceFile File object pointing to XML file containing resources to be added.
     * @param target GlassFish server target.
     * @param timeout Administration command execution timeout [ms].
     * @return Add resource task response.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString addResource(PayaraServer server, File xmlResourceFile, String target, long timeout)
            throws PayaraIdeException {
        String METHOD = "addResource";
        Command command = new CommandAddResources(xmlResourceFile, target);
        Future<ResultString> future = ServerAdmin.<ResultString>exec(server, command);

        try {
            return future.get(timeout, MILLISECONDS);
        } catch (InterruptedException | ExecutionException | CancellationException ie) {
            throw new PayaraIdeException(LOGGER.excMsg(METHOD, "exception"), ie);
        } catch (TimeoutException te) {
            throw new PayaraIdeException(LOGGER.excMsg(METHOD, "exceptionWithTimeout", Long.toString(timeout)), te);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** File object pointing to xml file that contains resources to be added. */
    File xmlResFile;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server add-resources command entity.
     * <p/>
     *
     * @param xmlResourceFile File object pointing to XML file containing resources to be added.
     * @param target GlassFish server target.
     */
    public CommandAddResources(final File xmlResourceFile, final String target) {
        super(COMMAND, target);
        xmlResFile = xmlResourceFile;
    }
}
