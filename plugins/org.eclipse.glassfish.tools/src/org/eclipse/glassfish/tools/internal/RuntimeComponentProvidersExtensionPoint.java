/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.internal;

import static org.eclipse.glassfish.tools.utils.PluginUtil.findExtensions;
import static org.eclipse.glassfish.tools.utils.PluginUtil.findRequiredAttribute;
import static org.eclipse.glassfish.tools.utils.PluginUtil.getTopLevelElements;
import static org.eclipse.glassfish.tools.utils.PluginUtil.instantiate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;
import org.eclipse.glassfish.tools.utils.PluginUtil.InvalidExtensionException;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.server.core.IRuntime;

/**
 * Contains the logic for processing the <code>runtimeComponentProviders</code> extension point.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class RuntimeComponentProvidersExtensionPoint
{
    public static final String EXTENSION_POINT_ID = "runtimeComponentProviders";
    private static final String EL_RUNTIME_COMPONENT_PROVIDER = "runtime-component-provider";
    private static final String ATTR_CLASS = "class";
    
    private static List<RuntimeComponentProvider> providers = null;
    
    public static List<IRuntimeComponent> getRuntimeComponents( final IRuntime runtime )
    {
        final List<IRuntimeComponent> components = new ArrayList<IRuntimeComponent>();
        
        for( final RuntimeComponentProvider provider : getProviders() )
        {
            try
            {
                final List<IRuntimeComponent> res = provider.getRuntimeComponents( runtime );
                
                if( res != null )
                {
                    components.addAll( res );
                }
            }
            catch( final Exception e )
            {
                GlassfishToolsPlugin.log( e );
            }
        }
        
        return components;
    }

    private static synchronized List<RuntimeComponentProvider> getProviders()
    {
        if( providers == null )
        {
            final List<RuntimeComponentProvider> list = new ArrayList<RuntimeComponentProvider>();
            
            for( final ProviderDef pdef : readExtensions() )
            {
                final RuntimeComponentProvider provider
                    = instantiate( pdef.pluginId, pdef.className, RuntimeComponentProvider.class );
                
                if( provider != null )
                {
                    list.add( provider );
                }
            }
            
            providers = Collections.unmodifiableList( list );
        }
        
        return providers;
    }
    
    private static List<ProviderDef> readExtensions()
    {
        final List<ProviderDef> providers = new ArrayList<ProviderDef>();
        
        for( final IConfigurationElement element 
             : getTopLevelElements( findExtensions( GlassfishToolsPlugin.SYMBOLIC_NAME, EXTENSION_POINT_ID ) ) )
        {
            final String pluginId = element.getContributor().getName();
            
            if( element.getName().equals( EL_RUNTIME_COMPONENT_PROVIDER ) )
            {
                try
                {
                    final String className = findRequiredAttribute( element, ATTR_CLASS );
                    providers.add( new ProviderDef( pluginId, className ) );
                }
                catch( final InvalidExtensionException e )
                {
                    // Continue. The problem has been reported to the user via the log.
                }
            }
        }
        
        return providers;
    }
    
    private static final class ProviderDef
    {
        public final String pluginId;
        public final String className;
        
        public ProviderDef( final String pluginId, final String className )
        {
            this.pluginId = pluginId;
            this.className = className;
        }
    }
    
}
