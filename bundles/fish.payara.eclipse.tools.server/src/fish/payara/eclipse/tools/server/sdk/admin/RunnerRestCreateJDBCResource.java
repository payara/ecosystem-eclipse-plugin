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

package fish.payara.eclipse.tools.server.sdk.admin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.admin.response.ActionReport.ExitCode;

/**
 * Runner for create JDBC resource command via REST interface.
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerRestCreateJDBCResource extends RunnerRest {

    /**
     * Constructs an instance of administration command executor using REST interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestCreateJDBCResource(final PayaraServer server,
            final Command command) {
        super(server, command);
    }

    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        CommandCreateJDBCResource cmd = (CommandCreateJDBCResource) command;
        OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
        StringBuilder data = new StringBuilder();
        data.append("jndi_name=").append(cmd.jndiName);
        data.append("&connectionpoolid=").append(cmd.connectionPoolId);
        appendIfNotEmpty(data, "&target", cmd.target);
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
