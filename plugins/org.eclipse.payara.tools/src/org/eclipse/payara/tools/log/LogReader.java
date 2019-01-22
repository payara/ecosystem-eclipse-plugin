/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import org.eclipse.payara.tools.sdk.server.FetchLog;
import org.eclipse.ui.console.MessageConsoleStream;

public class LogReader implements Runnable {

    private FetchLog logFetcher;
    private MessageConsoleStream output;
    private CountDownLatch latch;
    private ILogFilter filter;
    
    private boolean hasLogged;
    private boolean hasProcessedPayara;

    LogReader(FetchLog logFetcher, MessageConsoleStream outputStream, CountDownLatch latch, ILogFilter filter) {
        this.logFetcher = logFetcher;
        this.output = outputStream;
        this.latch = latch;
        this.filter = filter;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(logFetcher.getInputStream(), StandardCharsets.UTF_8));

            for (String line = null; (line = reader.readLine()) != null;) {
                line = filter.process(line);
                if (line != null) {
                    hasLogged = true;
                    if (!hasProcessedPayara) {
                        hasProcessedPayara = filter.hasProcessedPayara();
                    }
                    output.println(line);
                }
            }
            output.flush();
        } catch (IOException e) {
            // this happens when input stream is closed, no need to print
            // e.printStackTrace();
        } finally {
            logFetcher.close();
            latch.countDown();
        }
    }
    
    public synchronized boolean hasLogged() {
        return hasLogged;
    }
    
    public synchronized boolean hasProcessedPayara() {
        return hasProcessedPayara;
    }

    public synchronized void stop() {
        logFetcher.close();
    }

}
