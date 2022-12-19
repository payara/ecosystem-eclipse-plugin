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

package fish.payara.eclipse.tools.server.handlers;

import static fish.payara.eclipse.tools.server.utils.WtpUtil.load;
import static org.eclipse.wst.server.core.IServer.STATE_STARTED;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.wst.server.core.IServer;

import fish.payara.eclipse.tools.server.PayaraServer;

public class PayaraStateTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IServer server = (IServer) receiver;

        if (property.equals("isRunning")) {
            return (server.getServerState() == STATE_STARTED);
        }

        if (property.equals("isRemote")) {
            PayaraServer payaraServer = load(server, PayaraServer.class);

            if (payaraServer != null) {
                return payaraServer.isRemote();
            }
        }

        return false;
    }

}
