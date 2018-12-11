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

package org.eclipse.payara.tools.sdk.data;

import org.eclipse.payara.tools.sdk.PayaraIdeException;

/**
 * GlassFish IDE SDK Exception related to server administration command package problems.
 * <p>
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class DataException extends PayaraIdeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Exception message for invalid GlassFish administration interface type.
     */
    static final String INVALID_ADMIN_INTERFACE = "Invalid GlassFish administration interface type";

    /** Exception message for invalid GlassFish version. */
    static final String INVALID_CONTAINER = "Invalid GlassFish container";

    /**
     * Exception message for invalid GlassFish URL. Used in IDE URL entity class.
     */
    public static final String INVALID_URL = "Invalid GlassFish URL";

    /** Exception for GlassFish installation root directory null value. */
    static final String SERVER_ROOT_NULL = "GlassFish installation root directory is null";

    /** Exception for GlassFish home directory null value. */
    static final String SERVER_HOME_NULL = "GlassFish home directory is null";

    /**
     * Exception for non existent GlassFish installation root directory. Requires 1 directory argument.
     */
    static final String SERVER_ROOT_NONEXISTENT = "GlassFish installation root directory {0} does not exist";

    /**
     * Exception for non existent GlassFish home directory. Requires 1 directory argument.
     */
    static final String SERVER_HOME_NONEXISTENT = "GlassFish home directory {0} does not exist";

    /**
     * Exception for unknown GlassFish version in GlassFish home directory.
     */
    static final String SERVER_HOME_NO_VERSION = "Unknown GlassFish version in home directory {0}";

    /** Exception for GlassFish URL null value. */
    static final String SERVER_URL_NULL = "GlassFish URL is null";

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>DataException</code> without detail message.
     */
    public DataException() {
        super();
    }

    /**
     * Constructs an instance of <code>DataException</code> with the specified detail message.
     * <p>
     *
     * @param msg The detail message.
     */
    public DataException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>DataException</code> with the specified detail message and
     * arguments.
     * <p/>
     * Uses {@link java.text.MessageFormat} to format message.
     * <p/>
     *
     * @param msg The detail message.
     * @param arguments Arguments to be inserted into message.
     */
    public DataException(final String msg, final Object... arguments) {
        super(msg, arguments);
    }

    /**
     * Constructs an instance of <code>DataException</code> with the specified detail message and cause.
     * Exception is logged on WARN level.
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
    public DataException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
