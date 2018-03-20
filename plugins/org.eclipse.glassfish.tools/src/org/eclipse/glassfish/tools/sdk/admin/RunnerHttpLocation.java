/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin;

import java.util.HashMap;
import java.util.jar.Attributes;

import org.eclipse.glassfish.tools.server.GlassFishServer;

/**
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpLocation extends RunnerHttp {
    
    /** Returned value is map where locations are stored under keys specified in
     * CommandLocation class.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultMap<String, String> result;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpLocation(final GlassFishServer server,
            final Command command) {
        super(server, command);
    }

    @Override
    protected Result createResult() {
        return result = new ResultMap<String, String>();
    }

    @Override
    protected boolean processResponse() {
        if (manifest == null)
            return false;
        
        result.value = new HashMap<String, String>();
        Attributes mainAttrs = manifest.getMainAttributes();
            if(mainAttrs != null) {
                result.value.put("Base-Root_value", mainAttrs.getValue("Base-Root_value"));
                result.value.put("Domain-Root_value", mainAttrs.getValue("Domain-Root_value"));
                result.value.put("message", mainAttrs.getValue("message"));
            }

        return true;
    }
    
}
