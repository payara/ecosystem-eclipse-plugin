/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.text.MessageFormat;

import org.eclipse.payara.tools.server.GlassFishServer;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerHttpSetProperty extends RunnerHttp {

    /**
     * Creates query string from command object properties.
     * <p/>
     * 
     * @param command GlassFish server administration command entity.
     * @return Query string from command object properties.
     */
    private static String query(CommandSetProperty command) {
        return MessageFormat.format(
                command.format, command.property, command.value);
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
    public RunnerHttpSetProperty(final GlassFishServer server,
            final Command command) {
        super(server, command, query((CommandSetProperty) command));
    }

}
