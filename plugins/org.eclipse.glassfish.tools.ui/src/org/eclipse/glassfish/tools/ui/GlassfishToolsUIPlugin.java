/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.internal.ResourceManager;
import org.eclipse.wst.server.core.internal.UpdateServerJob;

import org.eclipse.glassfish.tools.GlassFishServer;

@SuppressWarnings("restriction")
public class GlassfishToolsUIPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.glassfish.tools.ui";
	
	public static final String EAR_MODULE_IMG = "ear.img";
	public static final String EJB_MODULE_IMG = "ejb.img";
	public static final String GF_SERVER_IMG = "gf-server.img";
	public static final String LOG_FILE_IMG = "log-file.img";
	public static final String UPDATE_CENTER_IMG = "update-center.img";
	public static final String WEB_MODULE_IMG = "web.img";
	public static final String WEBSERVICE_IMG = "webservice.img";
	public static final String RESOURCES_IMG = "resources.img";
	public static final String GF_WIZARD = "wizard.img";
	
	private static GlassfishToolsUIPlugin instance;
	
	public GlassfishToolsUIPlugin() {
		instance = this;
		addServerLifecycleListener();
	}
	
	/**
	 * Start a UpdateServerJob to update status when GF server is added 
	 */
	private void addServerLifecycleListener() {

		IServerLifecycleListener serverLifecycleListener = new IServerLifecycleListener() {
			public void serverAdded(IServer server) {
				if (server.loadAdapter(GlassFishServer.class, new NullProgressMonitor())!=null) {
					if (server.getServerState() == IServer.STATE_UNKNOWN) {
						UpdateServerJob job = new UpdateServerJob(new IServer[] { server });
						job.schedule();
					}
				}
			}

			public void serverChanged(IServer server) {
			}

			public void serverRemoved(IServer server) {
			}
		};

		ResourceManager.getInstance().addServerLifecycleListener(
				serverLifecycleListener);
	}

	public static final GlassfishToolsUIPlugin getInstance() {
		return instance;
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		
		reg.put(EAR_MODULE_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/ear.gif")));
		reg.put(EJB_MODULE_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/ejb_module.gif")));
		reg.put(GF_SERVER_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/glassfishserver.gif")));
		reg.put(LOG_FILE_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/logfile.gif")));
		reg.put(UPDATE_CENTER_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/updateCenter.png")));
		reg.put(WEB_MODULE_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/web_module.gif")));
		reg.put(WEBSERVICE_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/webservice.png")));
		reg.put(RESOURCES_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/resources.gif")));
		reg.put(GF_WIZARD, ImageDescriptor.createFromURL(getBundle().getEntry("icons/wizard75x66.png")));
	}
	
	
}
