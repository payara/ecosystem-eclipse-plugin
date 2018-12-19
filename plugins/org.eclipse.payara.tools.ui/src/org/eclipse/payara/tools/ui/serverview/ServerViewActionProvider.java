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

package org.eclipse.payara.tools.ui.serverview;

import static org.eclipse.payara.tools.ui.serverview.ServerViewDynamicNodeProvider.GLASSFISH_MANAGEMENT;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.payara.tools.ui.serverview.actions.OpenInBrowserAction;
import org.eclipse.payara.tools.ui.serverview.actions.TestWebServiceAction;
import org.eclipse.payara.tools.ui.serverview.actions.UndeployAction;
import org.eclipse.payara.tools.ui.serverview.actions.UnregisterResourceAction;
import org.eclipse.payara.tools.ui.serverview.actions.WSDLInfoWebServiceAction;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.ApplicationNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.DeployedApplicationsNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.ResourcesNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.TreeNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.WebServiceNode;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * This provider adds context actions to some of the dynamic tree nodes provided by
 * {@link ServerViewDynamicNodeProvider}.
 *
 * <p>
 * E.g. it adds the "unregister" action when you right click on a resource such as a JDBC data
 * source
 * </p>
 *
 */
public class ServerViewActionProvider extends GenericActionProvider {

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

                if (obj instanceof ResourcesNode) {

                    // Add unregister action to resources

                    ResourcesNode resourcesNode = (ResourcesNode) obj;

                    if (resourcesNode.getResource() != null) {
                        menu.add(new Separator());
                        menu.add(new UnregisterResourceAction(selection, actionSite));
                    }

                } else if (obj instanceof ApplicationNode) {

                    // Add undeploy and open in browser to applications

                    menu.add(new Separator());
                    menu.add(new UndeployAction(selection, actionSite));
                    menu.add(new OpenInBrowserAction(selection));

                } else if (obj instanceof WebServiceNode) {

                    // Add test and info actions to (soap) web services

                    menu.add(new TestWebServiceAction(selection));
                    menu.add(new WSDLInfoWebServiceAction(selection));
                } 
            }
        }
    }

    @Override
    public void fillActionBars(IActionBars o) {
        super.fillActionBars(o);
    }

    @Override
    protected void refresh(Object selection) {
        super.refresh(selection);

        DeployedApplicationsNode root = null;

        if (selection instanceof DeployedApplicationsNode) {
            root = (DeployedApplicationsNode) selection;
        } else if (selection instanceof TreeNode) {
            TreeNode treeNode = (TreeNode) selection;
            if (treeNode.getName().equals(GLASSFISH_MANAGEMENT)) {
                for (Object child : treeNode.getChildren()) {
                    if (child instanceof DeployedApplicationsNode) {
                        root = (DeployedApplicationsNode) child;
                        break;
                    }
                }
            }
        }

        if (root != null) {
            root.refresh();
        }

    }

}
