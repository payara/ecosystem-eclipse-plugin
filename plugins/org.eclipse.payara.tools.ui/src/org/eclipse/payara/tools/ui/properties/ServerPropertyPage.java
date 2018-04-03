/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.properties;

import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_ADMIN;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_ADMINPASS;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_ADMINPORT;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_DEBUG_PORT;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_DOMAINPATH;
import static org.eclipse.payara.tools.server.PayaraServer.getDefaultDomainDir;
import static org.eclipse.swt.SWT.FILL;
import static org.eclipse.wst.server.core.IServer.PUBLISH_CLEAN;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPED;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.payara.tools.sapphire.IPayaraServerModel;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.payara.tools.server.deploying.PayaraServerBehaviour;
import org.eclipse.payara.tools.ui.wizards.BaseWizardFragment;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.swt.SapphireForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;

/**
 * Properties that are being shown for the Payara / GlassFish server when e.g. the server is right
 * clicked in the Servers view and "Properties" is chosen from the context menu.
 *
 */
@SuppressWarnings("restriction")
public class ServerPropertyPage extends PropertyPage {

    private IServerWorkingCopy serverWC;
    private PayaraServer payaraServer;
    private IPayaraServerModel model;

    FilteredListener<PropertyValidationEvent> listener = new FilteredListener<PropertyValidationEvent>() {
        @Override
        protected void handleTypedEvent(PropertyValidationEvent event) {
            refreshStatus();
        }
    };

    @Override
    protected Control createContents(Composite parent) {

        IServer server = (IServer) getElement();
        if (server instanceof IServerWorkingCopy) {
            serverWC = (IServerWorkingCopy) server;
        } else {
            serverWC = server.createWorkingCopy();
        }

        payaraServer = (PayaraServer) serverWC.loadAdapter(PayaraServer.class, new NullProgressMonitor());
        model = payaraServer.getModel();

        model.attach(listener, "*");

        final SapphireForm control = new SapphireForm(parent, model,
                DefinitionLoader.context(BaseWizardFragment.class)
                        .sdef("org.eclipse.payara.tools.ui.PayaraUI")
                        .form("payara.server"));

        control.setLayoutData(new GridData(FILL, FILL, true, true));

        refreshStatus();

        return control;

    }

    private void refreshStatus() {
        Status status = model.validation();

        if (status.severity() == Severity.ERROR) {
            setMessage(status.message(), ERROR);
            setValid(false);
        } else if (status.severity() == Severity.WARNING) {
            setMessage(status.message(), WARNING);
            setValid(true);
        } else {
            setMessage(null, NONE);
            setValid(true);
        }

    }

    // note that this is currently not working due to issue 140
    // public void propertyChange(PropertyChangeEvent evt) {
    // if (AbstractGlassfishServer.DOMAINUPDATE == evt.getPropertyName()) {
    // username.setText(payaraServer.getAdminUser());
    // password.setText(payaraServer.getAdminPassword());
    // adminServerPortNumber.setText(Integer.toString(payaraServer.getAdminPort()));
    // serverPortNumber.setText(Integer.toString(payaraServer.getPort()));
    // }
    // }

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

            Job job = new Job("Update Glassfish server state") { //$NON-NLS-1$
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        PayaraServerBehaviour serverBehavior = (PayaraServerBehaviour) serverWC
                                .loadAdapter(PayaraServerBehaviour.class, monitor);
                        serverBehavior.updateServerStatus();

                        Server gfServer = (Server) server;
                        gfServer.setServerPublishState(PUBLISH_CLEAN);

                    } catch (Exception e) {
                        ((Server) server).setServerState(STATE_STOPPED);
                    }
                    return OK_STATUS;
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
        serverWC.setAttribute(ATTR_ADMIN, "");
        serverWC.setAttribute(ATTR_ADMINPASS, "");
        serverWC.setAttribute(ATTR_DOMAINPATH, getDefaultDomainDir(serverWC.getRuntime().getLocation()).toString());
        serverWC.setAttribute(ATTR_ADMINPORT, "");
        serverWC.setAttribute(ATTR_DEBUG_PORT, "");
        model.refresh();
    }

}
