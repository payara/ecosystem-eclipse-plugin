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

package org.eclipse.payara.tools.internal;

import static java.util.Collections.emptyList;
import static org.eclipse.core.resources.IResourceChangeEvent.POST_CHANGE;
import static org.eclipse.jdt.core.IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME;
import static org.eclipse.jdt.core.JavaCore.getClasspathContainer;
import static org.eclipse.jdt.core.JavaCore.newClasspathAttribute;
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;
import static org.eclipse.jdt.core.JavaCore.setClasspathContainer;
import static org.eclipse.payara.tools.utils.PayaraLocationUtils.DEFAULT_LIBRARIES;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.payara.tools.PayaraToolsPlugin;
import org.eclipse.payara.tools.utils.PayaraLocationUtils;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.util.ListFactory;

/**
 * This container manages the Payara "system" libraries, which is a selection from the
 * jar files in mostly glassfish/modules
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */
public final class SystemLibrariesContainer implements IClasspathContainer {
    
    public static final String ID = "org.eclipse.payara.tools.lib.system";
    private static final IPath PATH = new Path(ID);

    private static final String FPROJ_METADATA_FILE = ".settings/org.eclipse.wst.common.project.facet.core.xml";

    private static boolean initialized;
    private static ContainersRefresherThread containersRefresherThread;

    private final List<IClasspathEntry> classpathEntries;

    private static synchronized void initialize() {
        if (!initialized) {
            initialized = true;

            ResourceChangeListener.register();

            containersRefresherThread = new ContainersRefresherThread();
            containersRefresherThread.setName("PayaraLibraryContainersRefresher");
            containersRefresherThread.start();
        }
    }

    private SystemLibrariesContainer(IPath containerPath, IJavaProject project) {
        PayaraLocationUtils locationUtils = PayaraLocationUtils.find(project);
        
        String libraryGroup = DEFAULT_LIBRARIES; 
        if (containerPath.segmentCount() > 1) {
            libraryGroup = containerPath.segment(1);
        }
        
        // Sets
        classpathEntries = locationUtils == null ? 
            emptyList() : 
            createClasspathEntriesForLibraries(
                project.getProject(), locationUtils.version(), locationUtils.getLibraries(libraryGroup));
    }

    @Override
    public IClasspathEntry[] getClasspathEntries() {
        return classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]);
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
    
    public static ContainersRefresherThread getContainersRefresherThread() {
        return containersRefresherThread;
    }

    public static boolean isOnClasspath(IProject project) throws CoreException {
        if (isJavaProject(project)) {
            return isOnClasspath(JavaCore.create(project));
        }

        return false;
    }

    private static boolean isOnClasspath(IJavaProject project) throws CoreException {
        for (IClasspathEntry classpathEntry : project.getRawClasspath()) {
            if (isSystemLibrariesContainer(classpathEntry)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSystemLibrariesContainer(IClasspathEntry classpathEntry) {
        return classpathEntry.getPath().equals(PATH);
    }

    static void refresh(IProject project) throws CoreException {
        if (isJavaProject(project)) {
            refresh(JavaCore.create(project));
        }
    }

    private static void refresh(IJavaProject project) throws CoreException {
        IPath containerPath = null;

        for (IClasspathEntry classpathEntry : project.getRawClasspath()) {
            if (isSystemLibrariesContainer(classpathEntry)) {
                containerPath = classpathEntry.getPath();
                break;
            }
        }

        if (containerPath != null) {
            SystemLibrariesContainer existingContainer = (SystemLibrariesContainer) getClasspathContainer(containerPath, project);
            SystemLibrariesContainer newContainer = new SystemLibrariesContainer(containerPath, project);

            if (!existingContainer.equals(newContainer)) {
                IJavaProject[] projectsArray = { project };
                IClasspathContainer[] containersArray = { newContainer };

                JavaCore.setClasspathContainer(containerPath, projectsArray, containersArray, null);
            }
        }
    }

    /**
     * Checks whether the specified project is a Java project.
     *
     * @param project the project to check
     * @return <code>true</code> if the project is a Java project
     */

    private static boolean isJavaProject(IProject project) {
        try {
            return project.getNature(JavaCore.NATURE_ID) != null;
        } catch (CoreException e) {
        }

        return false;
    }

    public static final class Initializer extends ClasspathContainerInitializer {
        @Override
        public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
            SystemLibrariesContainer.initialize();

            setClasspathContainer(
                containerPath, 
                new IJavaProject[] { project },
                new IClasspathContainer[] { new SystemLibrariesContainer(containerPath, project) },
                null);
        }

        @Override
        public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
            return true;
        }

        @Override
        public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion) throws CoreException {
            super.requestClasspathContainerUpdate(containerPath, project, containerSuggestion);

            // Save source path in meta data file
            IProject proj = project.getProject();
            SystemLibrariesSetting settings = SystemLibrariesSetting.load(proj);
            if (settings == null) {
                settings = new SystemLibrariesSetting();
            }
            
            List<Library> libraries = settings.getLibraryList();

            boolean needUpdate = false;
            for (IClasspathEntry classpathEntry : containerSuggestion.getClasspathEntries()) {
                IPath srcPath = classpathEntry.getSourceAttachmentPath();

                if (srcPath != null) {
                    needUpdate = true;

                    boolean foudEntry = false;
                    for (Library library : libraries) {
                        String cpePath = classpathEntry.getPath().toString();
                        if (library.getPath().equals(cpePath)) {
                            // Update source path
                            library.setSource(srcPath.toPortableString());
                            foudEntry = true;
                            break;
                        }
                    }
                    
                    if (!foudEntry) {
                        Library newLibrary = new Library();
                        newLibrary.setPath(classpathEntry.getPath().toString());
                        newLibrary.setSource(srcPath.toString());
                        libraries.add(newLibrary);
                    }
                } else {
                    // Remove entry from settings file
                    
                    Iterator<Library> it = libraries.iterator();
                    while (it.hasNext()) {
                        Library lib = it.next();
                        String cpePath = classpathEntry.getPath().toString();
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
                // Update the classpath container to reflect the changes
                initialize(containerPath, project);
            }
        }
    }
    
    public static List<IClasspathEntry> createClasspathEntriesForLibraries(IProject project, Version version, List<File> libraries) {
        ListFactory<IClasspathEntry> classpathListFactory = ListFactory.start();

        URL doc;
        String javaEEVersion = (version.matches("[5") ? "8" : (version.matches("[4") ? "7" : "6"));

        try {
            if ("8".equals(javaEEVersion)) {
                doc = new URL("https://javaee.github.io/javaee-spec/javadocs/");
            } else {
                doc = new URL("http://docs.oracle.com/javaee/" + javaEEVersion + "/api/");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        SystemLibrariesSetting libSettings = SystemLibrariesSetting.load(project);
        
        if (libSettings != null) {
            
            // We have settings for our system library, check it for each entry
            // if we have a source attachment
            
            for (File library : libraries) {
                classpathListFactory.add(
                    createLibraryEntry(
                        new Path(library.toString()), 
                        libSettings.getSourcePath(library), 
                        doc));
            }
        } else {
            
            // No settings for our system library, source is always null
            
            for (File library : libraries) {
                classpathListFactory.add(
                    createLibraryEntry(
                        new Path(library.toString()), 
                        null, 
                        doc));
            }
        }
       

        return classpathListFactory.result();
    }
    
    private static IClasspathEntry createLibraryEntry(IPath library, File src, URL javadoc) {
        IPath librarySourcePath = src == null ? null : new Path(src.getAbsolutePath());
        IAccessRule[] access = {};
        IClasspathAttribute[] libraryJavadocAttributes;

        if (javadoc == null) {
            libraryJavadocAttributes = new IClasspathAttribute[0];
        } else {
            libraryJavadocAttributes = new IClasspathAttribute[] { newClasspathAttribute(JAVADOC_LOCATION_ATTRIBUTE_NAME, javadoc.toExternalForm()) };
        }

        return newLibraryEntry(library, librarySourcePath, null, access, libraryJavadocAttributes, false);
    }

    private static final class ResourceChangeListener implements IResourceChangeListener {
        private final List<IPath> triggerFiles;

        public static void register() {
            ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(), POST_CHANGE);
        }

        private ResourceChangeListener() {
            triggerFiles = new ArrayList<>();
            triggerFiles.add(new Path(FPROJ_METADATA_FILE));
        }

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            for (IResourceDelta subdelta : event.getDelta().getAffectedChildren()) {
                IProject project = (IProject) subdelta.getResource();
                boolean relevant = false;

                for (IPath triggerFile : triggerFiles) {
                    if (subdelta.findMember(triggerFile) != null) {
                        try {
                            if (isOnClasspath(project)) {
                                relevant = true;
                            }
                        } catch (CoreException e) {
                            PayaraToolsPlugin.log(e);
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

    private static final class Resources extends NLS {
        public static String containerLabel;

        static {
            initializeMessages(SystemLibrariesContainer.class.getName(), Resources.class);
        }
    }

}
