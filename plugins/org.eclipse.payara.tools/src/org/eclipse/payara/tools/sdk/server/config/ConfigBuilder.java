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

package org.eclipse.payara.tools.sdk.server.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.sdk.data.GlassFishConfig;
import org.eclipse.payara.tools.sdk.data.GlassFishJavaEEConfig;
import org.eclipse.payara.tools.sdk.data.GlassFishJavaSEConfig;
import org.eclipse.payara.tools.sdk.data.GlassFishLibrary;
import org.eclipse.sapphire.Version;

/**
 * Provides GlassFish library information from XML configuration files.
 * <p/>
 * Instance of library builder for single version of GlassFish server. Version of GlassFish server
 * is supplied with first configuration getter call. Each subsequent configuration getter call on
 * the same instance must be used with the same GlassFish version.
 * <p/>
 * XML configuration file is read just once with first configuration getter call. Returned values
 * are cached for subsequent getter calls which are very fast.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ConfigBuilder {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build <code>List</code> of <code>GlassFishLibrary</code> objects representing libraries found in
     * particular GlassFish server installation.
     * <p/>
     *
     * @param libConfigs List of libraries configuration nodes.
     * @param classpathHome Directory tree to search for class path elements.
     * @param javadocsHome Directory tree to search for java doc.
     * @param srcHome Directory tree to search for source files.
     * @return <code>List</code> of <code>GlassFishLibrary</code> objects representing libraries found
     * in particular GlassFish server installation.
     */
    private static List<GlassFishLibrary> getLibraries(
            final List<LibraryNode> libConfigs, final File classpathHome,
            final File javadocsHome, final File srcHome) {

        List<GlassFishLibrary> result = new LinkedList<>();

        try {
            for (LibraryNode libConfig : libConfigs) {

                List<File> classpath = ConfigUtils.processFileset(
                        libConfig.classpath, classpathHome.getAbsolutePath());
                List<File> javadocs = ConfigUtils.processFileset(
                        libConfig.javadocs, javadocsHome.getAbsolutePath());
                List<URL> javadocUrls = ConfigUtils.processLinks(libConfig.javadocs);
                List<File> sources = ConfigUtils.processFileset(
                        libConfig.sources, srcHome.getAbsolutePath());
                result.add(new GlassFishLibrary(libConfig.libraryID,
                        buildUrls(classpath),
                        buildUrls(javadocs, javadocUrls),
                        libConfig.javadocs.getLookups(),
                        buildUrls(sources),
                        ConfigUtils.processClassPath(classpath)));
            }
        } catch (FileNotFoundException e) {
            throw new PayaraIdeException(
                    "Some files required by configuration were not found.", e);
        }
        return result;
    }

    /**
     * Converts provided list of files to <code>URL</code> objects and appends supplied <code>URL</code>
     * objects to this list.
     * <p/>
     *
     * @param files List of files to convert to <code>URL</code> objects.
     * @param urls <code>URL</code> objects to append to this list.
     * @return List of <code>URL</code> objects containing content of both supplied lists.
     */
    private static List<URL> buildUrls(
            final List<File> files, final List<URL> urls) {
        List<URL> result = buildUrls(files);
        result.addAll(urls);
        return result;
    }

    /**
     * Converts provided list of files to <code>URL</code> objects.
     * <p/>
     *
     * @param files List of files to convert to <code>URL</code> objects.
     * @return List of <code>URL</code> objects containing files from supplied list.
     */
    private static List<URL> buildUrls(final List<File> files) {
        ArrayList<URL> result = new ArrayList<>(files.size());
        for (File file : files) {
            URL url = ConfigUtils.fileToURL(file);
            if (url != null) {
                result.add(url);
            }
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Classpath search prefix. */
    private final File classpathHome;

    /** Javadoc search prefix. */
    private final File javadocsHome;

    /** Source code search prefix. */
    private final File srcHome;

    /** Stores information whether lassFish configuration was already read. */
    private volatile boolean fetchDone;

    /** Libraries cache. */
    private List<GlassFishLibrary> libraryCache;

    /** GlassFish JavaEE configuration cache. */
    private GlassFishJavaEEConfig javaEEConfigCache;

    /** GlassFish JavaSE configuration cache. */
    private GlassFishJavaSEConfig javaSEConfigCache;

    /** Version check. */
    private Version version;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish library builder.
     * <p/>
     * Stores provided GlassFish version to configuration file mapping.
     * <p/>
     *
     * @param config Library builder configuration. Should not be <code>null</code>.
     * @param classpathHome Classpath search prefix.
     * @param javadocsHome Javadoc search prefix.
     * @param srcHome Source code search prefix.
     */
    ConfigBuilder(final String classpathHome,
            final String javadocsHome, final String srcHome) {
        this.classpathHome = new File(classpathHome);
        this.javadocsHome = new File(javadocsHome);
        this.srcHome = new File(srcHome);
        this.fetchDone = false;
    }

    /**
     * Creates an instance of GlassFish library builder.
     * <p/>
     * Stores provided GlassFish version to configuration file mapping.
     * <p/>
     *
     * @param config Library builder configuration. Should not be <code>null</code>.
     * @param classpathHome Classpath search prefix.
     * @param javadocsHome Javadoc search prefix.
     * @param srcHome Source code search prefix.
     */
    ConfigBuilder(final File classpathHome,
            File javadocsHome, File srcHome) {
        this.classpathHome = classpathHome;
        this.javadocsHome = javadocsHome;
        this.srcHome = srcHome;
        this.fetchDone = false;
    }

    /**
     * Internal version check to avoid usage of a single builder instance for multiple GlassFish
     * versions.
     * <p/>
     *
     * @param version GlassFish version being checked.
     * @throws ServerConfigException when builder is used with multiple GlassFish versions.
     */
    private void versionCheck(final Version version)
            throws ServerConfigException {
        if (this.version == null) {
            this.version = version;
        } else if (this.version != version) {
            throw new ServerConfigException(
                    "Library builder was already used for GlassFish "
                            + this.version + " use new instance for GlassFish"
                            + version);
        }
    }

    private void fetch(final Version version) {
        synchronized (this) {
            if (!fetchDone) {
                GlassFishConfig configAdapter = GlassFishConfigManager.getConfig(
                        ConfigBuilderProvider.getBuilderConfig(version));
                List<LibraryNode> libConfigs = configAdapter.getLibrary();
                libraryCache = getLibraries(
                        libConfigs, classpathHome, javadocsHome, srcHome);
                javaEEConfigCache = new GlassFishJavaEEConfig(
                        configAdapter.getJavaEE(), classpathHome);
                javaSEConfigCache = new GlassFishJavaSEConfig(
                        configAdapter.getJavaSE());

                fetchDone = true;
            }
        }

    }

    /**
     * Get GlassFish libraries configured for provided GlassFish version.
     * <p/>
     * This method shall not be used with multiple GlassFish versions for the same instance of
     * {@link ConfigBuilder} class.
     * <p/>
     *
     * @param version GlassFish version.
     * @return List of libraries configured for GlassFish of given version.
     * @throws ServerConfigException when builder instance is used with multiple GlassFish versions.
     */
    public List<GlassFishLibrary> getLibraries(
            final Version version) throws ServerConfigException {
        versionCheck(version);
        if (!fetchDone) {
            fetch(version);
        }
        return libraryCache;
    }

    /**
     * Get GlassFish JavaEE configuration for provided GlassFish version.
     * <p/>
     * This method shall not be used with multiple GlassFish versions for the same instance of
     * {@link ConfigBuilder} class.
     * <p/>
     *
     * @param version GlassFish version.
     * @return GlassFish JavaEE configuration for provided GlassFish of given version.
     * @throws ServerConfigException when builder instance is used with multiple GlassFish versions.
     */
    public GlassFishJavaEEConfig getJavaEEConfig(
            final Version version) throws ServerConfigException {
        versionCheck(version);
        if (!fetchDone) {
            fetch(version);
        }
        return javaEEConfigCache;
    }

    /**
     * Get GlassFish JavaSE configuration for provided GlassFish version.
     * <p/>
     * This method shall not be used with multiple GlassFish versions for the same instance of
     * {@link ConfigBuilder} class.
     * <p/>
     *
     * @param version GlassFish version.
     * @return GlassFish JavaSE configuration for provided GlassFish of given version.
     * @throws ServerConfigException when builder instance is used with multiple GlassFish versions.
     */
    public GlassFishJavaSEConfig getJavaSEConfig(
            final Version version) throws ServerConfigException {
        versionCheck(version);
        if (!fetchDone) {
            fetch(version);
        }
        return javaSEConfigCache;
    }

}
