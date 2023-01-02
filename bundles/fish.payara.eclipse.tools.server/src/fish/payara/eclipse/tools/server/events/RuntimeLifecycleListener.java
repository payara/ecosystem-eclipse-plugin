/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
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

package fish.payara.eclipse.tools.server.events;

import static fish.payara.eclipse.tools.server.internal.SystemLibrariesContainer.getContainersRefresherThread;
import static fish.payara.eclipse.tools.server.internal.SystemLibrariesContainer.isOnClasspath;
import static fish.payara.eclipse.tools.server.utils.IsPayaraUtil.isPayara;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;

import fish.payara.eclipse.tools.server.PayaraServerPlugin;

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
                PayaraServerPlugin.log(e);
            }
        }
    }
}