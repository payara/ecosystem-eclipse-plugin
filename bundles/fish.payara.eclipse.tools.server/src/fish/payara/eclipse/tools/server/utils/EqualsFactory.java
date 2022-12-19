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

public abstract class EqualsFactory
{
    private static EqualsFactory TRUE = new TrueEqualsFactory();
    private static EqualsFactory FALSE = new FalseEqualsFactory();

    private EqualsFactory()
    {
    }

    public static EqualsFactory start()
    {
        return TRUE;
    }

    public abstract EqualsFactory add( Object x, Object y );
    public abstract EqualsFactory add( boolean x, boolean y );
    public abstract EqualsFactory add( char x, char y );
    public abstract EqualsFactory add( byte x, byte y );
    public abstract EqualsFactory add( short x, short y );
    public abstract EqualsFactory add( int x, int y );
    public abstract EqualsFactory add( long x, long y );
    public abstract EqualsFactory add( float x, float y );
    public abstract EqualsFactory add( double x, double y );

    public abstract boolean result();

    private static final class TrueEqualsFactory extends EqualsFactory
    {
        @Override
        public EqualsFactory add( final Object x, final Object y )
        {
            if( x == y )
            {
                return TRUE;
            }
            else if( x != null && y != null )
            {
                return ( x.equals( y ) ? TRUE : FALSE );
            }

            return FALSE;
        }

        @Override
        public EqualsFactory add( final boolean x, final boolean y )
        {
            return ( x == y ? TRUE : FALSE );
        }

        @Override
        public EqualsFactory add( final char x, final char y )
        {
            return ( x == y ? TRUE : FALSE );
        }

        @Override
        public EqualsFactory add( final byte x, final byte y )
        {
            return ( x == y ? TRUE : FALSE );
        }

        @Override
        public EqualsFactory add( final short x, final short y )
        {
            return ( x == y ? TRUE : FALSE );
        }

        @Override
        public EqualsFactory add( final int x, final int y )
        {
            return ( x == y ? TRUE : FALSE );
        }

        @Override
        public EqualsFactory add( final long x, final long y )
        {
            return ( x == y ? TRUE : FALSE );
        }

        @Override
        public EqualsFactory add( final float x, final float y )
        {
            return ( x == y ? TRUE : FALSE );
        }

        @Override
        public EqualsFactory add( final double x, final double y )
        {
            return ( x == y ? TRUE : FALSE );
        }

        @Override
        public boolean result()
        {
            return true;
        }
    }

    private static final class FalseEqualsFactory extends EqualsFactory
    {
        @Override
        public EqualsFactory add( final Object x, final Object y )
        {
            return this;
        }

        @Override
        public EqualsFactory add( final boolean x, final boolean y )
        {
            return this;
        }

        @Override
        public EqualsFactory add( final char x, final char y )
        {
            return this;
        }

        @Override
        public EqualsFactory add( final byte x, final byte y )
        {
            return this;
        }

        @Override
        public EqualsFactory add( final short x, final short y )
        {
            return this;
        }

        @Override
        public EqualsFactory add( final int x, final int y )
        {
            return this;
        }

        @Override
        public EqualsFactory add( final long x, final long y )
        {
            return this;
        }

        @Override
        public EqualsFactory add( final float x, final float y )
        {
            return this;
        }

        @Override
        public EqualsFactory add( final double x, final double y )
        {
            return this;
        }

        @Override
        public boolean result()
        {
            return false;
        }
    }

}
