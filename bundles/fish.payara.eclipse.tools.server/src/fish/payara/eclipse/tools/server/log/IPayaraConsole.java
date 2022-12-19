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

package fish.payara.eclipse.tools.server.log;

import org.eclipse.ui.console.IConsole;

import fish.payara.eclipse.tools.server.sdk.server.FetchLog;

public interface IPayaraConsole extends IConsole {

    void startLogging();

    void startLogging(FetchLog... logFetchers);

    void setLogFilter(ILogFilter filter);

    boolean isLogging();

    boolean hasLogged();

    boolean hasLoggedPayara();

    void stopLogging();

    void stopLogging(int afterSeconds);





}
