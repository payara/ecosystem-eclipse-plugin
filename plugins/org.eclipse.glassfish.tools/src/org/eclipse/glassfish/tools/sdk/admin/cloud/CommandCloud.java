/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin.cloud;

import org.eclipse.glassfish.tools.sdk.admin.Command;

/**
 * GlassFish cloud administration command entity.
 * <p/>
 * Holds common data for cloud administration command.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandCloud extends Command {
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Cloud account identifier. */
    final String account;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish cloud administration command entity
     * with specified cloud account and command.
     * <p/>
     * @param command Cloud command represented by this object.
     * @param account Cloud account identifier.
     */
     CommandCloud(final String command, final String account) {
        super(command);
        this.account = account;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns cloud account identifier of this command.
     * <p/>
     * @return Cloud account identifier.
     */
     public String getAccount() {
         return account;
     }

}
