/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.handlers;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractGlassfishSelectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().getSelection();
		if ((selection != null) && !selection.isEmpty()) {
			processSelection((IStructuredSelection)selection);
		}
		return null;
	}
	
	public abstract void processSelection(IStructuredSelection selection);
	
	protected void showMessageDialog(){
		showMessageDialog("GlassFish Server has to be up and running...\nPlease start the server.");
	}

	protected void showMessageDialog(String msg){
		MessageDialog message;
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String labels[] = new String[1];
		labels[0] = "OK";
		message = new MessageDialog(shell, "Cannot Execute this action", null,
				msg, 2, labels, 1);
		message.open();
	}
	
	protected void showPageInDefaultBrowser(String url) throws PartInitException, MalformedURLException {
		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
		browser.openURL(new URL(url));
	}
	
}
