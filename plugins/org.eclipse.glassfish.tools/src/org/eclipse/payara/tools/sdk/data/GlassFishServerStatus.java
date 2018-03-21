/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.data;

import org.eclipse.payara.tools.sdk.GlassFishStatus;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * GlassFish server status interface.
 * <p/>
 * GlassFish Server entity interface allows to use foreign entity classes.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface GlassFishServerStatus {

    ////////////////////////////////////////////////////////////////////////////
    // Interface Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish server entity.
     * <p/>
     *
     * @return GlassFish server entity.
     */
    public PayaraServer getServer();

    /**
     * Get current GlassFish server status.
     * <p/>
     *
     * @return Current GlassFish server status.
     */
    public GlassFishStatus getStatus();

}
