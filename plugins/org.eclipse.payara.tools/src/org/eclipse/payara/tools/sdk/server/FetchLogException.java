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

package org.eclipse.payara.tools.sdk.server;

import org.eclipse.payara.tools.sdk.PayaraIdeException;

/**
 * GlassFish IDE SDK Exception related to reading logs from server.
 * <p>
 * All exceptions are logging themselves on WARNING level when created.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogException extends PayaraIdeException {

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of <code>FetchLogException</code> without detail message.
     */
    public FetchLogException() {
        super();
    }

    /**
     * Constructs an instance of <code>FetchLogException</code> with the specified detail message.
     * <p>
     *
     * @param msg The detail message.
     */
    public FetchLogException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>FetchLogException</code> with the specified detail message and
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
    public FetchLogException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
