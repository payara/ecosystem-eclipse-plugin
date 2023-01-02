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
 * Abstract GlassFish Server Command Entity containing target.
 * <p/>
 * Contains common <code>target</code> attribute. Holds data for command. Objects of this class are
 * created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class CommandTarget extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Target GlassFish instance or cluster. */
    final String target;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server enable command entity.
     * <p/>
     *
     * @param command Server command represented by this object.
     * @param target Target GlassFish instance or cluster.
     */
    CommandTarget(String command, String target) {
        super(command);
        this.target = target;
    }

}
