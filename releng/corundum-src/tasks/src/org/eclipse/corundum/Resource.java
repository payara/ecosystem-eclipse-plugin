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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Resource {
    private final URL url;
    private final String name;

    public Resource(final URL url, final String name) {
        if (url == null) {
            throw new IllegalArgumentException();
        }

        if (name == null) {
            throw new IllegalArgumentException();
        }

        this.url = url;
        this.name = name;
    }

    public String text() {
        try (Reader reader = new InputStreamReader(this.url.openStream(), "UTF-8")) {
            final StringBuilder content = new StringBuilder();

            char[] buffer = new char[1024];
            int count = 0;

            while ((count = reader.read(buffer)) != -1) {
                content.append(buffer, 0, count);
            }

            return content.toString();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void copy(final File folder) {
        if (folder == null || !folder.isDirectory()) {
            throw new IllegalArgumentException();
        }

        try (InputStream in = this.url.openStream()) {
            FileUtil.write(new File(folder, this.name), in);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}