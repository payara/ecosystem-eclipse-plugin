/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.util.Map;

/**
 * Command that creates connector resource with the specified JNDI name and the interface definition
 * for a resource adapter on server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpCreateConnector.class)
@RunnerRestClass(runner = RunnerRestCreateConnector.class)
public class CommandCreateConnector extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create connector resource command. */
    private static final String COMMAND = "create-connector-resource";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** The JNDI name of this connector resource. */
    final String jndiName;

    /** Connection pool unique name (and ID). */
    final String poolName;

    /** Optional properties for configuring the resource. */
    final Map<String, String> properties;

    /** If this object is enabled. */
    final boolean enabled;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server create connector resource command entity.
     * <p/>
     *
     * @param jndiName The JNDI name of this connector resource.
     * @param poolName Connection pool unique name (and ID).
     * @param properties Optional properties for configuring the pool.
     * @param enabled If this object is enabled.
     * @param target Name of the cluster or instance where the resource will be created.
     */
    public CommandCreateConnector(final String jndiName, final String poolName,
            final Map<String, String> properties, final boolean enabled, final String target) {
        super(COMMAND, target);
        this.jndiName = jndiName;
        this.poolName = poolName;
        this.properties = properties;
        this.enabled = enabled;
    }

    /**
     * Constructs an instance of GlassFish server create connector resource command entity.
     * <p/>
     *
     * @param jndiName The JNDI name of this connector resource.
     * @param poolName Connection pool unique name (and ID).
     * @param properties Optional properties for configuring the pool.
     * @param enabled If this object is enabled.
     */
    public CommandCreateConnector(final String jndiName, final String poolName,
            final Map<String, String> properties, final boolean enabled) {
        this(jndiName, poolName, properties, enabled, null);
    }

    /**
     * Constructs an instance of GlassFish server create connector resource command entity.
     * <p/>
     * This object will be enabled on server by default.
     * <p/>
     *
     * @param jndiName The JNDI name of this connector resource.
     * @param poolName Connection pool unique name (and ID).
     * @param properties Optional properties for configuring the pool.
     */
    public CommandCreateConnector(final String jndiName, final String poolName,
            final Map<String, String> properties) {
        this(jndiName, poolName, properties, true);
    }

}
