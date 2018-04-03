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
import org.eclipse.payara.tools.sdk.admin.ResultString;
import org.eclipse.payara.tools.sdk.data.GlassFishStatusCheckResult;

/**
 * Individual server status result for <code>version</code> command including additional
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
class StatusResultVersion extends StatusResult {

    ////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////

    /** Command <code>version</code> execution result. */
    final ResultString result;

    ////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of individual server status result for <code>version</code> command.
     * <p/>
     * Command <code>version</code> result is stored.
     * <p/>
     *
     * @param result Command <code>version</code> execution result.
     * @param status Individual server status returned.
     * @param failureEvent Failure cause.
     */
    StatusResultVersion(final ResultString result,
            final GlassFishStatusCheckResult status,
            final TaskEvent failureEvent) {
        super(status, failureEvent);
        this.result = result;
    }

    ////////////////////////////////////////////////////////////////////////
    // Getters //
    ////////////////////////////////////////////////////////////////////////
    /**
     * Get <code>version</code> command execution result.
     * <p/>
     *
     * @return <code>version</code> command execution result.
     */
    public ResultString getResult() {
        return result;
    }
}
