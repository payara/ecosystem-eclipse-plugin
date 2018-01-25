/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.server;

import org.eclipse.sapphire.Version;

/**
 * GlassFish server JPA support matrix.
 * <p/>
 * @author Tomas Kraus
 */
public class JpaSupport {
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Individual JPA specifications support.
     */
    public static class ApiVersion {

        ////////////////////////////////////////////////////////////////////////
        // Class attributes                                                   //
        ////////////////////////////////////////////////////////////////////////

        /** JPA 1.0 supported. */
        final boolean _1_0;

        /** JPA 1.0 supported. */
        final boolean _2_0;

        /** JPA 2.1 supported. */
        final boolean _2_1;

        /** JPA provider class. */
        final String provider;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of individual JPA specifications support class.
         * <p/>
         * @param jpa_1_0  JPA 1.0 supported.
         * @param jpa_2_0  JPA 1.0 supported.
         * @param jpa_2_1  JPA 2.1 supported.
         * @param provider JPA provider class.
         */
        ApiVersion(boolean jpa_1_0, boolean jpa_2_0,
                boolean jpa_2_1, String provider) {
            this._1_0 = jpa_1_0;
            this._2_0 = jpa_2_0;
            this._2_1 = jpa_2_1;
            this.provider = provider;
        }

        ////////////////////////////////////////////////////////////////////////
        // Getters and setters                                                //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Is JPA 1.0 supported.
         * <p/>
         * @return Value of <code>true</code> when JPA 1.0 supported
         *         or <code>false</code> otherwise.
         */
        public boolean is10() {
            return _1_0;
        }

        /**
         * Is JPA 2.0 supported.
         * <p/>
         * @return Value of <code>true</code> when JPA 2.0 supported
         *         or <code>false</code> otherwise.
         */
        public boolean is20() {
            return _2_0;
        }

        /**
         * Is JPA 2.1 supported.
         * <p/>
         * @return Value of <code>true</code> when JPA 2.1 supported
         *         or <code>false</code> otherwise.
         */
        public boolean is21() {
            return _2_1;
        }

        /**
         * Get JPA provider class.
         * <p/>
         * @return JPA provider class name.
         */
        public String getProvider() {
            return provider;
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server JPA provider class since V3. */
    private static final String JPA_PROVIDER_SINCE_V3
            = "org.eclipse.persistence.jpa.PersistenceProvider";

    /**
     * Get GlassFish JPA support information for given GlassFish version.
     * <p/>
     * @param version GlassFish version to get JPA support information for.
     * @return GlassFish JPA support information for given GlassFish version.
     */
    public static ApiVersion getApiVersion(Version version)
    {
        if( version.matches( "[4" ) )
        {
            return new ApiVersion( true, true, true, JPA_PROVIDER_SINCE_V3 );
        }
        else
        {
            return new ApiVersion( true, true, false, JPA_PROVIDER_SINCE_V3 );
        }
    }

}
