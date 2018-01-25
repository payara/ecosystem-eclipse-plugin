/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.internal;

import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentVersion;
import org.eclipse.wst.common.project.facet.ui.IRuntimeComponentLabelProvider;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class GlassFishRuntimeComponentLabelProvider implements IRuntimeComponentLabelProvider
{
    private final IRuntimeComponent rc;

    public GlassFishRuntimeComponentLabelProvider( final IRuntimeComponent rc ) 
    {
        this.rc = rc;
    }

    public String getLabel() 
    {
        final IRuntimeComponentVersion rcv = this.rc.getRuntimeComponentVersion();
        final String verstr = rcv.getVersionString();
        
        return Resources.bind( Resources.label, verstr );
    }

    private static final class Resources extends NLS
    {
        public static String label;
        
        static
        {
            initializeMessages( GlassFishRuntimeComponentLabelProvider.class.getName(), Resources.class );
        }
    }
    
}
