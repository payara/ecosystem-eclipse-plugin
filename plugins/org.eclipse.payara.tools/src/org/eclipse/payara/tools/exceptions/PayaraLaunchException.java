/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.exceptions;

public class PayaraLaunchException extends Exception {

    private static final long serialVersionUID = -3931653934641477601L;

    private Process payaraProcess;

    public PayaraLaunchException() {
        super();
    }

    public PayaraLaunchException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public PayaraLaunchException(String message, Process gfProcess) {
        this(message, null, gfProcess);
    }

    public PayaraLaunchException(String message, Throwable cause, Process payaraProcess) {
        super(message, cause);
        this.payaraProcess = payaraProcess;
    }

    public PayaraLaunchException(String message) {
        this(message, null, null);
    }

    public PayaraLaunchException(Throwable cause) {
        this(null, cause, null);
    }

    public Process getStartedProcess() {
        return payaraProcess;
    }

}
