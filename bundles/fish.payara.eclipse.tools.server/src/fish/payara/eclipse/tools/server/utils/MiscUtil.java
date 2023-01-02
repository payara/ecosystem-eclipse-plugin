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

package fish.payara.eclipse.tools.server.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class MiscUtil
{

    private static final Logger LOG = Logger.getLogger(MiscUtil.class.getName());
    
    public static final String EMPTY_STRING = ""; //$NON-NLS-1$
    
    public static final boolean equal( final Object obj1, 
                                       final Object obj2 )
    {
        if( obj1 == obj2 )
        {
            return true;
        }
        else if( obj1 != null && obj2 != null )
        {
            return obj1.equals( obj2 );
        }

        return false;
    }
    

    public static boolean contains( final Object[] array,
                                    final Object object )
    {
        for( int i = 0; i < array.length; i++ )
        {
            if( array[ i ].equals( object ) )
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static <T> boolean containsUsingIdentity( final Collection<? extends T> collection, final T item )
    {
        for( T x : collection )
        {
            if( x == item )
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static int indexOf( final Object[] array,
                               final Object object )
    {
        for( int i = 0; i < array.length; i++ )
        {
            if( array[ i ].equals( object ) )
            {
                return i;
            }
        }
        
        throw new IllegalArgumentException();
    }
    
    public static String readTextContent( final Reader reader ) 
    
        throws IOException
        
    {
        final StringBuffer buf = new StringBuffer();
        final char[] chars = new char[ 8 * 1024 ];
        int count;

        while( ( count = reader.read( chars, 0, chars.length ) ) > 0 ) 
        {
            buf.append( chars, 0, count );
        }
        
        return buf.toString();
    }
    
    public static String readTextContent( final InputStream in ) 
    
        throws IOException
        
    {
        return readTextContent( new InputStreamReader( in, UTF_8 ) );
    }
    
    public static String readTextResource( final ClassLoader cl, final String resourceFullPath )
    {
        try( InputStream in = cl.getResourceAsStream( resourceFullPath ) )
        {
            return readTextContent( in );
        }
        catch( final IOException e )
        {
            LOG.log(Level.SEVERE, null, e);
            return "";
        }
    }
    
    public static String readTextResource( final Class<?> c, final String resourceLocalName )
    {
        final ClassLoader cl = c.getClassLoader();
        final String resourcePath = c.getName().replace( '.', '/' ) + "." + resourceLocalName;
        
        return readTextResource( cl, resourcePath );
    }
    
    public static final String createStringDigest( final String str )
    {
        try
        {
            final MessageDigest md = MessageDigest.getInstance( "SHA-256" );
            final byte[] input = str.getBytes( UTF_8 );
            final byte[] digest = md.digest( input );
            
            final StringBuilder buf = new StringBuilder();
            
            for( int i = 0; i < digest.length; i++ )
            {
                String hex = Integer.toHexString( 0xFF & digest[ i ] );
                
                if( hex.length() == 1 )
                {
                    buf.append( '0' );
                }
                
                buf.append( hex );
            }
            
            return buf.toString();
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
    public static String escapeForXml( final String string )
    {
        final StringBuilder result = new StringBuilder();
        
        for( int i = 0, n = string.length(); i < n; i++ )
        {
            final char ch = string.charAt( i );
            
            if( ch == '<' )
            {
                result.append( "&lt;" );
            }
            else if( ch == '>' )
            {
                result.append( "&gt;" );
            }
            else if( ch == '&' )
            {
                result.append( "&amp;" );
            }
            else if( ch == '"' )
            {
                result.append( "&quot;" );
            }
            else if( ch == '\'' )
            {
                result.append( "&apos;" );
            }
            else
            {
                result.append( ch );
            }
        }
        
        return result.toString();
    }
    
    @SafeVarargs
    
    public static <T> List<T> list( final T... items )
    {
        return Arrays.asList( items );
    }
    
    @SafeVarargs
    
    public static <T> Set<T> set( final T... items )
    {
        return new LinkedHashSet<T>( Arrays.asList( items ) );
    }
    
    public static String normalizeToNull( final String string )
    {
        return ( string != null && string.length() == 0 ? null : string );
    }
    
    public static String normalizeToEmptyString( final String string )
    {
        return ( string == null ? EMPTY_STRING : string );
    }
    
}
