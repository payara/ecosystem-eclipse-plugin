/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.server.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.sapphire.Version;

/**
 * Configuration builder provider.
 * <p/>
 * This class is responsible for handling providers for individual server
 * instances. Because {@link ConfigBuilder} class instance shall not be used
 * for multiple GlassFish server versions there must be one configuration class
 * instance for every single GlassFish server version.
 * Also every single server instance has it's own directory structure which
 * is used to search for modules. Because of that every single GlassFish server
 * instance must have it's own configuration builder.
 * Configuration builder is created with first request for given server version
 * and reused for every subsequent request.
 * <p/>
 * @author Tomas Kraus
 */
public class ConfigBuilderProvider {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Builders array for each server instance. */
    private static final Map<GlassFishServer, ConfigBuilder> builders
            = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get library builder configuration for given GlassFish server version.
     * <p/>
     * @param version GlassFish server version.
     * @return Library builder configuration for given GlassFish server version.
     */
    public static URL getBuilderConfig(final Version version) {
        if( version.matches( "[4" ) )
        {
            return ConfigBuilderProvider.class.getResource("GlassFishV4.xml");
        }
        else
        {
            return ConfigBuilderProvider.class.getResource("GlassFishV3.xml");
        }
    }

    /**
     * Get configuration builder instance for given GlassFish server entity
     * instance.
     * <p/>
     * @param server GlassFish server entity for which builder is returned.
     * <p/>
     * @return Configuration builder for given GlassFish server entity.
     * @throws ServerConfigException when there is no version ser in GlassFish
     *         server entity object or this object is null.
     */
    public static ConfigBuilder getBuilder(final GlassFishServer server) {
        if (server == null) {
            throw new ServerConfigException(
                    "GlassFish server entity shall not be null");
        }
        ConfigBuilder builder;
        synchronized (builders) {
            builder = builders.get(server);
            if (builder != null) {
                return builder;
            }
            String serverHome = server.getServerHome();
            builders.put(server, builder = new ConfigBuilder(serverHome, serverHome, serverHome));
        }
        return builder;
    }


    /**
     * Remove configuration builder instance for given GlassFish server entity
     * instance.
     * <p/>
     * Allows to free resources when configuration builder instance will no more
     * be needed (e.g. GlassFish server entity is being destroyed).
     * <p/>
     * @param server GlassFish server entity for which builder is destroyed.
     */
    public static void destroyBuilder(final GlassFishServer server) {
        if (server == null) {
            throw new ServerConfigException(
                    "GlassFish server entity shall not be null");
        }
        synchronized (builders) {
            builders.remove(server);
        }
    }

}
