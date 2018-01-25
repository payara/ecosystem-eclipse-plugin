/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.swt.SapphireForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;

import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.GlassFishServerBehaviour;
import org.eclipse.glassfish.tools.IGlassfishServerModel;
import org.eclipse.glassfish.tools.ui.wizards.GlassfishSapphireWizardFragment;

@SuppressWarnings("restriction")
public class ServerPropertyPage extends PropertyPage {

	IServerWorkingCopy serverWC = null;
	GlassFishServer sunserver;
	IGlassfishServerModel model;
	
	FilteredListener<PropertyValidationEvent> listener = new FilteredListener<PropertyValidationEvent>() {
		@Override
		protected void handleTypedEvent(final PropertyValidationEvent event) {
			refreshStatus();
		}
	};


	@Override
	protected Control createContents(Composite parent) {

		IServer server = (IServer) getElement();
		if (server instanceof IServerWorkingCopy)
			serverWC = (IServerWorkingCopy) server;
		else
			serverWC = server.createWorkingCopy();

		sunserver = (GlassFishServer) serverWC.loadAdapter(GlassFishServer.class,
				new NullProgressMonitor());
		model = sunserver.getModel();

		model.attach(listener, "*");

		final SapphireForm control = new SapphireForm(parent, model, DefinitionLoader
				.context(GlassfishSapphireWizardFragment.class).sdef("org.eclipse.glassfish.tools.ui.GlassfishUI").form("glassfish.server"));

		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		refreshStatus();
		
		return control;

	}

	private void refreshStatus() {
		final Status status = model.validation();

		if (status.severity() == Severity.ERROR) {
			setMessage(status.message(), IMessageProvider.ERROR);
			setValid(false);
		} else if (status.severity() == Severity.WARNING) {
			setMessage(status.message(), IMessageProvider.WARNING);
			setValid(true);
		} else {
			setMessage(null, IMessageProvider.NONE);
			setValid(true);
		}

	}

	

	// note that this is currently not working due to issue 140
//	public void propertyChange(PropertyChangeEvent evt) {
//		if (AbstractGlassfishServer.DOMAINUPDATE == evt.getPropertyName()) {
//			username.setText(sunserver.getAdminUser());
//			password.setText(sunserver.getAdminPassword());
//			adminServerPortNumber.setText(Integer.toString(sunserver.getAdminPort()));
//			serverPortNumber.setText(Integer.toString(sunserver.getPort()));
//		}
//	}

	@Override
	public boolean isValid() {
		return super.isValid();
	}

	@Override
	public boolean performCancel() {
		model.detach(listener, "*");
		return super.performCancel();
	}

	@Override
	protected void performApply() {
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			final IServer server = serverWC.save(true, monitor);
			// sunserver.saveConfiguration(monitor);
			// oldProps = new HashMap<String, String>(sunserver.getProps());
			Job job = new Job("Update Glassfish server state") { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						GlassFishServerBehaviour serverBehavior = (GlassFishServerBehaviour) serverWC
								.loadAdapter(GlassFishServerBehaviour.class, monitor);
						serverBehavior.updateServerStatus();
						
						Server gfServer = (Server)server;
						gfServer.setServerPublishState(IServer.PUBLISH_CLEAN);
						
					} catch (Exception e) {
						((Server) server).setServerState(IServer.STATE_STOPPED);
					}
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};

			job.schedule();
		} catch (CoreException e) {
			// no-op
			e.printStackTrace();
		}
	}

	@Override
	public boolean performOk() {
		model.detach(listener, "*");
		performApply();
		return true;
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		serverWC.setAttribute(GlassFishServer.ATTR_ADMIN, "");
		serverWC.setAttribute(GlassFishServer.ATTR_ADMINPASS, "");
		serverWC.setAttribute(GlassFishServer.ATTR_DOMAINPATH, GlassFishServer.getDefaultDomainDir(
				serverWC.getRuntime().getLocation()).toString());
		serverWC.setAttribute(GlassFishServer.ATTR_ADMINPORT, "");
		serverWC.setAttribute(GlassFishServer.ATTR_DEBUG_PORT, "");
		model.refresh();
	}

}
