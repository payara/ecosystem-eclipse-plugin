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
import org.eclipse.payara.tools.sdk.admin.ResultMap;
import org.eclipse.payara.tools.sdk.data.GlassFishStatusCheckResult;

/**
 * Server status task execution result for <code>__locations</code> command including additional
 * information.
 * <p/>
 * This class stores task execution result only. Value <code>SUCCESS</code> means that Locations
 * command task execution finished successfully but it does not mean that administration command
 * itself returned with <code>COMPLETED</code> status. When <code>SUCCESS</code> status is set,
 * stored <code>result</code> value shall be examined too to see real administration command
 * execution result.
 * <p/>
 *
 * @author Tomas Kraus
 */
class StatusResultLocations extends StatusResult {

    ////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////

    /** Command <code>__locations</code> execution result. */
    final ResultMap<String, String> result;

    ////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of individual server status result for <code>__locations</code> command.
     * <p/>
     * Command <code>__locations</code> result is stored.
     * <p/>
     *
     * @param result Command <code>__locations</code> execution result.
     * @param status Individual server status returned.
     * @param failureEvent Failure cause.
     */
    StatusResultLocations(final ResultMap<String, String> result,
            final GlassFishStatusCheckResult status,
            final TaskEvent failureEvent) {
        super(status, failureEvent);
        this.result = result;
    }

    ////////////////////////////////////////////////////////////////////////
    // Getters //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Get <code>__locations</code> command execution result.
     * <p/>
     *
     * @return <code>__locations</code> command execution result.
     */
    public ResultMap<String, String> getStatusResult() {
        return result;
    }

}
