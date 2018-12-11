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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Command that retrieves property (properties) from server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpGetProperty.class)
@RunnerRestClass(runner = RunnerRestGetProperty.class)
public class CommandGetProperty extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandGetProperty.class);

    /** Command string for create-cluster command. */
    private static final String COMMAND = "get";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Pattern that defines properties to retrieve. */
    String propertyPattern;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve properties from server.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param propertyPattern Pattern that defines properties to retrieve.
     * @return GlassFish command result containing map with key-value pairs returned by server.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultMap<String, String> getProperties(
            final PayaraServer server, final String propertyPattern)
            throws PayaraIdeException {
        final String METHOD = "getProperties";
        Future<ResultMap<String, String>> future = ServerAdmin.<ResultMap<String, String>>exec(
                server, new CommandGetProperty(propertyPattern));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new PayaraIdeException(
                    LOGGER.excMsg(METHOD, "exception", propertyPattern), ee);
        }
    }

    /**
     * Retrieve properties from server with timeout.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param propertyPattern Pattern that defines properties to retrieve.
     * @param timeout Administration command execution timeout [ms].
     * @return GlassFish command result containing map with key-value pairs returned by server.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultMap<String, String> getProperties(
            final PayaraServer server, final String propertyPattern,
            final long timeout)
            throws PayaraIdeException {
        final String METHOD = "getProperties";
        Future<ResultMap<String, String>> future = ServerAdmin.<ResultMap<String, String>>exec(
                server, new CommandGetProperty(propertyPattern));
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new PayaraIdeException(
                    LOGGER.excMsg(METHOD, "exception", propertyPattern), ee);
        } catch (TimeoutException te) {
            throw new PayaraIdeException(
                    LOGGER.excMsg(METHOD, "exceptionWithTimeout",
                            propertyPattern, Long.toString(timeout)),
                    te);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server get property command entity.
     * <p/>
     *
     * @param property Pattern that defines property tor retrieve.
     */
    public CommandGetProperty(final String property) {
        super(COMMAND);
        propertyPattern = property;
    }
}
