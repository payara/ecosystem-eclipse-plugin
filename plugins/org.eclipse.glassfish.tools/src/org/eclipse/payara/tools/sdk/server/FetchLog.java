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
import java.io.InputStream;
import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * Fetch GlassFish log from server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class FetchLog {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(FetchLog.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server for fetching server log. */
    GlassFishServer server;

    /** Input stream which will provide access to log retrieved from server. */
    final InputStream in;

    /** Request to skip to the end of log. */
    final boolean skip;

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor callback which will initialize log <code>InputStream</code>.
     * <p/>
     *
     * @return <code>InputStream</code> where log lines received from server will be available to read.
     */
    abstract InputStream initInputStream();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an empty instance of GlassFish server log fetcher using provided input stream.
     * <p/>
     * <code>InputStream</code> is set using constructor argument. Child class
     * <code>initInputStream</code> method is ignored.
     * <p/>
     *
     * @param in Input stream used to read server log.
     * @param skip Skip to the end of the log file.
     */
    FetchLog(InputStream in, boolean skip) {
        this.server = null;
        this.in = in;
        this.skip = skip;
    }

    /**
     * Constructs an instance of GlassFish server log fetcher.
     * <p/>
     * <code>InputStream</code> is set using child <code>initInputStream</code> method.
     * <p/>
     *
     * @param server GlassFish server for fetching server log.
     * @param skip Skip to the end of the log file.
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    FetchLog(GlassFishServer server, boolean skip) {
        this.server = server;
        this.in = initInputStream();
        this.skip = skip;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get input stream for reading lines from server log file.
     * <p/>
     *
     * @return Input stream for reading lines from server log file.
     */
    public InputStream getInputStream() {
        return this.in;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Close input stream used to access log lines received from server.
     * <p/>
     * This should be overridden in child classes to handle all streams and threads properly.
     */
    public void close() {
        final String METHOD = "close";
        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, METHOD, "cantClose", ioe);
            }
        } else {
            LOGGER.log(Level.INFO, METHOD, "isNull");
        }
    }

}
