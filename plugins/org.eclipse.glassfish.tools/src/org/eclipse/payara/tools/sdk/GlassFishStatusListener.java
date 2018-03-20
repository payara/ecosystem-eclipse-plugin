/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk;

import org.eclipse.payara.tools.sdk.data.GlassFishStatusTask;
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * GlassFish server status listener.
 * <p/>
 * Receives notifications about every GlassFish server status check result or about GlassFish server
 * status changes.
 * <p/>
 *
 * @author Tomas Kraus
 */
public interface GlassFishStatusListener {

    /**
     * Callback to notify about current server status after every check when enabled.
     * <p/>
     *
     * @param server GlassFish server instance being monitored.
     * @param status Current server status.
     * @param task Last GlassFish server status check task details.
     */
    public void currentState(final GlassFishServer server,
            final GlassFishStatus status, final GlassFishStatusTask task);

    /**
     * Callback to notify about server status change when enabled.
     * <p/>
     *
     * @param server GlassFish server instance being monitored.
     * @param status Current server status.
     * @param task Last GlassFish server status check task details.
     */
    public void newState(final GlassFishServer server,
            final GlassFishStatus status, final GlassFishStatusTask task);

    /**
     * Callback to notify about server status check failures.
     * <p/>
     *
     * @param server GlassFish server instance being monitored.
     * @param task GlassFish server status check task details.
     */
    public void error(final GlassFishServer server,
            final GlassFishStatusTask task);

    /**
     * Callback to notify about status listener being registered.
     * <p/>
     * May be called multiple times for individual event sets during registration phase.
     */
    public void added();

    /**
     * Callback to notify about status listener being unregistered.
     * <p/>
     * Will be called once during listener removal phase when was found registered for at least one
     * event set.
     */
    public void removed();

}
