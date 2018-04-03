/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin.response;

/**
 * Represents response returned from server after command execution.
 * <p>
 * Inspired by ActionReport class from module GF Admin Rest Service. In our case the interface
 * allows just read-only access.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface ActionReport {

    /** Possible exit codes that are sent by server. */
    public enum ExitCode {
        SUCCESS, WARNING, FAILURE, NA
    };

    /**
     * Get command execution exit code.
     * <p>
     *
     * @return exit code of the called operation
     */
    public ExitCode getExitCode();

    /**
     * Getter for message included in server response.
     * <p>
     *
     * @return message
     */
    public String getMessage();

    /**
     * Getter for command name (description).
     * <p>
     *
     * @return command name
     */
    public String getCommand();

}
