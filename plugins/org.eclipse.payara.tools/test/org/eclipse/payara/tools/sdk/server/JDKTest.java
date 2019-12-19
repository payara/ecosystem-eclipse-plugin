/** ****************************************************************************
 * Copyright (c) 2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ***************************************************************************** */
package org.eclipse.payara.tools.sdk.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


/**
 * Common JDK version functional test.
 * <p>
 * @author Gaurav Gupta
 */
public class JDKTest {

    /**
     * Test to parse the JDK.Version.
     */
    @Test
    public void parseJDKVersion() {
        Map<String, JDK.Version> jdkVersions = new HashMap<>();
        jdkVersions.put("1.8",
                new JDK.Version( 1, 8, 0, 0, null));
        jdkVersions.put("1.8.0",
                new JDK.Version( 1, 8, 0, 0, null));
        jdkVersions.put("1.8.0u121",
                new JDK.Version( 1, 8, 0, 121, null));
        jdkVersions.put("1.8.0_191",
                new JDK.Version( 1, 8, 0, 191, null));
        jdkVersions.put("1.8.0_232-ea-8u232-b09-0ubuntu1-b09",
                new JDK.Version( 1, 8, 0, 232, null));
        jdkVersions.put("9",
                new JDK.Version( 9, 0, 0, 0, null));
        jdkVersions.put("11.0.6",
                new JDK.Version( 11, 0, 6, 0, null));
        jdkVersions.put("11.0.6_234",
                new JDK.Version( 11, 0, 6, 234, null));
        jdkVersions.put("11.0.6u234",
                new JDK.Version( 11, 0, 6, 234, null));

        for (Entry<String, JDK.Version> version : jdkVersions.entrySet()) {
            assertTrue(version.getKey(), JDK.getVersion(version.getKey()).equals(version.getValue()));
        }
    }
}
