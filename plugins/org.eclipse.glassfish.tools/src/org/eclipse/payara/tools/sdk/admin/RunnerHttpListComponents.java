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
import java.util.HashMap;
import java.util.List;

import org.eclipse.payara.tools.sdk.utils.JavaUtils;
import org.eclipse.payara.tools.sdk.utils.ServerUtils;
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * Command runner for retrieving list of components from server.
 * <p>
 * 
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpListComponents extends RunnerHttpTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * GlassFish administration command result containing server components.
     * <p/>
     * Result instance life cycle is started with submitting task into <code>ExecutorService</code>'s
     * queue. method <code>call()</code> is responsible for correct <code>TaskState</code> and
     * receiveResult value handling.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultMap<String, List<String>> result;

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
    public RunnerHttpListComponents(final GlassFishServer server,
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
    protected ResultMap<String, List<String>> createResult() {
        return result = new ResultMap<>();
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
        String[] components = resourcesAttr != null
                ? resourcesAttr.split(ServerUtils.MANIFEST_COMPONENTS_SEPARATOR)
                : null;
        result.value = new HashMap<>();
        if (components != null) {
            for (String component : components) {
                String decodedComponent;
                try {
                    decodedComponent = URLDecoder.decode(
                            component, JavaUtils.UTF_8.name());
                } catch (UnsupportedEncodingException ex) {
                    decodedComponent = component;
                }
                ServerUtils.addComponentToMap(result.value, decodedComponent);
            }
        }
        return true;
    }

}
