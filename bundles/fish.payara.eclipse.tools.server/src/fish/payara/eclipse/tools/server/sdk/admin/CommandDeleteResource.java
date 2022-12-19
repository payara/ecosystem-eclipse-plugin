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
 * Command that deletes resource from server.
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
@RunnerHttpClass(runner = RunnerHttpDeleteResource.class)
@RunnerRestClass(runner = RunnerRestDeleteResource.class)
public class CommandDeleteResource extends CommandTarget {

    private static final String COMMAND_PREFIX = "delete-";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Name of the resource. */
    String name;

    /** Key name that defines the deleted property. */
    String cmdPropertyName;

    /** Delete also dependent resources. */
    boolean cascade;

    /**
     * Constructor for delete resource command entity.
     * <p/>
     *
     * @param target Target GlassFish instance or cluster.
     * @param name Name of resource to be deleted.
     * @param resourceCmdSuffix Resource related command suffix. Command string is build by appending
     * this value after <code>delete-</code>.
     * @param cmdPropertyName Name of query property which contains resource name.
     * @param cascade Delete also dependent resources.
     */
    public CommandDeleteResource(String target, String name,
            String resourceCmdSuffix, String cmdPropertyName, boolean cascade) {
        super(COMMAND_PREFIX + resourceCmdSuffix, target);
        this.name = name;
        this.cmdPropertyName = cmdPropertyName;
        this.cascade = cascade;
    }

    /**
     * Constructor for delete resource command entity.
     * <p/>
     *
     * @param name Name of resource to be deleted.
     * @param resourceCmdSuffix Resource related command suffix. Command string is build by appending
     * this value after <code>delete-</code>.
     * @param cmdPropertyName Name of query property which contains resource name.
     * @param cascade Delete also dependent resources.
     */
    public CommandDeleteResource(String name,
            String resourceCmdSuffix, String cmdPropertyName, boolean cascade) {
        this(null, name, resourceCmdSuffix, cmdPropertyName, cascade);
    }

}
