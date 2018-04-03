/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.log;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.payara.tools.PayaraToolsPlugin;
import org.eclipse.payara.tools.sdk.server.FetchLog;

// TODO will be GlassfishLocalConsole in the future, new GlassfishRemoteConsole to be implemented
public class PayaraConsole extends AbstractPayaraConsole implements IPayaraConsole {

    private static ScheduledExecutorService stopService = Executors.newSingleThreadScheduledExecutor();

    PayaraConsole(String name, ILogFilter filter) {
        super(name,
                PayaraToolsPlugin.getInstance().getImageRegistry().getDescriptor(PayaraToolsPlugin.GF_SERVER_IMG),
                filter);
    }

    // PayaraConsole(String name, FetchLog[] logFetchers) {
    // this(name);
    // }
    //
    // PayaraConsole(String name, FetchLog logFetcher) {
    // this(name, new FetchLog[] {logFetcher});
    // }

    @Override
    public void startLogging() {
        // will work after we make fetchlog class runnable more than once
        // stopLogging();
        // readers = new ArrayList<LogReader>(logFetchers.length);
        // out = newMessageStream();
        // for (FetchLog logFetcher : logFetchers) {
        // LogReader reader = new LogReader(logFetcher, out);
        // readers.add(reader);
        // Thread t = new Thread(reader);
        // t.start();
        // }
    }

    @Override
    public synchronized void startLogging(FetchLog... logFetchers) {
        if (stopJobResult != null) {
            try {
                stopJobResult.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            stopJobResult = null;
        } else {
            System.out.println("stopJobResult is null in console " + getName());
            stopLoggingImpl();
        }
        readers = new ArrayList<>(logFetchers.length);
        latch = new CountDownLatch(logFetchers.length);
        filter.reset();
        int i = 0;
        for (FetchLog logFetcher : logFetchers) {
            LogReader reader = new LogReader(logFetcher, out, latch, filter);
            readers.add(reader);
            Thread t = new Thread(reader, "LogReader Thread" + i++);
            t.start();
        }
    }

    @Override
    public synchronized void stopLogging() {
        if (isLogging()) {
            stopLoggingImpl();
        }
    }

    private void stopLoggingImpl() {
        if (readers == null) {
            return;
        }
        for (LogReader r : readers) {
            r.stop();
        }
        readers = null;
    }

    @Override
    public synchronized void stopLogging(int afterSeconds) {
        if (isLogging()) {
            stopJobResult = stopService.schedule(() -> stopLoggingImpl(), afterSeconds, SECONDS);
        }
    }

    @Override
    protected void dispose() {
        super.dispose();
        stopLogging();
        try {
            if (latch != null) {
                latch.await();
            }
            out.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized boolean isLogging() {
        boolean isLogging = (readers != null) && (readers.size() > 0) && (stopJobResult == null);
        return isLogging;
    }

    @Override
    public synchronized void setLogFilter(ILogFilter filter) {
        this.filter = filter;
    }

}
