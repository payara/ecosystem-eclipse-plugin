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
 * GlassFish server administration command entity.
 * <p/>
 * Holds common data for administration command.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class Command {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Server command represented by this object. */
    protected String command;

    /** Indicate whether we shall retry command execution. */
    protected boolean retry;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server administration command entity with specified server
     * command.
     * <p/>
     *
     * @param command Server command represented by this object.
     */
    protected Command(final String command) {
        this.command = command;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns server command represented by this object. Set in constructor. e.g. "deploy",
     * "list-applications", etc.
     * <p/>
     *
     * @return command string represented by this object.
     */
    public String getCommand() {
        return command;
    }

    // This is also kind of getter.
    /**
     * Sometimes (e.g. during startup), the server does not accept commands. In such cases, it will
     * block for 20 seconds and then return with the message "V3 cannot process this command at this
     * time, please wait".
     * <p/>
     * In such cases, we set a flag and have the option to reissue the command.
     * <p/>
     *
     * @return true if server responded with its "please wait" message.
     */
    public boolean retry() {
        return retry;
    }

}
