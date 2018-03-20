/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.wizards;

import static org.eclipse.payara.tools.GlassfishToolsPlugin.log;
import static org.eclipse.payara.tools.sapphire.IGlassfishServerModel.PROP_NAME;
import static org.eclipse.payara.tools.server.GlassFishServer.getDefaultDomainDir;
import static org.eclipse.payara.tools.ui.wizards.GlassfishWizardResources.wzdServerDescription;
import static org.eclipse.payara.tools.utils.WtpUtil.load;
import static org.eclipse.wst.server.core.TaskModel.TASK_SERVER;
import static org.eclipse.wst.server.core.internal.Server.AUTO_PUBLISH_DISABLE;
import static org.eclipse.wst.server.core.internal.Server.PROP_AUTO_PUBLISH_SETTING;

import org.eclipse.payara.tools.sapphire.IGlassfishServerModel;
import org.eclipse.payara.tools.server.GlassFishServer;
import org.eclipse.sapphire.Element;
import org.eclipse.wst.server.core.IServerWorkingCopy;

/**
 * This wizard fragment plugs-in the wizard flow when <code>Servers -> New Server -> Payara -> Payara</code>
 * is selected and subsequently the <code>next</code> button is pressed. 
 * 
 * <p>
 * This fragment essentially causes the screen with <code>Name</code>, <code>Host name</code>, <code>Domain path</code>
 * etc to be rendered, although a lot of the actual work is delegated by the {@link BaseWizardFragment} to Sapphire.
 * The UI layout for this wizard fragment is specified in the file <code>GlassfishUI.sdef</code> in the "payara.server"
 * section.
 *
 */
@SuppressWarnings("restriction")
public final class NewPayaraServerWizardFragment extends BaseWizardFragment {
	
	@Override
	protected String getTitle() {
		return server().getServerType().getName();
	}
	
	@Override
	protected String getDescription() {
		return wzdServerDescription;
	}
	
	/**
	 * The section in <code>GlassfishUI.sdef</code> that contains the UI layout for this wizard fragment.
	 */
	@Override
	protected String getUserInterfaceDef() {
		return "payara.server";
	}

	private IServerWorkingCopy server() {
		return (IServerWorkingCopy) getTaskModel().getObject(TASK_SERVER);
	}

	@Override
	protected Element getModel() {
		try {
			server().setAttribute(PROP_AUTO_PUBLISH_SETTING, AUTO_PUBLISH_DISABLE);
		} catch (Exception e) {
			log(e);
		}

		return load(server(), GlassFishServer.class).getModel();
	}

	@Override
	public void enter() {
		super.enter();

		// Set the default domain location
		((IGlassfishServerModel) getModel())
			.getDomainPath()
			.write(getDefaultDomainDir(server().getRuntime().getLocation()).toOSString());
	}

	@Override
	protected String getInitialFocus() {
		return PROP_NAME.name();
	}

}
