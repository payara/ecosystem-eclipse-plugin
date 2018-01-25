/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.server;

/**
 * Listens for events in GlassFish log fetcher.
 * <p/>
 * @author Tomas Kraus
 */
public interface FetchLogEventListener {

    /**
     * Notification method called when log fetcher state was changed.
     * <p/>
     * @param event GlassFish log fetcher state change event.
     */
    public void stateChanged(final FetchLogEvent event);

}
