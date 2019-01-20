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

package org.eclipse.payara.tools.ui.serverview.actions;

import static org.eclipse.ui.ISharedImages.IMG_TOOL_DELETE;
import static org.eclipse.ui.ISharedImages.IMG_TOOL_DELETE_DISABLED;
import static org.eclipse.ui.IWorkbenchCommandConstants.EDIT_DELETE;
import static org.eclipse.wst.server.core.IServer.PUBLISH_INCREMENTAL;
import static org.eclipse.wst.server.core.IServer.PUBLISH_STATE_FULL;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPED;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.payara.tools.server.deploying.PayaraServerBehaviour;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.DeployedApplicationsNode;
import org.eclipse.payara.tools.ui.serverview.dynamicnodes.TreeNode;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;

@SuppressWarnings("restriction")
public class UndeployAction extends Action {

    ISelection selection;
    ICommonActionExtensionSite actionSite;

    public UndeployAction(ISelection selection, ICommonActionExtensionSite actionSite) {
        setText("Undeploy");

        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setImageDescriptor(sharedImages.getImageDescriptor(IMG_TOOL_DELETE));
        setDisabledImageDescriptor(sharedImages.getImageDescriptor(IMG_TOOL_DELETE_DISABLED));
        setActionDefinitionId(EDIT_DELETE);

        this.selection = selection;
        this.actionSite = actionSite;
    }

    @Override
    public void runWithEvent(Event event) {
        if (selection instanceof TreeSelection) {
            TreeSelection ts = (TreeSelection) selection;
            Object obj = ts.getFirstElement();
            if (obj instanceof TreeNode) {
                final TreeNode module = (TreeNode) obj;
                final DeployedApplicationsNode target = (DeployedApplicationsNode) module.getParent();

                try {
                    final PayaraServerBehaviour be = target.getServer().getServerBehaviourAdapter();
                    IRunnableWithProgress op = new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor) {
                            try {

                                IServer server = be.getServer();

                                IModule[] modules = server.getModules();
                                IModule imodule = null;
                                for (IModule element : modules) {
                                    if (element.getName().equals(module.getName())) {
                                        imodule = element;

                                    }
                                }
                                if (imodule == null) {
                                    // undeploy and return
                                    // TODO review undeploy functionality
                                    be.undeploy(module.getName(), monitor);
                                    return;
                                }
                                try {
                                    IServerWorkingCopy wc = server.createWorkingCopy();
                                    wc.modifyModules(null, new IModule[] { imodule }, monitor);
                                    server = wc.save(true, monitor);

                                } catch (CoreException e) {
                                    e.printStackTrace();
                                }

                                if (server.getServerState() != STATE_STOPPED
                                        && ServerUIPlugin.getPreferences().getPublishOnAddRemoveModule()) {
                                    final IAdaptable info = new IAdaptable() {
                                        @Override
                                        public <T> T getAdapter(final Class<T> adapter) {
                                            if (Shell.class.equals(adapter)) {
                                                return adapter.cast(Display.getDefault().getActiveShell());
                                            }

                                            return null;
                                        }
                                    };
                                    server.publish(PUBLISH_INCREMENTAL, null, info, null);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }
                    };

                    Shell shell = Display.getDefault().getActiveShell();
                    if (shell != null) {
                        new ProgressMonitorDialog(shell).run(true, false, op);
                    }
                    target.refresh();
                    StructuredViewer view = actionSite.getStructuredViewer();
                    view.refresh(target);

                    // set to FULL to tell the system a full deploy is
                    // needed.
                    Server server = (Server) be.getServer();
                    server.setModulePublishState(server.getModules(), PUBLISH_STATE_FULL);

                } catch (Exception e) {
                }
            }
        }
        super.run();
    }

    @Override
    public void run() {
        this.runWithEvent(null);
    }

}
