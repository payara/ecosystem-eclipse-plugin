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

package org.eclipse.payara.tools.sdk;

import java.text.MessageFormat;
import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.logging.Logger;

/**
 * Common GlassFish IDE SDK Exception.
 * <p>
 * Base exception for GlassFish IDE SDK Exception contains all common code. All exceptions are
 * logging themselves on WARNING level when created.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class PayaraIdeException extends RuntimeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(PayaraIdeException.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create exception message from message pattern and arguments using
     * {@link java.text.MessageFormat}.
     * <p/>
     *
     * @param msg The detail message pattern.
     * @param arguments Arguments to be inserted into message pattern.
     */
    private static String formatMessage(String msg, Object... arguments) {
        if (arguments != null && arguments.length > 0) {
            return MessageFormat.format(msg, arguments);
        } else {
            return msg;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>GlassFishIdeException</code> without detail message.
     */
    public PayaraIdeException() {
        super();
        final String METHOD = "init";
        // Log exception in WARNING level.
        LOGGER.exception(Level.WARNING, LOGGER.excMsg(METHOD, "empty"));
    }

    /**
     * Constructs an instance of <code>GlassFishIdeException</code> with the specified detail message.
     * <p>
     *
     * @param msg The detail message.
     */
    public PayaraIdeException(String msg) {
        super(msg);
        final String METHOD = "init";
        // Log exception in WARNING level.
        if (LOGGER.isLoggable(Level.WARNING)) {
            String hdr = LOGGER.excMsg(METHOD, "msg");
            String sep = msg != null ? ": " : ".";
            StringBuilder sb = new StringBuilder(hdr.length() + sep.length()
                    + (msg != null ? msg.length() : 0));
            sb.append(hdr);
            sb.append(sep);
            if (msg != null) {
                sb.append(msg);
            }
            LOGGER.exception(Level.WARNING, sb.toString());
        }
    }

    /**
     * Constructs an instance of <code>GlassFishIdeException</code> with the specified detail message
     * and arguments.
     * <p/>
     * Uses {@link java.text.MessageFormat} to format message.
     * <p/>
     *
     * @param msg The detail message.
     * @param arguments Arguments to be inserted into message.
     */
    public PayaraIdeException(String msg, Object... arguments) {
        this(formatMessage(msg, arguments));
    }

    /**
     * Constructs an instance of <code>GlassFishIdeException</code> with the specified detail message
     * and cause. Exception is logged on WARN level.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically
     * incorporated int his runtime exception's detail message.
     * <p>
     *
     * @param msg the detail message (which is saved for later retrieval by the {@link #getMessage()}
     * method).
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A
     * <code>null</code> value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public PayaraIdeException(String msg, Throwable cause) {
        super(msg, cause);
        final String METHOD = "init";
        // Log exception in WARNING level.
        if (LOGGER.isLoggable(Level.WARNING)) {
            String hdr = LOGGER.excMsg(METHOD, "msg");
            String sep = msg != null ? ": " : ".";
            StringBuilder sb = new StringBuilder(hdr.length() + sep.length()
                    + (msg != null ? msg.length() : 0));
            sb.append(hdr);
            sb.append(sep);
            if (msg != null) {
                sb.append(msg);
            }
            LOGGER.exception(Level.WARNING, sb.toString());
            // Log cause exception in WARNING level.
            if (cause != null) {
                String className = cause.getClass().getName();
                msg = cause.getMessage();
                sep = msg != null ? ": " : ".";
                hdr = LOGGER.excMsg(METHOD, "cause");
                sb = new StringBuilder(hdr.length() + className.length()
                        + sep.length() + (msg != null ? msg.length() : 0));
                sb.append(hdr);
                sb.append(className);
                sb.append(sep);
                if (msg != null) {
                    sb.append(msg);
                }
                LOGGER.exception(Level.WARNING, sb.toString());
            }
        }
    }

}
