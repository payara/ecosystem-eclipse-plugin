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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.tools.ant.BuildException;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateInventoryTask extends AbstractTask {
    private static final String BUNDLES_INFO_PATH = "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info";

    private File eclipse = null;
    private File destination = null;

    public void setEclipse(final File eclipse) {
        this.eclipse = eclipse;
    }

    public void setDest(final File destination) {
        this.destination = destination;
    }

    @Override

    public void execute() {
        File root = this.eclipse;
        File bundlesInfoFile = new File(root, BUNDLES_INFO_PATH);

        if (!bundlesInfoFile.exists()) {
            root = new File(this.eclipse, "Eclipse.app/Contents/Eclipse");
            bundlesInfoFile = new File(root, BUNDLES_INFO_PATH);
        }

        if (!bundlesInfoFile.exists()) {
            throw new BuildException("Could not find bundles.info in " + this.eclipse);
        }

        final BundleInventory inventory = new BundleInventory();

        try {
            try (BufferedReader bundlesInfoReader = new BufferedReader(new FileReader(bundlesInfoFile))) {
                for (String line = bundlesInfoReader.readLine(); line != null; line = bundlesInfoReader.readLine()) {
                    line = line.trim();

                    if (line.length() != 0 && !line.startsWith("#")) {
                        final String[] segments = line.split(",");
                        final String location = segments[2];
                        final File file;

                        if (location.startsWith("file:/")) {
                            file = new File(location.substring(6));
                        } else {
                            file = new File(root, location);
                        }

                        if (segments.length == 5) {
                            inventory.addBundle(new BundleInfo(file));
                        }
                    }
                }
            }
        } catch (final Exception e) {
            throw new BuildException(e);
        }

        try {
            inventory.write(this.destination);
        } catch (final IOException e) {
            throw new BuildException(e);
        }
    }

}
