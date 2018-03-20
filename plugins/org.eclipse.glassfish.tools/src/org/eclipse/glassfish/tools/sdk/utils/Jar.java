/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * JAR file utilities.
 * <p/>
 * This class is a stream wrapper. {@link #close} method should be called
 * before class instance is abandoned like when working with streams.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class Jar {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** JManifest attribute containing version string. */
    public static final String MANIFEST_BUNDLE_VERSION = "Bundle-Version";

     ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    /** JAR file input stream. */
    private final JarInputStream jar;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of JAR file.
     * <p/>
     * @param jarfile JAR file to be opened.
     */
    public Jar(File jarfile) {
        JarInputStream jarStream = null;
        try {
        jarStream = new JarInputStream(new FileInputStream(jarfile));
        } catch (IOException ioe) {
            jar = null;
            throw new JarException(JarException.OPEN_ERROR, ioe);
        }
        jar = jarStream;
    }

    /**
     * Creates an instance of JAR file.
     * <p/>
     * @param jarfile JAR file to be opened.
     */
    public Jar(String jarfile) {
        JarInputStream jarStream = null;
        try {
        jarStream = new JarInputStream(new FileInputStream(jarfile));
        } catch (IOException ioe) {
            jar = null;
            throw new JarException(JarException.OPEN_ERROR, ioe);
        }
        jar = jarStream;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the <code>Manifest</code> for this JAR file, or
     * <code>null</code> if none.
     * <p/>
     * @return The <code>Manifest</code> for this JAR file, or
     *         <code>null</code> if none.
     */
    public Manifest getManifest() {
        return jar.getManifest();
    }

    /**
     * Returns the bundle version string from Manifest file.
     * <p/>
     * @return Bundle version string from Manifest file or <code>null</code>
     *         when no such attribute exists.
     */
    public String getBundleVersion() {
        Manifest manifest = jar.getManifest();
        Attributes attrs = manifest != null
                ? manifest.getMainAttributes() : null;
        return attrs != null ? attrs.getValue(MANIFEST_BUNDLE_VERSION) : null;
    }

    /**
     * Close JAR file and release all allocated resources.
     * <p/>
     * This method should be called when this object is being released to avoid
     * memory leaks.
     */
    public void close() {
        if (jar != null) {
            try {
                jar.close();
            } catch (IOException ioe) {
                throw new JarException(JarException.CLOSE_ERROR, ioe);
            }
        }
    }


}
