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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ReadManifestTask extends AbstractTask {
    private File path;
    private final List<ManifestEntryRequest> requests = new ArrayList<>();

    public void setPath(final File path) {
        this.path = path;
    }

    public ManifestEntryRequest createEntry() {
        final ManifestEntryRequest request = new ManifestEntryRequest();
        this.requests.add(request);
        return request;
    }

    @Override
    public void execute() throws BuildException {
        try {
            final Map<String, String> entries = ManifestUtil.readManifest(this.path);
            final Project project = getProject();

            for (ManifestEntryRequest request : this.requests) {
                final String key = request.getKey();

                String value = request.getDefault();
                value = entries.get(key);

                if (value != null) {
                    project.setProperty(request.getProperty(), value);
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    public static final class ManifestEntryRequest {
        private String entryKey;
        private String property;
        private String defaultValue;

        public String getKey() {
            return this.entryKey;
        }

        public void setKey(final String key) {
            this.entryKey = key;
        }

        public String getProperty() {
            return this.property;
        }

        public void setProperty(final String property) {
            this.property = property;
        }

        public String getDefault() {
            return this.defaultValue;
        }

        public void setDefault(final String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

}
