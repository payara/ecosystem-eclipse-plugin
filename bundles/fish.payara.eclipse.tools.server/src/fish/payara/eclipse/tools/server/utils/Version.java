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

import static java.lang.Math.max;

import java.util.Collections;
import java.util.List;

/**
 * Represents a version as a sequence of long integers. In string format, it is represented as a dot-separated
 * list of numeric segments, such as "1.2.3" or "5.7.3.2012070310003".
 *
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Version implements Comparable<Version>
{
    private final List<Long> segments;
    private final int length;

    public Version( final long version )
    {
        if( version == 0 )
        {
            this.segments = Collections.emptyList();
            this.length = 0;
        }
        else if( version > 0 )
        {
            this.segments = Collections.singletonList( version );
            this.length = 1;
        }
        else
        {
            throw new IllegalArgumentException( String.valueOf( version ) );
        }
    }

    public Version( final String versionString ) throws IllegalArgumentException
    {
        if( versionString == null )
        {
            throw new IllegalArgumentException();
        }

        final String str = versionString.trim();

        if( str.startsWith( "." ) || str.endsWith( "." ) || str.contains( ".." ) )
        {
            throw new IllegalArgumentException( str );
        }

        final ListFactory<Long> segments = ListFactory.start();

        for( String segment : str.split( "\\." ) )
        {
            final long segmentAsLong;

            try
            {
                segmentAsLong = Long.parseLong( segment );
            }
            catch( NumberFormatException e )
            {
                throw new IllegalArgumentException( str );
            }

            if( segmentAsLong < 0 )
            {
                throw new IllegalArgumentException( str );
            }

            segments.add( segmentAsLong );
        }

        for( int i = segments.size() - 1; i >= 0; i-- )
        {
            if( segments.get( i ) == 0 )
            {
                segments.remove( i );
            }
            else
            {
                break;
            }
        }

        this.segments = segments.result();
        this.length = this.segments.size();
    }

    public List<Long> segments()
    {
        return this.segments;
    }

    public long segment( final int position )
    {
        if( position < 0 )
        {
            throw new IllegalArgumentException( String.valueOf( position ) );
        }
        else if( position < this.length )
        {
            return this.segments.get( position );
        }
        else
        {
            return 0;
        }
    }

    public int length()
    {
        return this.length;
    }

    public boolean matches( final String constraint )
    {
        return matches( new VersionConstraint( constraint ) );
    }

    public boolean matches( final VersionConstraint constraint )
    {
        return constraint.check( this );
    }

    @Override
	public int compareTo( final Version version )
    {
        for( int i = 0, n = max( length(), version.length ); i < n; i++ )
        {
            final long res = segment( i ) - version.segment( i );

            if( res > 0 )
            {
                return 1;
            }
            else if( res < 0 )
            {
                return -1;
            }
        }

        return 0;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof Version )
        {
            final Version version = (Version) obj;

            if( length() == version.length() )
            {
                for( int i = 0, n = length(); i < n; i++ )
                {
                    if( segment( i ) != version.segment( i ) )
                    {
                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 1;

        for( int i = 0, n = length(); i < n; i++ )
        {
            hashCode *= segment( i );
        }

        return hashCode;
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        for( long segment : this.segments )
        {
            if( buf.length() > 0 )
            {
                buf.append( '.' );
            }

            buf.append( segment );
        }

        return buf.toString();
    }

}
