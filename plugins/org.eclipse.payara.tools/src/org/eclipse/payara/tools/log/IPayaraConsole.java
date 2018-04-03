/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.log;

import org.eclipse.payara.tools.sdk.server.FetchLog;
import org.eclipse.ui.console.IConsole;

public interface IPayaraConsole extends IConsole {

    public void startLogging();

    public void startLogging(FetchLog... logFetchers);

    public void stopLogging();

    public void stopLogging(int afterSeconds);

    public boolean isLogging();

    public void setLogFilter(ILogFilter filter);

}
