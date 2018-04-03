/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.wizards;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.swt.SWT.NONE;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.payara.tools.ui.PayaraToolsUIPlugin;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.swt.SapphireForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

public abstract class BaseWizardFragment extends WizardFragment {

    private IWizardHandle wizard;
    private Composite composite;
    private SapphireForm form;

    public BaseWizardFragment() {
        setComplete(false);
    }

    @Override
    public final boolean hasComposite() {
        return true;
    }

    protected abstract String getTitle();

    protected abstract String getDescription();

    protected abstract Element getModel();

    protected abstract String getUserInterfaceDef();

    protected abstract String getInitialFocus();

    @Override
    public Composite createComposite(final Composite parent, final IWizardHandle handle) {
        this.wizard = handle;

        this.wizard.setTitle(getTitle());
        this.wizard.setDescription(getDescription());
        this.wizard.setImageDescriptor(getImageDescriptor());

        this.composite = new Composite(parent, NONE);
        this.composite.setLayout(glayout(1, 0, 0));

        render();

        return this.composite;
    }

    @Override
    public void enter() {
        super.enter();

        // We need to render new UI every time the page is entered since switching host
        // between
        // localhost and remote on the server type selection screen after initially
        // entering
        // this page will associated a new server working copy with this page. That is,
        // we cannot
        // depend on the working copy being constant between repeated invocations of
        // this method
        // as users navigates backwards in the wizard and re-enters this page.

        render();
    }

    protected Composite render() {
        if (this.form != null) {
            this.form.dispose();
        }

        this.form = new SapphireForm(this.composite, getModel(),
                DefinitionLoader.context(BaseWizardFragment.class)
                        .sdef("org.eclipse.payara.tools.ui.PayaraUI").form(getUserInterfaceDef()));

        this.form.part().attach(new FilteredListener<PartValidationEvent>() {
            @Override
            protected void handleTypedEvent(final PartValidationEvent event) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        refreshStatus();
                    }
                });
            }
        });

        this.form.setLayoutData(gdfill());
        this.form.part().setFocus(getInitialFocus());
        this.composite.layout(true, true);

        refreshStatus();

        return this.form;
    }

    private void refreshStatus() {
        final Status status = this.form.part().validation();

        if (status.severity() == Severity.ERROR) {
            this.wizard.setMessage(status.message(), IMessageProvider.ERROR);
            setComplete(false);
        } else if (status.severity() == Severity.WARNING) {
            this.wizard.setMessage(status.message(), IMessageProvider.WARNING);
            setComplete(true);
        } else {
            this.wizard.setMessage(null, IMessageProvider.NONE);
            setComplete(true);
        }

        this.wizard.update();
    }

    protected ImageDescriptor getImageDescriptor() {
        return PayaraToolsUIPlugin.getInstance().getImageRegistry().getDescriptor(PayaraToolsUIPlugin.GF_WIZARD);
    }

}
