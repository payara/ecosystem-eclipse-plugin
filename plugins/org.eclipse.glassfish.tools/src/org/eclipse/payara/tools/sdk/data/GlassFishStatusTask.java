/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.data;

import org.eclipse.payara.tools.sdk.TaskEvent;

/**
 * GlassFish server status check task details.
 * <p/>
 * Provides access to server status check task details in status listener
 * callback methods.
 * <p/>
 * @author Tomas Kraus
 */
public interface GlassFishStatusTask {

    /**
     * Get server status check type.
     * <p/>
     * @return Server status check type.
     */
    public GlassFishStatusCheck getType();

    /**
     * Get last command task execution status.
     * <p/>
     * @return Last command task execution status.
     */
    public GlassFishStatusCheckResult getStatus();

    /**
     * Get last command task execution status.
     * <p/>
     * @return Last command task execution status.
     */
    public TaskEvent getEvent();

}
