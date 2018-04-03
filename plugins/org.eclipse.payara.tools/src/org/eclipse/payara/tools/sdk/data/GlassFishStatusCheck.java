/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.data;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.payara.tools.sdk.logging.Logger;

/**
 * Server status check type.
 * <p/>
 *
 * @author Tomas Kraus
 */
public enum GlassFishStatusCheck {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values //
    ////////////////////////////////////////////////////////////////////////////

    /** Administration port check. */
    PORT,

    /** Version command check. */
    VERSION,

    /** Locations command check. */
    LOCATIONS;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(GlassFishStatusCheck.class);

    /** GlassFish version enumeration length. */
    public static final int length = GlassFishStatusCheck.values().length;

    /** A <code>String</code> representation of PORT value. */
    private static final String PORT_STR = "PORT";

    /** A <code>String</code> representation of VERSION value. */
    private static final String VERSION_STR = "VERSION";

    /** A <code>String</code> representation of LOCATIONS value. */
    private static final String LOCATIONS_STR = "LOCATIONS";

    /**
     * Stored <code>String</code> values for backward <code>String</code> conversion.
     */
    private static final Map<String, GlassFishStatusCheck> stringValuesMap = new HashMap(values().length);
    static {
        for (GlassFishStatusCheck state : GlassFishStatusCheck.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>GlassFishStatusCheck</code> with a value represented by the specified
     * <code>String</code>.
     * <p/>
     * The <code>GlassFishStatusCheck</code> returned represents existing value only if specified
     * <code>String</code> matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     *
     * @param name Value containing <code>GlassFishStatusCheck</code> <code>toString</code>
     * representation.
     * @return <code>GlassFishStatusCheck</code> value represented by <code>String</code> or
     * <code>null</code> if value was not recognized.
     */
    public static GlassFishStatusCheck toValue(final String name) {
        if (name != null) {
            return (stringValuesMap.get(name.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>GlassFishStatusCheck</code> value to <code>String</code>.
     * <p/>
     *
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        final String METHOD = "toString";
        switch (this) {
        case PORT:
            return PORT_STR;
        case VERSION:
            return VERSION_STR;
        case LOCATIONS:
            return LOCATIONS_STR;
        // This is unrecheable. Being here means this class does not handle
        // all possible values correctly.
        default:
            throw new DataException(
                    LOGGER.excMsg(METHOD, "invalidStatusCheck"));
        }
    }

}
