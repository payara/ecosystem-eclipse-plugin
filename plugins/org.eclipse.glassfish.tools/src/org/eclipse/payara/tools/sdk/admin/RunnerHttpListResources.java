/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.jar.Attributes;

import org.eclipse.payara.tools.sdk.utils.ServerUtils;
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * Command runner for retrieving resources from server.
 * <p>
 * <p/>
 * 
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpListResources extends RunnerHttpTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * GlassFish administration command result containing server resources.
     * <p/>
     * Result instance life cycle is started with submitting task into <code>ExecutorService</code>'s
     * queue. method <code>call()</code> is responsible for correct <code>TaskState</code> and
     * receiveResult value handling.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultList<String> result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     * 
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpListResources(final GlassFishServer server,
            final Command command) {
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

    /**
     * Extracts result value from internal <code>Manifest</code> object. Value of <i>message</i>
     * attribute in <code>Manifest</code> object is stored as <i>value</i> into
     * <code>ResultString</code> result object.
     * <p/>
     * 
     * @return true if result was extracted correctly. <code>null</code> <i>message</i>value is
     * considered as failure.
     */
    @Override
    protected boolean processResponse() {
        String resourcesAttr = manifest.getMainAttributes()
                .getValue("children");
        String[] resources = resourcesAttr != null
                ? resourcesAttr.split(ServerUtils.MANIFEST_RESOURCES_SEPARATOR)
                : null;
        int resoucesCount = resources != null ? resources.length : 0;
        result.value = new ArrayList<>(resoucesCount);
        if (resources != null) {
            for (String resource : resources) {
                Attributes resourceAttr = manifest.getAttributes(resource);
                String resourceMsg = resourceAttr.getValue("message");
                String name;
                try {
                    if (resourceMsg != null) {
                        name = URLDecoder.decode(resourceMsg, "UTF-8");
                    } else {
                        name = null;
                    }
                    if (name == null || name.length() <= 0) {
                        name = URLDecoder.decode(resource.trim(), "UTF-8");
                    }
                } catch (UnsupportedEncodingException uee) {
                    throw new CommandException(
                            CommandException.HTTP_RESP_UNS_ENC_EXCEPTION, uee);
                }
                result.value.add(name);
            }
        }
        return true;
    }
}
