/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.config;

import java.net.URL;

import org.eclipse.payara.tools.sdk.data.GlassFishConfig;

/**
 * GlassFish configuration manager.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class GlassFishConfigManager {

    /**
     * Get GlassFish configuration access object.
     * <p/>
     *
     * @param configFile GlassFish configuration XML file.
     * @return GlassFish configuration API.
     */
    public static GlassFishConfig getConfig(URL configFile) {
        return new GlassFishConfigXMLImpl(configFile);
    }
}
