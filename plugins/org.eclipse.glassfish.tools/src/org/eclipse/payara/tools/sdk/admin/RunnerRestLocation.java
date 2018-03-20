/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.util.HashMap;
import java.util.Properties;

import org.eclipse.payara.tools.server.GlassFishServer;

/**
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestLocation extends RunnerRest {

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandLocation command;

    /**
     * Returned value is map where locations are stored under keys specified in CommandLocation class.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultMap<String, String> result;

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
    public RunnerRestLocation(final GlassFishServer server,
            final Command command) {
        super(server, command);
        this.command = (CommandLocation) command;
    }

    @Override
    protected Result createResult() {
        return result = new ResultMap<>();
    }

    @Override
    protected boolean processResponse() {
        if (report == null) {
            return false;
        }
        Properties props = report.getTopMessagePart().getProperties();
        result.value = new HashMap<>(props.size());
        for (String key : props.stringPropertyNames()) {
            result.value.put(key, props.getProperty(key));
        }
        return true;
    }

}
