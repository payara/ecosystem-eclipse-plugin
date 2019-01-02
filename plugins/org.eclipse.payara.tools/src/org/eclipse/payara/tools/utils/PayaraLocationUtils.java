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

package org.eclipse.payara.tools.utils;

import static org.eclipse.jdt.core.IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME;
import static org.eclipse.jdt.core.JavaCore.newClasspathAttribute;
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;
import static org.eclipse.payara.tools.internal.ManifestUtil.readManifestEntry;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.DirectoryScanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.payara.tools.internal.SystemLibrariesSetting;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;

/**
 * Series of utils related to the location where Payara / GlassFish is installed.
 *
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */
public final class PayaraLocationUtils {

    private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]\\.[0-9]+(\\.[0-9])?(\\.[0-9])?)(\\..*)?.*");

    private static final String[] LIBRARIES_3_1 = {
            "glassfish/modules/javax.*.jar",
            "glassfish/modules/weld-osgi-bundle.jar",
            "glassfish/modules/bean-validator.jar",
            "glassfish/modules/jersey-*.jar",
            "glassfish/modules/grizzly-comet.jar",
            "glassfish/modules/grizzly-websockets.jar",
            "glassfish/modules/glassfish-api.jar",
            "glassfish/modules/ha-api.jar",
            "glassfish/modules/endorsed/*.jar",
            "glassfish/modules/jsf-api.jar",
            "glassfish/modules/jsf-impl.jar",
            "glassfish/modules/jstl-impl.jar",
            "glassfish/modules/org.eclipse.persistence*.jar",
            "glassfish/modules/jaxb*.jar",
            "glassfish/modules/webservices*.jar",
            "glassfish/modules/woodstox-osgi*.jar",
            "mq/lib/jaxm-api*.jar"
    };

    private static final String[] LIBRARIES_3_1_2 = {
            "glassfish/modules/javax.*.jar",
            "glassfish/modules/weld-osgi-bundle.jar",
            "glassfish/modules/bean-validator.jar",
            "glassfish/modules/jersey-*.jar",
            "glassfish/modules/grizzly-comet.jar", //
            "glassfish/modules/grizzly-websockets.jar", //
            "glassfish/modules/glassfish-api.jar",
            "glassfish/modules/ha-api.jar",
            "glassfish/modules/endorsed/*.jar",
            "glassfish/modules/org.eclipse.persistence*.jar",
            "glassfish/modules/jaxb*.jar",
            "glassfish/modules/webservices*.jar",
            "glassfish/modules/woodstox-osgi*.jar", //
            "mq/lib/jaxm-api*.jar"
    };

    private static final String[] LIBRARIES_4 = {
            "glassfish/modules/javax.*.jar",
            "glassfish/modules/weld-osgi-bundle.jar",
            "glassfish/modules/bean-validator.jar",
            "glassfish/modules/jersey-*.jar",
            "glassfish/modules/glassfish-api.jar",
            "glassfish/modules/ha-api.jar",
            "glassfish/modules/endorsed/*.jar",
            "glassfish/modules/org.eclipse.persistence*.jar",
            "glassfish/modules/jaxb*.jar",
            "glassfish/modules/webservices*.jar",
            "glassfish/modules/cdi-api.jar", // +
            "mq/lib/jaxm-api.jar"
    };

    private static final String[] LIBRARIES_5 = LIBRARIES_4;

    // Defined as:
    // <extension point="org.eclipse.wst.common.project.facet.core.runtimes">
    // <runtime-component-type id="payara.runtime"/>
    private static final String RUNTIME_COMPONENT_ID = "payara.runtime"; //$NON-NLS-1$

    private static final Map<File, SoftReference<PayaraLocationUtils>> CACHE = new HashMap<>();

    private final Version version;
    private final List<File> libraries;
    
    
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

        if (location == null || !location.exists() || !location.isDirectory()) {
            throw new IllegalArgumentException();
        }

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

        ListFactory<File> librariesListFactory = ListFactory.start();
        String[] libraryIncludes = getLibraryIncludes(version);

        if (libraryIncludes != null) {
            File parentFolderToLocation = payaraLocation.getParentFile();
            DirectoryScanner scanner = new DirectoryScanner();

            scanner.setBasedir(parentFolderToLocation);
            scanner.setIncludes(libraryIncludes);
            scanner.scan();

            for (String libraryRelativePath : scanner.getIncludedFiles()) {
                librariesListFactory.add(new File(parentFolderToLocation, libraryRelativePath));
            }
        }

        libraries = librariesListFactory.result();
    }

    public Version version() {
        return version;
    }

    public List<IClasspathEntry> classpath(IProject project) {
        ListFactory<IClasspathEntry> classpathListFactory = ListFactory.start();

        URL doc;
        String javaEEVersion = (version.matches("[5") ? "8" : (version.matches("[4") ? "7" : "6"));

        try {
            doc = new URL("http://docs.oracle.com/javaee/" + javaEEVersion + "/api/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        SystemLibrariesSetting libSettings = SystemLibrariesSetting.load(project);

        for (File library : libraries) {
            File srcPath = libSettings != null ? libSettings.getSourcePath(library) : null;
            classpathListFactory.add(createLibraryEntry(new Path(library.toString()), srcPath, doc));
        }

        return classpathListFactory.result();
    }
    
    
    
    // #### Private methods

    private IClasspathEntry createLibraryEntry(final IPath library, final File src, final URL javadoc) {
        IPath srcpath = src == null ? null : new Path(src.getAbsolutePath());
        IAccessRule[] access = {};
        IClasspathAttribute[] attrs;

        if (javadoc == null) {
            attrs = new IClasspathAttribute[0];
        } else {
            attrs = new IClasspathAttribute[] { newClasspathAttribute(JAVADOC_LOCATION_ATTRIBUTE_NAME, javadoc.toExternalForm()) };
        }

        return newLibraryEntry(library, srcpath, null, access, attrs, false);
    }
    
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
    
    private String[] getLibraryIncludes(Version version) {

        if (version.matches("[5")) {
            return LIBRARIES_5;
        }

        if (version.matches("[4-5)")) {
            return LIBRARIES_4;
        }

        if (version.matches("[3.1.2-4)")) {
            return LIBRARIES_3_1_2;
        }

        if (version.matches("[3.1-3.1.2)")) {
            return LIBRARIES_3_1;
        }

        return null;
    }

}
