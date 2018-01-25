/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.data;
 
/**
 * Individual server check status returned.
 * <p/>
 * There is also minimal algebra defined to support <code>AND</code>
 * and <code>OR</code>.
 * <p/>
 * @author Tomas Kraus
 */
public enum GlassFishStatusCheckResult {

    /** Server status check passed. */
    SUCCESS,

    /** Server status check failed with <code>FAILED</code> result. */
    FAILED;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Full <code>AND</code> operator state space. */ 
    private static final GlassFishStatusCheckResult[][] and = {
      // SUCCESS  FAILED
        {SUCCESS, FAILED}, // SUCCESS
        { FAILED, FAILED}  // FAILED
    };

    /** Full <code>OR</code> operator state space. */ 
    private static final GlassFishStatusCheckResult[][] or = {
      // SUCCESS   FAILED
        {SUCCESS, SUCCESS}, // SUCCESS
        {SUCCESS,  FAILED}  // FAILED
    };

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Compute logical <code>AND</code> of two status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     */
    public static GlassFishStatusCheckResult and(
            final GlassFishStatusCheckResult s1,
            final GlassFishStatusCheckResult s2) {
        return and[s1.ordinal()][s2.ordinal()];
    }

    /**
     * Compute logical <code>OR</code> of two status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     */
    public static  GlassFishStatusCheckResult or(
            final GlassFishStatusCheckResult s1,
            final GlassFishStatusCheckResult s2) {
        return or[s1.ordinal()][s2.ordinal()];
    }

    /**
     * Compute logical <code>AND</code> of three status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     * @param s3 Third operand.
     */
    public static GlassFishStatusCheckResult and(
            final GlassFishStatusCheckResult s1,
            final GlassFishStatusCheckResult s2,
            final GlassFishStatusCheckResult s3) {
        return and[s1.ordinal()][and[s2.ordinal()][s3.ordinal()].ordinal()];
    }

    /**
     * Compute logical <code>OR</code> of three status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     * @param s3 Third operand.
     */
    public static GlassFishStatusCheckResult or(
            final GlassFishStatusCheckResult s1,
            final GlassFishStatusCheckResult s2,
            final GlassFishStatusCheckResult s3) {
        return or[s1.ordinal()][or[s2.ordinal()][s3.ordinal()].ordinal()];
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>GlassFishStatusCheckResult</code> value
     * to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value
     *         of this object.
     */
    @Override
    public String toString() {
        switch(this) {
            case SUCCESS:   return "SUCCESS";
            case FAILED:    return "FAILED";
            default:
                throw new IllegalStateException("Unknown Status value");
        }
    }

}

