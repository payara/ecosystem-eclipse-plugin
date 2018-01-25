/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools;

import org.eclipse.glassfish.tools.utils.Utils;
import org.eclipse.wst.server.core.IServer;

public class AdminURLHelper {
	private static final String fallbackHost = "localhost";  //$NON-NLS-1$
	private static final int fallbackPort = 4848;       //$NON-NLS-1$

	private AdminURLHelper() {
		super();
	}

	public static String getURL(String urlSuffix, IServer server) {
		String hostName = fallbackHost;
		int portNumber = fallbackPort;

	    if (server != null){
			GlassFishServerBehaviour sab = (GlassFishServerBehaviour)server.loadAdapter(
					GlassFishServerBehaviour.class, null);
			GlassFishServer sunserver = sab.getGlassfishServerDelegate();
			hostName = sunserver.getServer().getHost();
			portNumber = sunserver.getAdminPort();
	    }
	    return Utils.getHttpListenerProtocol(hostName, portNumber) + "://" + hostName + ":" + portNumber + urlSuffix; //$NON-NLS-1$
	}
}
