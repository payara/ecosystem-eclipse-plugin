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

package org.eclipse.payara.tools.sdk;

/**
 * GlassFish server administration command execution state report callback.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface TaskStateListener {

    ////////////////////////////////////////////////////////////////////////////
    // Interface Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Callback to notify about GlassFish server administration command execution state change.
     * <p/>
     *
     * @param newState New command execution state.
     * @param event Event related to execution state change.
     * @param args Additional String arguments.
     */
    public void operationStateChanged(TaskState newState, TaskEvent event,
            String... args);

}
