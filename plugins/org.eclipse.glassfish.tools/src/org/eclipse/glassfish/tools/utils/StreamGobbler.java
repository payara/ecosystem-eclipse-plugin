/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StreamGobbler extends Thread
{
    private final InputStream stream;
    private final StringBuffer output;

    public StreamGobbler( final InputStream stream )
    {
        if( stream == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.stream = stream;
        this.output = new StringBuffer();
    }

    public void run()
    {
        try
        {
            final BufferedReader br = new BufferedReader( new InputStreamReader( this.stream ) );
            
            String line = null;
            
            while( ( line = br.readLine() ) != null )
            {
                read( line );
            }
        }
        catch( final IOException e )
        {
            e.printStackTrace();  
        }
    }
    
    protected void read( final String line )
    {
        this.output.append( line );
        this.output.append( '\n' );
    }
    
    public final String output()
    {
        return this.output.toString();
    }
    
}

