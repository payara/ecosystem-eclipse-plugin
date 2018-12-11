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

package org.eclipse.payara.tools.sdk.server.config;

import org.eclipse.payara.tools.sdk.PayaraIdeException;

/**
 * GlassFish IDE SDK Exception related to server configuration problems.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ServerConfigException extends PayaraIdeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** Exception message for invalid GlassFish JavaSE profile version. */
    static final String INVALID_SE_PLATFORM_VERSION = "Invalid GlassFish JavaSE version";

    /** Exception message for invalid GlassFish JavaEE profile type. */
    static final String INVALID_EE_PLATFORM_TYPE = "Invalid GlassFish JavaEE profile type";

    /** Exception message for invalid GlassFish module type name. */
    static final String INVALID_MODULE_TYPE_NAME = "Invalid GlassFish module type name";

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>ServerConfigException</code> without detail message.
     */
    public ServerConfigException() {
        super();
    }

    /**
     * Constructs an instance of <code>ServerConfigException</code> with the specified detail message.
     * <p>
     *
     * @param msg The detail message.
     */
    public ServerConfigException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>ServerConfigException</code> with the specified detail message
     * and arguments.
     * <p/>
     * Uses {@link java.text.MessageFormat} to format message.
     * <p/>
     *
     * @param msg The detail message.
     * @param arguments Arguments to be inserted into message.
     */
    public ServerConfigException(final String msg, final Object... arguments) {
        super(msg, arguments);
    }

    /**
     * Constructs an instance of <code>ServerConfigException</code> with the specified detail message
     * and cause. Exception is logged on WARN level.
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
    public ServerConfigException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
