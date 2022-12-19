/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
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

/**
 * Runner executes set property command via REST interface.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerRestSetProperty extends RunnerRest {

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Constructs an instance of administration command executor using REST interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestSetProperty(final PayaraServer server,
            final Command command) {
        super(server, command, "/command/", null);
    }

    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
        CommandSetProperty spCommand = (CommandSetProperty) command;
        StringBuilder data = new StringBuilder();
        data.append("values=");
        data.append(spCommand.property);
        data.append("=\"");
        data.append(spCommand.value);
        data.append("\"");
        wr.write(data.toString());
        wr.flush();
        wr.close();
    }
}
