/******************************************************************************
 * Copyright (c) 2018 Oracle
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

import org.eclipse.payara.tools.sdk.admin.response.ActionReport.ExitCode;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Runner for create JDBC connection pool command via REST interface.
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerRestCreateJDBCConnectionPool extends RunnerRest {

    /**
     * Constructs an instance of administration command executor using REST interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestCreateJDBCConnectionPool(final PayaraServer server,
            final Command command) {
        super(server, command);
    }

    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        CommandCreateJDBCConnectionPool cmd = (CommandCreateJDBCConnectionPool) command;
        OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
        StringBuilder data = new StringBuilder();
        data.append("jdbc_connection_pool_id=").append(cmd.connectionPoolId);
        data.append("&datasourceClassname=").append(cmd.dataSourceClassName);
        data.append("&resType=").append(cmd.resType);
        appendProperties(data, cmd.properties, "property", true);
        wr.write(data.toString());
        wr.close();
    }

    /**
     * Overridden because server returns WARNING even when it creates the resource.
     */
    @Override
    protected boolean isSuccess() {
        return report.isSuccess() || report.getExitCode().equals(ExitCode.WARNING);
    }
}
