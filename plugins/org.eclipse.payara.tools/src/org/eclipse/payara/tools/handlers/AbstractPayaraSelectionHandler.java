/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.handlers;

import static org.eclipse.ui.browser.IWorkbenchBrowserSupport.LOCATION_BAR;
import static org.eclipse.ui.browser.IWorkbenchBrowserSupport.NAVIGATION_BAR;
import static org.eclipse.ui.handlers.HandlerUtil.getActiveWorkbenchWindow;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;

public abstract class AbstractPayaraSelectionHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (selection != null && !selection.isEmpty()) {
            processSelection((IStructuredSelection) selection);
        }

        return null;
    }

    public void processSelection(IStructuredSelection selection) {
        IServer server = (IServer) selection.getFirstElement();
        if (server != null) {
            processSelection(server);
        }
    }

    public void processSelection(IServer server) {

    }

    protected void showMessageDialog() {
        showMessageDialog("Payara Server has to be up and running...\nPlease start the server.");
    }

    protected void showMessageDialog(String msg) {
        new MessageDialog(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                "Cannot Execute this action", null, msg, 2, new String[] { "OK" }, 1)
                        .open();
    }

    protected void showPageInDefaultBrowser(String url) throws PartInitException, MalformedURLException {
        PlatformUI.getWorkbench()
                .getBrowserSupport()
                .createBrowser(LOCATION_BAR | NAVIGATION_BAR, null, null, null)
                .openURL(new URL(url));
    }

}
