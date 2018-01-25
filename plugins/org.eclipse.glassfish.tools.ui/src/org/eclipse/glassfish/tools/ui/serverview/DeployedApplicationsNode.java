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
import java.util.Map.Entry;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;
import org.eclipse.glassfish.tools.serverview.AppDesc;
import org.eclipse.glassfish.tools.utils.NodesUtils;

public class DeployedApplicationsNode extends TreeNode {

	GlassFishServer server = null;
	ApplicationNode[] deployedapps = null;

	public DeployedApplicationsNode(GlassFishServer server) {
		super("Deployed Applications", null, null);
		this.server = server;

	}

	public GlassFishServer getServer() {
		return this.server;
	}

	public Object[] getChildren() {

		ArrayList<ApplicationNode> appsList = new ArrayList<ApplicationNode>();
		if (this.deployedapps == null) {

			try {
				if (server == null) {
					this.deployedapps = appsList
							.toArray(new ApplicationNode[appsList
									.size()]);
					return this.deployedapps;
				}

				try {
					java.util.Map<String, List<AppDesc>> appMap = NodesUtils.getApplications(server, null);
					for (Entry<String, List<AppDesc>> entry : appMap.entrySet()) {
						List<AppDesc> apps = entry.getValue();
						for (AppDesc app : apps) {
							ApplicationNode t = new ApplicationNode(this, server,
									app);

							appsList.add(t);
						}
					}
				} catch (Exception ex) {
					GlassfishToolsPlugin.logError("get Applications is failing=", ex); //$NON-NLS-1$

				}
			} catch (Exception e) {
			}
			this.deployedapps = appsList
					.toArray(new ApplicationNode[appsList.size()]);
		}

		return this.deployedapps;
	}

	public void refresh() {
		this.deployedapps = null;
	}



}
