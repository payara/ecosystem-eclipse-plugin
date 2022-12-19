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

package fish.payara.eclipse.tools.server.sdk;

import fish.payara.eclipse.tools.server.sdk.logging.Logger;

/**
 * GlassFisg Tooling Library configuration.
 * <p/>
 *
 * @author Tomas Kraus
 */
public class GlassFishToolsConfig {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(GlassFishToolsConfig.class);

    /** Proxy settings usage for loopback addresses. */
    private static volatile boolean proxyForLoopback = true;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Do not use proxy settings for loopback addresses.
     */
    public static void noProxyForLoopback() {
        proxyForLoopback = false;
    }

    /**
     * Use proxy settings for loopback addresses.
     * <p/>
     * This is default behavior.
     */
    public static void useProxyForLoopback() {
        proxyForLoopback = true;
    }

    /**
     * Get proxy settings usage for loopback addresses configuration value.
     * <p/>
     *
     * @return Proxy settings usage for loopback addresses configuration value.
     */
    public static boolean getProxyForLoopback() {
        return proxyForLoopback;
    }

}
