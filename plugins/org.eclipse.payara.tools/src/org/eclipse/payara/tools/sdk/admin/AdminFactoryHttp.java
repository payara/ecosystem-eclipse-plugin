/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Payara Server HTTP Command Factory.
 * <p>
 * Selects correct Payara server administration functionality using HTTP command interface.
 * <p>
 * Factory is implemented as singleton.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class AdminFactoryHttp extends AdminFactory {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Singleton object instance. */
    private static volatile AdminFactoryHttp instance;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return existing singleton instance of this class or create a new one when no instance exists.
     * <p>
     *
     * @return <code>AdminFactoryHttp</code> singleton instance.
     */
    static AdminFactoryHttp getInstance() {
        if (instance != null) {
            return instance;
        }
        
        synchronized (AdminFactoryHttp.class) {
            if (instance == null) {
                instance = new AdminFactoryHttp();
            }
        }
        
        return instance;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build runner for HTTP command interface execution and connect it with provided
     * <code>Command</code> instance.
     * <p>
     *
     * @param server GlassFish server entity object.
     * @param cmd GlassFish server administration command entity.
     * @return GlassFish server administration command execution object.
     */
    @Override
    public Runner getRunner(PayaraServer server, Command cmd) {
        Runner runner;
        
        Class<? extends Command> commandClass = cmd.getClass();
        RunnerHttpClass runnerHttpClass = (RunnerHttpClass) commandClass.getAnnotation(RunnerHttpClass.class);
        
        if (runnerHttpClass != null) {
            String command = runnerHttpClass.command();
            runner = newRunner(server, cmd, runnerHttpClass.runner());
            if (command != null && !command.isEmpty()) {
                cmd.command = command;
            }
        } else {
            runner = new RunnerHttp(server, cmd);
        }
        
        return runner;
    }

}
