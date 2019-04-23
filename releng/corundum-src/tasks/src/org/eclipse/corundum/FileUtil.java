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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class FileUtil {
    public static void write(File file, InputStream content) throws IOException {
        byte[] buffer = new byte[16 * 1024];
        int bufferUsedLength = 0;
        int bytesRead = 0;

        while ((bytesRead = content.read(buffer, bufferUsedLength, buffer.length - bufferUsedLength)) != -1) {
            bufferUsedLength += bytesRead;

            if (buffer.length - bufferUsedLength < 1024) {
                byte[] newBuffer = new byte[buffer.length * 2];
                System.arraycopy(buffer, 0, newBuffer, 0, bufferUsedLength);
                buffer = newBuffer;
            }
        }

        byte[] array = new byte[bufferUsedLength];
        System.arraycopy(buffer, 0, array, 0, bufferUsedLength);

        write(file, array);
    }

    public static void write(File file, String content) throws IOException {
        write(file, content.getBytes("UTF-8"));
    }

    public static void write(File file, byte[] content) throws IOException {
        boolean write = true;

        if (file.exists()) {
            final int length = content.length;

            if (file.length() == length) {
                try (InputStream in = new FileInputStream(file)) {
                    byte[] buffer = new byte[4 * 1024];
                    int count = 0;
                    int position = 0;
                    write = false;

                    while (!write && (count = in.read(buffer)) != -1) {
                        for (int i = 0; !write && i < count; i++, position++) {
                            if (buffer[i] != content[position]) {
                                write = true;
                            }
                        }
                    }
                }
            }
        }

        if (write) {
            try (OutputStream out = new FileOutputStream(file)) {
                out.write(content);
            }
        }
    }

}