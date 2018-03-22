/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipError;
import java.util.zip.ZipFile;

/**
 * Utility class for reading the manifest file.
 *
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ManifestUtil {
    public static final String MANIFEST_FILE_NAME = "MANIFEST.MF";
    public static final String MANIFEST_PATH = "META-INF/" + MANIFEST_FILE_NAME;

    private ManifestUtil() {
    }

    public static Manifest readManifest(final File library) throws IOException {
        if (library.isFile()) {
            final ZipFile zip = openZipFile(library);

            try {
                return readManifest(zip);
            } finally {
                try {
                    zip.close();
                } catch (IOException e) {
                }
            }
        } else {
            final File manifestFile = new File(library, MANIFEST_PATH);

            if (manifestFile.exists()) {
                final InputStream in = new FileInputStream(manifestFile);

                try {
                    return readManifest(new BufferedInputStream(in));
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }

            return null;
        }
    }

    public static Manifest readManifest(final ZipFile zip) throws IOException {
        final ZipEntry zipentry = getZipEntry(zip, MANIFEST_PATH);

        if (zipentry != null) {
            final InputStream in = zip.getInputStream(zipentry);

            try {
                return readManifest(in);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }

    public static Manifest readManifest(final InputStream stream) throws IOException {
        final Manifest manifest = new Manifest();
        manifest.read(stream);
        return manifest;
    }

    public static String readManifestEntry(final File location, final String key) throws IOException {
        final Manifest manifest = readManifest(location);

        if (manifest != null) {
            return readManifestEntry(manifest, key);
        }

        return null;
    }

    public static String readManifestEntry(final Manifest manifest, final String key) {
        for (Map.Entry<Object, Object> entry : manifest.getMainAttributes().entrySet()) {
            final String name = ((Attributes.Name) entry.getKey()).toString();

            if (key.equals(name)) {
                return (String) entry.getValue();
            }
        }

        return null;
    }

    private static ZipFile openZipFile(final File file) throws IOException {
        try {
            return new ZipFile(file);
        } catch (FileNotFoundException e) {
            final FileNotFoundException fnfe = new FileNotFoundException(file.getAbsolutePath());

            fnfe.initCause(e);

            throw fnfe;
        }
    }

    private static ZipEntry getZipEntry(final ZipFile zip, final String name) throws IOException {
        final String lcasename = name.toLowerCase();

        try {
            for (Enumeration<?> itr = zip.entries(); itr.hasMoreElements();) {
                final ZipEntry zipentry = (ZipEntry) itr.nextElement();

                if (zipentry.getName().toLowerCase().equals(lcasename)) {
                    return zipentry;
                }
            }
        } catch (ZipError e) {
            // This error can be thrown if the ZIP file is corrupt. Note that it's
            // an Error, not an Exception. We are going to convert it to an IOException,
            // which the code dealing with ZIP files generally knows how to handle.
            //
            // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4615343

            throw new IOException(e);
        }

        return null;
    }

}
