/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools;

import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class GlassFishTools
{
    public static boolean isGlassFish( final org.eclipse.wst.server.core.IRuntime runtime )
    {
        if( runtime != null )
        {
            final String type = runtime.getRuntimeType().getId();
            
            return type.equals( "glassfish" );
        }
        
        return false;
    }
    public static boolean isGlassFish( final IRuntime runtime )
    {
        if( runtime != null )
        {
            for( final IRuntimeComponent component : runtime.getRuntimeComponents() )
            {
                return isGlassFish( component );
            }
        }
        
        return false;
    }
    
    public static boolean isGlassFish( final IRuntimeComponent component )
    {
        if( component != null )
        {
            return ( component.getRuntimeComponentType().getId().equals( "glassfish" ) );
        }
        
        return false;
    }
}
