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

import java.util.HashMap;
import java.util.Map;

/**
 * GlassFish administration command result.
 * <p>
 * Stores administration command result values and command execution state. Result value is
 * <code>Map</code>.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResultMap<K, V> extends Result<Map<K, V>> {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Value returned by administration command execution. */
    HashMap<K, V> value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish administration command result for <code>Map</code> result
     * value.
     */
    ResultMap() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get value returned by administration command execution.
     *
     * @return Value returned by administration command execution.
     */
    @Override
    public Map<K, V> getValue() {
        return value;
    }

}
