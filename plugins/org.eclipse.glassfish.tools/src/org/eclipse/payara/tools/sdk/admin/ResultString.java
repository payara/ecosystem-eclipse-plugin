/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

/**
 * GlassFish administration command result containing <code>String</code> value.
 * <p/>
 * Stores administration command result values and command execution state. Result value is
 * <code>String</code>.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResultString extends Result<String> {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Value returned by administration command execution. */
    String value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish administration command result for <code>String</code> result
     * value.
     */
    ResultString() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get value returned by administration command execution.
     * <p/>
     *
     * @return Value returned by administration command execution.
     */
    @Override
    public String getValue() {
        return value;
    }

}
