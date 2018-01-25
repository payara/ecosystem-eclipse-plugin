/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.handlers;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.GlassFishServerBehaviour;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.wst.server.core.IServer;

public class OpenServerHomeHandler extends AbstractGlassfishSelectionHandler {

	@Override
	public void processSelection(IStructuredSelection selection) {
		IServer server = (IServer) selection.getFirstElement();
		if (server != null){
			GlassFishServerBehaviour sab = (GlassFishServerBehaviour)server.loadAdapter(
					GlassFishServerBehaviour.class, null);
			GlassFishServer sunserver = sab.getGlassfishServerDelegate();
			try {
				//showPageInDefaultBrowser(AdminURLHelper.getURL("", server));
				URIHelper.showURI(URIHelper.getServerHomeURI(sunserver));
			} catch (Exception e) {
		           GlassfishToolsPlugin.logMessage("Error opening browser: "+e.getMessage());
			}
	    }
	}

}
