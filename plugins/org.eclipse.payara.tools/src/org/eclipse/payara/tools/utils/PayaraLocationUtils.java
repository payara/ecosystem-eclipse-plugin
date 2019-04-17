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

package org.eclipse.payara.tools.utils;

import static java.util.Collections.emptyList;
import static org.eclipse.payara.tools.internal.ManifestUtil.readManifestEntry;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.DirectoryScanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.payara.tools.internal.SystemLibraries;
import org.eclipse.sapphire.Version;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;

/**
 * Series of utils related to the location where Payara / GlassFish is installed.
 * 
 * <p>
 * Primarily supplies the version and the libraries associated with the Payara location.
 *
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */
public final class PayaraLocationUtils {
    
    public static final String DEFAULT_LIBRARIES = "default";
    public static final String ALL_LIBRARIES = "all";

    private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]\\.[0-9]+(\\.[0-9])?(\\.[0-9])?)(\\..*)?.*");

    // Defined as:
    // <extension point="org.eclipse.wst.common.project.facet.core.runtimes">
    // <runtime-component-type id="payara.runtime"/>
    private static final String RUNTIME_COMPONENT_ID = "payara.runtime";

    private static final Map<File, SoftReference<PayaraLocationUtils>> CACHE = new HashMap<>();

    private final Version version;
    private final Map<String, List<File>> libraries;
    
    
    // #### static factory / finder methods
    

    public static synchronized PayaraLocationUtils find(IJavaProject project) {
        if (project != null) {
            return find(project.getProject());
        }

        return null;
    }

    public static synchronized PayaraLocationUtils find(IProject project) {
        if (project != null) {
            IFacetedProject facetedProject = null;

            try {
                facetedProject = ProjectFacetsManager.create(project);
            } catch (CoreException e) {
                // Intentionally ignored. If project isn't faceted or another error occurs,
                // all that matters is that the Payara install is not found, which is signaled by null
                // return.
            }

            return find(facetedProject);
        }

        return null;
    }
    
    public static synchronized PayaraLocationUtils find(IFacetedProject project) {
        if (project != null) {
            IRuntime primary = project.getPrimaryRuntime();

            if (primary != null) {
                PayaraLocationUtils payaraLocation = find(primary);

                if (payaraLocation != null) {
                    return payaraLocation;
                }

                for (IRuntime runtime : project.getTargetedRuntimes()) {
                    if (runtime != primary) {
                        payaraLocation = find(runtime);

                        if (payaraLocation != null) {
                            return payaraLocation;
                        }
                    }
                }
            }
        }

        return null;
    }
    
    public static synchronized PayaraLocationUtils find(IRuntime runtime) {
        if (runtime != null) {
            for (IRuntimeComponent component : runtime.getRuntimeComponents()) {
                PayaraLocationUtils payaraLocation = find(component);

                if (payaraLocation != null) {
                    return payaraLocation;
                }
            }
        }

        return null;
    }
    
    public static synchronized PayaraLocationUtils find(IRuntimeComponent component) {
        if (component != null && component.getRuntimeComponentType().getId().equals(RUNTIME_COMPONENT_ID)) {
            String location = component.getProperty("location");

            if (location != null) {
                return find(new File(location));
            }
        }

        return null;
    }
    
    public static synchronized PayaraLocationUtils find(File location) {
        
        // Lazily cleanup cache keys
        for (Iterator<Map.Entry<File, SoftReference<PayaraLocationUtils>>> itr = CACHE.entrySet().iterator(); itr.hasNext();) {
            if (itr.next().getValue().get() == null) {
                itr.remove();
            }
        }

        PayaraLocationUtils payaraLocation = null;

        if (location != null) {
            SoftReference<PayaraLocationUtils> payaraLocationReference = CACHE.get(location);

            if (payaraLocationReference != null) {
                payaraLocation = payaraLocationReference.get();
            }

            if (payaraLocation == null) {
                try {
                    payaraLocation = new PayaraLocationUtils(location);
                } catch (IllegalArgumentException e) {
                    return null;
                }

                CACHE.put(location, new SoftReference<>(payaraLocation));
            }
        }

        return payaraLocation;
    }
  
    
    
    // #### PayaraLocation instance methods
    
    private PayaraLocationUtils(File location) {
        checkLocationIsValid(location);

        File payaraLocation = location;

        File gfApiJar = new File(payaraLocation, "modules/glassfish-api.jar");

        if (!gfApiJar.exists()) {
            payaraLocation = new File(payaraLocation, "glassfish");

            gfApiJar = new File(payaraLocation, "modules/glassfish-api.jar");

            if (!gfApiJar.exists()) {
                throw new IllegalArgumentException();
            }
        }

        if (!gfApiJar.isFile()) {
            throw new IllegalArgumentException();
        }

        version = readPayaraVerionFromAPIJar(gfApiJar);
        libraries = readLibraryFilesFromPayaraLocation(payaraLocation, version);
    }

    public Version version() {
        return version;
    }
    
    public List<File> getLibraries(String libraryGroup) {
        return libraries.get(libraryGroup);
    }
    
    
    
    // #### Private methods

    private Version readPayaraVerionFromAPIJar(File gfApiJar) {
        String versionString;
        try {
            versionString = readManifestEntry(gfApiJar, "Bundle-Version");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        Matcher versionMatcher = VERSION_PATTERN.matcher(versionString);

        if (!versionMatcher.matches()) {
            throw new IllegalArgumentException();
        }

        return new Version(versionMatcher.group(1));
    }
    
    /**
     * Gets the relative file name patterns for the system libraries corresponding to the given Payara
     * version, and turns these into a list of actual files for the given Payara location on disk.
     * 
     * @param payaraLocation location where Payara is installed
     * @param payaraVersion version of Payara for which libraries are to be retrieved
     * 
     * @return list of system libraries as actual files
     */
    private Map<String, List<File>> readLibraryFilesFromPayaraLocation(File payaraLocation, Version payaraVersion) {
        Map<String, List<File>> librariesPerVariant = new HashMap<>();
        
        librariesPerVariant.put(
            DEFAULT_LIBRARIES, 
            readLibrariesByPattern(payaraLocation, SystemLibraries.getLibraryIncludesByVersion(payaraVersion)));
        
        librariesPerVariant.put(
            ALL_LIBRARIES, 
            readLibrariesByPattern(payaraLocation, new String[] {"**/*.jar"}, new String[] {"**/osgi-cache/**"}));
        
        return librariesPerVariant;
    }
    
    private List<File> readLibrariesByPattern(File payaraLocation, String[] inclusionPattern) {
        return readLibrariesByPattern(payaraLocation, inclusionPattern, null);
    }
    
    private List<File> readLibrariesByPattern(File payaraLocation, String[] inclusionPattern, String[] exclusionPattern) {
        if (inclusionPattern == null) {
            return emptyList();
        }
        
        File parentFolderToLocation = payaraLocation.getParentFile();
        
        // Use a directory scanner to resolve the wildcards and obtain an expanded
        // list of relative files.
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(parentFolderToLocation);
        scanner.setIncludes(inclusionPattern);
        if (exclusionPattern != null) {
            scanner.setExcludes(exclusionPattern);
        }
        scanner.scan();

        // Turn the expanded, but still relative, string based paths into absolute files.
        List<File> libraries = new ArrayList<>();
        for (String libraryRelativePath : scanner.getIncludedFiles()) {
            libraries.add(new File(parentFolderToLocation, libraryRelativePath));
        }
        
        return libraries;
        
    }
    
    private void checkLocationIsValid(File location) {
        if (location == null || !location.exists() || !location.isDirectory()) {
            throw new IllegalArgumentException();
        }
    }

}
