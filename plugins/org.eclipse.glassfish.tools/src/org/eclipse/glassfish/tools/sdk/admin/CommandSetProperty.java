/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.sdk.GlassFishIdeException;

/**
 * Command that sets property (properties) on the server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpSetProperty.class)
@RunnerRestClass(runner=RunnerRestSetProperty.class)
public class CommandSetProperty extends Command {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create-cluster command. */
    private static final String COMMAND = "set";
    
    /** Error message prefix for administration command execution exception .*/
    private static final String ERROR_MESSAGE_PREFIX
            = "Could not set value ";

    /** Error message middle part for administration command execution
     *  exception .*/
    private static final String ERROR_MESSAGE_MIDDLE
            = " of property ";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build error message from command property name and it's value.
     * <p/>
     * @param command Command used to build error message.
     * @return Error message for administration command execution exception.
     */
    private static String errorMessage(final CommandSetProperty command) {
        int valueLen = command.value != null
                ? command.value.length() : 0;
        int propertyLen = command.property != null
                ? command.property.length() : 0;
        StringBuilder sb = new StringBuilder(ERROR_MESSAGE_PREFIX.length()
                + ERROR_MESSAGE_MIDDLE.length() + valueLen + propertyLen);
        sb.append(ERROR_MESSAGE_PREFIX);
        sb.append(valueLen > 0 ? command.value : "");
        sb.append(ERROR_MESSAGE_MIDDLE);
        sb.append(propertyLen > 0 ? command.property : "");
        return sb.toString();
    }

    /**
     * Put property to server.
     * <p/>
     * @param server  GlassFish server entity.
     * @param command Command to set property value.
     * @return GlassFish command result containing <code>String</code> with
     *         result message.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString setProperty(
            final GlassFishServer server, final CommandSetProperty command)
            throws GlassFishIdeException {
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new GlassFishIdeException(errorMessage(command), ee);
        }
    }

    /**
     * Put property to server.
     * <p/>
     * @param server  GlassFish server entity.
     * @param command Command to set property value.
     * @param timeout         Administration command execution timeout [ms].
     * @return GlassFish command result containing <code>String</code> with
     *         result message.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString setProperty(
            final GlassFishServer server, final CommandSetProperty command,
            final long timeout) throws GlassFishIdeException {
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new GlassFishIdeException(errorMessage(command), ee);
        } catch (TimeoutException te) {
            throw new GlassFishIdeException(errorMessage(command)
                    + " in " + timeout + "ms", te);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
    /** Name of the property to set. */
    final String property;
    
    /** Value of the property to set. */
    final String value;
    
    /** Format for the query string. */
    final String format;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server set property command entity.
     * <p/>
     * @param property Name of the property to set.
     * @param value    Value of the property to set.
     * @param format   Format for the query string.
     */
    public CommandSetProperty(final String property, final String value,
            final String format) {
        super(COMMAND);
        this.property = property;
        this.value = value;
        this.format = format;
    }
    
    /**
     * Constructs an instance of GlassFish server set property command entity.
     * <p/>
     * @param property Name of the property to set.
     * @param value    Value of the property to set.
     */
    public CommandSetProperty(final String property, final String value) {
        super(COMMAND);
        this.property = property;
        this.value = value;
        this.format = "DEFAULT={0}={1}";
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get name of the property to set.
     * <p/>
     * @return Name of the property to set.
     */
    public String getProperty() {
        return property;
    }

    /**
     * Get value of the property to set.
     * <p/>
     * @return Value of the property to set.
     */
    public String getValue() {
        return value;
    }

}
