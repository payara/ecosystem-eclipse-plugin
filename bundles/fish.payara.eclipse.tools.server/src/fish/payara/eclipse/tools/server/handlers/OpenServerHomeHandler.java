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

package fish.payara.eclipse.tools.server.handlers;

import static fish.payara.eclipse.tools.server.PayaraServerPlugin.logMessage;
import static fish.payara.eclipse.tools.server.utils.URIHelper.getServerHomeURI;
import static fish.payara.eclipse.tools.server.utils.URIHelper.showURI;
import static fish.payara.eclipse.tools.server.utils.WtpUtil.load;

import org.eclipse.wst.server.core.IServer;

import fish.payara.eclipse.tools.server.deploying.PayaraServerBehaviour;

public class OpenServerHomeHandler extends AbstractPayaraSelectionHandler {

    @Override
    public void processSelection(IServer server) {
        try {
            showURI(getServerHomeURI(load(server, PayaraServerBehaviour.class).getPayaraServerDelegate()));
        } catch (Exception e) {
            logMessage("Error opening folder in desktop " + e.getMessage());
        }
    }

}
