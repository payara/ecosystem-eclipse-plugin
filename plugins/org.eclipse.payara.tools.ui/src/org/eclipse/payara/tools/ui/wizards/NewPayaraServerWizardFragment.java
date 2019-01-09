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

package org.eclipse.payara.tools.ui.wizards;

import static org.eclipse.payara.tools.PayaraToolsPlugin.log;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_NAME;
import static org.eclipse.payara.tools.server.PayaraServer.getDefaultDomainDir;
import static org.eclipse.payara.tools.ui.wizards.GlassfishWizardResources.wzdServerDescription;
import static org.eclipse.payara.tools.utils.WtpUtil.load;
import static org.eclipse.wst.server.core.TaskModel.TASK_SERVER;
import static org.eclipse.wst.server.core.internal.Server.AUTO_PUBLISH_RESOURCE;
import static org.eclipse.wst.server.core.internal.Server.PROP_AUTO_PUBLISH_SETTING;
import static org.eclipse.wst.server.core.internal.Server.PROP_AUTO_PUBLISH_TIME;

import org.eclipse.payara.tools.sapphire.IPayaraServerModel;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.sapphire.Element;
import org.eclipse.wst.server.core.IServerWorkingCopy;

/**
 * This wizard fragment plugs-in the wizard flow when
 * <code>Servers -> New Server -> Payara -> Payara</code> is selected and subsequently the
 * <code>next</code> button is pressed.
 *
 * <p>
 * This fragment essentially causes the screen with <code>Name</code>, <code>Host name</code>,
 * <code>Domain path</code> etc to be rendered, although a lot of the actual work is delegated by
 * the {@link BaseWizardFragment} to Sapphire. The UI layout for this wizard fragment is specified
 * in the file <code>PayaraUI.sdef</code> in the "payara.server" section.
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
     * The section in <code>PayaraUI.sdef</code> that contains the UI layout for this wizard
     * fragment.
     */
    @Override
    protected String getUserInterfaceDef() {
        return "payara.server";
    }

    @Override
    protected Element getModel() {
        try {
            server().setAttribute(PROP_AUTO_PUBLISH_SETTING, AUTO_PUBLISH_RESOURCE);
            server().setAttribute(PROP_AUTO_PUBLISH_TIME, 1);
        } catch (Exception e) {
            log(e);
        }

        // IPayaraServerModel contains the entries corresponding to PayaraUI.sdef, which are the fields
        // that will be rendered by Saphire, e.g. Name, HostName, Remote, etc
        
        return load(server(), PayaraServer.class).getModel();
    }

    @Override
    public void enter() {
        super.enter();

        // Set the default domain location
        ((IPayaraServerModel) getModel())
                .getDomainPath()
                .write(getDefaultDomainDir(server().getRuntime().getLocation()).toOSString());
    }

    @Override
    protected String getInitialFocus() {
        return PROP_NAME.name();
    }
    
    private IServerWorkingCopy server() {
        return (IServerWorkingCopy) getTaskModel().getObject(TASK_SERVER);
    }

}
