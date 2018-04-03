/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.handlers;

import static org.eclipse.payara.tools.PayaraToolsPlugin.logMessage;
import static org.eclipse.payara.tools.log.PayaraConsoleManager.getServerLogFileConsole;
import static org.eclipse.payara.tools.log.PayaraConsoleManager.removeServerLogFileConsole;
import static org.eclipse.payara.tools.log.PayaraConsoleManager.showConsole;
import static org.eclipse.payara.tools.utils.WtpUtil.load;

import org.eclipse.payara.tools.log.IPayaraConsole;
import org.eclipse.payara.tools.sdk.server.FetchLogPiped;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.payara.tools.server.ServerStatus;
import org.eclipse.wst.server.core.IServer;

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
