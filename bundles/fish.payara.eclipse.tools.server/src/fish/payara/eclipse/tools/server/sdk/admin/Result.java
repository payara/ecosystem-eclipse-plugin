/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.sdk.admin;

import fish.payara.eclipse.tools.server.sdk.TaskState;

/**
 * GlassFish administration command result.
 * <p/>
 * Stores administration command result values and command execution state.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class Result<T> {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** State of GlassFish server administration command execution. */
    TaskState state;

    /**
     * Authorization status.
     * <p/>
     * Value of <code>true</code> means that there was no authorization issue. Value of
     * <code>false</code> means that authorization failed.
     */
    boolean auth;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish administration command result.
     */
    Result() {
        this.state = null;
        this.auth = true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get state of GlassFish server administration command execution.
     * <p/>
     *
     * @return State of GlassFish server administration command execution.
     */
    public TaskState getState() {
        return state;
    }

    /**
     * Get value returned by administration command execution.
     * <p/>
     *
     * @return Value returned by administration command execution.
     */
    public abstract T getValue();

    /**
     * Get administration command execution authorization status.
     * <p/>
     *
     * @return Value of <code>true</code> means that there was no authorization issue. Value of
     * <code>false</code> means that authorization failed.
     */
    public boolean isAuth() {
        return auth;
    }

    /**
     * Set administration command execution authorization status.
     * <p/>
     * Use only in administration command runners to set result value.
     * <p/>
     *
     * @param auth Authorization status: Value of <code>true</code> means that there was no
     * authorization issue. Value of <code>false</code> means that authorization failed.
     */
    public void setAuth(final boolean auth) {
        this.auth = auth;
    }

}
