/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin;

import java.net.Authenticator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.sdk.TaskStateListener;
import org.eclipse.glassfish.tools.sdk.data.IdeContext;

/**
 * GlassFish Administration Command API.
 * <p>
 * GlassFish command facade allows remote and local server handling.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ServerAdmin {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set global password authenticator for GlassFish servers.
     * <p/>
     * This method must be called before first usage
     * of <code>Runner.call()</code> method.
     * <p/>
     * @param authenticator External authenticator for GlassFish servers
     *                      to be supplied.
     */
    public static void init(final Authenticator authenticator) {
        Runner.init(authenticator);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target GlassFish server.
     * @param cmd Server administration command to me executed.
     * @param ide IDE Context object (not used).
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final GlassFishServer srv, final Command cmd,
            final IdeContext ide) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target GlassFish server.
     * @param cmd Server administration command to me executed.
     * @param ide IDE Context object (not used).
     * @param listeners Listeners that are called when command execution status changes.
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final GlassFishServer srv, final Command cmd, final IdeContext ide,
            final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute();
    }

    /**
     * Get individual executor pool for remote administration command
     * execution.
     * <p/>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     * @param size Thread pool size (how many tasks to execute in parallel)..
     * @return Individual <code>Executor</code> instance.
     */
    public static ExecutorService executor(final int size) {
        return Runner.parallelExecutor(size);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     * @param executor Executor service used to start task.
     * @param srv      Target GlassFish server.
     * @param cmd      Server administration command to me executed.
     * @param ide      IDE Context object (not used).
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final GlassFishServer srv,
            final Command cmd, final IdeContext ide) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * @param executor Executor service used to start task.
     * @param srv      Target GlassFish server.
     * @param cmd      Server administration command to me executed.
     * @param ide      IDE Context object (not used).
     * @param listeners Listeners that are called when command execution status changes.
     * @deprecated {@link IdeContext} class will be removed.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final GlassFishServer srv, 
            final Command cmd, final IdeContext ide,
            final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target GlassFish server.
     * @param cmd Server administration command to me executed.
     */
    public static <E extends Result> Future<E> exec(
            final GlassFishServer srv, final Command cmd) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal
     * executor.
     * <p>
     * @param srv Target GlassFish server.
     * @param cmd Server administration command to me executed.
     * @param listeners Listeners that are called when command execution status changes.
     */
    public static <E extends Result> Future<E> exec(
            final GlassFishServer srv, final Command cmd,
            final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     * @param executor Executor service used to start task.
     * @param srv      Target GlassFish server.
     * @param cmd      Server administration command to me executed.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final GlassFishServer srv,
            final Command cmd) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * @param executor Executor service used to start task.
     * @param srv      Target GlassFish server.
     * @param cmd      Server administration command to me executed.
     * @param listeners Listeners that are called when command execution status changes.
     */
    public static <E extends Result> Future<E> exec(
            final ExecutorService executor, final GlassFishServer srv,
            final Command cmd, final TaskStateListener... listeners) {
        AdminFactory af = AdminFactory.getInstance(srv.getAdminInterface());
        Runner runner = af.getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute(executor);
    }
    
}
  
