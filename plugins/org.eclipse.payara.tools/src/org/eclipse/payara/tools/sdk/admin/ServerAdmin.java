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

package org.eclipse.payara.tools.sdk.admin;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.payara.tools.sdk.TaskState.COMPLETED;

import java.net.Authenticator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.payara.tools.sdk.TaskStateListener;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Payara Administration Command API.
 * <p>
 * Payara command facade allows remote and local server handling.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ServerAdmin {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set global password authenticator for Payara servers.
     * <p/>
     * This method must be called before first usage of <code>Runner.call()</code> method.
     * <p/>
     *
     * @param authenticator External authenticator for Payara servers to be supplied.
     */
    public static void init(Authenticator authenticator) {
        Runner.init(authenticator);
    }

    /**
     * Get individual executor pool for remote administration command execution.
     * <p/>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     *
     * @param size Thread pool size (how many tasks to execute in parallel)..
     * @return Individual <code>Executor</code> instance.
     */
    public static ExecutorService executor(final int size) {
        return Runner.parallelExecutor(size);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal executor.
     * <p>
     *
     * @param payaraServer Target GlassFish server.
     * @param command Server administration command to me executed.
     */
    public static <E extends Result<?>> Future<E> exec(PayaraServer payaraServer, Command command) {
        return (Future<E>) AdminFactory.getInstance(payaraServer.getAdminInterface())
                .getRunner(payaraServer, command)
                .execute();
    }

    public static CommandBuilder executeOn(PayaraServer server) {
        return new CommandBuilder(server);
    }

    public static class CommandBuilder {

        private final PayaraServer server;
        
        private Command cmd;
        private long timeout = 300;
        private TimeUnit timeUnit = SECONDS;
        private Consumer<ResultString> onNotCompleted;

        public CommandBuilder(PayaraServer server) {
            this.server = server;
        }

        public CommandBuilder command(Command cmd) {
            this.cmd = cmd;
            return this;
        }

        public CommandBuilder timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public CommandBuilder timeOut(long timeout, TimeUnit timeUnit) {
            this.timeout = timeout;
            this.timeUnit = timeUnit;
            return this;
        }

        public CommandBuilder onNotCompleted(Consumer<ResultString> onNotCompleted) {
            this.onNotCompleted = onNotCompleted;
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> T get() throws InterruptedException, ExecutionException, TimeoutException {
            Future<Result<?>> future = ServerAdmin.exec(server, cmd);
            Result<?> result = future.get(timeout, timeUnit);

            if (onNotCompleted != null && result instanceof ResultString) {

                ResultString outcome = (ResultString) result;

                if (!COMPLETED.equals(outcome.getState())) {
                    onNotCompleted.accept(outcome);
                }
            }

            return (T) result;
        }

    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * Execution of administration command is serialized using internal executor.
     * <p>
     *
     * @param payaraServer Target GlassFish server.
     * @param cmd Server administration command to me executed.
     * @param listeners Listeners that are called when command execution status changes.
     */
    public static <E extends Result> Future<E> exec(PayaraServer payaraServer, Command cmd, TaskStateListener... listeners) {
        Runner runner = AdminFactory.getInstance(payaraServer.getAdminInterface()).getRunner(payaraServer, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute();
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     * This allows to execute tasks in parallel using provided executor.
     * <p/>
     *
     * @param executor Executor service used to start task.
     * @param payaraServer Target GlassFish server.
     * @param cmd Server administration command to me executed.
     */
    public static <E extends Result> Future<E> exec(ExecutorService executor, PayaraServer payaraServer, Command cmd) {
        Runner runner = AdminFactory.getInstance(payaraServer.getAdminInterface()).getRunner(payaraServer, cmd);
        return (Future<E>) runner.execute(executor);
    }

    /**
     * Execute remote administration command on GlassFish server.
     * <p>
     *
     * @param executor Executor service used to start task.
     * @param srv Target GlassFish server.
     * @param cmd Server administration command to me executed.
     * @param listeners Listeners that are called when command execution status changes.
     */
    public static <E extends Result> Future<E> exec(ExecutorService executor, PayaraServer srv, Command cmd, TaskStateListener... listeners) {
        Runner runner = AdminFactory.getInstance(srv.getAdminInterface()).getRunner(srv, cmd);
        runner.stateListeners = listeners;
        return (Future<E>) runner.execute(executor);
    }

}
