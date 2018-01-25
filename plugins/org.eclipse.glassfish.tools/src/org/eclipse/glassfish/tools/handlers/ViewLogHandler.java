/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.handlers;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;
import org.eclipse.glassfish.tools.ServerStatus;
import org.eclipse.glassfish.tools.log.GlassfishConsoleManager;
import org.eclipse.glassfish.tools.log.IGlassFishConsole;
import org.eclipse.glassfish.tools.sdk.server.FetchLogPiped;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.wst.server.core.IServer;

public class ViewLogHandler extends AbstractGlassfishSelectionHandler {

	@Override
	public void processSelection(IStructuredSelection selection) {
		IServer server = (IServer) selection.getFirstElement();
		try {
			GlassFishServer serverAdapter = (GlassFishServer) server.loadAdapter(
					GlassFishServer.class, null);
						
			if (serverAdapter.isRemote()) {
				if (!serverAdapter.getServerBehaviourAdapter().getServerStatus(true)
						.equals(ServerStatus.RUNNING_DOMAIN_MATCHING)) {
					showMessageDialog();
					return;
				} else {
					GlassfishConsoleManager.removeServerLogFileConsole(serverAdapter);
				}
			}
			
			IGlassFishConsole console = GlassfishConsoleManager.getServerLogFileConsole(serverAdapter);
			GlassfishConsoleManager.showConsole(console);
			if (!console.isLogging())
				console.startLogging(FetchLogPiped.create(serverAdapter, false));

		} catch (Exception e) {
			GlassfishToolsPlugin.logMessage("Error opening log: " + e.getMessage());

		}
	}

}
