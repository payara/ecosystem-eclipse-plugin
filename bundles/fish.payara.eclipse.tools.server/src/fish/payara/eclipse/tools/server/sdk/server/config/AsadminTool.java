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

package fish.payara.eclipse.tools.server.sdk.server.config;

import fish.payara.eclipse.tools.server.sdk.data.ToolConfig;
import fish.payara.eclipse.tools.server.sdk.utils.OsUtils;

/**
 * GlassFish asadmin tool.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class AsadminTool extends GlassFishTool implements ToolConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Asadmin tool JAR path (relative under GlassFish home). */
    private final String jar;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish asadmin tool.
     * <p/>
     *
     * @param lib Tools library directory (relative under GlassFish home).
     * @param jar Asadmin tool JAR (relative under tools library directory).
     */
    public AsadminTool(final String lib, final String jar) {
        super(lib);
        this.jar = OsUtils.joinPaths(lib, jar);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get asadmin tool JAR path (relative under GlassFish home)
     * <p/>
     *
     * @return Asadmin tool JAR path (relative under GlassFish home)
     */
    @Override
    public String getJar() {
        return jar;
    }

}
