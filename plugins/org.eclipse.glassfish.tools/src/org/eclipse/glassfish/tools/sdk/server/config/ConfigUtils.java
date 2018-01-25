/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.server.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.glassfish.tools.sdk.data.GlassFishLibrary;
import org.eclipse.glassfish.tools.sdk.logging.Logger;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ConfigUtils {

    /** Maven Group ID property name. */
    private static final String MVN_PROP_GROUP_ID = "groupId";

    /** Maven Artifact ID property name. */
    private static final String MVN_PROP_ARTIFACT_ID = "artifactId";

    /** Maven Version property name. */
    private static final String MVN_PROP_VERSION = "version";

    /** */
    private static final Pattern MVN_PROPS_PATTERN
            = Pattern.compile("META-INF/maven/[^/]+/[^/]+/pom.properties");

    /**
     * Convert {@link File} to {@link URL}.
     * <p/>
     * @param file {@link File} to be converted to {@link URL}.
     */
    static URL fileToURL(File file) {
        try {
            return file != null ? file.toURI().normalize().toURL() : null;
        } catch (MalformedURLException ex) {
            Logger.log(Level.WARNING, "Unable to convert file "
                    + file.getAbsolutePath() + " to URL", ex);
            return null;
        }
    }

    /**
     * Process <code>List</code> of links from library node and convert them
     * to <code>List</code> of {@link URL}s.
     * <p/>
     * @param fileset Library node.
     * @return <code>List</code> of {@link URL}s from library node.
     */
    static List<URL> processLinks(FileSet fileset) {
        List<String> links = fileset.getLinks();
        ArrayList<URL> result = new ArrayList<>(links.size());
        for (String urlString : links) {
            try {
                result.add(new URL(urlString));
            } catch (MalformedURLException mue) {
                Logger.log(Level.WARNING, "Cannot process URL: " + urlString
                        + ".", mue);
            }
        }
        return result;
    }

    /**
     * Process <code>List</code> of links from library node and convert them
     * of <code>List</code> of {@link File}s.
     * <p/>
     * @param fileset Library node.
     * @param rootDir File system search root.
     * @return <code>List</code> of {@link File}s from library node.
     * @throws FileNotFoundException When file from paths element was not found.
     */
    static List<File> processFileset(FileSet fileset, String rootDir)
            throws FileNotFoundException {
        Map<String, List<String>> filesets = fileset.getFilesets();
        List<String> paths = fileset.getPaths();
        ArrayList<File> result = new ArrayList<>();

        for (String dir : filesets.keySet()) {
            File d = new File(dir);
            String dirPrefix;
            if (!d.isAbsolute()) {
                dirPrefix = new File(rootDir, d.getPath()).getAbsolutePath();
            } else {
                dirPrefix = d.getAbsolutePath();
            }
            
            List<Pattern> patterns = compilePatterns(filesets.get(dir));
            File[] fileArray = new File(dirPrefix).listFiles(createFilter(
                    patterns));
            if (fileArray != null) {
                Collections.addAll(result, fileArray);
            }
        }
        
        for (String path : paths) {
            File f = new File(path);
            if (!f.isAbsolute()) {
                f = new File(rootDir, f.getPath());
            }
            if (!f.exists()) {
                throw new FileNotFoundException("File with name "
                        + path + " does not exist.");
            }
            result.add(f);
        }
        
        return result;
    }

    /**
     * Search class path for Maven information.
     * <p/>
     * @param classpath List of class path JAR files.
     * @return List of Maven information
     */
    static List<GlassFishLibrary.Maven> processClassPath(List<File> classpath) {
        List<GlassFishLibrary.Maven> mvnList = new LinkedList<>();
        for (File jar : classpath) {
            ZipFile zip = null;
            try {
                zip = new ZipFile(jar);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    Matcher matcher
                            = MVN_PROPS_PATTERN.matcher(entry.getName());
                    if (matcher.matches()) {
                        GlassFishLibrary.Maven mvnInfo
                                = getMvnInfoFromProperties(zip.getInputStream(
                                entry));
                        if (mvnInfo != null) {
                            mvnList.add(mvnInfo);
                            break;
                        }
                    }
                }
            } catch (ZipException ze) {
                 Logger.log(Level.WARNING, "Cannot open JAR file "
                         + jar.getAbsolutePath() + ":", ze);
            } catch (IOException ioe) {
                 Logger.log(Level.WARNING, "Cannot process JAR file "
                         + jar.getAbsolutePath() + ":", ioe);
            } catch (IllegalStateException ise) {
                 Logger.log(Level.WARNING, "Cannot process JAR file "
                         + jar.getAbsolutePath() + ":", ise);
            } finally {
                if (zip != null) try {
                    zip.close();
                } catch (IOException ioe) {
                    Logger.log(Level.WARNING, "Cannot close JAR file "
                         + jar.getAbsolutePath() + ":", ioe);
                }
            }
            
        }
        return mvnList;
    }

    /**
     * Process <code>pom.properties</code> content to retrieve Maven information
     * from JAR.
     * <p/>
     * @param propStream Input stream to read <code>pom.properties</code>
     *                   file from JAR.
     */
    private static GlassFishLibrary.Maven getMvnInfoFromProperties(
            InputStream propStream) throws IOException {
        Properties props = new Properties();
        props.load(propStream);
        String groupId = props.getProperty(MVN_PROP_GROUP_ID);
        String artifactId = props.getProperty(MVN_PROP_ARTIFACT_ID);
        String version = props.getProperty(MVN_PROP_VERSION);
        if (groupId != null && artifactId != null && version != null) {
            return new GlassFishLibrary.Maven(groupId, artifactId, version);
        } else {
            return null;
        }
    }

    /**
     * Creates file name filter from <code>List</code>
     * of <cpode>Pattern</code>s.
     * <p/>
     * @param patterns <code>List</code> of <cpode>Pattern</code>s.
     * @return File name filter.
     */
    private static FilenameFilter createFilter(final List<Pattern> patterns) {
        return new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                for (Pattern p : patterns) {
                    if (p.matcher(name).matches()) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Compile pattern <code>String</code>s.
     * <p/>
     * @param names <code>List</code> of pattern <code>String</code>s.
     * @return <code>List</code> of compiled <code>Pattern</code>s.
     */
    private static List<Pattern> compilePatterns(List<String> names) {
        ArrayList<Pattern> patterns = new ArrayList<>(names.size());
        for (String name : names) {
            patterns.add(Pattern.compile(name));
        }
        return patterns;
    }

}
