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

package fish.payara.eclipse.tools.server.sdk.data;

import java.util.List;
import java.util.Map;

/**
 * This interface provides IDE and user specific arguments for starting the server.
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface StartupArgs {

    public List<String> getGlassfishArgs();

    public List<String> getJavaArgs();

    public Map<String, String> getEnvironmentVars();

    public String getJavaHome();

}
