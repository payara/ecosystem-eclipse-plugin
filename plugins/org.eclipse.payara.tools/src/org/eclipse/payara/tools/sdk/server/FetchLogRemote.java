/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.TaskState;
import org.eclipse.payara.tools.sdk.admin.CommandFetchLogData;
import org.eclipse.payara.tools.sdk.admin.ResultLog;
import org.eclipse.payara.tools.sdk.admin.ServerAdmin;
import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.sdk.utils.OsUtils;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * Fetch GlassFish log from remote server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogRemote extends FetchLogPiped {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(FetchLogPiped.class);

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish remote server log fetcher.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method which initializes
     * <code>InputStream</code> as <code>PipedInputStream</code> before this constructor code is being
     * executed. Here we can simply connect already initialized <code>PipedInputStream</code> with newly
     * created <code>PipedInputStream</code>.
     * <p/>
     *
     * @param server GlassFish server for fetching server log.
     * @param skip Skip to the end of the log file.
     */
    FetchLogRemote(final PayaraServer server, final boolean skip) {
        super(server, skip);
    }

    /**
     * Constructs an instance of GlassFish remote server log fetcher with external
     * {@link ExecutorService}.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method which initializes
     * <code>InputStream</code> as <code>PipedInputStream</code> before this constructor code is being
     * executed. Here we can simply connect already initialized <code>PipedInputStream</code> with newly
     * created <code>PipedInputStream</code>.
     * <p/>
     *
     * @param executor Executor service used to start task.
     * @param server GlassFish server for fetching server log.
     * @param skip Skip to the end of the log file.
     */
    FetchLogRemote(final ExecutorService executor, final PayaraServer server,
            final boolean skip) {
        super(executor, server, skip);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Runnable call() Method //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Remote server log lines reading task.
     * <p/>
     * Reads new log lines from server using GlassFish remote administration API and sends them into
     * pipe (<code>PipedInputStream</code>).
     * <p/>
     *
     * @return <code>TaskState.COMPLETED</code> when remote administration API stopped responding or
     * <code>TaskState.FAILED</code> when exception was caught.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public TaskState call() {
        final String METHOD = "call";
        notifyListeners(TaskState.RUNNING);
        String paramsAppendNext = null;
        Future<ResultLog> future = ServerAdmin.<ResultLog>exec(server,
                new CommandFetchLogData());
        try {
            ResultLog result = future.get();
            if (!skip && result.getState() == TaskState.COMPLETED) {
                paramsAppendNext = result.getValue().getParamsAppendNext();
                for (String line : result.getValue().getLines()) {
                    out.write(line.getBytes());
                    out.write(OsUtils.LINES_SEPARATOR.getBytes());
                }
                out.flush();
            }
            byte[] lineSeparatorOut = OsUtils.LINES_SEPARATOR.getBytes();
            while (taksExecute && result.getState() == TaskState.COMPLETED) {
                future = ServerAdmin.<ResultLog>exec(server,
                        new CommandFetchLogData(
                                paramsAppendNext));
                result = future.get();
                if (result.getState() == TaskState.COMPLETED) {
                    paramsAppendNext = result.getValue().getParamsAppendNext();
                    for (String line : result.getValue().getLines()) {
                        byte[] lineOut = line.getBytes();
                        LOGGER.log(Level.FINEST, METHOD, "read", new Object[] {
                                new Integer(lineOut.length
                                        + lineSeparatorOut.length) });
                        out.write(lineOut);
                        out.write(lineSeparatorOut);
                    }
                    out.flush();
                }
                Thread.sleep(LOG_REFRESH_DELAY);
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.INFO, METHOD, "interrupted", ie.getMessage());
            Thread.currentThread().interrupt();
            return notifyListeners(TaskState.COMPLETED);
        } catch (ExecutionException ee) {
            LOGGER.log(Level.INFO, METHOD, "exception", ee);
            return notifyListeners(TaskState.FAILED);
        } catch (InterruptedIOException ie) {
            LOGGER.log(Level.INFO, METHOD, "interruptedIO", ie.getMessage());
            Thread.currentThread().interrupt();
            return notifyListeners(TaskState.COMPLETED);
        } catch (IOException ioe) {
            if (taksExecute) {
                LOGGER.log(Level.INFO, METHOD, "ioException", ioe);
                return notifyListeners(TaskState.FAILED);
            } else {
                LOGGER.log(Level.INFO, METHOD,
                        "ioExceptionMsg", ioe.getMessage());
                return notifyListeners(TaskState.COMPLETED);
            }
        }
        return notifyListeners(TaskState.COMPLETED);
    }

}
