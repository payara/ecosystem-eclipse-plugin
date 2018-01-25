/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.Element;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.Server;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;
import org.eclipse.glassfish.tools.IGlassfishServerModel;

@SuppressWarnings("restriction")
public final class GlassfishSapphireServerWizardFragment extends GlassfishSapphireWizardFragment
{
	@Override
	protected String getTitle() {
		return server().getServerType().getName();
	}
	
	private IServerWorkingCopy server(){
		IServerWorkingCopy server = (IServerWorkingCopy) getTaskModel().getObject( TaskModel.TASK_SERVER );
		return server;
	}

	@Override
	protected String getDescription() {
		return GlassfishWizardResources.wzdServerDescription;
	}

	
	@Override
	protected Element getModel() {
        try
        {
            server().setAttribute( Server.PROP_AUTO_PUBLISH_SETTING, Server.AUTO_PUBLISH_DISABLE );
        }
        catch( Exception e )
        {
            GlassfishToolsPlugin.log( e);
        }
        
        GlassFishServer serverDelegate = (GlassFishServer) server().loadAdapter(GlassFishServer.class, null);
        return serverDelegate.getModel();
	}

	@Override
	protected String getUserInterfaceDef() {
		return "glassfish.server";
	}

	@Override
	public void enter() {
		super.enter();
		
		final IGlassfishServerModel model = (IGlassfishServerModel) getModel();
		
        // Set the default domain location
        
        final IPath runtimeLocation = server().getRuntime().getLocation();
        final IPath defaultDomainLocation = GlassFishServer.getDefaultDomainDir( runtimeLocation );
        model.getDomainPath().write( defaultDomainLocation.toOSString() );
	}

	@Override
	protected String getInitialFocus() {
	    return IGlassfishServerModel.PROP_NAME.name();
	}
	
}
