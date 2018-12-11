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

package org.eclipse.payara.tools.sdk.utils;

import org.eclipse.payara.tools.sdk.PayaraIdeException;

/**
 * Utils JAR Exception related to JAR file handling problems.
 * <p>
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class JarException extends PayaraIdeException {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** Exception message for JAR file opening issues. */
    static final String OPEN_ERROR = "Cannot open JAR file.";

    /** Exception message for JAR file opening issues. */
    static final String CLOSE_ERROR = "Cannot close JAR file.";

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>JarException</code> without detail message.
     */
    public JarException() {
        super();
    }

    /**
     * Constructs an instance of <code>JarException</code> with the specified detail message.
     * <p>
     *
     * @param msg The detail message.
     */
    public JarException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>JarException</code> with the specified detail message and
     * arguments.
     * <p/>
     * Uses {@link java.text.MessageFormat} to format message.
     * <p/>
     *
     * @param msg The detail message.
     * @param arguments Arguments to be inserted into message.
     */
    public JarException(String msg, Object... arguments) {
        super(msg, arguments);
    }

    /**
     * Constructs an instance of <code>JarException</code> with the specified detail message and cause.
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
    public JarException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
