/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.sdk.data;

import java.util.Set;

import fish.payara.eclipse.tools.server.sdk.server.config.JavaSEPlatform;
import fish.payara.eclipse.tools.server.sdk.server.config.JavaSESet;

/**
 * Container of GlassFish JavaSE features configuration.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class GlassFishJavaSEConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Platforms retrieved from XML elements. */
    private final Set<JavaSEPlatform> platforms;

    /** Highest JavaEE specification version implemented. */
    private final String version;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of avaEE configuration entity using JavaEE set for GlassFish features
     * configuration as source of instance content.
     * <p/>
     *
     * @param javaSEconfig Container of GlassFish JavaEE features configuration.
     */
    public GlassFishJavaSEConfig(final JavaSESet javaSEconfig) {
        platforms = javaSEconfig.platforms();
        version = javaSEconfig.getVersion();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get highest JavaSE specification version implemented.
     * <p/>
     *
     * @return Highest JavaSE specification version implemented.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get supported JavaSE platforms.
     * <p/>
     *
     * @return Supported JavaSE platforms.
     */
    public Set<JavaSEPlatform> getPlatforms() {
        return platforms;
    }

}
