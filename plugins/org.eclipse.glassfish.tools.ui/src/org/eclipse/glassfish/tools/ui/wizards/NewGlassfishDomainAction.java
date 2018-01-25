/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.wizards;

import org.eclipse.glassfish.tools.GlassFishRuntime;
import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.ICreateGlassfishDomainOp;
import org.eclipse.glassfish.tools.IGlassfishServerModel;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.swt.SapphireWizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;

public class NewGlassfishDomainAction extends SapphireActionHandler {

	@Override
	protected Object run(Presentation context) {
		IServerWorkingCopy wc = context.part().getModelElement()
				.adapt(IServerWorkingCopy.class);
		GlassFishServer glassfish = (GlassFishServer) wc
				.loadAdapter(GlassFishServer.class, null);
		IRuntime rt = glassfish.getServer().getRuntime();
		
		Path root = Path.fromPortableString(rt.getLocation().toPortableString()); //runtimeModel.getServerRoot().content();

		ICreateGlassfishDomainOp op = ICreateGlassfishDomainOp.TYPE
				.instantiate();
		op.setLocation(root);
		
		GlassFishRuntime glassfishRt = (GlassFishRuntime)rt.loadAdapter(GlassFishRuntime.class,null);
		String vmLocation = glassfishRt.getVMInstall().getInstallLocation().getAbsolutePath();
		op.setJavaLocation(vmLocation);

		final SapphireWizard<ICreateGlassfishDomainOp> wizard = new SapphireWizard<ICreateGlassfishDomainOp>(
				op, DefinitionLoader.context(GlassfishSapphireWizardFragment.class)
				.sdef("org.eclipse.glassfish.tools.ui.GlassfishUI").wizard("new-domain-wizard"));

		WizardDialog dlg = new WizardDialog(Display.getDefault()
				.getActiveShell(), wizard);
		if (dlg.open() == Dialog.OK) {
			IGlassfishServerModel model = (IGlassfishServerModel )context.part().getModelElement();
			Path domainPath = op.getDomainDir().content().append(op.getName().content());
			model.setDomainPath( domainPath );
			Integer portBase = op.getPortBase().content();
			model.setDebugPort( portBase + 9 );
		}
		op.dispose();

		return null;
	}

}
