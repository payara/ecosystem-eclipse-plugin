/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import static java.util.logging.Level.WARNING;
import static org.eclipse.payara.tools.sdk.admin.CommandException.RUNNER_INIT;
import static org.eclipse.payara.tools.sdk.admin.CommandException.UNKNOWN_ADMIN_INTERFACE;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.payara.tools.sdk.data.GlassFishAdminInterface;
import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.sapphire.Version;

/**
 * GlassFish Abstract Server Command Factory.
 * <p/>
 * Selects correct GlassFish server administration functionality depending on given GlassFish server
 * entity object.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class AdminFactory {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(AdminFactory.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates specific <code>AdminFactory</code> child class instance to build GlassFish server
     * administration command runner and data objects based on provided GlassFish server version.
     * <p>
     *
     * @param version GlassFish server version.
     * @return Child factory class instance to work with given GlassFish server.
     */
    static AdminFactory getInstance(Version version) throws CommandException {
        // Use REST interface for GlassFish 3 and 4.
        return AdminFactoryRest.getInstance();
    }

    /**
     * Creates specific <code>AdminFactory</code> child class instance to build GlassFish server
     * administration command runner and data objects based on provided GlassFish server administration
     * interface type.
     * <p/>
     *
     * @param adminInterface GlassFish server administration interface type.
     * @return Child factory class instance to work with given GlassFish server.
     */
    public static AdminFactory getInstance(GlassFishAdminInterface adminInterface) throws CommandException {
        switch (adminInterface) {
        case REST:
            return AdminFactoryRest.getInstance();
        case HTTP:
            return AdminFactoryHttp.getInstance();
        // Anything else is unknown.
        default:
            throw new CommandException(UNKNOWN_ADMIN_INTERFACE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build runner for command interface execution and connect it with provided <code>Command</code>
     * instance.
     * <p/>
     *
     * @param payaraServer Target GlassFish server.
     * @param command GlassFish server administration command entity.
     * @return GlassFish server administration command execution object.
     */
    public abstract Runner getRunner(PayaraServer payaraServer, Command command);

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of selected <code>Runner</code> child class.
     * <p/>
     *
     * @param payaraServer Target GlassFish server.
     * @param command GlassFish server administration command entity.
     * @param runnerClass Class of newly instantiated <code>runner</code>
     * @return GlassFish server administration command execution object.
     * @throws <code>CommandException</code> if construction of new instance fails.
     */
    Runner newRunner(PayaraServer payaraServer, Command command, Class<? extends Runner> runnerClass) throws CommandException {
        
        Constructor<? extends Runner> runnerConstructor = null;
        Runner runner = null;
        try {
            runnerConstructor = runnerClass.getConstructor(PayaraServer.class, Command.class);
        } catch (NoSuchMethodException | SecurityException nsme) {
            throw new CommandException(RUNNER_INIT, nsme);
        }
        
        if (runnerConstructor == null) {
            return runner;
        }
        
        try {
            runner = runnerConstructor.newInstance(payaraServer, command);
        } catch (InstantiationException | IllegalAccessException ie) {
            throw new CommandException(RUNNER_INIT, ie);
        } catch (InvocationTargetException ite) {
            LOGGER.log(WARNING, "exceptionMsg", ite.getMessage());
            Throwable t = ite.getCause();
            if (t != null) {
                LOGGER.log(WARNING, "cause", t.getMessage());
            }
            
            throw new CommandException(RUNNER_INIT, ite);
        }
        
        return runner;
    }

}
