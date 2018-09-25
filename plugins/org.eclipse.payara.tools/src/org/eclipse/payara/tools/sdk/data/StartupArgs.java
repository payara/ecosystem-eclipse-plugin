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

package org.eclipse.payara.tools.sdk.data;

import java.util.List;
import java.util.Map;

/**
 * This interface provides IDE and user specific arguments for starting the server.
 *
 */
public interface StartupArgs {

    /** Command line arguments passed to bootstrap jar. */
    public List<String> getGlassfishArgs();

    /** Command line arguments passed to JVM. */
    public List<String> getJavaArgs();

    /** Environment variables set before JVM execution. */
    public Map<String, String> getEnvironmentVars();

    /** Installation home of Java SDK used to run GlassFish. */
    public String getJavaHome();

    /** Whether to replace or add getAdditionalEnvironmentVars() to the native Environment 
     * variables before JVM execution. */
    public boolean isReplaceNativeEnvironment();

}
