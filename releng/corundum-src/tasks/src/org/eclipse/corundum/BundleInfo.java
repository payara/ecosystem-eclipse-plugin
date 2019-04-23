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

import static org.eclipse.corundum.ManifestUtil.readManifest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.tools.ant.BuildException;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BundleInfo {
    private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    private static final String BUNDLE_VERSION = "Bundle-Version";

    private final File location;
    private final String id;
    private final BundleVersion version;

    public BundleInfo(File location) throws Exception {
        this.location = location;

        Map<String, String> manifest;

        try {
            manifest = readManifest(this.location);
        } catch (IOException e) {
            throw new BuildException(e);
        }

        String bundleId = manifest.get(BUNDLE_SYMBOLIC_NAME);
        if (bundleId == null) {
            System.out.println("WARNING: Bundle " + location.getName() + " does not have a valid id!");
            throw new Exception("Missing Bundle-SymbolicName in plugin manifest.mf file: " + location.getName());
        }

        int semicolon = bundleId.indexOf(';');

        if (semicolon != -1) {
            bundleId = bundleId.substring(0, semicolon);
        }

        this.id = bundleId;

        String vstr = manifest.get(BUNDLE_VERSION);
        if (vstr == null) {
            System.out.println("WARNING: Bundle " + location.getName() + " version is null ");
            throw new Exception("Missing Bundle-Version in plugin manifest.mf file: " + location.getName());
        }
        
        this.version = new BundleVersion(vstr);
    }

    public BundleInfo(final String id, final BundleVersion version) {
        this.location = null;
        this.id = id;
        this.version = version;
    }

    public static boolean isValidBundle(File location) {
        return (location.isDirectory()) || (location.isFile() && location.getName().endsWith(".jar"));
    }

    public File getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    public BundleVersion getVersion() {
        return version;
    }
}
