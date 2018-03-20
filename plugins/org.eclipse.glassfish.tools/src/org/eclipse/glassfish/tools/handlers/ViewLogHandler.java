/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.handlers;

import static org.eclipse.glassfish.tools.GlassfishToolsPlugin.logMessage;
import static org.eclipse.glassfish.tools.log.GlassfishConsoleManager.getServerLogFileConsole;
import static org.eclipse.glassfish.tools.log.GlassfishConsoleManager.removeServerLogFileConsole;
import static org.eclipse.glassfish.tools.log.GlassfishConsoleManager.showConsole;
import static org.eclipse.glassfish.tools.utils.WtpUtil.load;

import org.eclipse.glassfish.tools.log.IGlassFishConsole;
import org.eclipse.glassfish.tools.sdk.server.FetchLogPiped;
import org.eclipse.glassfish.tools.server.GlassFishServer;
import org.eclipse.glassfish.tools.server.ServerStatus;
import org.eclipse.wst.server.core.IServer;

public class ViewLogHandler extends AbstractGlassfishSelectionHandler {

	@Override
	public void processSelection(IServer server) {
		try {
			GlassFishServer serverAdapter = load(server, GlassFishServer.class);
						
			if (serverAdapter.isRemote()) {
				if (!serverAdapter.getServerBehaviourAdapter().getServerStatus(true)	.equals(ServerStatus.RUNNING_DOMAIN_MATCHING)) {
					showMessageDialog();
					return;
				} 
				
				removeServerLogFileConsole(serverAdapter);
			}
			
			IGlassFishConsole console = getServerLogFileConsole(serverAdapter);
			showConsole(getServerLogFileConsole(serverAdapter));
			
			if (!console.isLogging()) {
				console.startLogging(FetchLogPiped.create(serverAdapter, false));
			}

		} catch (Exception e) {
			logMessage("Error opening log: " + e.getMessage());

		}
	}

}
