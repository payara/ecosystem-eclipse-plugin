/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.internal;

import static java.util.Collections.singletonList;
import static org.eclipse.jdt.core.JavaCore.newContainerEntry;
import static org.eclipse.wst.common.project.facet.core.ProjectFacetsManager.getGroup;

import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jst.common.project.facet.core.IClasspathProvider;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PayaraClasspathProvider implements IClasspathProvider {
    private static final String MODULES_GROUP_ID = "modules";

    @Override
    public List<IClasspathEntry> getClasspathEntries(IProjectFacetVersion facetVersion) {
        if (!ProjectFacetsManager.isGroupDefined(MODULES_GROUP_ID)) {
            return null;
        }

        if (getGroup(MODULES_GROUP_ID).getMembers().contains(facetVersion)) {
            return singletonList(newContainerEntry(new Path(SystemLibrariesContainer.ID)));
        }

        return null;
    }

    @SuppressWarnings("rawtypes")
    public static final class Factory implements IAdapterFactory {
        private static final Class[] ADAPTER_TYPES = { IClasspathProvider.class };

        @Override
        public Class[] getAdapterList() {
            return ADAPTER_TYPES;
        }

        @Override
        public Object getAdapter(final Object adaptableObject, final Class adapterType) {
            return new PayaraClasspathProvider();
        }
    }

}
