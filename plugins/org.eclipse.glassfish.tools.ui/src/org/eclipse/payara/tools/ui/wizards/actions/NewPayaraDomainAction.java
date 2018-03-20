/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.wizards.actions;

import static org.eclipse.payara.tools.utils.WtpUtil.load;
import static org.eclipse.sapphire.modeling.Path.fromPortableString;
import static org.eclipse.sapphire.ui.def.DefinitionLoader.context;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.payara.tools.sapphire.ICreateGlassfishDomainOp;
import org.eclipse.payara.tools.sapphire.IGlassfishServerModel;
import org.eclipse.payara.tools.server.GlassFishRuntime;
import org.eclipse.payara.tools.server.GlassFishServer;
import org.eclipse.payara.tools.ui.wizards.BaseWizardFragment;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.forms.swt.SapphireWizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;

/**
 * Action that is invoked when the user clicks on the "new domain" icon next to the "Domain Path"
 * input field in the "new server" wizard.
 *
 */
public class NewPayaraDomainAction extends SapphireActionHandler {

    @Override
    protected Object run(Presentation context) {
        IRuntime runtime = load(context.part().getModelElement().adapt(IServerWorkingCopy.class), GlassFishServer.class)
                .getServer()
                .getRuntime();

        ICreateGlassfishDomainOp createDomainOperation = ICreateGlassfishDomainOp.TYPE.instantiate();

        // Set existing domain location
        createDomainOperation.setLocation(
                fromPortableString(runtime.getLocation().toPortableString()));

        // Set existing JDK location
        createDomainOperation.setJavaLocation(
                load(runtime, GlassFishRuntime.class).getVMInstall().getInstallLocation().getAbsolutePath());

        // Explicitly open Sapphire dialog that asks the user to fill out fields for new domain
        WizardDialog dlg = new WizardDialog(
                Display.getDefault().getActiveShell(),
                new SapphireWizard<>(
                        createDomainOperation,
                        context(BaseWizardFragment.class)
                                .sdef("org.eclipse.payara.tools.ui.PayaraUI")
                                .wizard("new-domain-wizard")));

        // If user okay'ed dialog, copy the provided values to our model
        if (dlg.open() == Dialog.OK) {
            IGlassfishServerModel model = (IGlassfishServerModel) context.part().getModelElement();

            model.setDomainPath(createDomainOperation.getDomainDir().content().append(createDomainOperation.getName().content()));
            model.setDebugPort(createDomainOperation.getPortBase().content() + 9);
        }

        createDomainOperation.dispose();

        return null;
    }

}
