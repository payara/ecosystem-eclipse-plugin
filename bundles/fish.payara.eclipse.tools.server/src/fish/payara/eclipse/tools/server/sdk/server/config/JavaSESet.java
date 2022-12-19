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

package fish.payara.eclipse.tools.server.sdk.server.config;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Container of GlassFish JavaSE features configuration.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JavaSESet extends JavaSet {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Platforms retrieved from XML elements. */
    private final List<String> platforms;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of container of GlassFish JavaSE features configuration.
     * <p/>
     *
     * @param platforms Platforms retrieved from XML elements.
     * @param version Highest JavaSE specification version implemented.
     */
    public JavaSESet(final List<String> platforms, final String version) {
        super(version);
        this.platforms = platforms;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get platforms retrieved from XML elements.
     * <p/>
     *
     * @return Platforms retrieved from XML elements.
     */
    public List<String> getPlatforms() {
        return platforms;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build {@link Set} of {@link JavaSEPlatform} for known platforms retrieved from XML elements.
     * <p/>
     *
     * @return {@link Set} of {@link JavaSEPlatform} for known platforms.
     */
    public Set<JavaSEPlatform> platforms() {
        int size = platforms != null ? platforms.size() : 0;
        EnumSet<JavaSEPlatform> platformsSet = EnumSet.noneOf(JavaSEPlatform.class);
        if (size > 0) {
            for (String name : platforms) {
                JavaSEPlatform type = JavaSEPlatform.toValue(name);
                if (type != null) {
                    platformsSet.add(type);
                }
            }
        }
        return platformsSet;
    }

}
