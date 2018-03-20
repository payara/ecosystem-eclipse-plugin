/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin;

import java.util.List;

/**
 * GlassFish Administration Command Result.
 * <p>
 * Stores administration command result values and command execution state.
 * Result value is <code>List</code>.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResultList<T> extends Result<List<T>> {
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Value returned by administration command execution. */
    List<T> value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish administration command result for
     * <code>List</code> result value.
     */
    ResultList() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get value returned by administration command execution.
     * @return Value returned by administration command execution.
     */
    @Override
    public List<T> getValue() {
        return value;
    }
}
