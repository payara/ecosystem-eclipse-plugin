/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.server;

import org.eclipse.glassfish.tools.sdk.TaskState;

/**
 * Events in GlassFish log fetcher.
 * <p/>
 * @author Tomas Kraus
 */
public class FetchLogEvent {
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Current log fetcher task state. */
    private TaskState state;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of event in GlassFish log fetcher.
     * <p/>
     * @param state Current log fetcher task state.
     */
    FetchLogEvent(TaskState state) {
        this.state = state;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get current log fetcher task state.
     * <p/>
     * @return Current log fetcher task state.
     */
    public TaskState getState() {
        return state;
    }

}
