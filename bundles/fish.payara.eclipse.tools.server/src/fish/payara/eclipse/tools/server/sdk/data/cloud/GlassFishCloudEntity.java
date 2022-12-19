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

package fish.payara.eclipse.tools.server.sdk.data.cloud;

import fish.payara.eclipse.tools.server.PayaraServer;

/**
 * GlassFish Cloud Entity.
 * <p/>
 * GlassFish cloud entity instance which is used when not defined externally in IDE.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishCloudEntity implements GlassFishCloud {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish cloud name (display name in IDE). */
    protected String name;

    /** GlassFish cloud host. */
    protected String host;

    /** GlassFish cloud port. */
    protected int port;

    /** GlassFish cloud local server. */
    protected PayaraServer localServer;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs empty class instance. No default values are set.
     */
    public GlassFishCloudEntity() {
    }

    /**
     * Constructs class instance with ALL values set.
     * <p/>
     *
     * @param name GlassFish cloud name to set.
     * @param host GlassFish cloud host to set.
     * @param port GlassFish server port to set.
     * @param localServer GlassFish cloud local server to set.
     */
    public GlassFishCloudEntity(String name, String host, int port,
            PayaraServer localServer) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.localServer = localServer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish cloud name (display name in IDE).
     * <p/>
     *
     * @return GlassFish cloud name (display name in IDE).
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set GlassFish cloud name (display name in IDE).
     * <p/>
     *
     * @param name GlassFish cloud name to set (display name in IDE).
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get GlassFish cloud host.
     * <p/>
     *
     * @return GlassFish cloud host.
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * Set GlassFish cloud host.
     * <p/>
     *
     * @param host GlassFish cloud host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get GlassFish server port.
     * <p/>
     *
     * @return GlassFish server port.
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * Set GlassFish server port.
     * <p/>
     *
     * @param port GlassFish server port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get GlassFish cloud local server.
     * <p/>
     *
     * @return GlassFish cloud local server.
     */
    @Override
    public PayaraServer getLocalServer() {
        return localServer;
    }

    /**
     * Set GlassFish cloud local server.
     * <p/>
     *
     * @param localServer GlassFish cloud local server to set.
     */
    public void setLocalServer(PayaraServer localServer) {
        this.localServer = localServer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * String representation of this GlassFish cloud entity.
     * <p/>
     *
     * @return String representation of this GlassFish cloud entity.
     */
    @Override
    public String toString() {
        return name;
    }

}
