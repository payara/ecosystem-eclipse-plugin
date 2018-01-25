/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.data.cloud;

/**
 * GlassFish Cloud User Account Entity.
 * <p/>
 * GlassFish Cloud User Account entity interface allows to use foreign
 * entity classes.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface GlassFishAccount {

    /**
     * Get GlassFish cloud user account name.
     * <p/>
     * This is display name given to the cluster.
     * <p/>
     * @return GlassFish cluster name.
     */
    public String getName();

    /**
     * Get GlassFish cloud account name.
     * <p/>
     * @return GlassFish cloud account name.
     */
    public String getAcount();

    /**
     * Get GlassFish cloud user name under account.
     * <p/>
     * @return GlassFish cloud user name under account.
     */
    public String getUserName();

    /**
     * Get GlassFish cloud URL.
     * <p/>
     * @return Cloud URL.
     */
    public String getUrl();

    /**
     * Get GlassFish cloud user password under account.
     * <p/>
     * @return GlassFish cloud user password under account.
     */
    public String getUserPassword();

    /**
     * Get GlassFish cloud entity reference.
     * <p/>
     * @return GlassFish cloud entity reference.
     */
    public GlassFishCloud getCloudEntity();

    }
