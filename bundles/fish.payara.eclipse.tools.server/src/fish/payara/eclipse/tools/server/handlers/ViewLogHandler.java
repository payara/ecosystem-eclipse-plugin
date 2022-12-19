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

package fish.payara.eclipse.tools.server.handlers;

import static fish.payara.eclipse.tools.server.PayaraServerPlugin.logMessage;
import static fish.payara.eclipse.tools.server.log.PayaraConsoleManager.getServerLogFileConsole;
import static fish.payara.eclipse.tools.server.log.PayaraConsoleManager.removeServerLogFileConsole;
import static fish.payara.eclipse.tools.server.log.PayaraConsoleManager.showConsole;
import static fish.payara.eclipse.tools.server.utils.WtpUtil.load;

import org.eclipse.wst.server.core.IServer;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.ServerStatus;
import fish.payara.eclipse.tools.server.log.IPayaraConsole;
import fish.payara.eclipse.tools.server.sdk.server.FetchLogPiped;

public class ViewLogHandler extends AbstractPayaraSelectionHandler {

    @Override
    public void processSelection(IServer server) {
        try {
            PayaraServer serverAdapter = load(server, PayaraServer.class);

            if (serverAdapter.isRemote()) {
                if (!serverAdapter.getServerBehaviourAdapter().getServerStatus(true).equals(ServerStatus.RUNNING_DOMAIN_MATCHING)) {
                    showMessageDialog();
                    return;
                }

                removeServerLogFileConsole(serverAdapter);
            }

            IPayaraConsole console = getServerLogFileConsole(serverAdapter);
            showConsole(getServerLogFileConsole(serverAdapter));

            if (!console.isLogging()) {
                console.startLogging(FetchLogPiped.create(serverAdapter, false));
            }

        } catch (Exception e) {
            logMessage("Error opening log: " + e.getMessage());

        }
    }

}
