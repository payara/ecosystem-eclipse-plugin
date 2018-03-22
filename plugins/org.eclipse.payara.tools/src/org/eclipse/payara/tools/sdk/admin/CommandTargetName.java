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
 * Abstract GlassFish Server Command Entity containing name and target.
 * <p/>
 * Contains common <code>name</code> and <code>target</code> attribute. Holds data for command.
 * Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class CommandTargetName extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Name of module/cluster/instance to modify. */
    final String name;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server enable command entity.
     * <p/>
     *
     * @param command Server command represented by this object.
     * @param name Name of module/cluster/instance to modify.
     * @param target Target GlassFish instance or cluster where <code>name</code> is stored.
     */
    CommandTargetName(String command, String name, String target) {
        super(command, target);
        this.name = name;
    }

}
