/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.util.List;

import org.eclipse.payara.tools.sdk.utils.Utils;

/**
 * GlassFish server log.
 * <p/>
 * 
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ValueLog {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server log lines. */
    final List<String> lines;

    /**
     * URL parameters from HTTP header <code>X-Text-Append-Next</code>.
     * <p/>
     * <code>X-Text-Append-Next</code> header contains the entire URL to pass to the GET method to
     * return the changes since the last call. You can use those URL parameters to construct URL to get
     * all log entries that were added in particular interval starting from call that returned this
     * result.
     */
    final String paramsAppendNext;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish server log entity.
     * <p/>
     * Entity is initialized with values stored in <code>Runner</code> internal attributes in
     * <code>processResponse</code> method.
     * <p/>
     * 
     * @param lines GlassFish server log lines.
     * @param paramsAppendNext URL parameters from HTTP header <code>X-Text-Append-Next</code>
     */
    ValueLog(List<String> lines, String paramsAppendNext) {
        this.lines = lines;
        this.paramsAppendNext = paramsAppendNext;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish server log lines.
     * <p/>
     * 
     * @return GlassFish server log lines.
     */
    public List<String> getLines() {
        return lines;
    }

    /**
     * Get URL parameters from HTTP header <code>X-Text-Append-Next</code>.
     * <p/>
     * 
     * @return URL parameters from HTTP header <code>X-Text-Append-Next</code>.
     */
    public String getParamsAppendNext() {
        return paramsAppendNext;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert stored server log lines to <code>String</code>.
     * <p>
     * 
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        String lineSeparator = Utils.lineSeparator();
        int lineSeparatorLength = lineSeparator.length();
        if (lines != null) {
            // Calculate total log length to avoid StringBuffer resizing.
            int length = 0;
            for (String line : lines) {
                length += line != null
                        ? line.length() + lineSeparatorLength
                        : lineSeparatorLength;
            }
            StringBuilder sb = new StringBuilder(length);
            for (String line : lines) {
                if (line != null) {
                    sb.append(line);
                }
                sb.append(lineSeparator);
            }
            return sb.toString();
        } else {
            return null;
        }
    }

}
