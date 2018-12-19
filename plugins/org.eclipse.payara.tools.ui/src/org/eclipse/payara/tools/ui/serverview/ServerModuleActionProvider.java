/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.serverview;

import static org.eclipse.payara.tools.utils.WtpUtil.load;
import static org.eclipse.ui.PlatformUI.getWorkbench;

import java.io.File;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.payara.tools.server.deploying.PayaraServerBehaviour;
import org.eclipse.payara.tools.ui.serverview.actions.GotoModuleProjectInExplorerAction;
import org.eclipse.payara.tools.ui.serverview.actions.OpenModuleInFileBrowserAction;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;

/**
 * This provider adds context actions to a deployed module
 * </p>
 * @author Arjan Tijms
 */
public class ServerModuleActionProvider extends GenericActionProvider {

    private ICommonActionExtensionSite actionSite;

    @Override
    public void init(ICommonActionExtensionSite actionExtensionSite) {
        super.init(actionExtensionSite);
        this.actionSite = actionExtensionSite;
    }

    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);

        ICommonViewerSite site = actionSite.getViewSite();

        if (site instanceof ICommonViewerWorkbenchSite) {

            ISelection selection = site.getSelectionProvider().getSelection();

            if (selection instanceof TreeSelection) {

                Object obj = ((TreeSelection) selection).getFirstElement();

                if (obj instanceof IServerModule) {

                    IServerModule module = (IServerModule) obj;

                    IServer server = module.getServer();

                    PayaraServerBehaviour serverBehaviour = load(server, PayaraServerBehaviour.class);

                    if (serverBehaviour == null) {
                        // Probably an other server was selected than Payara
                        return;
                    }

                    IModule[] modules = module.getModule();
                    if (modules.length > 1 && !new File(serverBehaviour.getModuleDeployPath(modules[1])).exists()) {
                        return;
                    }

                    if (menu instanceof MenuManager) {
                        MenuManager menuManager = (MenuManager) menu;

                        MenuManager showInSubMenu = (MenuManager) menuManager.find("org.eclipse.ui.navigate.showInQuickMenu");

                        if (showInSubMenu == null) {
                            String text = "Show In";

                            TriggerSequence[] activeBindings = getWorkbench().getAdapter(IBindingService.class)
                                    .getActiveBindingsFor("org.eclipse.ui.navigate.showInQuickMenu");

                            if (activeBindings.length > 0) {
                                text += "\t" + activeBindings[0].format();
                            }

                            showInSubMenu = new MenuManager(text, "org.eclipse.ui.navigate.showInQuickMenu");
                            menuManager.add(showInSubMenu);
                        }

                        showInSubMenu.add(new OpenModuleInFileBrowserAction(selection));
                    }
                    
                    menu.add(new GotoModuleProjectInExplorerAction(selection));

                }
            }
        }
    }

}
