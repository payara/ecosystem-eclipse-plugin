/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.handlers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.wst.server.core.IServer;

public class GlassFishStateTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		IServer server = (IServer) receiver;
		if( property.equals("isRunning"))
			return (server.getServerState() == IServer.STATE_STARTED);
		if( property.equals("isRemote")){
			GlassFishServer gf = (GlassFishServer)server.loadAdapter(GlassFishServer.class,
					new NullProgressMonitor());
			if( gf!=null)
				return gf.isRemote();
		}
//		SunAppServer sunServer = (SunAppServer)server.getAdapter(SunAppServer.class);
//		if (sunServer == null) {
//			sunServer = (SunAppServer) server.loadAdapter(SunAppServer.class, new NullProgressMonitor());
//		}
//		try {
//			return sunServer.isRunning();
//		} catch (CoreException e) {
//			SunAppSrvPlugin.logMessage("Testing server state failed", e);
//		}
		return false;
	}

}
