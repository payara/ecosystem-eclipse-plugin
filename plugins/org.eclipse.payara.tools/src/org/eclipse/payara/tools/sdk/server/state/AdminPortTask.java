/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.state;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.TaskEvent;
import org.eclipse.payara.tools.sdk.TaskState;
import org.eclipse.payara.tools.sdk.data.GlassFishStatusCheck;
import org.eclipse.payara.tools.sdk.data.GlassFishStatusCheckResult;
import org.eclipse.payara.tools.sdk.logging.Logger;

/**
 * Individual server status check task to verify if server administration port is alive.
 * <p/>
 *
 * @author Tomas Kraus
 */
class AdminPortTask extends AbstractTask {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(AdminPortTask.class);

    /** Log message identifier suffix when using message with timestamp. */
    private final String TM_SUFFIX = "Tm";

    /** Task name for logging purposes. */
    private final String TASK_NAME = "port-check";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Format time value in miliseconds to be printed as value in seconds and miliseconds
     * <code>s.ms<code>.
     * <p/>
     *
     * @param tm Time value in miliseconds
     * @return Time string formated as econds and miliseconds <code>s.ms<code>.
     */
    static String tm(final long tm) {
        StringBuilder sb = new StringBuilder(8);
        sb.append(Long.toString(tm / 1000));
        sb.append('.');
        sb.append(Long.toString(tm % 1000));
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Socked connecting timeout [ms]. */
    int timeout;

    /** Server administration port status check result. */
    private StatusResult result;

    /**
     * Task start time. Used for logging purposes. Value of <code>-1</code> means that start time was
     * not set.
     */
    private long tmStart;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of administration port status check.
     * <p/>
     *
     * @param job Server status check job internal data.
     * @param task Individual status check task data.
     * @param timeout Socked connecting timeout.
     */
    AdminPortTask(final StatusJob job, final StatusJob.Task task,
            final int timeout) {
        super(job, task, GlassFishStatusCheck.PORT);
        this.timeout = timeout;
        this.result = null;
        this.tmStart = -1;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get last command task execution result.
     * <p/>
     *
     * @return Last command task execution result.
     */
    StatusResult getResult() {
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build arguments for operationStateChanged method listening for state changes.
     * <p/>
     * Send <code>false</code> display argument to operationStateChanged method to not display GUI
     * message about this event.
     * <p/>
     *
     * @param exMessage Exception message.
     * @return Arguments for operationStateChanged method listening for state changes.
     */
    String[] stateChangeArgs(final String exMessage) {
        return new String[] {
                job.getStatus().getServer().getHost(), TASK_NAME,
                exMessage, Boolean.toString(false)
        };
    }

    /**
     * Close socket and handle <code>IOException</code> that could be thrown.
     * <p/>
     *
     * @param socket Socket to be closed.
     */
    private void closeSocket(final Socket socket) {
        final String METHOD = "closeSocket";
        try {
            socket.close();
        } catch (IOException ioe) {
            handleIOException(ioe, job.getStatus().getServer().getHost(),
                    job.getStatus().getServer().getAdminPort(),
                    METHOD, "failed");
        }
    }

    /**
     * Handle IO Exception caught in server administration port verification task.
     * <p/>
     * Set task result and call registered listeners.
     * <p/>
     *
     * @param ioe <code>IOException</code> caught.
     * @param host Server administration host.
     * @param port Server administration port.
     * @param message Message to be logged. Shall not be <code>null</code>.
     */
    private void handleIOException(final IOException ioe,
            final String host, final int port,
            final String method, final String message) {
        if (tmStart >= 0 && LOGGER.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder(
                    message.length() + TM_SUFFIX.length());
            sb.append(message).append(TM_SUFFIX);
            long tm = System.currentTimeMillis() - tmStart;
            LOGGER.log(Level.FINEST, method, sb.toString(), new Object[] {
                    tm(tm), host, Integer.toString(port), ioe.getMessage() });

        } else {
            LOGGER.log(Level.FINEST, method, message, new Object[] {
                    host, Integer.toString(port), ioe.getMessage() });
        }
        result = new StatusResult(
                GlassFishStatusCheckResult.FAILED, TaskEvent.EXCEPTION);
        handleStateChange(TaskState.FAILED, TaskEvent.EXCEPTION,
                stateChangeArgs(ioe.getLocalizedMessage()));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Runnable run() method //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Thread main method to be executed.
     * <p/>
     * Runs administration port check.
     */
    @Override
    public void run() {
        final String METHOD = "run";
        if (cancelled) {
            LOGGER.log(Level.FINER, METHOD, "cancelled");
            throw new IllegalStateException(LOGGER.excMsg(METHOD, "cancelled"));
        }
        LOGGER.log(Level.FINER, METHOD, "started", new String[] {
                job.getStatus().getServer().getName(), jobState.toString() });
        String host = job.getStatus().getServer().getHost();
        int port = job.getStatus().getServer().getAdminPort();
        if (port < 0 || host == null) {
            result = new StatusResult(GlassFishStatusCheckResult.FAILED);
            handleStateChange(TaskState.FAILED,
                    TaskEvent.CMD_FAILED, stateChangeArgs(null));
        }
        this.tmStart = System.currentTimeMillis();
        InetSocketAddress sa = new InetSocketAddress(host, port);
        Socket socket = new Socket();
        try {
            socket.connect(sa, timeout);
            socket.setSoTimeout(timeout);
        } catch (java.net.ConnectException ce) {
            handleIOException(ce, host, port, METHOD, "connect");
            return;
        } catch (java.net.SocketTimeoutException ste) {
            handleIOException(ste, host, port, METHOD, "timeout");
            return;
        } catch (IOException ioe) {
            handleIOException(ioe, host, port, METHOD, "ioException");
            return;
        } finally {
            closeSocket(socket);
        }
        if (tmStart >= 0 && LOGGER.isLoggable(Level.FINEST)) {
            long tm = System.currentTimeMillis() - tmStart;
            LOGGER.log(Level.FINEST, METHOD, "success",
                    new Object[] { tm(tm), jobState.toString(),
                            host, Integer.toString(port) });
        }
        result = new StatusResult(
                GlassFishStatusCheckResult.SUCCESS, TaskEvent.CMD_COMPLETED);
        handleStateChange(TaskState.COMPLETED, TaskEvent.CMD_COMPLETED,
                stateChangeArgs(null));
    }

}
