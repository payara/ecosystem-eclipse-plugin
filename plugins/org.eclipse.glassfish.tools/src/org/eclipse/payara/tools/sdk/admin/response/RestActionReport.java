/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of the response returned by REST administration service.
 * <p>
 * 
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RestActionReport implements ActionReport {

    /** Top part of the message, can be the only one. */
    MessagePart topMessagePart = new MessagePart();
    /** Nested reports. */
    List<? extends ActionReport> subActions = new ArrayList<>();
    /** Exit code returned by server. */
    ExitCode exitCode = ActionReport.ExitCode.NA;
    /** Description of command which is the report related to. */
    String actionDescription;

    public List<? extends ActionReport> getSubActionsReport() {
        return subActions;
    }

    @Override
    public ExitCode getExitCode() {
        return exitCode;
    }

    @Override
    public String getMessage() {
        return topMessagePart.getMessage();
    }

    @Override
    public String getCommand() {
        return actionDescription;
    }

    void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    void setExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    void setMessage(String message) {
        topMessagePart.setMessage(message);
    }

    public boolean isSuccess() {
        return getExitCode().equals(ExitCode.SUCCESS);
    }

    public MessagePart getTopMessagePart() {
        return topMessagePart;
    }

}
