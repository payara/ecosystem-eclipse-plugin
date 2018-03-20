/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.state;

import org.eclipse.payara.tools.sdk.TaskEvent;
import org.eclipse.payara.tools.sdk.data.GlassFishStatusCheckResult;

/**
 * Individual server status result including additional information.
 * <p/>
 * @author tomas Kraus
 */
class StatusResult {

    ////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                //
    ////////////////////////////////////////////////////////////////////////

    /** Individual server status returned. */
    final GlassFishStatusCheckResult status;

    /** Task failure event. */
    final TaskEvent event;

    ////////////////////////////////////////////////////////////////////////
    // Constructors                                                       //
    ////////////////////////////////////////////////////////////////////////
    /**
     * Creates an instance of individual server status result.
     * <p/>
     * @param status Individual server status returned.
     * @param event  Current status cause.
     */
    StatusResult(final GlassFishStatusCheckResult status,
            final TaskEvent event) {
        this.status = status;
        this.event = event;
    }

    /**
     * Creates an instance of individual server status result.
     * <p/>
     * @param status Individual server status returned.
     */
    StatusResult(final GlassFishStatusCheckResult status) {
        this(status, null);
    }

    ////////////////////////////////////////////////////////////////////////
    // Getters                                                            //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Get individual check task status.
     * <p/>
     * @return Individual check task status.
     */
    public GlassFishStatusCheckResult getStatus() {
        return status;
    }

    /**
     * Get task failure event.
     * <p/>
     * @return Task failure event.
     */
    public TaskEvent getEvent() {
        return event;
    }
    
}
