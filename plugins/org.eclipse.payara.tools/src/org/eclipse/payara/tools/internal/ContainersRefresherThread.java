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

package org.eclipse.payara.tools.internal;

import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.payara.tools.PayaraToolsPlugin;

public class ContainersRefresherThread extends Thread {
    private final LinkedList<IProject> projects;

    public ContainersRefresherThread() {
        this.projects = new LinkedList<>();
    }

    public IProject getProjectFromQueue() {
        synchronized (projects) {
            if (projects.isEmpty()) {
                try {
                    projects.wait();
                } catch (InterruptedException e) {
                    return null;
                }
            }

            return projects.removeFirst();
        }
    }

    public void addProjectToQueue(final IProject project) {
        synchronized (projects) {
            projects.addLast(project);
            projects.notify();
        }
    }

    @Override
    public void run() {
        while (true) {
            IProject project = getProjectFromQueue();

            if (project == null) {
                return;
            }

            try {
                IWorkspaceRunnable workspaceRunnable = monitor -> SystemLibrariesContainer.refresh(project);

                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                workspace.run(workspaceRunnable, workspace.getRoot(), 0, null);
            } catch (CoreException e) {
                PayaraToolsPlugin.log(e);
            }
        }
    }
}