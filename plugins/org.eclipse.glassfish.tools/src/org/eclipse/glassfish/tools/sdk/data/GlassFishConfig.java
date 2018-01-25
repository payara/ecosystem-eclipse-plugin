/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.data;

import java.util.List;

import org.eclipse.glassfish.tools.sdk.server.config.JavaEESet;
import org.eclipse.glassfish.tools.sdk.server.config.JavaSESet;
import org.eclipse.glassfish.tools.sdk.server.config.LibraryNode;

/**
 * GlassFish configuration reader API interface.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public interface GlassFishConfig {

    /**
     * Get GlassFish libraries configuration.
     * <p/>
     * @return GlassFish libraries configuration.
     */
    public List<LibraryNode> getLibrary();
    
    /**
     * Get GlassFish Java EE configuration.
     * <p/>
     * @return GlassFish JavaEE configuration.
     */
    public JavaEESet getJavaEE();
    
    /**
     * Get GlassFish Java SE configuration.
     * <p/>
     * @return GlassFish JavaSE configuration.
     */
    public JavaSESet getJavaSE();

    /**
     * Get GlassFish tools configuration.
     * <p/>
     * @return GlassFish tools configuration.
     */
    public ToolsConfig getTools();
    
}
