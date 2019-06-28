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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.payara.tools.PayaraToolsPlugin;
import org.eclipse.sapphire.Filter;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.modeling.Status;
import org.osgi.framework.Bundle;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JdtUtil {
    @Text("Java installation not found in the specified folder")
    private static LocalizableText invalidJavaLocationMessage;

    @Text("Java Development Kit (JDK) is required rather than a JRE")
    private static LocalizableText jdkIsRequiredMessage;

    @Text("Java {0} is required")
    private static LocalizableText javaVersionRequiredSingle;

    @Text("Java {0} or higher is required")
    private static LocalizableText javaVersionRequiredMin;

    @Text("Java {0} or lower is required")
    private static LocalizableText javaVersionRequiredMax;

    @Text("Java {0} or {1} is required")
    private static LocalizableText javaVersionRequiredOr;

    @Text("Java {0} through {1} is required")
    private static LocalizableText javaVersionRequiredRange;

    static {
        LocalizableText.init(JdtUtil.class);
    }

    private static final Map<File, String> jvmLocationToVersionMap = new HashMap<>();

    public static final class JvmValidator {

        private File location;
        private Status status;

        private JvmValidator(final File location) {
            this.location = location;
            this.status = (isValidJvmInstall(location) ? Status.createOkStatus()
                    : Status.createErrorStatus(invalidJavaLocationMessage.text()));
        }

        public JvmValidator jdk() {
            return this;
        }

        public JvmValidator version(final String constraint) {
            if (this.status.ok()) {
                version(new VersionConstraint(constraint));
            }

            return this;
        }

        /**
         * Adds a Java version constraint.
         *
         * @param constraint the Java version constraint or null for no constraint
         * @return this JvmValidator object for method chaining
         */

        public JvmValidator version(final VersionConstraint constraint) {
            if (this.status.ok() && constraint != null) {
                if (constraint.ranges().size() != 1) {
                    throw new IllegalArgumentException();
                }

                final VersionConstraint.Range range = constraint.ranges().get(0);
                final VersionConstraint.Range.Limit min = range.min();
                final VersionConstraint.Range.Limit max = range.max();

                if (min != null && !min.inclusive()) {
                    throw new IllegalArgumentException();
                }

                if (max != null && !max.inclusive()) {
                    throw new IllegalArgumentException();
                }

                String version = detectJavaVersion(this.location);

                if (version != null) {
                    final String[] segments = version.split("\\.");

                    if (segments.length >= 2) {
                        version = segments[0] + "." + segments[1];
                    }
                }

                if (version == null || !constraint.check(version)) {
                    if (min == null) {
                        this.status = Status.createErrorStatus(javaVersionRequiredMax.format(toDisplayVersion(max.version())));
                    } else {
                        if (max == null) {
                            this.status = Status.createErrorStatus(javaVersionRequiredMin.format(toDisplayVersion(min.version())));
                        } else {
                            final Version minver = min.version();
                            final Version maxver = max.version();

                            if (minver.equals(maxver)) {
                                this.status = Status.createErrorStatus(javaVersionRequiredSingle.format(toDisplayVersion(minver)));
                            } else if (minver.segments().size() == 2 && maxver.segments().size() == 2
                                    && minver.segment(1) + 1L == maxver.segment(1)) {
                                this.status = Status.createErrorStatus(
                                        javaVersionRequiredOr.format(toDisplayVersion(minver), toDisplayVersion(maxver)));
                            } else {
                                this.status = Status.createErrorStatus(
                                        javaVersionRequiredRange.format(toDisplayVersion(minver), toDisplayVersion(maxver)));
                            }
                        }
                    }
                }
            }

            return this;
        }

        public Status result() {
            return this.status;
        }

        private static String toDisplayVersion(final Version version) {
            final List<Long> segments = version.segments();

            if (segments.size() == 2) {
                final Long secondSegment = segments.get(1);

                if (secondSegment >= 5L) {
                    return String.valueOf(secondSegment);
                }
            }

            return version.toString();
        }
    }

    public static JvmValidator validateJvm(final File location) {
        return new JvmValidator(location);
    }

    public static JvmValidator validateJvm(final IVMInstall jvm) {
        return new JvmValidator(jvm.getInstallLocation());
    }

    public static boolean isValidJvmInstall(final IPath location) {
        return (location != null && isValidJvmInstall(location.toFile()));
    }

    public static boolean isValidJvmInstall(final File location) {
        return (location != null && findJavaExecutable(location) != null);
    }

    public static IVMInstallType findStandardJvmType() {
        return JavaRuntime.getVMInstallType("org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType");
    }

    public static IVMInstall findOrCreateJvm(final String location) {
        if (location != null) {
            return findOrCreateJvm(new File(location));
        }

        return null;
    }

    public static IVMInstall findOrCreateJvm(final File location) {
        IVMInstall jvm = null;

        if (isValidJvmInstall(location)) {
            jvm = findJvmByLocation(location);

            if (jvm == null) {
                final String jvmName = findUniqueJvmName(location.getName());
                final IVMInstallType standardJvmType = findStandardJvmType();

                final VMStandin jvmStandin = new VMStandin(standardJvmType, jvmName);
                jvmStandin.setName(jvmName);
                jvmStandin.setInstallLocation(location);

                jvm = jvmStandin.convertToRealVM();

                try {
                    JavaRuntime.saveVMConfiguration();
                    JavaRuntime.getDefaultVMInstall();
                } catch (final Exception e) {
                    PayaraToolsPlugin.log(e);
                }
            }
        }

        return jvm;
    }

    public static final IVMInstall findJvmByLocation(final String location) {
        if (location != null) {
            return findJvmByLocation(new File(location));
        }

        return null;
    }

    public static final IVMInstall findJvmByLocation(final File location) {
        if (location != null) {
            for (final IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
                for (final IVMInstall jvm : type.getVMInstalls()) {
                    if (location.equals(jvm.getInstallLocation())) {
                        return jvm;
                    }
                }
            }
        }

        return null;
    }

    public static final IVMInstall findJvmByName(final String name) {
        if (name != null) {
            for (final IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
                final IVMInstall jvm = type.findVMInstallByName(name);

                if (jvm != null) {
                    return jvm;
                }
            }
        }

        return null;
    }

    public static IVMInstall findJdkByVersion(final VersionConstraint versionConstraint) {
        return findJvm(new JdkFilter(versionConstraint));
    }

    public static IVMInstall findJvm(final Filter<IVMInstall> filter) {
        IVMInstall jvm = null;

        for (final IVMInstallType vmInstallType : JavaRuntime.getVMInstallTypes()) {
            for (final IVMInstall vmInstall : vmInstallType.getVMInstalls()) {
                if (filter.allows(vmInstall)) {
                    jvm = newer(jvm, vmInstall);
                }
            }
        }

        return (jvm == null ? null : jvm);
    }

    public static String findUniqueJvmName(final String baseName) {
        if (baseName == null) {
            throw new IllegalArgumentException();
        }

        int counter = 0;
        boolean unique = false;
        String name = null;

        while (!unique) {
            counter++;
            name = baseName + (counter == 1 ? "" : " (" + counter + ")");
            unique = (findJvmByName(name) == null);
        }

        return name;
    }

    public static IVMInstall newer(final IVMInstall a, final IVMInstall b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            final String av = ((IVMInstall2) a).getJavaVersion();
            final String bv = ((IVMInstall2) b).getJavaVersion();

            if (new Version(av).compareTo(new Version(bv)) >= 0) {
                return a;
            } else {
                return b;
            }
        }
    }

    public static String detectJavaVersion(final File location) {
        if (location != null) {
            synchronized (jvmLocationToVersionMap) {
                String version = jvmLocationToVersionMap.get(location);

                if (version == null) {
                    final File exec = findJavaExecutable(location);

                    if (exec != null) {
                        try {
                            final Bundle bundle = Platform.getBundle("org.eclipse.payara.tools.jver");

                            File cp = FileLocator.getBundleFile(bundle);

                            if (cp.isDirectory()) {
                                cp = new File(cp, "bin");
                            }

                            final Process process = Runtime.getRuntime().exec(
                                    new String[] {
                                            exec.getAbsolutePath(),
                                            "-cp",
                                            cp.getAbsolutePath(),
                                            "org.eclipse.payara.tools.jver.JavaVersionDetector"
                                    });

                            final StreamGobbler outStreamGobbler = new StreamGobbler(process.getInputStream());
                            final StreamGobbler errStreamGobbler = new StreamGobbler(process.getErrorStream());

                            outStreamGobbler.start();
                            errStreamGobbler.start();

                            try {
                                process.waitFor();
                            } catch (final InterruptedException e) {
                            }

                            final String output = outStreamGobbler.output().trim() + errStreamGobbler.output().trim();

                            if (output.length() > 0) {
                                version = output;
                                jvmLocationToVersionMap.put(location, version);
                            }
                        } catch (final Exception e) {
                            PayaraToolsPlugin.log(e);
                        }
                    }
                }

                return version;
            }
        }

        return null;
    }

    private static final String[] fgCandidateJavaFiles = { "java", "java.exe" };
    private static final String[] fgCandidateJavaLocations = { "bin" + File.separatorChar,
            "jre" + File.separatorChar + "bin" + File.separatorChar };

    private static File findJavaExecutable(File vmInstallLocation) {
        // Try each candidate in order. The first one found wins. Thus, the order
        // of fgCandidateJavaLocations and fgCandidateJavaFiles is significant.
        for (String fgCandidateJavaFile : fgCandidateJavaFiles) {
            for (String fgCandidateJavaLocation : fgCandidateJavaLocations) {
                File javaFile = new File(vmInstallLocation, fgCandidateJavaLocation + fgCandidateJavaFile);
                if (javaFile.isFile()) {
                    return javaFile;
                }
            }
        }
        return null;
    }

    public static void addToClasspath(IJavaProject project, IClasspathEntry entry) throws CoreException {
        final IClasspathEntry[] oldEntries = project.getRawClasspath();

        for (IClasspathEntry x : oldEntries) {
            if (x.equals(entry)) {
                return;
            }
        }

        int oldEntriesLength = oldEntries.length;
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntriesLength + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntriesLength);

        newEntries[oldEntriesLength] = entry;

        project.setRawClasspath(newEntries, null);
    }

}
