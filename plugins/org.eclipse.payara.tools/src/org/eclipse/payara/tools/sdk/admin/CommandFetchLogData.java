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
 * GlassFish Server View Log Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerRestFetchLogData.class)
@RunnerRestClass(runner = RunnerRestFetchLogData.class)
public class CommandFetchLogData extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for view log command. */
    private static final String COMMAND = "view-log";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Query parameters to be used to read only log entries added in particular interval starting from
     * previous call that returned this value of <code>paramsAppendNext</code> stored in returned
     * <code>ValueLog</code>.
     * <p/>
     * Content of HTTP header <code>X-Text-Append-Next</code>.
     */
    final String paramsAppendNext;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server view log command entity.
     * <p/>
     * All existing log entries will be returned.
     */
    public CommandFetchLogData() {
        super(COMMAND);
        this.paramsAppendNext = null;
    }

    /**
     * Constructs an instance of GlassFish server view log command entity.
     * <p/>
     * Only log entries added in particular interval starting from previous call that returned this
     * value of <code>paramsAppendNext</code> will be returned.
     * <p/>
     *
     * @param paramsAppendNext Interval query parameters from Last View Log command execution.
     */
    public CommandFetchLogData(String paramsAppendNext) {
        super(COMMAND);
        this.paramsAppendNext = paramsAppendNext;
    }

}
