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

package fish.payara.eclipse.tools.server;

import static fish.payara.eclipse.tools.server.utils.MiscUtil.equal;
import fish.payara.eclipse.tools.server.utils.SortedSetFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Status
{
    public static final String TYPE_MISC_OK = "Miscellaneous.Ok";
    public static final String TYPE_MISC_PROBLEM = "Miscellaneous.Problem";
    
    private static final String DEFAULT_OK_MESSAGE = "ok";

    private static final Status OK_STATUS = new Status( Severity.OK, TYPE_MISC_OK, DEFAULT_OK_MESSAGE, null, SortedSetFactory.<Status>empty() );
    
    public static Status createOkStatus()
    {
        return OK_STATUS;
    }
    
    public static Status createWarningStatus( final String message )
    {
        return createStatus( Severity.WARNING, message );
    }
    
    public static Status createErrorStatus( final String message )
    {
        return createErrorStatus( message, null );
    }
    
    public static Status createErrorStatus( final Throwable exception )
    {
        if( exception == null )
        {
            throw new IllegalArgumentException();
        }
        
        final String text = exception.getMessage();
        final StringBuilder msg = new StringBuilder();
        
        msg.append( exception.getClass().getSimpleName() );
        
        if( text != null && text.length() != 0 )
        {
            msg.append( ": " );
            msg.append( text );
        }
        
        return createErrorStatus( msg.toString(), exception );
    }
    
    public static Status createErrorStatus( final String message,
                                            final Throwable exception )
    {
        return createStatus( Severity.ERROR, message, exception );
    }
    
    public static Status createStatus( final Severity severity,
                                       final String message,
                                       final Throwable exception )
    {
        return factoryForLeaf().severity( severity ).message( message ).exception( exception ).create();
    }
    
    public static Status createStatus( final Severity severity,
                                       final String message )
    {
        return createStatus( severity, message, null );
    }
    
    public static LeafStatusFactory factoryForLeaf()
    {
        return new LeafStatusFactory();
    }
    
    public static CompositeStatusFactory factoryForComposite()
    {
        return new CompositeStatusFactory();
    }
    
    private final Severity severity;
    private final String type;
    private final String message;
    private final Throwable exception;
    private final SortedSet<Status> children;
    
    private Status( final Severity severity,
                    final String type,
                    final String message,
                    final Throwable exception,
                    final SortedSet<Status> children )
    {
        this.severity = severity;
        this.type = ( type == null ? ( severity == Severity.OK ? TYPE_MISC_OK : TYPE_MISC_PROBLEM ) : type );
        this.message = message;
        this.exception = exception;
        this.children = children;
    }
    
    public boolean ok()
    {
        return ( this.severity == Severity.OK );
    }

    public Severity severity()
    {
        return this.severity;
    }
    
    public String type()
    {
        return this.type;
    }

    public String message()
    {
        return this.message;
    }

    public Throwable exception()
    {
        return this.exception;
    }

    public SortedSet<Status> children()
    {
        return this.children;
    }
    
    public boolean contains( final String type )
    {
        if( equal( this.type, type ) )
        {
            return true;
        }
        
        for( Status child : this.children )
        {
            if( child.contains( type ) )
            {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj == this )
        {
            return true;
        }
        
        if( obj instanceof Status )
        {
            final Status st = (Status) obj;
            
            if( st.severity() == severity() && 
                st.children().size() == children().size() && 
                st.exception() == exception() && 
                st.message().equals( message() ) )
            {
                for( Iterator<Status> itr1 = st.children().iterator(), itr2 = children().iterator(); itr1.hasNext(); )
                {
                    if( ! itr1.next().equals( itr2.next() ) )
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
        return severity().code() ^ message().hashCode();
    }
    
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        
        buf.append( severity().name() );
        buf.append( " : " );
        buf.append( message() );
        
        final Throwable e = exception();
        
        if( e != null )
        {
            buf.append( System.getProperty( "line.separator" ) );

            final StringWriter sw = new StringWriter();
            e.printStackTrace( new PrintWriter( sw ) );
            buf.append( sw.toString() );
        }
        
        return buf.toString();
    }

    public enum Severity
    {
        OK( 0 ),
        WARNING( 1 ),
        ERROR( 2 );
        
        private int code;
        
        private Severity( final int code )
        {
            this.code = code;
        }
        
        public int code()
        {
            return this.code;
        }
    }
    
    public static final class LeafStatusFactory
    {
        private Severity severity;
        private String type;
        private String message;
        private Throwable exception;
        
        private LeafStatusFactory()
        {
            // No direct public instantiation. Use factoryForLeaf() method instead.
        }
        
        public LeafStatusFactory severity( final Severity severity )
        {
            this.severity = severity;
            return this;
        }

        public LeafStatusFactory type( final String type )
        {
            this.type = type;
            return this;
        }

        public LeafStatusFactory message( final String message )
        {
            this.message = message;
            return this;
        }

        public LeafStatusFactory exception( final Throwable exception )
        {
            this.exception = exception;
            return this;
        }
        
        public Status create()
        {
            if( this.severity == null )
            {
                throw new IllegalStateException();
            }
            
            if( this.message == null )
            {
                throw new IllegalStateException();
            }
            
            return new Status( this.severity, this.type, this.message, this.exception, SortedSetFactory.<Status>empty() );
        }
    }
    
    public static final class CompositeStatusFactory
    {
        private final SortedSetFactory<Status> children = SortedSetFactory.start( StatusComparator.INSTANCE );
        
        private CompositeStatusFactory()
        {
            // No direct public instantiation. Use factoryForComposite() method instead.
        }
        
        public CompositeStatusFactory merge( final Status status ) 
        {
            if( status != null )
            {
                final SortedSet<Status> children = status.children();
                
                if( children.isEmpty() )
                {
                    final Severity sev = status.severity();
                    
                    if( sev != Severity.OK )
                    {
                        this.children.add( status );
                    }
                }
                else
                {
                    for( Status st : children )
                    {
                        merge( st );
                    }
                }
            }
            
            return this;
        }
        
        public Status create()
        {
            final int count = this.children.size();
            
            if( count == 0 )
            {
                return createOkStatus();
            }
            else
            {
                final Status first = this.children.first();
                
                if( count == 1 )
                {
                    return first;
                }
                else
                {
                    return new Status( first.severity(), TYPE_MISC_PROBLEM, first.message(), first.exception(), this.children.result() );
                }
            }
        }
    }
    
    private static final class StatusComparator implements Comparator<Status>
    {
        private static final StatusComparator INSTANCE = new StatusComparator();
        
        public int compare( final Status x,
                            final Status y )
        {
            int result = y.severity.code() - x.severity.code();
            
            if( result == 0 )
            {
                result = x.message.compareTo( y.message );
                
                if( result == 0 )
                {
                    result = x.type.compareTo( y.type );
                    
                    if( result == 0 )
                    {
                        if( x.exception != y.exception )
                        {
                            if( x.exception == null )
                            {
                                result = -1;
                            }
                            else if( y.exception == null )
                            {
                                result = 1;
                            }
                            else
                            {
                                result = x.exception.getClass().getName().compareTo( y.exception.getClass().getName() );
                                
                                if( result == 0 )
                                {
                                    result = System.identityHashCode( x.exception ) - System.identityHashCode( y.exception );
                                }
                            }
                        }
                        
                        if( result == 0 )
                        {
                            result = x.children.size() - y.children.size();
                            
                            if( result == 0 )
                            {
                                final Iterator<Status> xChildren = x.children.iterator();
                                final Iterator<Status> yChildren = y.children.iterator();
                                
                                while( xChildren.hasNext() && result == 0 )
                                {
                                    result = compare( xChildren.next(), yChildren.next() );
                                }
                            }
                        }
                    }
                }
            }
            
            return result;
        }
    }
    
}
