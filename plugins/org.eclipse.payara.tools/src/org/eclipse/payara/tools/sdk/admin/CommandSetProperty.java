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
import static org.eclipse.payara.tools.sdk.admin.ServerAdmin.exec;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Command that sets property (properties) on the server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpSetProperty.class)
@RunnerRestClass(runner = RunnerRestSetProperty.class)
public class CommandSetProperty extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create-cluster command. */
    private static final String COMMAND = "set";

    /** Error message prefix for administration command execution exception . */
    private static final String ERROR_MESSAGE_PREFIX = "Could not set value ";

    /**
     * Error message middle part for administration command execution exception .
     */
    private static final String ERROR_MESSAGE_MIDDLE = " of property ";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build error message from command property name and it's value.
     * <p/>
     *
     * @param command Command used to build error message.
     * @return Error message for administration command execution exception.
     */
    private static String errorMessage(final CommandSetProperty command) {
        int valueLen = command.value != null
                ? command.value.length()
                : 0;
                
        int propertyLen = command.property != null
                ? command.property.length()
                : 0;
                
        return new StringBuilder(ERROR_MESSAGE_PREFIX.length() + ERROR_MESSAGE_MIDDLE.length() + valueLen + propertyLen)
            .append(ERROR_MESSAGE_PREFIX)
            .append(valueLen > 0 ? command.value : "")
            .append(ERROR_MESSAGE_MIDDLE)
            .append(propertyLen > 0 ? command.property : "")
            .toString();
    }

    /**
     * Put property to server.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param command Command to set property value.
     * @return GlassFish command result containing <code>String</code> with result message.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString setProperty(PayaraServer server, CommandSetProperty command) throws PayaraIdeException {
        Future<ResultString> future = exec(server, command);
        
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException | CancellationException ee) {
            throw new PayaraIdeException(errorMessage(command), ee);
        }
    }

    /**
     * Put property to server.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param command Command to set property value.
     * @param timeout Administration command execution timeout [ms].
     * @return GlassFish command result containing <code>String</code> with result message.
     * @throws PayaraIdeException When error occurred during administration command execution.
     */
    public static ResultString setProperty(PayaraServer server, CommandSetProperty command, long timeout) throws PayaraIdeException {
        Future<ResultString> future = exec(server, command);
        
        try {
            return future.get(timeout, MILLISECONDS);
        } catch (ExecutionException | InterruptedException | CancellationException ee) {
            throw new PayaraIdeException(errorMessage(command), ee);
        } catch (TimeoutException te) {
            throw new PayaraIdeException(errorMessage(command)
                    + " in " + timeout + "ms", te);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Name of the property to set. */
    final String property;

    /** Value of the property to set. */
    final String value;

    /** Format for the query string. */
    final String format;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server set property command entity.
     * <p/>
     *
     * @param property Name of the property to set.
     * @param value Value of the property to set.
     * @param format Format for the query string.
     */
    public CommandSetProperty(String property, String value, String format) {
        super(COMMAND);
        this.property = property;
        this.value = value;
        this.format = format;
    }

    /**
     * Constructs an instance of GlassFish server set property command entity.
     * <p/>
     *
     * @param property Name of the property to set.
     * @param value Value of the property to set.
     */
    public CommandSetProperty(String property, String value) {
        super(COMMAND);
        this.property = property;
        this.value = value;
        this.format = "DEFAULT={0}={1}";
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get name of the property to set.
     * <p/>
     *
     * @return Name of the property to set.
     */
    public String getProperty() {
        return property;
    }

    /**
     * Get value of the property to set.
     * <p/>
     *
     * @return Value of the property to set.
     */
    public String getValue() {
        return value;
    }

}
