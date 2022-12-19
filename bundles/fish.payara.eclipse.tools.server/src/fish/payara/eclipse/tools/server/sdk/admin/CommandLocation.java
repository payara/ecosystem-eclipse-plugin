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

import java.io.File;
import java.net.URI;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.utils.ServerUtils;

/**
 * Locations command used to determine locations (installation, domain etc.) where the DAS is
 * running.
 * <p/>
 * Result of the command will be in the form of <code>Map<String, String></code> object. The keys to
 * particular locations are as followed: Installation root - "Base-Root_value" Domain root -
 * "Domain-Root_value"
 * <p/>
 * Minimal <code>__locations</code> command support exists since GlassFish 3.0.1 where both
 * Base-Root and Domain-Root values are returned.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner = RunnerHttpLocation.class)
@RunnerRestClass(runner = RunnerRestLocation.class)
public class CommandLocation extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for location command. */
    private static final String COMMAND = "__locations";

    /** Result key to retrieve <code>Domain-Root</code> value. */
    public static final String DOMAIN_ROOT_RESULT_KEY = "Domain-Root_value";

    /** Result key to retrieve <code>Basic-Root</code> value. */
    public static final String BASIC_ROOT_RESULT_KEY = "Base-Root_value";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Verifies if domain directory returned by location command result matches domain directory of
     * provided GlassFish server entity.
     * <p/>
     *
     * @param resultMap Locations command result.
     * @param server GlassFish server entity.
     * @return For local server value of <code>true</code> means that domain directory returned by
     * location command result matches domain directory of provided GlassFish server entity and value of
     * <code>false</code> that they differs. For remote serve this test makes no sense and value of
     * <code>true</code> is always returned.
     */
    public static boolean verifyResult(
            final ResultMap<String, String> resultMap,
            final PayaraServer server) {
        if (!server.isRemote()) {
            boolean result = false;
            String domainRootResult = resultMap.getValue().get(DOMAIN_ROOT_RESULT_KEY);
            String domainRootServer = ServerUtils.getDomainPath(server);
            if (domainRootResult != null && domainRootServer != null) {
                URI rootResult = new File(domainRootResult).toURI().normalize();
                URI rootServer = new File(domainRootServer).toURI().normalize();
                if (rootResult != null && rootServer != null) {
                    result = rootServer.equals(rootResult);
                }
            }
            return result;
        } else {
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server location command entity.
     */
    public CommandLocation() {
        super(COMMAND);
    }

}
