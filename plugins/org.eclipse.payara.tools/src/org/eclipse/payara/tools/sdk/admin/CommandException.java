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

import org.eclipse.payara.tools.sdk.PayaraIdeException;

/**
 * GlassFish IDE SDK Exception related to server administration command package problems.
 * <p>
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandException extends PayaraIdeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    private static final long serialVersionUID = 1L;

    /** Exception message for unsupported GlassFish version. */
    static final String UNSUPPORTED_VERSION = "Unsupported GlassFish version";

    /**
     * Exception message for unknown GlassFish administration interface type.
     */
    static final String UNKNOWN_ADMIN_INTERFACE = "Unknown GlassFish administration interface type";

    /** Exception message for unknown GlassFish version. */
    static final String UNKNOWN_VERSION = "Unknown GlassFish version";

    /** Exception message for unsupported operation. */
    static final String UNSUPPORTED_OPERATION = "Operation is not supported";

    /** Exception message for IOException when reading HTTP response. */
    static final String HTTP_RESP_IO_EXCEPTION = "Can not read HTTP response, caught IOException";
    /**
     * Exception message for exceptions when initializing <code>Runner</code> object.
     */
    static final String RUNNER_INIT = "Cannot initialize Runner class";

    /**
     * Exception message for exceptions when preparing headers for HTTP connection.
     */
    static final String RUNNER_HTTP_HEADERS = "Cannos set headers for HTTP connection";

    /** Exception message for exceptions when building command URL. */
    static final String RUNNER_HTTP_URL = "Cannot build HTTP command URL";

    /** Exception message for illegal <code>Command</code> instance provided. */
    static final String ILLEGAL_COMAND_INSTANCE = "Illegal command instance provided";

    /** Exception message for illegal <code>null</code> value provided. */
    static final String ILLEGAL_NULL_VALUE = "Value shall not be null";

    /**
     * Exception message for UnsupportedEncodingException when processing <code>Manifest</code>
     * retrieved from server.
     */
    static final String HTTP_RESP_UNS_ENC_EXCEPTION = "Can not read HTTP response, caught UnsupportedEncodingException";

    /** Exception message for invalid server component (application) item. */
    public static final String MANIFEST_INVALID_COMPONENT_ITEM = "Invalid component item";

    /**
     * Exception message for invalid constant representing <code>boolean</code> value.
     */
    public static final String INVALID_BOOLEAN_CONSTANT = "Invalid String representing boolean constant.";

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>CommandException</code> without detail message.
     */
    public CommandException() {
        super();
    }

    /**
     * Constructs an instance of <code>CommandException</code> with the specified detail message.
     * <p>
     *
     * @param msg The detail message.
     */
    public CommandException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>CommandException</code> with the specified detail message and
     * arguments.
     * <p/>
     * Uses {@link java.text.MessageFormat} to format message.
     * <p/>
     *
     * @param msg The detail message.
     * @param arguments Arguments to be inserted into message.
     */
    public CommandException(String msg, Object... arguments) {
        super(msg, arguments);
    }

    /**
     * Constructs an instance of <code>CommandException</code> with the specified detail message and
     * cause. Exception is logged on WARN level.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically
     * incorporated in this runtime exception's detail message.
     * <p>
     *
     * @param msg the detail message (which is saved for later retrieval by the {@link #getMessage()}
     * method).
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A
     * <code>null</code> value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CommandException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
