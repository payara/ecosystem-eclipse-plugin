/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.internal;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.common.project.facet.core.IClasspathProvider;
import org.eclipse.wst.common.project.facet.core.IGroup;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class GlassFishClasspathProvider implements IClasspathProvider
{
    private static final String MODULES_GROUP_ID = "modules";

    public List<IClasspathEntry> getClasspathEntries( final IProjectFacetVersion fv )
    {
        if( ! ProjectFacetsManager.isGroupDefined( MODULES_GROUP_ID ) )
        {
            return null;
        }
        
        final IGroup group = ProjectFacetsManager.getGroup( MODULES_GROUP_ID );
        
        if( group.getMembers().contains( fv ) )
        {
            final IPath p = new Path( SystemLibrariesContainer.ID );
            final IClasspathEntry cpentry = JavaCore.newContainerEntry( p );
            return Collections.singletonList( cpentry );
        }
        
        return null;
    }

    @SuppressWarnings( "rawtypes" )
    public static final class Factory implements IAdapterFactory
    {
        private static final Class[] ADAPTER_TYPES = { IClasspathProvider.class };

        public Class[] getAdapterList()
        {
            return ADAPTER_TYPES;
        }

        public Object getAdapter( final Object adaptableObject, final Class adapterType )
        {
            return new GlassFishClasspathProvider();
        }
    }
    
}
