/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.data;

import java.util.List;

import org.eclipse.payara.tools.sdk.server.config.JavaEESet;
import org.eclipse.payara.tools.sdk.server.config.JavaSESet;
import org.eclipse.payara.tools.sdk.server.config.LibraryNode;

/**
 * Payara configuration reader API interface.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public interface GlassFishConfig {

    /**
     * Get Payara libraries configuration.
     * <p/>
     *
     * @return Payara libraries configuration.
     */
    public List<LibraryNode> getLibrary();

    /**
     * Get Payara Java EE configuration.
     * <p/>
     *
     * @return Payara JavaEE configuration.
     */
    public JavaEESet getJavaEE();

    /**
     * Get Payara Java SE configuration.
     * <p/>
     *
     * @return Payara JavaSE configuration.
     */
    public JavaSESet getJavaSE();

    /**
     * Get Payara tools configuration.
     * <p/>
     *
     * @return Payara tools configuration.
     */
    public ToolsConfig getTools();

}
