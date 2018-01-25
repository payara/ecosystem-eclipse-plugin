/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.serverview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;
import org.eclipse.glassfish.tools.serverview.WSDesc;
import org.eclipse.glassfish.tools.utils.NodesUtils;

public class DeployedWebServicesNode extends TreeNode {

	GlassFishServer server = null;
	WebServiceNode[] deployedapps = null;

	public DeployedWebServicesNode(GlassFishServer server) {
		super("Deployed Web Services", null, null);
		this.server = server;

	}

	public GlassFishServer getServer() {
		return this.server;
	}

	public Object[] getChildren() {

		ArrayList<WebServiceNode> appsList = new ArrayList<WebServiceNode>();
		if (this.deployedapps == null) {
			try {
				if (server == null) {
					this.deployedapps = appsList
							.toArray(new WebServiceNode[appsList
									.size()]);
					return this.deployedapps;
				}

				try {
					List<WSDesc> wss = NodesUtils.getWebServices(server);
							

						for (WSDesc app : wss) {
							WebServiceNode t = new WebServiceNode(this, server,
									app);

							appsList.add(t);
						}
				
				} catch (Exception ex) {
					GlassfishToolsPlugin.logError("get Applications is failing=", ex); //$NON-NLS-1$

				}
			} catch (Exception e) {
			}
			this.deployedapps = appsList
					.toArray(new WebServiceNode[appsList.size()]);
		}

		return this.deployedapps;
	}

	public void refresh() {
		this.deployedapps = null;
	}



}
