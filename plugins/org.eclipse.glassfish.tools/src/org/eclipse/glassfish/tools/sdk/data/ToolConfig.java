/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.data;

/**
 * GlassFish tool.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public interface ToolConfig {
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get tool JAR path (relative under GlassFish home).
     * <p/>
     * @return ToolConfig JAR path (relative under GlassFish home)
     */
    public String getJar();

}
