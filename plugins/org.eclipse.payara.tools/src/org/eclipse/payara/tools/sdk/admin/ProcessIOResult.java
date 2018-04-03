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
 * Response result.
 * <p/>
 * Order of values defines values priority.
 * <p/>
 *
 * @author Tomas Kraus
 */
public enum ProcessIOResult {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values //
    ////////////////////////////////////////////////////////////////////////////

    /** Unknown response. */
    UNKNOWN,

    /** Successful response. */
    SUCCESS,

    /** Error response. */
    ERROR
}
