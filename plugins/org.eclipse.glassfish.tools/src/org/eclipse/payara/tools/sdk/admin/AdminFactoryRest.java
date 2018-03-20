/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * GlassFish Server REST Command Factory.
 * <p>
 * Selects correct GlassFish server administration functionality using REST command interface.
 * <p>
 * Factory is implemented as singleton.
 * <p>
 * 
 * @author Tomas Kraus, Peter Benedikovic
 */
public class AdminFactoryRest extends AdminFactory {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Singleton object instance. */
    private static volatile AdminFactoryRest instance;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return existing singleton instance of this class or create a new one when no instance exists.
     * <p>
     * 
     * @return <code>AdminFactoryRest</code> singleton instance.
     */
    static AdminFactoryRest getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (AdminFactoryRest.class) {
            if (instance == null) {
                instance = new AdminFactoryRest();
            }
        }
        return instance;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build runner for REST command interface execution and connect it with provided
     * <code>Command</code> instance.
     * <p>
     * 
     * @param srv GlassFish server entity object.
     * @param cmd GlassFish server administration command entity.
     * @return GlassFish server administration command execution object.
     */
    @Override
    public Runner getRunner(final GlassFishServer srv, final Command cmd) {
        Runner runner;
        Class cmcClass = cmd.getClass();
        RunnerRestClass rc = (RunnerRestClass) cmcClass.getAnnotation(
                RunnerRestClass.class);
        if (rc != null) {
            Class runnerClass = rc.runner();
            String command = rc.command();
            runner = newRunner(srv, cmd, runnerClass);
            if (command != null && command.length() > 0) {
                cmd.command = command;
            }
        } else {
            runner = new RunnerRest(srv, cmd);
        }
        return runner;
    }

}
