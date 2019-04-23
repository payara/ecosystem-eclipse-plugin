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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ZipUtil {

    /**
     * This class is a container for static methods and is not meant to be instantiated.
     */
    private ZipUtil() {
    }

    public static ZipFile open(final File file) throws IOException {
        try {
            return new ZipFile(file);
        } catch (FileNotFoundException e) {
            FileNotFoundException fnfe = new FileNotFoundException(file.getAbsolutePath());
            fnfe.initCause(e);

            throw fnfe;
        }
    }

    public static ZipEntry getZipEntry(ZipFile zip, final String name) throws IOException {
        String lcasename = name.toLowerCase();

        for (Enumeration<?> itr = zip.entries(); itr.hasMoreElements();) {
            ZipEntry zipentry = (ZipEntry) itr.nextElement();

            if (zipentry.getName().toLowerCase().equals(lcasename)) {
                return zipentry;
            }
        }

        return null;
    }

}
