/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.log;

import java.io.File;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

/**
 * This factory class enforces certain rules regarding glassfish consoles. 1.
 * There is only one standard glassfish console. 2. User can trigger showing the
 * server log file console that shows whole server.log file. 3. Startup process
 * console exists during the startup process of glassfish. Unless the startup
 * does not fail it will be not shown to user.
 * 
 * @author Peter Benedikovic
 * 
 */
public class GlassfishConsoleManager {

	private static IConsoleManager manager = ConsolePlugin.getDefault()
			.getConsoleManager();

	public static IGlassFishConsole showConsole(IGlassFishConsole console) {
		manager.addConsoles(new IConsole[] { console });
		manager.showConsoleView(console);
		return console;
	}

	/**
	 * Returns standard console for specified server. For each server there is
	 * only one console. It reads information from server.log file but only
	 * newly added lines.
	 * 
	 * @param server
	 * @return
	 */
	public static IGlassFishConsole getStandardConsole(
			GlassFishServer server) {
		String consoleID = createStandardConsoleName(server);
		IGlassFishConsole gfConsole = findConsole(consoleID);
		if (gfConsole == null) {
			gfConsole = new GlassfishConsole(consoleID, AbstractLogFilter.createFilter(server));//, FetchLog.create(server, true));
		}
		return gfConsole;
	}

	/**
	 * Returns console for showing contents of the whole server.log file.
	 * For the same server.log file there is only one console at the time.
	 * 
	 * @param server
	 * @return
	 */
	public static IGlassFishConsole getServerLogFileConsole(
			GlassFishServer server) {
		String consoleID = createServerLogConsoleName(server);
		IGlassFishConsole gfConsole = findConsole(consoleID);
		if (gfConsole == null) {
			gfConsole = new GlassfishConsole(consoleID, AbstractLogFilter.createFilter(server));//, FetchLog.create(server, false));
		}
		return gfConsole;
	}

	/**
	 * Creates new startup process console. There should be only one
	 * for particular GF server.
	 * 
	 * @param server
	 * @return
	 */
	public static IGlassFishConsole getStartupProcessConsole(
			GlassFishServer server, Process launchProcess) {
		String consoleID = createStartupProcessConsoleName(server);
		IGlassFishConsole gfConsole = findConsole(consoleID);
		if (gfConsole == null) {
			gfConsole = new GlassfishStartupConsole(consoleID, new NoOpFilter());//, new FetchLog[] {new FetchLogSimple(launchProcess.getInputStream()),
					//new FetchLogSimple(launchProcess.getErrorStream())});
		}
		return gfConsole;
	}
	
	public static void removeServerLogFileConsole(GlassFishServer server) {
		String consoleID = createServerLogConsoleName(server);
		IGlassFishConsole gfConsole = findConsole(consoleID);
		if (gfConsole != null) {
			manager.removeConsoles(new IConsole[] {gfConsole});
		}
	}
	
	private static String createServerLogConsoleName(GlassFishServer server) {
		return server.isRemote() ? server.getServer().getName()
				: server.getDomainsFolder()
				+ File.separator + server.getDomainName() + File.separator
				+ "logs" + File.separator + "server.log";
	}
	
	private static String createStartupProcessConsoleName(GlassFishServer server) {
		return server.getServer().getName() + " startup process";
	}
	
	private static String createStandardConsoleName(GlassFishServer server) {
		return server.getServer().getName();
	}

	private static IGlassFishConsole findConsole(String name) {
		IConsole[] existing = manager.getConsoles();
		IGlassFishConsole myConsole = null;

		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				myConsole = (IGlassFishConsole) existing[i];
				return myConsole;
			}
		}
		return null;
	}

}
