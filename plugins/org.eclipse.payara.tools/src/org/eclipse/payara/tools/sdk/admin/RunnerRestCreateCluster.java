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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Command runner for creating a cluster.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestCreateCluster extends RunnerRest {

    /**
     * Constructs an instance of administration command executor using REST interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestCreateCluster(final PayaraServer server,
            final Command command) {
        super(server, command, "/management/domain/clusters/cluster/", null);
    }

    @Override
    protected String constructCommandUrl() throws CommandException {
        String protocol = "http";
        URI uri;
        try {
            uri = new URI(protocol, null, server.getHost(), server.getAdminPort(), path, query, null);
        } catch (URISyntaxException use) {
            throw new CommandException(CommandException.RUNNER_HTTP_URL, use);
        }
        return uri.toASCIIString();
    }

    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
        wr.write("name=" + ((CommandTarget) command).target);
        wr.flush();
        wr.close();
    }

}
