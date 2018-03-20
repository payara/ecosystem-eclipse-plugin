/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.config;

import org.eclipse.payara.tools.sdk.data.ToolsConfig;

/**
 * Payara tools.
 * <p/>
 * 
 * @author Peter Benedikovic, Tomas Kraus
 */
public class Tools implements ToolsConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Asadmin tool. */
    private final AsadminTool asadmin;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Payara tools.
     * <p/>
     * 
     * @param asadmin Payara asadmin tool.
     */
    public Tools(AsadminTool asadmin) {
        this.asadmin = asadmin;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get asadmin tool.
     * <p/>
     * 
     * @return Asadmin tool.
     */
    @Override
    public AsadminTool getAsadmin() {
        return asadmin;
    }

}
