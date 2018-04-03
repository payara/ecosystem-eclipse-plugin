/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.handlers;

import static org.eclipse.payara.tools.utils.WtpUtil.load;

import java.io.File;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.wst.server.core.IServer;

public class PayaraVersionTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        return new File(load((IServer) receiver, PayaraServer.class).getServerInstallationDirectory() + "/modules").exists();
    }

}
