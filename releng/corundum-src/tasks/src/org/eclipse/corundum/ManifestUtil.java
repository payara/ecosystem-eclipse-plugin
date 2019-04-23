/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.corundum;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ManifestUtil {
    public static final String MANIFEST_FILE_NAME = "MANIFEST.MF";
    public static final String MANIFEST_PATH = "META-INF/" + MANIFEST_FILE_NAME;

    public static final String ATTR_CLASSPATH = "Class-Path";
    public static final String ATTR_IMPLEMENTATION_VERSION = "Implementation-Version";

    private ManifestUtil() {
    }

    public static File findManifestFile(final File baseDir) {
        return new File(baseDir, MANIFEST_PATH);
    }

    public static Map<String, String> readManifest(File location) throws IOException {
        if (location.isFile()) {
            try (ZipFile zip = ZipUtil.open(location)) {
                return readManifest(zip);
            }
        } else {
            final File manifestFile = new File(location, MANIFEST_PATH);

            if (manifestFile.exists()) {
                try (InputStream in = new FileInputStream(manifestFile)) {
                    return readManifest(new BufferedInputStream(in));
                }
            } else {
                return Collections.emptyMap();
            }
        }
    }

    public static String readManifestEntry(File location, String key) throws IOException {
        final Map<String, String> manifest = readManifest(location);

        if (manifest != null) {
            return manifest.get(key);
        }

        return null;
    }

    public static Map<String, String> readManifest(ZipFile zip) throws IOException {
        final ZipEntry zipentry = ZipUtil.getZipEntry(zip, MANIFEST_PATH);

        if (zipentry != null) {
            try (InputStream in = zip.getInputStream(zipentry)) {
                return readManifest(in);
            }
        } else {
            return Collections.emptyMap();
        }
    }

    public static Map<String, String> readManifest(InputStream stream) throws IOException {
        Map<String, String> entries = new HashMap<>();
        Manifest manifest = new Manifest();

        manifest.read(stream);

        for (Map.Entry<Object, Object> entry : manifest.getMainAttributes().entrySet()) {
            Attributes.Name name = (Attributes.Name) entry.getKey();
            entries.put(name.toString(), (String) entry.getValue());
        }

        return entries;
    }

    public static List<File> readManifestClasspath(File archive) throws IOException {
        final String manifestClasspathEntry = readManifestEntry(archive, ATTR_CLASSPATH);

        if (manifestClasspathEntry == null) {
            return Collections.emptyList();
        }

        List<File> manifestClasspath = new ArrayList<>();
        File baseDir = archive.getParentFile();

        for (String entry : manifestClasspathEntry.split(" ")) {
            String trimmedEntry = entry.trim();

            if (trimmedEntry.length() > 0) {
                File f = new File(baseDir, trimmedEntry);
                manifestClasspath.add(f);
            }
        }

        return manifestClasspath;
    }

    public static void setManifestEntry(File manifestFile, String entryKey, String entryValue) throws IOException {
        setManifestEntries(manifestFile, Collections.singletonMap(entryKey, entryValue));
    }

    public static void setManifestEntries(File manifestFile, Map<String, String> entries) throws IOException {
        Manifest manifest = new Manifest();

        if (manifestFile.exists()) {
            try (InputStream in = new FileInputStream(manifestFile)) {
                manifest.read(new BufferedInputStream(in));
            }
        }

        Attributes mainAttributes = manifest.getMainAttributes();

        for (Map.Entry<String, String> entry : entries.entrySet()) {
            mainAttributes.putValue(entry.getKey(), entry.getValue());
        }

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(manifestFile))) {
            manifest.write(out);
            out.flush();
        }
    }

}
