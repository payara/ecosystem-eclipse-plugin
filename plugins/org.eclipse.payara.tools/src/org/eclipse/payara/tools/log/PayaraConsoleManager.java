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

package org.eclipse.payara.tools.log;

import static java.io.File.separator;
import static org.eclipse.payara.tools.log.AbstractLogFilter.createFilter;

import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

/**
 * This factory class enforces certain rules regarding Payara consoles.
 * 
 * <ol>
 *     <li>There is only one standard Payara console.</li>
 *     <li>A user can trigger showing the server log file console that shows the whole server.log file. </li>
 *     <li>A startup process console exists during the startup process of Payara. Unless the startup
 *         does not fail it will not be shown to the user.</li>
 * </ol>
 *
 * @author Peter Benedikovic
 *
 */
public class PayaraConsoleManager {

    private static IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();

    public static IPayaraConsole showConsole(IPayaraConsole console) {
        manager.addConsoles(new IConsole[] { console });
        manager.showConsoleView(console);
        return console;
    }

    /**
     * Returns standard console for specified server. For each server there is only one console. It
     * reads information from server.log file but only newly added lines.
     *
     * @param server
     * @return
     */
    public static IPayaraConsole getStandardConsole(PayaraServer server) {
        String consoleID = createStandardConsoleName(server);
        IPayaraConsole gfConsole = findConsole(consoleID);
        if (gfConsole == null) {
            gfConsole = new PayaraConsole(consoleID, AbstractLogFilter.createFilter(server));
        }

        return gfConsole;
    }

    /**
     * Returns console for showing contents of the whole server.log file. For the same server.log file
     * there is only one console at the time.
     *
     * @param server
     * @return
     */
    public static IPayaraConsole getServerLogFileConsole(PayaraServer server) {
        String consoleID = createServerLogConsoleName(server);
        IPayaraConsole gfConsole = findConsole(consoleID);
        if (gfConsole == null) {
            gfConsole = new PayaraConsole(consoleID, createFilter(server));
        }

        return gfConsole;
    }

    /**
     * Creates new startup process console. There should be only one for a particular Payara server.
     *
     * @param server
     * @return
     */
    public static IPayaraConsole getStartupProcessConsole(PayaraServer server, Process launchProcess) {
        String consoleID = createStartupProcessConsoleName(server);
        IPayaraConsole payaraConsole = findConsole(consoleID);
        if (payaraConsole == null) {
            payaraConsole = new PayaraStartupConsole(consoleID, new NoOpFilter());
        }

        return payaraConsole;
    }

    public static void removeServerLogFileConsole(PayaraServer server) {
        String consoleID = createServerLogConsoleName(server);
        IPayaraConsole payaraConsole = findConsole(consoleID);
        if (payaraConsole != null) {
            manager.removeConsoles(new IConsole[] { payaraConsole });
        }
    }

    private static String createServerLogConsoleName(PayaraServer server) {
        return server.isRemote() ? server.getServer().getName()
                : server.getDomainsFolder() + separator + server.getDomainName() + separator + "logs"
                        + separator + "server.log";
    }

    private static String createStartupProcessConsoleName(PayaraServer server) {
        return server.getServer().getName() + " startup process";
    }

    private static String createStandardConsoleName(PayaraServer server) {
        return server.getServer().getName();
    }

    private static IPayaraConsole findConsole(String name) {
        IConsole[] existing = manager.getConsoles();

        for (IConsole element : existing) {
            if (name.equals(element.getName())) {
                return (IPayaraConsole) element;
            }
        }

        return null;
    }

}
