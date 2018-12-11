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
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server;

import java.util.Optional;

/**
 * A simple class that fills a hole in the JDK.  It parses out the version numbers
 * of the JDK we are running.
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
     * See if the current JDK is legal for running GlassFish
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

    public static class Version {
        
    	private final int major;
        private final Optional<Integer> minor;
        private final Optional<Integer> subminor;
        private final Optional<Integer> update;

        private Version(String string) {
            // Split java version into its constituent parts, i.e.
            // 1.2.3.4 -> [ 1, 2, 3, 4]
            // 1.2.3u4 -> [ 1, 2, 3, 4]
            // 1.2.3_4 -> [ 1, 2, 3, 4]
            String[] split = string.split("[\\._u\\-]+");

            major = split.length > 0 ? Integer.parseInt(split[0]) : 0;
            minor = split.length > 1 ? Optional.of(Integer.parseInt(split[1])) : Optional.empty();
            subminor = split.length > 2 ? Optional.of(Integer.parseInt(split[2])) : Optional.empty();
            update = split.length > 3 ? Optional.of(Integer.parseInt(split[3])) : Optional.empty();
        }
        
        private Version(int major, int minor, int subminor, int update) {
        	this.major = major;
        	this.minor = Optional.of(minor);
        	this.subminor = Optional.of(subminor);
            this.update = Optional.of(update);
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
            if(!leftHandSide.isPresent() || !rightHandSide.isPresent()) {
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
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
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
        if (string != null && string.matches("([0-9]+[\\._u\\-]+)*[0-9]+")) {
            // Make sure the string is a valid JDK version, i.e.
            // 1.8.0_162 or something that is returned by "java -version"
            return new Version(string);
        }
        
        return null;
    }
    
    public static Version getVersion(String jv, String javaSpecificationVersion) {
        int[] versions = parseVersions(jv, javaSpecificationVersion);
        
		return new Version(versions[MAJOR], versions[MINOR], versions[SUBMINOR], versions[UPDATE]);
    }

    public static Version getVersion() {
        return new Version(major, minor, subminor, update);
    }

    public static boolean isCorrectJDK(Optional<Version> minVersion, Optional<Version> maxVersion) {
       return isCorrectJDK(JDK_VERSION, minVersion, maxVersion);
    }
    
    public static boolean isCorrectJDK(Version JDKversion, Optional<Version> minVersion, Optional<Version> maxVersion) {
        boolean correctJDK = true;
        
        if (minVersion.isPresent()) {
            correctJDK = JDKversion.newerOrEquals(minVersion.get());
        }
        if (correctJDK && maxVersion.isPresent()) {
            correctJDK = JDKversion.olderOrEquals(maxVersion.get());
        }
        
        return correctJDK;
    }
       

    /**
     * No instances are allowed so it is pointless to override toString
     * @return Parsed version numbers
     */
    public static String toStringStatic() {
        return "major: " + JDK.getMajor() +
        "\nminor: " + JDK.getMinor() +
        "\nsubminor: " + JDK.getSubMinor() +
        "\nupdate: " + JDK.getUpdate() +
        "\nOK ==>" + JDK.ok();
    }

    static {
        initialize();
    }

    // DO NOT initialize these variables.  You'll be sorry if you do!
    private static int major;
    private static int minor;
    private static int subminor;
    private static int update;
    
    // DO initialize these variables.  You'll be sorry if you don't!
    private final static int MAJOR = 0;
    private final static int MINOR = 1;
    private final static int SUBMINOR = 2;
    private final static int UPDATE = 3;
    
    // DO NOT initialize this variable.  You'll again be sorry if you do!
    private static Version JDK_VERSION;

    private static void initialize() {
    	
    	// Silently fall back to ridiculous defaults if something is crazily wrong...
        major = 1;
        minor = subminor = update = 0;
        
        try {
            String jv = System.getProperty("java.version");
            /*In JEP 223 java.specification.version will be a single number versioning , not a dotted versioning . So if we get a single
            integer as versioning we know that the JDK is post JEP 223
            For JDK 8:
                java.specification.version  1.8
                java.version    1.8.0_122
             For JDK 9:
                java.specification.version 9
                java.version 9.1.2
            */
            String javaSpecificationVersion = System.getProperty("java.specification.version");
            
            int[] versions = parseVersions(jv, javaSpecificationVersion);
            
            major = versions[MAJOR];
            minor = versions[MINOR];
            subminor = versions[SUBMINOR];
            update = versions[UPDATE];
        }
        catch(Exception e) {
            // ignore -- use defaults
        }

        JDK_VERSION = new Version(major, minor, subminor, update);
    }
    
    static int[] parseVersions(String jv, String javaSpecificationVersion) {
    	
    	int[] versions = {1, 0, 0, 0};
    	
    	String[] jsvSplit = javaSpecificationVersion.split("\\.");
        if (jsvSplit.length == 1) {
            
        	// This is handle early access builds. For example "9-ea"
        	
            String[] jvSplit = jv.split("-");
            String jvReal = jvSplit[0];
            String[] split = jvReal.split("[\\.]+");

            if (split.length > 0) {
                if (split.length > 0) {
                	versions[MAJOR] = Integer.parseInt(split[0]);
                }
                if (split.length > 1) {
                	versions[MINOR] = Integer.parseInt(split[1]);
                }
                if (split.length > 2) {
                	versions[SUBMINOR] = Integer.parseInt(split[2]);
                }
                if (split.length > 3) {
                	versions[UPDATE] = Integer.parseInt(split[3]);
                }
            }
        } else {
        	if (jv == null || jv.length() <= 0) {
                return versions; // not likely!!
        	}

            String[] ss = jv.split("\\.");

            if (ss.length < 3 || !ss[0].equals("1")) {
                return versions;
            }

            versions[MAJOR] = Integer.parseInt(ss[0]);
            versions[MINOR] = Integer.parseInt(ss[1]);
            
            
            ss = ss[2].split("_");
            if (ss.length < 1) {
                return versions;
            }

            versions[SUBMINOR] = Integer.parseInt(ss[0]);

            if (ss.length > 1) {
            	versions[UPDATE] = Integer.parseInt(ss[1]);
            }
        }
        
        return versions;
    }
}
