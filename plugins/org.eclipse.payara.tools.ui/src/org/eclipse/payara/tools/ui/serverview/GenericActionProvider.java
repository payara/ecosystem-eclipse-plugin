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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Super class of action providers. Currently only used as super class for
 * {@link ServerViewActionProvider}.
 *
 */
public abstract class GenericActionProvider extends CommonActionProvider {

    private Action refreshAction;
    protected ICommonActionExtensionSite actionSite;

    @Override
    public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);

        this.actionSite = aSite;

        ICommonViewerSite site = aSite.getViewSite();

        if (site instanceof ICommonViewerWorkbenchSite) {
            StructuredViewer v = aSite.getStructuredViewer();
            if (v instanceof CommonViewer) {
                CommonViewer cv = (CommonViewer) v;
                ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
                makeActions(cv, wsSite.getSelectionProvider());
            }
        }
    }

    private void makeActions(CommonViewer cv, ISelectionProvider selectionProvider) {
        refreshAction = new RefreshAction(selectionProvider.getSelection());
    }

    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);

        ICommonViewerSite site = actionSite.getViewSite();
        IStructuredSelection selection = null;

        if (site instanceof ICommonViewerWorkbenchSite) {
            ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
            selection = (IStructuredSelection) wsSite.getSelectionProvider().getSelection();

            refreshAction = new RefreshAction(selection);
            menu.add(refreshAction);
            menu.add(new Separator());
        }

    }

    protected void refresh(Object selection) {

    }

    class RefreshAction extends Action {

        ISelection selection;

        public RefreshAction(ISelection selection) {
            setText("Refresh");
            this.selection = selection;
        }

        @Override
        public void runWithEvent(Event event) {
            if (selection instanceof TreeSelection) {
                TreeSelection treeSelection = (TreeSelection) selection;
                
                Object obj = treeSelection.getFirstElement();
                
                refresh(obj);
                actionSite.getStructuredViewer().refresh(obj);
            }

            super.run();

        }

        @Override
        public void run() {
            runWithEvent(null);
        }
    }

}
