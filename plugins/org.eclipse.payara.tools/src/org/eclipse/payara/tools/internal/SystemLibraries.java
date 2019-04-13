/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.internal;

import org.eclipse.sapphire.Version;

/**
 * This class supplies all the libraries that will be contributed to the classpath
 * when Payara is used as a target runtime.
 * 
 * <p>
 * This is typically only used for Eclipse proprietary projects. Maven projects for instance
 * handle their own dependency management.
 * 
 * @author Arjan Tijms
 *
 */
public class SystemLibraries {
    
    private static final String[] LIBRARIES_3_1 = {
            "glassfish/modules/javax.*.jar",
            "glassfish/modules/weld-osgi-bundle.jar",
            "glassfish/modules/bean-validator.jar",
            "glassfish/modules/jersey-*.jar",
            "glassfish/modules/grizzly-comet.jar",
            "glassfish/modules/grizzly-websockets.jar",
            "glassfish/modules/glassfish-api.jar",
            "glassfish/modules/ha-api.jar",
            "glassfish/modules/endorsed/*.jar",
            "glassfish/modules/jsf-api.jar",
            "glassfish/modules/jsf-impl.jar",
            "glassfish/modules/jstl-impl.jar",
            "glassfish/modules/org.eclipse.persistence*.jar",
            "glassfish/modules/jaxb*.jar",
            "glassfish/modules/webservices*.jar",
            "glassfish/modules/woodstox-osgi*.jar",
            "mq/lib/jaxm-api*.jar"
    };

    private static final String[] LIBRARIES_3_1_2 = {
            "glassfish/modules/javax.*.jar",
            "glassfish/modules/weld-osgi-bundle.jar",
            "glassfish/modules/bean-validator.jar",
            "glassfish/modules/jersey-*.jar",
            "glassfish/modules/grizzly-comet.jar", //
            "glassfish/modules/grizzly-websockets.jar", //
            "glassfish/modules/glassfish-api.jar",
            "glassfish/modules/ha-api.jar",
            "glassfish/modules/endorsed/*.jar",
            "glassfish/modules/org.eclipse.persistence*.jar",
            "glassfish/modules/jaxb*.jar",
            "glassfish/modules/webservices*.jar",
            "glassfish/modules/woodstox-osgi*.jar", //
            "mq/lib/jaxm-api*.jar"
    };

    private static final String[] LIBRARIES_4 = {
            "glassfish/modules/javax.*.jar",
            "glassfish/modules/weld-osgi-bundle.jar",
            "glassfish/modules/bean-validator.jar",
            "glassfish/modules/jersey-*.jar",
            "glassfish/modules/glassfish-api.jar",
            "glassfish/modules/ha-api.jar",
            "glassfish/modules/endorsed/*.jar",
            "glassfish/modules/org.eclipse.persistence*.jar",
            "glassfish/modules/jaxb*.jar",
            "glassfish/modules/webservices*.jar",
            "glassfish/modules/cdi-api.jar", // +
            "mq/lib/jaxm-api.jar"
    };

    private static final String[] LIBRARIES_5 = LIBRARIES_4;
    
    private static final String[] LIBRARIES_5_191 = {
            "glassfish/modules/javax.*.jar",
            "glassfish/modules/jakarta.*.jar",
            "glassfish/modules/weld-osgi-bundle.jar",
            "glassfish/modules/bean-validator.jar",
            "glassfish/modules/jersey-*.jar",
            "glassfish/modules/glassfish-api.jar",
            "glassfish/modules/ha-api.jar",
            "glassfish/modules/endorsed/*.jar",
            "glassfish/modules/org.eclipse.persistence*.jar",
            "glassfish/modules/jaxb*.jar",
            "glassfish/modules/webservices*.jar",
            "glassfish/modules/cdi-api.jar", // +
            "mq/lib/jaxm-api.jar"
    };
    
    public static String[] getLibraryIncludesByVersion(Version version) {
        if (version.matches("[5.191")) {
            return LIBRARIES_5_191;
        }
        
        if (version.matches("[5")) {
            return LIBRARIES_5;
        }

        if (version.matches("[4-5)")) {
            return LIBRARIES_4;
        }

        if (version.matches("[3.1.2-4)")) {
            return LIBRARIES_3_1_2;
        }

        if (version.matches("[3.1-3.1.2)")) {
            return LIBRARIES_3_1;
        }

        return null;
    }

}
