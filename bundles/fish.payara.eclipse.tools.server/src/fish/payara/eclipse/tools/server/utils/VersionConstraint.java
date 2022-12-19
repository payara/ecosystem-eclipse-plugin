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

import java.util.List;

/**
 * A version constraint is a boolean expression that can check versions for applicability. In string
 * format, it is represented as a comma-separated list of specific versions, closed
 * ranges (expressed using "[1.2.3-4.5)" syntax and open ranges (expressed using "[1.2.3" or "4.5)"
 * syntax). The square brackets indicate that the range includes the specified version. The parenthesis
 * indicate that the range goes up to, but does not actually include the specified version.
 *
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class VersionConstraint
{
    private static final int SM_RANGE_STARTING = 0;
    private static final int SM_RANGE_ENDED = 1;
    private static final int SM_VERSION_STARTING = 2;
    private static final int SM_VERSION_SEGMENT_STARTING = 3;
    private static final int SM_VERSION_SEGMENT_CONTINUING = 4;
    private static final int SM_VERSION_ENDED = 5;

    private final List<Range> ranges;

    public VersionConstraint( final String expr )
    {
        if( expr == null )
        {
            throw new IllegalArgumentException();
        }

        final ListFactory<Range> rangesListFactory = ListFactory.start();

        int state = SM_RANGE_STARTING;
        Range.Factory range = null;
        StringBuilder buf = null;

        for( int position = 0, n = expr.length(); position < n; position++ )
        {
            final char ch = expr.charAt( position );

            switch( state )
            {
                case SM_RANGE_STARTING:
                {
                    if( ch == ' ' )
                    {
                        // ignore
                    }
                    else if( ch == '[' )
                    {
                        range = new Range.Factory();
                        range.minVersionInclusive = true;
                        buf = new StringBuilder();
                        state = SM_VERSION_STARTING;
                    }
                    else if( ch == '(' )
                    {
                        range = new Range.Factory();
                        range.minVersionInclusive = false;
                        buf = new StringBuilder();
                        state = SM_VERSION_STARTING;
                    }
                    else if( ch >= '0' && ch <= '9' )
                    {
                        buf = new StringBuilder();
                        buf.append( ch );
                        state = SM_VERSION_SEGMENT_CONTINUING;
                    }
                    else
                    {
                        throw new IllegalArgumentException();
                    }

                    break;
                }
                case SM_RANGE_ENDED:
                {
                    if( ch == ' ' )
                    {
                        // ignore
                    }
                    else if( ch == ',' )
                    {
                        state = SM_RANGE_STARTING;
                    }
                    else
                    {
                        throw new IllegalArgumentException();
                    }

                    break;
                }
                case SM_VERSION_STARTING:
                {
                    if( ch == ' ' )
                    {
                        // ignore
                    }
                    else if( ch >= '0' && ch <= '9' )
                    {
                        buf.append( ch );
                        state = SM_VERSION_SEGMENT_CONTINUING;
                    }
                    else
                    {
                        throw new IllegalArgumentException();
                    }

                    break;
                }
                case SM_VERSION_SEGMENT_STARTING:
                {
                    if( ch >= '0' && ch <= '9' )
                    {
                        buf.append( ch );
                        state = SM_VERSION_SEGMENT_CONTINUING;
                    }
                    else
                    {
                        throw new IllegalArgumentException();
                    }

                    break;
                }
                case SM_VERSION_SEGMENT_CONTINUING:
                {
                    if( ch >= '0' && ch <= '9' )
                    {
                        buf.append( ch );
                    }
                    else if( ch == '.' )
                    {
                        buf.append( ch );
                        state = SM_VERSION_SEGMENT_STARTING;
                    }
                    else if( ch == ' ' )
                    {
                        state = SM_VERSION_ENDED;
                    }
                    else if( ch == ']' )
                    {
                        if( range == null )
                        {
                            range = new Range.Factory();
                        }

                        range.maxVersion = new Version( buf.toString() );
                        range.maxVersionInclusive = true;

                        rangesListFactory.add( range.create() );

                        range = null;
                        buf = null;

                        state = SM_RANGE_ENDED;
                    }
                    else if( ch == ')' )
                    {
                        if( range == null )
                        {
                            range = new Range.Factory();
                        }

                        range.maxVersion = new Version( buf.toString() );
                        range.maxVersionInclusive = false;

                        rangesListFactory.add( range.create() );

                        range = null;
                        buf = null;

                        state = SM_RANGE_ENDED;
                    }
                    else if( ch == '-' )
                    {
                        if( range == null )
                        {
                            throw new IllegalArgumentException();
                        }

                        range.minVersion = new Version( buf.toString() );

                        buf = new StringBuilder();

                        state = SM_VERSION_STARTING;
                    }
                    else if( ch == ',' )
                    {
                        if( range == null )
                        {
                            range = new Range.Factory();
                            range.minVersion = new Version( buf.toString() );
                            range.maxVersion = range.minVersion;
                            range.minVersionInclusive = true;
                            range.maxVersionInclusive = true;
                        }
                        else
                        {
                            range.minVersion = new Version( buf.toString() );
                        }

                        rangesListFactory.add( range.create() );

                        range = null;
                        buf = null;

                        state = SM_RANGE_STARTING;
                    }
                    else
                    {
                        throw new IllegalArgumentException();
                    }

                    break;
                }
                case SM_VERSION_ENDED:
                {
                    if( ch == ' ' )
                    {
                        // ignore
                    }
                    else if( ch == ']' )
                    {
                        if( range == null )
                        {
                            range = new Range.Factory();
                        }

                        range.maxVersion = new Version( buf.toString() );
                        range.maxVersionInclusive = true;

                        rangesListFactory.add( range.create() );

                        range = null;
                        buf = null;

                        state = SM_RANGE_ENDED;
                    }
                    else if( ch == ')' )
                    {
                        if( range == null )
                        {
                            range = new Range.Factory();
                        }

                        range.maxVersion = new Version( buf.toString() );
                        range.maxVersionInclusive = false;

                        rangesListFactory.add( range.create() );

                        range = null;
                        buf = null;

                        state = SM_RANGE_ENDED;
                    }
                    else if( ch == '-' )
                    {
                        if( range == null )
                        {
                            throw new IllegalArgumentException();
                        }

                        range.minVersion = new Version( buf.toString() );

                        buf = new StringBuilder();

                        state = SM_VERSION_STARTING;
                    }
                    else if( ch == ',' )
                    {
                        if( range == null )
                        {
                            range = new Range.Factory();
                            range.minVersion = new Version( buf.toString() );
                            range.maxVersion = range.minVersion;
                            range.minVersionInclusive = true;
                            range.maxVersionInclusive = true;
                        }
                        else
                        {
                            range.minVersion = new Version( buf.toString() );
                        }

                        rangesListFactory.add( range.create() );

                        range = null;
                        buf = null;

                        state = SM_RANGE_STARTING;
                    }
                    else
                    {
                        throw new IllegalArgumentException();
                    }

                    break;
                }
                default:
                {
                    throw new IllegalStateException();
                }
            }
        }

        if( state == SM_VERSION_SEGMENT_CONTINUING || state == SM_VERSION_ENDED )
        {
            if( range == null )
            {
                range = new Range.Factory();
                range.minVersion = new Version( buf.toString() );
                range.maxVersion = range.minVersion;
                range.minVersionInclusive = true;
                range.maxVersionInclusive = true;
            }
            else
            {
                range.minVersion = new Version( buf.toString() );
            }

            rangesListFactory.add( range.create() );

            range = null;
            buf = null;

            state = SM_RANGE_ENDED;
        }

        if( state != SM_RANGE_ENDED )
        {
            throw new IllegalArgumentException();
        }

        this.ranges = rangesListFactory.result();
    }

    public List<Range> ranges()
    {
        return this.ranges;
    }

    public boolean check( final Version version )
    {
        for( Range subexpr : this.ranges )
        {
            if( subexpr.check( version ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean check( final String version )
    {
        return check( new Version( version ) );
    }

    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof VersionConstraint )
        {
            final VersionConstraint constraint = (VersionConstraint) obj;
            return this.ranges.equals( constraint.ranges );
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return this.ranges.hashCode();
    }

    @Override
    public String toString()
    {
        final StringBuffer buf = new StringBuffer();

        for( Range subexpr : this.ranges )
        {
            if( buf.length() > 0 ) buf.append( ',' );
            buf.append( subexpr.toString() );
        }

        return buf.toString();
    }

    public static final class Range
    {
        private final Limit min;
        private final Limit max;

        private Range( final Limit min,
                       final Limit max )
        {
            if( min == null && max == null )
            {
                throw new IllegalArgumentException();
            }

            this.min = min;
            this.max = max;
        }

        public Limit min()
        {
            return this.min;
        }

        public Limit max()
        {
            return this.max;
        }

        public boolean check( final Version version )
        {
            if( this.min != null )
            {
                final int res = version.compareTo( this.min.version() );

                if( ! ( res > 0 || ( res == 0 && this.min.inclusive() ) ) )
                {
                    return false;
                }
            }

            if( this.max != null )
            {
                final int res = version.compareTo( this.max.version() );

                if( ! ( res < 0 || ( res == 0 && this.max.inclusive() ) ) )
                {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof Range )
            {
                final Range range = (Range) obj;
                return EqualsFactory.start().add( this.min, range.min ).add( this.max, range.max ).result();
            }

            return false;
        }

        @Override
        public int hashCode()
        {
            return HashCodeFactory.start().add( this.min ).add( this.max ).result();
        }

        @Override
        public String toString()
        {
            if( this.min != null && this.max != null &&
                this.min.version().equals( this.max.version() ) &&
                (this.min.inclusive() == this.max.inclusive()) )
            {
                return this.min.version().toString();
            }
            else
            {
                final StringBuffer buf = new StringBuffer();

                if( this.min != null )
                {
                    buf.append( this.min.inclusive() ? '[' : '(' );
                    buf.append( this.min.version().toString() );
                }

                if( this.max != null )
                {
                    if( buf.length() != 0 )
                    {
                        buf.append( '-' );
                    }

                    buf.append( this.max.version().toString() );
                    buf.append( this.max.inclusive() ? ']' : ')' );
                }

                return buf.toString();
            }
        }

        public static final class Limit
        {
            private final Version version;
            private final boolean inclusive;

            private Limit( final Version version,
                           final boolean inclusive )
            {
                if( version == null )
                {
                    throw new IllegalArgumentException();
                }

                this.version = version;
                this.inclusive = inclusive;
            }

            public Version version()
            {
                return this.version;
            }

            public boolean inclusive()
            {
                return this.inclusive;
            }

            @Override
            public boolean equals( final Object obj )
            {
                if( obj instanceof Limit )
                {
                    final Limit limit = (Limit) obj;
                    return this.version.equals( limit.version ) && this.inclusive == limit.inclusive;
                }

                return false;
            }

            @Override
            public int hashCode()
            {
                return this.version.hashCode() ^ Boolean.valueOf( this.inclusive ).hashCode();
            }
        }

        private static final class Factory
        {
            public Version minVersion;
            public boolean minVersionInclusive;
            public Version maxVersion;
            public boolean maxVersionInclusive;

            public Range create()
            {
                final Limit min = ( this.minVersion == null ? null : new Limit( this.minVersion, this.minVersionInclusive ) );
                final Limit max = ( this.maxVersion == null ? null : new Limit( this.maxVersion, this.maxVersionInclusive ) );

                return new Range( min, max );
            }
        }

    }

}
