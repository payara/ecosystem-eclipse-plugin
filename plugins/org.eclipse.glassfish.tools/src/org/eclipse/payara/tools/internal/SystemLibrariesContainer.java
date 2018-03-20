/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.internal;

import static org.eclipse.payara.tools.utils.IsGlassFishUtil.isGlassFish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.payara.tools.GlassfishToolsPlugin;
import org.eclipse.payara.tools.utils.GlassFishLocationUtils;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SystemLibrariesContainer implements IClasspathContainer {
    public static final String ID = "org.eclipse.payara.tools.lib.system";
    private static final IPath PATH = new Path(ID);

    private static final String FPROJ_METADATA_FILE = ".settings/org.eclipse.wst.common.project.facet.core.xml";

    private static boolean initialized;
    private static ContainersRefresherThread containersRefresherThread;

    private final List<IClasspathEntry> entries;

    private static synchronized void initialize() {
        if (!initialized) {
            initialized = true;

            RuntimeLifecycleListener.register();
            ResourceChangeListener.register();

            containersRefresherThread = new ContainersRefresherThread();
            containersRefresherThread.start();
        }
    }

    private SystemLibrariesContainer(final IJavaProject project) {
        final GlassFishLocationUtils gf = GlassFishLocationUtils.find(project);
        this.entries = (gf == null ? Collections.<IClasspathEntry>emptyList() : gf.classpath(project.getProject()));
    }

    @Override
    public IClasspathEntry[] getClasspathEntries() {
        return this.entries.toArray(new IClasspathEntry[this.entries.size()]);
    }

    @Override
    public String getDescription() {
        return Resources.containerLabel;
    }

    @Override
    public int getKind() {
        return K_APPLICATION;
    }

    @Override
    public IPath getPath() {
        return PATH;
    }

    private static boolean isOnClasspath(final IProject project) throws CoreException {
        if (isJavaProject(project)) {
            return isOnClasspath(JavaCore.create(project));
        }

        return false;
    }

    private static boolean isOnClasspath(final IJavaProject project) throws CoreException {
        final IClasspathEntry[] cp = project.getRawClasspath();

        for (IClasspathEntry cpe : cp) {
            if (isSystemLibrariesContainer(cpe)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSystemLibrariesContainer(final IClasspathEntry cpe) {
        return cpe.getPath().equals(PATH);
    }

    private static void refresh(final IProject project) throws CoreException {
        if (isJavaProject(project)) {
            refresh(JavaCore.create(project));
        }
    }

    private static void refresh(final IJavaProject project) throws CoreException {
        final IClasspathEntry[] cp = project.getRawClasspath();
        IPath containerPath = null;

        for (IClasspathEntry cpe : cp) {
            if (isSystemLibrariesContainer(cpe)) {
                containerPath = cpe.getPath();
                break;
            }
        }

        if (containerPath != null) {
            final IClasspathContainer cont = JavaCore.getClasspathContainer(containerPath, project);
            final SystemLibrariesContainer existingContainer = (SystemLibrariesContainer) cont;
            final SystemLibrariesContainer newContainer = new SystemLibrariesContainer(project);

            if (!existingContainer.equals(newContainer)) {
                final IJavaProject[] projectsArray = { project };
                final IClasspathContainer[] containersArray = { newContainer };

                JavaCore.setClasspathContainer(containerPath, projectsArray, containersArray, null);
            }
        }
    }

    /**
     * Checks whether the specified project is a Java project.
     *
     * @param pj the project to check
     * @return <code>true</code> if the project is a Java project
     */

    private static boolean isJavaProject(final IProject project) {
        try {
            return project.getNature(JavaCore.NATURE_ID) != null;
        } catch (CoreException e) {
        }

        return false;
    }

    public static final class Initializer extends ClasspathContainerInitializer {
        @Override
        public void initialize(final IPath containerPath, final IJavaProject project) throws CoreException {
            SystemLibrariesContainer.initialize();

            JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project },
                    new IClasspathContainer[] { new SystemLibrariesContainer(project) }, null);
        }

        @Override
        public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
            return true;
        }

        @Override
        public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project,
                IClasspathContainer containerSuggestion) throws CoreException {
            super.requestClasspathContainerUpdate(containerPath, project, containerSuggestion);

            // save source path in meta data file
            IProject proj = project.getProject();
            SystemLibrariesSetting settings = SystemLibrariesSetting.load(proj);
            if (settings == null) {
                settings = new SystemLibrariesSetting();
            }
            ArrayList<Library> libsList = settings.getLibraryList();

            boolean needUpdate = false;
            for (IClasspathEntry cpe : containerSuggestion.getClasspathEntries()) {
                // IClasspathEntry cpe = findClasspathEntry(containerPath.toString(),
                // containerSuggestion );
                IPath srcPath = cpe.getSourceAttachmentPath();

                if (srcPath != null) {
                    needUpdate = true;

                    boolean foudEntry = false;
                    for (Library lib : libsList) {
                        String cpePath = cpe.getPath().toString();
                        if (lib.getPath().equals(cpePath)) {
                            // update source path
                            lib.setSource(srcPath.toPortableString());
                            foudEntry = true;
                            break;
                        }
                    }
                    if (!foudEntry) {
                        Library newLib = new Library();
                        newLib.setPath(cpe.getPath().toString());
                        newLib.setSource(srcPath.toString());
                        libsList.add(newLib);
                    }
                } else {
                    // remove enty from settings file,
                    Iterator<Library> it = libsList.iterator();
                    while (it.hasNext()) {
                        Library lib = it.next();
                        String cpePath = cpe.getPath().toString();
                        if (lib.getPath().equals(cpePath)) {
                            it.remove();
                            needUpdate = true;
                            break;
                        }
                    }

                }
            }
            if (needUpdate) {
                SystemLibrariesSetting.save(proj, settings);
                // update the classpath container to reflect the changes
                initialize(containerPath, project);
            }
        }
    }

    private static final class RuntimeLifecycleListener implements IRuntimeLifecycleListener {
        public static void register() {
            ServerCore.addRuntimeLifecycleListener(new RuntimeLifecycleListener());
        }

        @Override
        public void runtimeAdded(final IRuntime runtime) {
            handleEvent(runtime);
        }

        @Override
        public void runtimeChanged(final IRuntime runtime) {
            handleEvent(runtime);
        }

        @Override
        public void runtimeRemoved(final IRuntime runtime) {
            handleEvent(runtime);
        }

        private void handleEvent(final IRuntime runtime) {
            if (isGlassFish(runtime)) {
                for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                    try {
                        if (isOnClasspath(project)) {
                            containersRefresherThread.addProjectToQueue(project);
                        }
                    } catch (CoreException e) {
                        GlassfishToolsPlugin.log(e);
                    }
                }
            }
        }
    }

    private static final class ResourceChangeListener implements IResourceChangeListener {
        private final List<IPath> triggerFiles;

        public static void register() {
            final IWorkspace ws = ResourcesPlugin.getWorkspace();

            ws.addResourceChangeListener(new ResourceChangeListener(), IResourceChangeEvent.POST_CHANGE);
        }

        private ResourceChangeListener() {
            this.triggerFiles = new ArrayList<>();
            this.triggerFiles.add(new Path(FPROJ_METADATA_FILE));
        }

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            for (IResourceDelta subdelta : event.getDelta().getAffectedChildren()) {
                final IProject project = (IProject) subdelta.getResource();
                boolean relevant = false;

                for (IPath p : this.triggerFiles) {
                    if (subdelta.findMember(p) != null) {
                        try {
                            if (SystemLibrariesContainer.isOnClasspath(project)) {
                                relevant = true;
                            }
                        } catch (CoreException e) {
                            GlassfishToolsPlugin.log(e);
                        }

                        break;
                    }
                }

                if (relevant) {
                    containersRefresherThread.addProjectToQueue(project);
                }
            }
        }
    }

    private static final class ContainersRefresherThread extends Thread {
        private final LinkedList<IProject> projects;

        public ContainersRefresherThread() {
            this.projects = new LinkedList<>();
        }

        public IProject getProjectFromQueue() {
            synchronized (this.projects) {
                if (this.projects.isEmpty()) {
                    try {
                        this.projects.wait();
                    } catch (InterruptedException e) {
                        return null;
                    }
                }

                return this.projects.removeFirst();
            }
        }

        public void addProjectToQueue(final IProject project) {
            synchronized (this.projects) {
                this.projects.addLast(project);
                this.projects.notify();
            }
        }

        @Override
        public void run() {
            while (true) {
                final IProject project = getProjectFromQueue();

                if (project == null) {
                    return;
                }

                try {
                    final IWorkspaceRunnable wsr = monitor -> refresh(project);

                    final IWorkspace ws = ResourcesPlugin.getWorkspace();
                    ws.run(wsr, ws.getRoot(), 0, null);
                } catch (CoreException e) {
                    GlassfishToolsPlugin.log(e);
                }
            }
        }
    }

    private static final class Resources extends NLS {
        public static String containerLabel;

        static {
            initializeMessages(SystemLibrariesContainer.class.getName(), Resources.class);
        }
    }

}
