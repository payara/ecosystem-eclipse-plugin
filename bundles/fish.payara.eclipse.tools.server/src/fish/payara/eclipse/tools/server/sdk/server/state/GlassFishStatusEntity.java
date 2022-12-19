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

package fish.payara.eclipse.tools.server.sdk.server.state;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.GlassFishStatus;
import fish.payara.eclipse.tools.server.sdk.data.GlassFishServerStatus;
import fish.payara.eclipse.tools.server.sdk.logging.Logger;

/**
 * GlassFish server status entity.
 * <p/>
 *
 * @author Tomas Kraus
 */
public class GlassFishStatusEntity implements GlassFishServerStatus {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(GlassFishStatusEntity.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server entity. */
    private PayaraServer server;

    /** Current GlassFish server status. */
    private GlassFishStatus status;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server status entity.
     * <p/>
     * Initial server status value is set as unknown.
     * <p/>
     *
     * @param server GlassFish server entity.
     */
    public GlassFishStatusEntity(final PayaraServer server) {
        this.server = server;
        this.status = GlassFishStatus.UNKNOWN;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish server entity.
     * <p/>
     *
     * @return GlassFish server entity.
     */
    @Override
    public PayaraServer getServer() {
        return server;
    }

    /**
     * Set GlassFish server entity.
     * <p/>
     *
     * @param server GlassFish server entity.
     */
    void setServer(final PayaraServer server) {
        this.server = server;
    }

    /**
     * Get current GlassFish server status.
     * <p/>
     *
     * @return Current GlassFish server status.
     */
    @Override
    public GlassFishStatus getStatus() {
        return status;
    }

    /**
     * Set current GlassFish server status.
     * <p/>
     *
     * @param status Current GlassFish server status.
     */
    void setStatus(final GlassFishStatus status) {
        this.status = status;
    }

}
