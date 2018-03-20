/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.handlers;

import static org.eclipse.payara.tools.GlassfishToolsPlugin.logMessage;
import static org.eclipse.payara.tools.utils.URIHelper.getServerHomeURI;
import static org.eclipse.payara.tools.utils.URIHelper.showURI;
import static org.eclipse.payara.tools.utils.WtpUtil.load;

import org.eclipse.payara.tools.server.deploying.GlassFishServerBehaviour;
import org.eclipse.wst.server.core.IServer;

public class OpenServerHomeHandler extends AbstractGlassfishSelectionHandler {

	@Override
	public void processSelection(IServer server) {
		try {
			showURI(getServerHomeURI(load(server, GlassFishServerBehaviour.class).getGlassfishServerDelegate()));
		} catch (Exception e) {
			logMessage("Error opening folder in desktop " + e.getMessage());
		}
	}

}
