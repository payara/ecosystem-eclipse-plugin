/******************************************************************************
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/******************************************************************************
 * Copyright (c) 2018-2026 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.sdk.server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A simple class that fills a hole in the JDK. It parses out the version
 * numbers of the JDK we are running.
 *
 * <p>
 * Example: <br>
 *
 * 1.6.0_u14 == major = 1 minor = 6, subminor = 0, update = 14
 *
 * @author bnevins
 */
public final class JDK {

    /**
     * See if the current JDK is legal for running Payara
     *
     * @return true if the JDK is >= 1.6.0
     */
    public static boolean ok() {
        return major == 1 && minor >= 6;
    }

    public static int getMajor() {
        return major;
    }

    public static int getMinor() {
        return minor;
    }

    public static int getSubMinor() {
        return subminor;
    }

    public static int getUpdate() {
        return update;
    }

    public static String getVendor() {
        return vendor;
    }

    public static class Version {

        private final int major;
        private final Optional<Integer> minor;
        private final Optional<Integer> subminor;
        private final Optional<Integer> update;
        private final Optional<String> vendor;

        private Version(String version, String vendor) {
            int[] versions = parseVersions(version);
            this.major = versions[MAJOR_INDEX];
            this.minor = Optional.ofNullable(versions[MINOR_INDEX]);
            this.subminor = Optional.ofNullable(versions[SUBMINOR_INDEX]);
            this.update = Optional.ofNullable(versions[UPDATE_INDEX]);
            this.vendor = Optional.ofNullable(vendor);
        }

        Version(int major, int minor, int subminor, int update, String vendor) {
            this.major = major;
            this.minor = Optional.ofNullable(minor);
            this.subminor = Optional.ofNullable(subminor);
            this.update = Optional.ofNullable(update);
            this.vendor = Optional.ofNullable(vendor);
        }

        public boolean newerThan(Version version) {
            if (major > version.major) {
                return true;
            }

            if (major == version.major) {
                if (greaterThan(minor, version.minor)) {
                    return true;
                }

                if (equals(minor, version.minor)) {
                    if (greaterThan(subminor, version.subminor)) {
                        return true;
                    }

                    if (equals(subminor, version.subminor)) {
                        if (greaterThan(update, version.update)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        public boolean olderThan(Version version) {
            if (major < version.major) {
                return true;
            } else if (major == version.major) {
                if (lessThan(minor, version.minor)) {
                    return true;
                } else if (equals(minor, version.minor)) {
                    if (lessThan(subminor, version.subminor)) {
                        return true;
                    } else if (equals(subminor, version.subminor)) {
                        if (lessThan(update, version.update)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        private static boolean greaterThan(Optional<Integer> leftHandSide, Optional<Integer> rightHandSide) {
            return leftHandSide.orElse(0) > rightHandSide.orElse(0);
        }

        private static boolean lessThan(Optional<Integer> leftHandSide, Optional<Integer> rightHandSide) {
            return leftHandSide.orElse(0) < rightHandSide.orElse(0);
        }

        /**
         * if either left-hand-side or right-hand-side is empty, it is equals
         *
         * @param leftHandSide
         * @param rightHandSide
         * @return true if equals, otherwise false
         */
        private static boolean equals(Optional<Integer> leftHandSide, Optional<Integer> rightHandSide) {
            if (!leftHandSide.isPresent() || !rightHandSide.isPresent()) {
                return true;
            }
            return leftHandSide.orElse(0).equals(rightHandSide.orElse(0));
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + this.major;
            hash = 61 * hash + this.minor.orElse(0);
            hash = 61 * hash + this.subminor.orElse(0);
            hash = 61 * hash + this.update.orElse(0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            final Version other = (Version) obj;
            if (this.major != other.major) {
                return false;
            }
            if (!equals(this.minor, other.minor)) {
                return false;
            }
            if (!equals(this.subminor, other.subminor)) {
                return false;
            }
            if (!equals(this.update, other.update)) {
                return false;
            }
            return true;
        }

        public boolean newerOrEquals(Version version) {
            return newerThan(version) || equals(version);
        }

        public boolean olderOrEquals(Version version) {
            return olderThan(version) || equals(version);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(10);
            sb.append(major);
            if (minor.isPresent()) {
                sb.append('.').append(minor.get());
            }
            if (subminor.isPresent()) {
                sb.append('.').append(subminor.get());
            }
            if (update.isPresent()) {
                sb.append('.').append(update.get());
            }
            return sb.toString();
        }
    }

    public static Version getVersion(String string) {
        return getVersion(string, null);
    }

    public static Version getVersion(String version, String vendor) {
        if (version != null && version.matches(VERSION_MATCHER)) {
            // Make sure the string is a valid JDK version, i.e.
            // 1.8.0_162 or something that is returned by "java -version"
            return new Version(version, vendor);
        }

        return null;
    }

    public static Version getVersion() {
        return new Version(major, minor, subminor, update, vendor);
    }

    public static boolean isCorrectJDK(Optional<Version> minVersion,
            Optional<Version> maxVersion) {
        return isCorrectJDK(
                JDK_VERSION,
                Optional.empty(),
                minVersion,
                maxVersion,
                null,
                null
        );
    }

    /**
     * Checks whether a JVM option is applicable for the given JDK.
     * Applies vendor, min/max version checks and additionally validates
     * CRaC-related options against the selected JDK.
     *
     * @param version     JDK version used to start the server
     * @param vendor      optional vendor restriction
     * @param minVersion  optional minimum supported JDK version
     * @param maxVersion  optional maximum supported JDK version
     * @param jvmOption   JVM option string from domain.xml
     * @param javaHome    Java home of the selected server JDK
     * @return true if the option is valid for the given JDK
     */
    public static boolean isCorrectJDK(
            Version version,
            Optional<String> vendor,
            Optional<Version> minVersion,
            Optional<Version> maxVersion,
            String jvmOption,
            String javaHome) {

        boolean correctJDK = true;

        if (vendor.isPresent()) {
            if (version.vendor.isPresent()) {
                correctJDK = version.vendor.get().contains(vendor.get());
            } else {
                correctJDK = false;
            }
        }

        if (correctJDK && minVersion.isPresent()) {
            correctJDK = version.newerOrEquals(minVersion.get());
        }

        if (correctJDK && maxVersion.isPresent()) {
            correctJDK = version.olderOrEquals(maxVersion.get());
        }

        if (correctJDK
                && jvmOption != null
                && jvmOption.matches("^-XX:[+-]?CRaC.*")) {

            correctJDK = isCRaCJDK(javaHome);
        }

        return correctJDK;
    }

    /**
     * Checks whether the given JDK installation supports CRaC by verifying the
     * presence of the lib/criu directory.
     *
     * @param javaHome Java home directory to check
     * @return true if the JDK appears to be CRaC-enabled
     */
    public static boolean isCRaCJDK(String javaHome) {
        return Optional.ofNullable(javaHome)
                .map(home -> Path.of(home, "lib", "criu"))
                .map(Files::exists)
                .orElse(false);
    }

    /**
     * No instances are allowed so it is pointless to override toString
     *
     * @return Parsed version numbers
     */
    public static String toStringStatic() {
        return "major: " + JDK.getMajor()
                + "\nminor: " + JDK.getMinor()
                + "\nsubminor: " + JDK.getSubMinor()
                + "\nupdate: " + JDK.getUpdate()
                + "\nOK ==>" + JDK.ok();
    }

    static {
        initialize();
    }

    // DO NOT initialize these variables.  You'll be sorry if you do!
    private static int major;
    private static int minor;
    private static int subminor;
    private static int update;
    private static String vendor;

    // DO initialize these variables.  You'll be sorry if you don't!
    private final static int MAJOR_INDEX = 0;
    private final static int MINOR_INDEX = 1;
    private final static int SUBMINOR_INDEX = 2;
    private final static int UPDATE_INDEX = 3;

    // DO NOT initialize this variable.  You'll again be sorry if you do!
    public static Version JDK_VERSION;

    private static final String VERSION_MATCHER = "(\\d+(\\.\\d+)*)([_u\\-]+[\\S]+)*";

    private static void initialize() {

    	// Silently fall back to ridiculous defaults if something is crazily wrong...
        major = 1;
        minor = subminor = update = 0;

            String javaVersion = System.getProperty("java.version");
            vendor = System.getProperty("java.vendor");
            /*In JEP 223 java.specification.version will be a single number versioning , not a dotted versioning . So if we get a single
            integer as versioning we know that the JDK is post JEP 223
            For JDK 8:
                java.specification.version  1.8
                java.version    1.8.0_122
             For JDK 9:
                java.specification.version 9
                java.version 9.1.2
             */
            int[] versions = parseVersions(javaVersion);

            major = versions[MAJOR_INDEX];
            minor = versions[MINOR_INDEX];
            subminor = versions[SUBMINOR_INDEX];
            update = versions[UPDATE_INDEX];

        JDK_VERSION = new Version(
                major,
                minor,
                subminor,
                update,
                vendor
        );
    }

    /**
     *
     * @param javaVersion the Java Version e.g 1.8.0u222,
     * 1.8.0_232-ea-8u232-b09-0ubuntu1-b09, 11.0.5
     * @return
     */
    static int[] parseVersions(String javaVersion) {

        int[] versions = {1, 0, 0, 0};
        if (javaVersion == null || javaVersion.length() <= 0) {
            return versions; // not likely!!
        }
        String[] javaVersionSplit = javaVersion.split("-");
        String[] split = javaVersionSplit[0].split("\\.");

        if (split.length > 0) {
            if (split.length > 0) {
                versions[MAJOR_INDEX] = Short.parseShort(split[0]);
            }
            if (split.length > 1) {
                versions[MINOR_INDEX] = Short.parseShort(split[1]);
            }
            if (split.length > 2) {
                split = split[2].split("[_u]");
                versions[SUBMINOR_INDEX] = Short.parseShort(split[0]);
                if (split.length > 1) {
                    versions[UPDATE_INDEX] = Short.parseShort(split[1]);
                }
            }
        }
        return versions;
    }
}
