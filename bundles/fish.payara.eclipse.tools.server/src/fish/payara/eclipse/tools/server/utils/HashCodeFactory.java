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

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class HashCodeFactory
{
    private final int result;

    private HashCodeFactory( final int result )
    {
        this.result = result;
    }

    public static HashCodeFactory start()
    {
        return new HashCodeFactory( 1 );
    }

    public HashCodeFactory add( final Object object )
    {
        if( object == null )
        {
            return this;
        }
        else
        {
            return new HashCodeFactory( this.result ^ object.hashCode() );
        }
    }

    public HashCodeFactory add( final boolean value )
    {
        return new HashCodeFactory( this.result ^ ( value ? 1231 : 1237 ) );
    }

    public HashCodeFactory add( final char value )
    {
        return new HashCodeFactory( this.result ^ value );
    }

    public HashCodeFactory add( final byte value )
    {
        return new HashCodeFactory( this.result ^ value );
    }

    public HashCodeFactory add( final short value )
    {
        return new HashCodeFactory( this.result ^ value );
    }

    public HashCodeFactory add( final int value )
    {
        return new HashCodeFactory( this.result ^ value );
    }

    public HashCodeFactory add( final long value )
    {
        return new HashCodeFactory( this.result ^ (int) ( value ^ ( value >>> 32 ) ) );
    }

    public HashCodeFactory add( final float value )
    {
        return new HashCodeFactory( this.result ^ Float.floatToIntBits( value ) );
    }

    public HashCodeFactory add( final double value )
    {
        final long v = Double.doubleToLongBits( value );
        return new HashCodeFactory( this.result ^ (int) ( v ^ ( v >>> 32 ) ) );
    }

    public int result()
    {
        return this.result;
    }

}
