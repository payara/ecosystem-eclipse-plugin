/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.payara.tools.sdk.admin.response.MessagePart;
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * Command runner for commands that retrieve some kind of list.
 * <p>
 * 
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestList extends RunnerRest {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Result object - contains list of JDBC resources names. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultList<String> result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using REST interface.
     * <p/>
     * 
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestList(final GlassFishServer server, final Command command) {
        super(server, command);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create <code>ResultList</code> object corresponding to server log command execution value to be
     * returned.
     */
    @Override
    protected ResultList<String> createResult() {
        return result = new ResultList<>();
    }

    @Override
    protected boolean processResponse() {
        List<MessagePart> childMessages = report.getTopMessagePart().getChildren();
        if ((childMessages != null) && !childMessages.isEmpty()) {
            result.value = new ArrayList<>(childMessages.size());
            for (MessagePart msg : childMessages) {
                result.getValue().add(msg.getMessage());
            }
        }
        return true;
    }

}
