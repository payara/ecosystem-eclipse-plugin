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

package org.eclipse.payara.tools.sdk.admin;

/**
 * GlassFish Administratio Command Result containing server log as <code>List&ltString&gt</code>
 * values.
 * <p/>
 * Stores administration command result values and command execution state. Result value is
 * <code>List&ltString&gt</code> with individual log lines</li>. <code>String</code> with
 * <code>X-Text-Append-Next</code> response URL parameters is also stored.</li>
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResultLog extends Result<ValueLog> {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Server log value returned by admin command execution. */
    ValueLog value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish admin command result for <code>List&ltString&gt</code> result
     * value containing server log.
     */
    ResultLog() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get server log value returned by admin command execution.
     * <p/>
     *
     * @return Server log value returned by admin command execution.
     */
    @Override
    public ValueLog getValue() {
        return value;
    }

}
