/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * Current state of GlassFish server administration command execution
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum TaskState {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values //
    ////////////////////////////////////////////////////////////////////////////

    /** Value representing task waiting in executor queue. */
    READY,

    /** Value representing running task. */
    RUNNING,

    /** Value representing successfully completed task (with no errors). */
    COMPLETED,

    /** Value representing failed task. */
    FAILED;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** A <code>String</code> representation of READY value. */
    private static final String READY_STR = "READY";

    /** A <code>String</code> representation of RUNNING value. */
    private static final String RUNNING_STR = "RUNNING";

    /** A <code>String</code> representation of COMPLETED value. */
    private static final String COMPLETED_STR = "COMPLETED";

    /** A <code>String</code> representation of FAILED value. */
    private static final String FAILED_STR = "FAILED";

    /**
     * Stored <code>String</code> values for backward <code>String</code> conversion.
     */
    private static final Map<String, TaskState> stringValuesMap = new HashMap(values().length);

    // Initialize backward String conversion <code>Map</code>.
    static {
        for (TaskState state : TaskState.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>TaskState</code> with a value represented by the specified <code>String</code>.
     * The <code>TaskState</code> returned represents existing value only if specified
     * <code>String</code> matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     *
     * @param stateStr Value containing <code>TaskState</code> <code>toString</code> representation.
     * @return <code>TaskState</code> value represented by <code>String</code> or <code>null</code> if
     * value was not recognized.
     */
    public static TaskState toValue(final String stateStr) {
        if (stateStr != null) {
            return (stringValuesMap.get(stateStr.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>TaskState</code> value to <code>String</code>.
     * <p>
     *
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
        case READY:
            return READY_STR;
        case RUNNING:
            return RUNNING_STR;
        case COMPLETED:
            return COMPLETED_STR;
        case FAILED:
            return FAILED_STR;
        // This is unrecheable. Returned null value means that some
        // enum value is not handled correctly.
        default:
            return null;
        }
    }

}
