/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.state;

import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.TaskEvent;
import org.eclipse.payara.tools.sdk.TaskState;
import org.eclipse.payara.tools.sdk.TaskStateListener;
import org.eclipse.payara.tools.sdk.admin.AdminFactory;
import org.eclipse.payara.tools.sdk.admin.Command;
import org.eclipse.payara.tools.sdk.admin.Result;
import org.eclipse.payara.tools.sdk.admin.Runner;
import org.eclipse.payara.tools.sdk.data.GlassFishStatusCheck;
import org.eclipse.payara.tools.sdk.logging.Logger;

/**
 * Individual server administrator command task to verify if server
 * is responding properly.
 * <p/>
 * @author Tomas Kraus
 */
class RunnerTask extends AbstractTask {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Command execution listener.
     * <p/>
     * Passes {@link TaskStateListener} arguments to job command task listeners
     * registered for {@link RunnerTask} class.
     */
    private class RunnerListener implements TaskStateListener {

        /** Server administrator command task. */
        final RunnerTask runnerTask;

        /**
         * Constructs an instance of {@link Runner} listener.
         */
        private RunnerListener(final RunnerTask runnerTask) {
            this.runnerTask = runnerTask;
        }

        /**
         * Get notification about state change in {@link Runner} task.
         * <p/>
         * This is being called in {@link Runner#call()} method execution
         * context. 
         * <p/>
         * <code>String</codce> arguments passed to state listener:<ul>
         *   <li><code>args[0]</code> server name</li>
         *   <li><code>args[1]</code> administration command</li>
         *   <li><code>args[2]</code> exception message</li>
         *   <li><code>args[3]</code> display message in GUI</li></ul>
         * <p/>
         * @param newState New command execution state.
         * @param event    Event related to execution state change.
         * @param args     Additional String arguments.
         */
        @Override
        public void operationStateChanged(final TaskState newState,
        final TaskEvent event, final String... args) {
            runnerTask.handleStateChange(newState, event, args);
        }
        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerTask.class);
    
    /** Server administration command to be executed. */
    private final Command cmd;

    /** Runner task execution result. */
    Result result;

    /**
     * Constructs an instance of individual server administrator command task.
     * <p/>
     * @param job  Server status check job internal data.
     * @param task Individual status check task data.
     * @param type Server status check type.
     */
    RunnerTask(final StatusJob job, final StatusJob.RunnerTask task,
            final GlassFishStatusCheck type) {
        super(job, task, type);
        this.cmd = task.getCommand();
        this.result = null;
    }

    ////////////////////////////////////////////////////////////////////////
    // Runnable run() method                                              //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Thread main method to be executed.
     * <p/>
     * Runs command runner without starting new thread.
     */
    @Override
    public void run() {
        final String METHOD = "run";
        if (cancelled) {
            LOGGER.log(Level.FINER, METHOD, "cancelled");
            throw new IllegalStateException(LOGGER.excMsg(METHOD, "cancelled"));
        }
        LOGGER.log(Level.FINER, METHOD, "started", new String[] {
            job.getStatus().getServer().getName(), jobState.toString()});
        TaskStateListener[] listeners = task.getListeners();
        AdminFactory af = AdminFactory.getInstance(
                job.getStatus().getServer().getAdminInterface());
        Runner runner = af.getRunner(job.getStatus().getServer(), cmd);
        if (listeners != null) {
            for (int i = 0 ; i < listeners.length ; i++) {
                if (listeners[i] instanceof StatusJob.Listener) {
                    ((StatusJob.Listener)listeners[i]).setRunner(runner);
                }
            }
        }
        runner.setStateListeners(
                new TaskStateListener[] {new RunnerListener(this)});
        runner.setReadyState();
        result = runner.call();
        if (listeners != null) {
            for (int i = 0 ; i < listeners.length ; i++) {
                if (listeners[i] instanceof StatusJob.Listener) {
                    ((StatusJob.Listener)listeners[i]).clearRunner();
                }
            }
        }
    }

}
