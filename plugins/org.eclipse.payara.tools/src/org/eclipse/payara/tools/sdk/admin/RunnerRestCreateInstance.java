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

import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Command runner for creating an instance.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestCreateInstance extends RunnerRest {

    /**
     * Constructs an instance of administration command executor using REST interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestCreateInstance(final PayaraServer server,
            final Command command) {
        super(server, command);
    }

    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
        CommandCreateInstance createCommand = (CommandCreateInstance) command;
        StringBuilder data = new StringBuilder();
        data.append("instance_name=").append(createCommand.name);
        data.append("&node=").append(createCommand.node);
        if (createCommand.target != null) {
            data.append("&cluster=").append(createCommand.target);
        }

        wr.write(data.toString());
        wr.flush();
        wr.close();
    }

}
