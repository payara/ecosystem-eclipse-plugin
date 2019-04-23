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
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class FileSystemExcludes {
    private final List<ResourceCollection> resourceCollections = new ArrayList<>();

    public void add(ResourceCollection resourceCollection) {
        this.resourceCollections.add(resourceCollection);
    }

    public List<File> list() {
        List<File> list = new ArrayList<>();

        for (ResourceCollection resourceCollection : resourceCollections) {
            for (Resource resource : resourceCollection) {
                if (resource instanceof FileResource) {
                    list.add(((FileResource) resource).getFile());
                }
            }
        }

        return list;
    }

}
