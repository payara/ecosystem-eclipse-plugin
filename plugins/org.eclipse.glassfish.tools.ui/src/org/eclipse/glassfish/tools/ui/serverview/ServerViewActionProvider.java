/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.serverview;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import org.eclipse.glassfish.tools.ui.serverview.actions.OpenInBrowserAction;
import org.eclipse.glassfish.tools.ui.serverview.actions.TestWebServiceAction;
import org.eclipse.glassfish.tools.ui.serverview.actions.UndeployAction;
import org.eclipse.glassfish.tools.ui.serverview.actions.UnregisterResourceAction;
import org.eclipse.glassfish.tools.ui.serverview.actions.WSDLInfoWebServiceAction;

public class ServerViewActionProvider extends GenericActionProvider {

	
	// used in plugin.xml as an ID.!!!!
	private static final String SERVERVIEW_EXTENTION_ID = "glassfish31.serverview.contentprovider";
	private ICommonActionExtensionSite actionSite;



	@Override
	protected String getExtensionId() {
		return SERVERVIEW_EXTENTION_ID;
	}

	public void init(ICommonActionExtensionSite s) {
		super.init(s);
		this.actionSite = s;
	}

	@Override
	protected void refresh(Object selection) {
		super.refresh(selection);
		DeployedApplicationsNode root = null;
		if (selection instanceof DeployedApplicationsNode) {
			root = (DeployedApplicationsNode) selection;
		}else if (selection instanceof TreeNode) {
			TreeNode tn = (TreeNode)selection;
			if( tn.getName().equals( ServerViewContentProvider.GLASSFISH_MANAGEMENT)){
				for( Object o : tn.getChildren() ){
					if( o instanceof DeployedApplicationsNode ){
						root = (DeployedApplicationsNode) o;
						break;
					}
				}
			}
		}
		if( root !=null )
			root.refresh();
	}
	
	@Override
	public void fillActionBars(IActionBars o) {
		super.fillActionBars(o);
	}



	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider()
					.getSelection();

			if (selection instanceof TreeSelection) {
				TreeSelection ts = (TreeSelection) selection;
				Object obj = ts.getFirstElement();
				if (obj instanceof DeployedApplicationsNode) {

				} else if (obj instanceof ResourcesNode) {
					ResourcesNode r = (ResourcesNode) obj;
					if (r.getResource()!=null){
					menu.add(new Separator());
					menu.add(new UnregisterResourceAction(selection,   actionSite));
					}
				} else if (obj instanceof ApplicationNode) {
					menu.add(new Separator());
					// Add undeploy action
					Action undeployAction = new UndeployAction(selection , actionSite);
					menu.add(undeployAction);
					menu.add(new OpenInBrowserAction(selection));
				} else if (obj instanceof WebServiceNode) {
					menu.add(new TestWebServiceAction(selection));
					menu.add(new WSDLInfoWebServiceAction(selection));
					
				}else if (obj instanceof TreeNode) {

				}

				//Set system property to control Glassfish server property page
				/* AbstractGlassfishServer glassfish = null;
				if( obj instanceof IServer ){
					IServer server = (IServer)obj;
					glassfish = (AbstractGlassfishServer)server.getAdapter(org.eclipse.glassfish.tools.AbstractGlassfishServer.class);
				}
				System.setProperty("org.eclipse.glassfish.tools.ui.IS_GLASSFISH_SERVER", Boolean.toString( glassfish!=null) ); */
			}
		}
	}
}
