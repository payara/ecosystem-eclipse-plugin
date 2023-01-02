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

/**
 * GlassFish Admin Command Result containing process execution result values.
 * <p/>
 * Stores admin command result values and command execution state. Result value is set of values
 * describing process execution.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResultProcess extends Result<ValueProcess> {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Value returned by admin command execution. */
    ValueProcess value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish admin command result for <code>ValueProcess</code> result
     * value.
     */
    ResultProcess() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get value returned by admin command execution.
     * <p/>
     *
     * @return Value returned by admin command execution.
     */
    @Override
    public ValueProcess getValue() {
        return value;
    }

}
