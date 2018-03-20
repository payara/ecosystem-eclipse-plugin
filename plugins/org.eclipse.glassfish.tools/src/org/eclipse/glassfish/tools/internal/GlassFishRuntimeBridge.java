/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.glassfish.tools.server.GlassFishRuntime;
import org.eclipse.jst.common.project.facet.core.StandardJreRuntimeComponent;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.util.SetFactory;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeBridge;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentType;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentVersion;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Runtime;

public final class GlassFishRuntimeBridge implements IRuntimeBridge
{
    @Override

    public Set<String> getExportedRuntimeNames() throws CoreException
    {
        final SetFactory<String> namesSetFactory = SetFactory.start();
        
        for( final IRuntime runtime : ServerCore.getRuntimes() )
        {
            final IRuntimeType type = runtime.getRuntimeType();
            
            if( type != null && "glassfish".equals( type.getId() ) )
            {
                namesSetFactory.add( runtime.getId() );
            }
        }
        
        return namesSetFactory.result();
    }
    
    @Override

    public IStub bridge( final String name ) throws CoreException
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new Stub( name );
    }

    private static class Stub extends IRuntimeBridge.Stub
    {
        private String id;

        public Stub(String id) {
            this.id = id;
        }

        public List<IRuntimeComponent> getRuntimeComponents() {
            List<IRuntimeComponent> components = new ArrayList<IRuntimeComponent>(2);
            final IRuntime runtime = findRuntime( this.id );
            
            if (runtime == null)
                return components;
            
            final GlassFishRuntime gfRuntime = (GlassFishRuntime) runtime.loadAdapter( GlassFishRuntime.class, new NullProgressMonitor() );
            
            if( gfRuntime != null )
            {
                final Version gfVersion = gfRuntime.getVersion();
                
                if( gfVersion != null )
                {
                    // GlassFish
                    
                    final IRuntimeComponentType gfComponentType = RuntimeManager.getRuntimeComponentType( "glassfish" );
                    final String gfComponentVersionStr = gfVersion.matches( "[5" ) ? "5" : ( gfVersion.matches( "[4" ) ? "4" : "3.1" );
                    final IRuntimeComponentVersion gfComponentVersion = gfComponentType.getVersion( gfComponentVersionStr );
                    
                    Map<String, String> properties = new HashMap<String, String>(5);
                    if (runtime.getLocation() != null)
                        properties.put("location", runtime.getLocation().toPortableString());
                    else
                        properties.put("location", "");
                    properties.put("name", runtime.getName());
                    properties.put("id", runtime.getId());
                    if (runtime.getRuntimeType() != null) {
                        properties.put("type", runtime.getRuntimeType().getName());
                        properties.put("type-id", runtime.getRuntimeType().getId());
                    }
                    
                    components.add( RuntimeManager.createRuntimeComponent( gfComponentVersion, properties ) );
                    
                    // Java Runtime Environment
                    
                    components.add( StandardJreRuntimeComponent.create( gfRuntime.getVMInstall() ) );
                    
                    // Other
                    
                    components.addAll( RuntimeComponentProvidersExtensionPoint.getRuntimeComponents( runtime ) );
                }
            }
            
            return components;
        }

        public Map<String, String> getProperties() {
            final Map<String, String> props = new HashMap<String, String>();
            final IRuntime runtime = findRuntime( this.id );
            if (runtime != null) {
                props.put("id", runtime.getId());
                props.put("localized-name", runtime.getName());
                String s = ((Runtime)runtime).getAttribute("alternate-names", (String)null);
                if (s != null)
                    props.put("alternate-names", s);
            }
            return props;
        }
        
        public IStatus validate(final IProgressMonitor monitor) {
            final IRuntime runtime = findRuntime( this.id );
            if( runtime != null ) {
                return runtime.validate( monitor );
            }
            return Status.OK_STATUS; 
        }
        
        private static final IRuntime findRuntime( final String id )
        {
            IRuntime[] runtimes = ServerCore.getRuntimes();
            int size = runtimes.length;

            for (int i = 0; i < size; i++) {
                if (runtimes[i].getId().equals(id))
                    return runtimes[i];
                if (runtimes[i].getName().equals(id))
                    return runtimes[i];

            }
            return null;
        }
    }
    
}