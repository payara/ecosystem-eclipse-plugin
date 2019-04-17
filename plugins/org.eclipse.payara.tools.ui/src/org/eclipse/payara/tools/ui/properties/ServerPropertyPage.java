/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.properties;

import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_RESTART_PATTERN;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_ADMIN;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_ADMINPASS;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_ADMINPORT;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_DEBUG_PORT;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_DOMAINPATH;
import static org.eclipse.payara.tools.server.PayaraServer.getDefaultDomainDir;
import static org.eclipse.payara.tools.utils.Jobs.scheduleShortJob;
import static org.eclipse.payara.tools.utils.WtpUtil.load;
import static org.eclipse.swt.SWT.FILL;
import static org.eclipse.wst.server.core.IServer.PUBLISH_CLEAN;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPED;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.wst.server.ui.internal.editor.GlobalCommandManager;

/**
 * Properties that are being shown for the Payara / GlassFish server when e.g. the server is right
 * clicked in the Servers view and "Properties" is chosen from the context menu.
 *
 */
public class ServerPropertyPage extends PropertyPage {

    private IServerWorkingCopy serverWorkingCopy;
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
            serverWorkingCopy = (IServerWorkingCopy) server;
        } else {
            serverWorkingCopy = server.createWorkingCopy();
        }

        model = load(serverWorkingCopy, PayaraServer.class).getModel();
        model.attach(listener, "*");

        SapphireForm control = new SapphireForm(parent, model,
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
    public boolean performCancel() {
        model.detach(listener, "*");
        return super.performCancel();
    }

    @Override
    protected void performApply() {
        try {
            IServer server = serverWorkingCopy.save(true, new NullProgressMonitor());
            GlobalCommandManager.getInstance().reload(server.getId());
            
            scheduleShortJob("Update Payara server state", monitor -> {
                
                PayaraServerBehaviour serverBehavior = null;
                
                try {
                    serverBehavior = load(server, PayaraServerBehaviour.class);
                    
                    serverBehavior.updateServerStatus();
                    serverBehavior.setPayaraServerPublishState(PUBLISH_CLEAN);
                } catch (Exception e) {
                    if (serverBehavior != null) {
                        serverBehavior.setPayaraServerState(STATE_STOPPED);
                    }
                }
            });
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
        
        serverWorkingCopy.setAttribute(ATTR_ADMIN, "");
        serverWorkingCopy.setAttribute(ATTR_ADMINPASS, "");
        serverWorkingCopy.setAttribute(ATTR_DOMAINPATH, getDefaultDomainDir(serverWorkingCopy.getRuntime().getLocation()).toString());
        serverWorkingCopy.setAttribute(ATTR_ADMINPORT, "");
        serverWorkingCopy.setAttribute(ATTR_DEBUG_PORT, "");
        serverWorkingCopy.setAttribute(ATTR_DEBUG_PORT, "");
        serverWorkingCopy.setAttribute(PROP_RESTART_PATTERN.name(), "");
        
        model.refresh();
    }

}
