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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BundleInventory {
    private static final String NL = System.getProperty("line.separator");

    private static final Comparator<BundleInfo> BUNDLE_COMPARATOR = (b1, b2) -> {
        int result = b1.getId().compareTo(b2.getId());

        if (result == 0) {
            result = b1.getVersion().compareTo(b2.getVersion());
        }

        return result;
    };

    private final SortedSet<BundleInfo> bundles = new TreeSet<>(BUNDLE_COMPARATOR);

    public Set<BundleInfo> getBundles() {
        return bundles;
    }

    public BundleInfo getBundle(String bundleId) {
        for (BundleInfo bundle : bundles) {
            if (bundle.getId().equals(bundleId)) {
                return bundle;
            }
        }

        return null;
    }

    public void addBundle(BundleInfo bundle) {
        bundles.add(bundle);
    }

    public void write(File f) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            for (BundleInfo bundle : this.bundles) {
                writer.write(bundle.getId());
                writer.write(" : ");
                writer.write(bundle.getVersion().toString());
                writer.write(NL);
            }

            writer.flush();
        }
    }

    public void read(File file) throws IOException {
        bundles.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] segments = line.split(":");
                String id = segments[0].trim();
                BundleVersion version = new BundleVersion(segments[1]);

                bundles.add(new BundleInfo(id, version));
            }
        }
    }

}
