/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.server.events;

import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.payara.tools.internal.SystemLibrariesContainer.getContainersRefresherThread;
import static org.eclipse.payara.tools.internal.SystemLibrariesContainer.isOnClasspath;
import static org.eclipse.payara.tools.utils.IsPayaraUtil.isPayara;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.payara.tools.PayaraToolsPlugin;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;

public class RuntimeLifecycleListener implements IRuntimeLifecycleListener {

    @Override
    public void runtimeAdded(IRuntime runtime) {
        if (!isPayara(runtime)) {
            return;
        }
        
        tryRefreshContainers(runtime);
    }

    @Override
    public void runtimeChanged(IRuntime runtime) {
        if (!isPayara(runtime)) {
            return;
        }
        
        tryRefreshContainers(runtime);
    }

    @Override
    public void runtimeRemoved(IRuntime runtime) {
        if (!isPayara(runtime)) {
            return;
        }
        
        tryRefreshContainers(runtime);
    }

    private void tryRefreshContainers(IRuntime runtime) {
        for (IProject project : getWorkspace().getRoot().getProjects()) {
            try {
                if (isOnClasspath(project)) {
                    getContainersRefresherThread().addProjectToQueue(project);
                }
            } catch (CoreException e) {
                PayaraToolsPlugin.log(e);
            }
        }
    }
}