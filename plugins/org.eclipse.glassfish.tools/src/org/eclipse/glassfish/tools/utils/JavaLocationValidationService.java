/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.utils;

import java.io.File;

import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class JavaLocationValidationService extends ValidationService
{
    @Override
    
    protected Status compute()
    {
        final String location = context( Value.class ).text();
        
        if( location != null )
        {
            final File locationFile = new File( location );
            
            if( locationFile.exists() && locationFile.isDirectory() )
            {
                return validate( locationFile );
            }
        }
        
        return Status.createOkStatus();
    }
    
    protected abstract Status validate( File location );
    
}
