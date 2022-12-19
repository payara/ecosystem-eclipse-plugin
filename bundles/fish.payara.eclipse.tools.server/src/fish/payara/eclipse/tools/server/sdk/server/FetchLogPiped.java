/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.sdk.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.TaskState;
import fish.payara.eclipse.tools.server.sdk.logging.Logger;
import fish.payara.eclipse.tools.server.sdk.utils.LinkedList;
import fish.payara.eclipse.tools.server.sdk.utils.NetUtils;

/**
 * Fetch GlassFish log from local or remote server.
 * <p/>
 * Data are fetched in service thread and passed into <code>PipedOutputStream</code>.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class FetchLogPiped
        extends FetchLog implements Callable<TaskState> {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(FetchLogPiped.class);

    /** Size of internal buffer in pipe input stream. */
    static final int PIPE_BUFFER_SIZE = 8192;

    /** Log refresh delay in miliseconds. */
    static final int LOG_REFRESH_DELAY = 1000;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server log fetcher depending on server being remote or local.
     * <p/>
     * Decision if server is local or remote depends on domains folder and domain name attributes stored
     * in <code>PayaraServer</code> object.
     * <p/>
     *
     * @param server GlassFish server for fetching server log.
     * @param skip Skip to the end of the log file.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final PayaraServer server,
            final boolean skip) {
        boolean isLocal = NetUtils.isLocahost(server.getHost());
        FetchLogPiped fetchLog = isLocal
                ? new FetchLogLocal(server, skip)
                : new FetchLogRemote(server, skip);
        fetchLog.start();
        return fetchLog;
    }

    /**
     * Constructs an instance of GlassFish server log fetcher depending on server being remote or local.
     * <p/>
     * Decision if server is local or remote depends on domains folder and domain name attributes stored
     * in <code>PayaraServer</code> object. Log file is passed whole as is without skipping to the
     * end.
     * <p/>
     *
     * @param server GlassFish server for fetching server log.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final PayaraServer server) {
        return create(server, false);
    }

    /**
     * Constructs an instance of GlassFish server log fetcher depending on server being remote or local
     * with external {@link ExecutorService}.
     * <p/>
     * Decision if server is local or remote depends on domains folder and domain name attributes stored
     * in <code>PayaraServer</code> object.
     * <p/>
     *
     * @param executor Executor service used to start task.
     * @param server GlassFish server for fetching server log.
     * @param skip Skip to the end of the log file.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final ExecutorService executor,
            final PayaraServer server, final boolean skip) {
        boolean isLocal = NetUtils.isLocahost(server.getHost());
        FetchLogPiped fetchLog = isLocal
                ? new FetchLogLocal(executor, server, skip)
                : new FetchLogRemote(executor, server, skip);
        fetchLog.start();
        return fetchLog;
    }

    /**
     * Constructs an instance of GlassFish server log fetcher depending on server being remote or local
     * with external {@link ExecutorService}.
     * <p/>
     * Decision if server is local or remote depends on domains folder and domain name attributes stored
     * in <code>PayaraServer</code> object. Log file is passed whole as is without skipping to the
     * end.
     * <p/>
     *
     * @param executor Executor service used to start task.
     * @param server GlassFish server for fetching server log.
     * @return Newly created <code>FetchLog</code> instance.
     */
    public static FetchLogPiped create(final ExecutorService executor,
            final PayaraServer server) {
        return create(executor, server, false);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Output stream where to write retrieved remote server log. */
    final PipedOutputStream out;

    /** Running task that reads log lines from remote server. */
    Future<TaskState> task;

    /** <code>ExecutorService</code> used to run read remote server log tasks. */
    private ExecutorService executor;

    /** Internal <code>ExecutorService</code> was used. */
    private final boolean internalExecutor;

    /** Indicate whether log lines reading task should continue or exit. */
    volatile boolean taksExecute;

    /** Listeners for state change events in GlassFish log fetcher. */
    private final LinkedList<FetchLogEventListener> eventListeners;

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
    FetchLogPiped(final PayaraServer server, boolean skip) {
        super(server, skip);
        final String METHOD = "init";
        this.eventListeners = new LinkedList();
        try {
            out = new PipedOutputStream((PipedInputStream) this.in);
        } catch (IOException ioe) {
            super.close();
            throw new FetchLogException(LOGGER.excMsg(METHOD, "cantInit"), ioe);
        }
        taksExecute = true;
        // Create internal executor to run log reader task.
        executor = new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), (ThreadFactory) r -> {
                    Thread t = new Thread(r, FetchLogPiped.class.getName()
                            + server.getUrl() != null ? " (Localhost)" : server.getUrl());
                    t.setDaemon(true);
                    return t;
                });
        internalExecutor = true;
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
    @SuppressWarnings("LeakingThisInConstructor")
    FetchLogPiped(final ExecutorService executor, final PayaraServer server,
            boolean skip) {
        super(server, skip);
        final String METHOD = "init";
        this.eventListeners = new LinkedList();
        try {
            out = new PipedOutputStream((PipedInputStream) this.in);
        } catch (IOException ioe) {
            super.close();
            throw new FetchLogException(LOGGER.excMsg(METHOD, "cantInit"), ioe);
        }
        taksExecute = true;
        // Use external executor to run log reader task.
        this.executor = executor;
        internalExecutor = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor callback which initializes log <code>InputStream</code> as
     * <code>PipedInputStream</code> sending data from remote server log reader.
     * <p/>
     * This initialization is called form <code>FetchLog</code> super class constructor. It already
     * exists when <code>FetchLogRemote</code> constructor is running so it may be used as argument for
     * local <code>PipedOutputStream</code> initialization.
     * <p/>
     *
     * @return <code>PipedInputStream</code> where log lines received from server will be available to
     * read.
     */
    @Override
    InputStream initInputStream() {
        return new PipedInputStream(PIPE_BUFFER_SIZE);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add GlassFish log fetcher state change listener at the end of listeners list.
     * <p/>
     *
     * @param listener Listener for state change events in GlassFish log fetcher to be added. Value
     * shall not be <code>null</code>.
     * @throws FetchLogException When <code>listener</code> parameter is <code>null</code>.
     */
    public final void addListener(final FetchLogEventListener listener)
            throws FetchLogException {
        final String METHOD = "addListener";
        if (listener == null) {
            throw new FetchLogException(LOGGER.excMsg(METHOD, "listenerNull"));
        }
        synchronized (eventListeners) {
            eventListeners.addLast(listener);
        }
    }

    /**
     * Remove all occurrences of log fetcher state change listener from listeners list.
     * <p/>
     *
     * @param listener Listener for state change events in GlassFish log fetcher to be removed. Value
     * shall not be <code>null</code>.
     * @return Value of <code>true</code> when at least one listener was removed or <code>false</code>
     * otherwise.
     * @throws FetchLogException When <code>listener</code> parameter is <code>null</code>.
     */
    public final boolean removeListener(final FetchLogEventListener listener)
            throws FetchLogException {
        final String METHOD = "removeListener";
        if (listener == null) {
            throw new FetchLogException(LOGGER.excMsg(METHOD, "listenerNull"));
        }
        boolean removed = false;
        synchronized (eventListeners) {
            boolean isElement = !eventListeners.isEmpty();
            eventListeners.first();
            while (isElement) {
                if (listener.equals(eventListeners.getCurrent())) {
                    isElement = eventListeners.isNext();
                    eventListeners.removeAndNextOrPrevious();
                    removed = true;
                } else {
                    isElement = eventListeners.next();
                }
            }
        }
        return removed;
    }

    /**
     * Notify all GlassFish log fetcher state change listeners about state change event.
     * <p/>
     *
     * @param state Current GlassFish log fetcher state.
     * @return Current GlassFish log fetcher state.
     */
    final TaskState notifyListeners(final TaskState state) {
        if (!eventListeners.isEmpty()) {
            synchronized (eventListeners) {
                boolean isElement = !eventListeners.isEmpty();
                if (isElement) {
                    FetchLogEvent event = new FetchLogEvent(state);
                    eventListeners.first();
                    while (isElement) {
                        eventListeners.getCurrent().stateChanged(event);
                        isElement = eventListeners.next();
                    }
                }
            }
        }
        return state;
    }

    /**
     * Start task.
     */
    private void start() {
        task = executor.submit(this);
        notifyListeners(TaskState.READY);
    }

    /**
     * Stop running task if it's still running.
     * <p/>
     *
     * @return Task execution result.
     */
    private TaskState stop() {
        final String METHOD = "stop";
        taksExecute = false;
        if (this.out != null) {
            try {
                this.out.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, METHOD, "cantClose", ioe);
            }
        } else {
            LOGGER.log(Level.INFO, METHOD, "isNull");
        }
        TaskState result;
        try {
            result = task.get();
        } catch (InterruptedException ie) {
            throw new FetchLogException(
                    LOGGER.excMsg(METHOD, "interrupted"), ie);
        } catch (ExecutionException ee) {
            throw new FetchLogException(
                    LOGGER.excMsg(METHOD, "exception"), ee);
        } catch (CancellationException ce) {
            throw new FetchLogException(
                    LOGGER.excMsg(METHOD, "cancelled"), ce);
        }
        return result;
    }

    /**
     * Stop log lines reading task and close input and output streams used to access log lines received
     * from server.
     */
    @Override
    public void close() {
        final String METHOD = "close";
        TaskState result = stop();
        super.close();
        // Clean up internal executor.
        if (internalExecutor) {
            executor.shutdownNow();
        }
        // We may possibly change this to throw an exception when needed.
        // But streams must be cleaned up first.
        if (result != TaskState.COMPLETED) {
            LOGGER.log(Level.INFO, METHOD, "failed");
        }
    }

    /**
     * Check if log lines reading task is running.
     * <p/>
     *
     * @return Returns <code>true</code> when task is still running or <code>false></code> otherwise.
     */
    public boolean isRunning() {
        return !task.isDone();
    }
}
