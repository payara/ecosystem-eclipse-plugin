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

package org.eclipse.payara.tools.handlers;

import static org.eclipse.payara.tools.PayaraToolsPlugin.logMessage;
import static org.eclipse.payara.tools.utils.URIHelper.getDomainHomeURI;
import static org.eclipse.payara.tools.utils.URIHelper.showURI;
import static org.eclipse.payara.tools.utils.WtpUtil.load;

import org.eclipse.payara.tools.server.deploying.PayaraServerBehaviour;
import org.eclipse.wst.server.core.IServer;

public class OpenDomainHomeHandler extends AbstractPayaraSelectionHandler {

    @Override
    public void processSelection(IServer server) {
        try {
            showURI(getDomainHomeURI(load(server, PayaraServerBehaviour.class).getPayaraServerDelegate()));
        } catch (Exception e) {
            logMessage("Error opening browser: " + e.getMessage());
        }
    }

}
