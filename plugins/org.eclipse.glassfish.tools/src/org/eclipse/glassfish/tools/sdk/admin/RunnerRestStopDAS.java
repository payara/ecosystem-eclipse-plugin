/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin;

import org.eclipse.glassfish.tools.server.GlassFishServer;

/**
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestStopDAS extends RunnerRest {
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestStopDAS(final GlassFishServer server,
            final Command command) {
        super(server, command);
    }

//    @Override
//    protected String constructCommandUrl() throws CommandException {
//        String protocol = "http";
//        URI uri;
//        try {
//            uri = new URI(protocol, null, server.getHost(), server.getAdminPort(), path + "stop", query, null);
//        } catch (URISyntaxException use) {
//            throw new CommandException(CommandException.RUNNER_HTTP_URL, use);
//        }
//        return uri.toASCIIString();
//    }
//
//    @Override
//    protected String getRequestMethod() {
//        return "POST";
//    }
}
