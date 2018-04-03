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

package org.eclipse.payara.tools.sdk.admin.cloud;

import java.io.File;

import org.eclipse.payara.tools.sdk.admin.RunnerHttpClass;
import org.eclipse.payara.tools.sdk.admin.RunnerRestClass;

/**
 * GlassFish cloud deploy command entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpCloud.class)
@RunnerRestClass(runner = RunnerRestCloudDeploy.class)
public class CommandCloudDeploy extends CommandCloud {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for deploy command. */
    private static final String COMMAND = "deploy";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** File to deploy. */
    final File path;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server deploy command entity.
     * <p/>
     *
     * @param account Cloud account identifier.
     * @param path File to deploy.
     */
    public CommandCloudDeploy(final String account, final File path) {
        super(COMMAND, account);
        this.path = path;
    }

}
