/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.TaskState;
import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.sdk.utils.ServerUtils;
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * Fetch GlassFish log from local server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogLocal extends FetchLogPiped {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(FetchLogLocal.class);

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish local server log fetcher.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method which initializes
     * <code>InputStream</code> as <code>FileInputStream</code> before this constructor code is being
     * executed.
     * <p/>
     *
     * @param server GlassFish server for fetching local server log. Both <code>getDomainsFolder</code>
     * and <code>getDomainName</code> should not return null.
     * @param skip Skip to the end of the log file.
     */
    FetchLogLocal(final GlassFishServer server, final boolean skip) {
        super(server, skip);
    }

    /**
     * Constructs an instance of GlassFish local server log fetcher with external
     * {@link ExecutorService}.
     * <p/>
     * Super class constructor will call <code>initInputStream</code> method which initializes
     * <code>InputStream</code> as <code>FileInputStream</code> before this constructor code is being
     * executed.
     * <p/>
     *
     * @param executor Executor service used to start task.
     * @param server GlassFish server for fetching local server log. Both <code>getDomainsFolder</code>
     * and <code>getDomainName</code> should not return null.
     * @param skip Skip to the end of the log file.
     */
    FetchLogLocal(final ExecutorService executor, final GlassFishServer server,
            final boolean skip) {
        super(executor, server, skip);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initializes active log file <code>InputStream</code> as <code>FileInputStream</code> sending data
     * from local server log file.
     * <p/>
     *
     * @return <code>FileInputStream</code> where log lines from server active log file will be
     * available to read.
     */
    private InputStream initInputFile() {
        final String METHOD = "initInputFile";
        File logFile = ServerUtils.getServerLogFile(server);
        InputStream log;
        try {
            log = new FileInputStream(logFile);
        } catch (FileNotFoundException fnfe) {
            LOGGER.log(Level.INFO, METHOD,
                    "fileNotFound", logFile.getAbsolutePath());
            return null;
        }
        if (skip) {
            int count;
            try {
                while ((count = log.available()) > 0) {
                    log.skip(count);
                }
            } catch (IOException ioe) {
                try {
                    log.close();
                } catch (IOException ioec) {
                    LOGGER.log(Level.INFO, METHOD, "cantClose", ioec);
                }
                throw new FetchLogException(
                        LOGGER.excMsg(METHOD, "cantInit"), ioe);
            }
        }
        return log;
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
        InputStream fIn = initInputFile();
        byte[] buff = new byte[PIPE_BUFFER_SIZE];
        File logFile = ServerUtils.getServerLogFile(server);
        int inCount;
        long lastModified;
        if (fIn == null) {
            return notifyListeners(TaskState.FAILED);
        }
        while (taksExecute) {
            try {
                inCount = fIn.available();
                lastModified = logFile.lastModified();
                // Nothing to read. Check log rotation after delay.
                if (inCount <= 0) {
                    Thread.sleep(LOG_REFRESH_DELAY);
                    inCount = fIn.available();
                    if (inCount <= 0 && logFile.lastModified() > lastModified) {
                        LOGGER.log(Level.FINER, METHOD, "rotation");
                        fIn.close();
                        out.flush();

                        fIn = initInputFile();
                    }
                }
                if (inCount > 0) {
                    while (inCount > 0) {
                        int count = fIn.read(buff);
                        LOGGER.log(Level.FINEST, METHOD, "read",
                                new Object[] { new Integer(count) });
                        if (count > 0) {
                            out.write(buff, 0, count);
                            inCount -= count;
                        } else {
                            // Return -1: If log file is rotated, the original file handle is no longer valid
                            break;
                        }
                        if (inCount <= 0) {
                            inCount = fIn.available();
                        }
                    }
                    out.flush();
                }
            } catch (InterruptedException ie) {
                LOGGER.log(Level.INFO, METHOD, "interrupted", ie.getMessage());
                Thread.currentThread().interrupt();
                return notifyListeners(TaskState.COMPLETED);
            } catch (InterruptedIOException ie) {
                LOGGER.log(Level.INFO, METHOD,
                        "interruptedIO", ie.getMessage());
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

        }
        return notifyListeners(TaskState.COMPLETED);
    }

}
