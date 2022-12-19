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
 * GlassFish Server Cerate Cluster Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpTarget.class)
@RunnerRestClass(runner = RunnerRestCreateCluster.class)
public class CommandCreateCluster extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create-cluster command. */
    private static final String COMMAND = "create-cluster";

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server create-cluster command entity.
     * <p/>
     *
     * @param clusterName Name of the created cluster.
     */
    public CommandCreateCluster(String clusterName) {
        super(COMMAND, clusterName);
    }

}
