/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.data.cloud;

import org.eclipse.payara.tools.server.PayaraServer;

/**
 * GlassFish Cloud Entity Interface.
 * <p/>
 * GlassFish Cloud entity interface allows to use foreign entity classes.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface GlassFishCloud {

    ////////////////////////////////////////////////////////////////////////////
    // Interface Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish cloud name.
     * <p/>
     * This is display name given to the cloud.
     * <p/>
     *
     * @return GlassFish cloud name.
     */
    public String getName();

    /**
     * Get GlassFish cloud (CPAS) host.
     * <p/>
     *
     * @return GlassFish cloud (CPAS) host.
     */
    public String getHost();

    /**
     * Get GlassFish cloud port.
     * <p/>
     *
     * @return GlassFish cloud port.
     */
    public int getPort();

    /**
     * Get GlassFish cloud local server.
     * <p/>
     *
     * @return GlassFish cloud local server.
     */
    public PayaraServer getLocalServer();

}
