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

package fish.payara.eclipse.tools.server.sdk.admin;

/**
 * GlassFish Server Undeploy Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpUndeploy.class)
@RunnerRestClass(runner = RunnerRestUndeploy.class)
public class CommandUndeploy extends CommandTargetName {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for undeploy command. */
    private static final String COMMAND = "undeploy";

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of the Payara server undeploy command entity.
     * <p/>
     *
     * @param target Target GlassFish instance.
     */
    public CommandUndeploy(String name) {
        this(name, null);
    }

    /**
     * Constructs an instance of Payara server undeploy command entity.
     * <p/>
     *
     * @param target Target Payara instance.
     */
    public CommandUndeploy(String name, String target) {
        super(COMMAND, name, target);
    }

}
