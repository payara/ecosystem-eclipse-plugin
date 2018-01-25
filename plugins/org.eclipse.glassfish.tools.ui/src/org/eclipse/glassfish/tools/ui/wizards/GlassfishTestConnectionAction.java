/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.common.frameworks.internal.dialog.ui.MessageDialog;
import org.eclipse.wst.server.core.IServerWorkingCopy;

import org.eclipse.glassfish.tools.GlassFishRuntime;
import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.GlassFishServerBehaviour;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;
import org.eclipse.glassfish.tools.Messages;
import org.eclipse.glassfish.tools.ServerStatus;
import org.eclipse.glassfish.tools.utils.ServerStatusHelper;

@SuppressWarnings("restriction")
public class GlassfishTestConnectionAction extends SapphireActionHandler {

	@Override
	protected Object run( final Presentation context ) {
		IServerWorkingCopy wc = context.part().getModelElement().adapt(IServerWorkingCopy.class);
		GlassFishServer glassfish = (GlassFishServer)wc.loadAdapter(GlassFishServer.class, null);
		ServerStatus s = ServerStatusHelper.checkServerStatus(glassfish);

		if (!s.equals(ServerStatus.RUNNING_DOMAIN_MATCHING)) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("Cannot communicate with ");
			errorMessage.append(glassfish.getServer().getHost());
			errorMessage.append(":");
			errorMessage.append(glassfish.getAdminPort());
			errorMessage.append(" remote server.");

			// give some hints
			if (s.equals(ServerStatus.STOPPED_NOT_LISTENING)) {
				errorMessage.append(" Is it up?");
			} else if (s.equals(ServerStatus.RUNNING_REMOTE_NOT_SECURE)) {
				errorMessage
						.append(" Is it secure? (Hint: run asadmin enable-secure-admin)");
			} else if (s
					.equals(ServerStatus.RUNNING_CREDENTIAL_PROBLEM)) {
				errorMessage
						.append(" Wrong user name or password. Check your credentials.");
			} else if (s.equals(ServerStatus.RUNNING_PROXY_ERROR)) {
				errorMessage.append(" Check your proxy settings.");
			} else if (s.equals(ServerStatus.RUNNING_CONNECTION_ERROR)) {
				// add all possible hints
				errorMessage.append(" Is it up?");
				errorMessage
						.append(" Is it secure? (Hint: run asadmin enable-secure-admin)");
			}

			IStatus status = new Status(IStatus.ERROR,
					GlassfishToolsPlugin.SYMBOLIC_NAME,
					errorMessage.toString());

			MessageDialog.openMessage(Display.getDefault().getActiveShell(), "Error", "Error connecting to remote server", status);
		
		} else {
			//Check server version
			String remoteServerVersion = GlassFishServerBehaviour.getVersion(glassfish); 
			GlassFishRuntime gfRuntime =  wc.getRuntime().getAdapter(GlassFishRuntime.class);
			String thisServerVersion = gfRuntime.getVersion().toString();
			int n = thisServerVersion.indexOf(".X");
			if( n>0 )
				thisServerVersion = thisServerVersion.substring(0,n+1);
			if( remoteServerVersion!=null && remoteServerVersion.indexOf(thisServerVersion) <0 ){
				String errorMessage = "The remote server version is " + remoteServerVersion;
				IStatus status = new Status(IStatus.ERROR,
						GlassfishToolsPlugin.SYMBOLIC_NAME,
						errorMessage.toString());
				MessageDialog.openMessage(Display.getDefault().getActiveShell(), "Error", Messages.versionsNotMatching, status);
			} else {
				// everything seems to be OK
				MessageDialog.openMessage(Display.getDefault().getActiveShell(), "Connection successful", "Connection to server was successful", 
						new Status(IStatus.INFO, GlassfishToolsPlugin.SYMBOLIC_NAME, "Connection to server was successful"));
			}
		}
		
		return null;
		
	}

}
