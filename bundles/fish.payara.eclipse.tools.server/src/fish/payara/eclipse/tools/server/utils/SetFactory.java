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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SetFactory<E>
{
    private Filter<E> filter;
    private E firstElement = null;
    private Set<E> set = null;
    private boolean exported = false;
    
    private SetFactory() {}
    
    public static <E> Set<E> empty()
    {
        return Collections.emptySet();
    }
    
    public static <E> Set<E> singleton( final E element )
    {
        return Collections.singleton( element );
    }
    
    @SafeVarargs
    
    public static <E> Set<E> unmodifiable( final E... elements )
    {
        return SetFactory.<E>start().add( elements ).result();
    }
    
    public static <E> Set<E> unmodifiable( final Collection<E> elements )
    {
        return SetFactory.<E>start().add( elements ).result();
    }
    
    public static <E> SetFactory<E> start()
    {
        return new SetFactory<E>();
    }
    
    public SetFactory<E> filter( final Filter<E> filter )
    {
        if( this.exported )
        {
            throw new IllegalStateException();
        }
        
        this.filter = filter;
        
        if( this.filter != null )
        {
            if( this.set != null )
            {
                for( Iterator<E> itr = this.set.iterator(); itr.hasNext(); )
                {
                    if( ! this.filter.allows( itr.next() ) )
                    {
                        itr.remove();
                    }
                }
                
                final int size = this.set.size();
                
                if( size == 1 )
                {
                    this.firstElement = this.set.iterator().next();
                    this.set = null;
                }
                else if( size == 0 )
                {
                    this.set = null;
                }
            }
            else if( this.firstElement != null )
            {
                if( ! this.filter.allows( this.firstElement ) )
                {
                    this.firstElement = null;
                }
            }
        }
        
        return this;
    }
    
    public SetFactory<E> add( final E element )
    {
        if( this.exported )
        {
            throw new IllegalStateException();
        }
        
        if( element != null && ( this.filter == null || this.filter.allows( element ) ) )
        {
            if( this.set != null )
            {
                this.set.add( element );
            }
            else if( this.firstElement != null )
            {
                this.set = new LinkedHashSet<E>();
                this.set.add( this.firstElement );
                this.set.add( element );
                this.firstElement = null;
            }
            else
            {
                this.firstElement = element;
            }
        }
        
        return this;
    }
    
    @SafeVarargs
    
    public final SetFactory<E> add( final E... elements )
    {
    	if( elements != null )
    	{
	        for( E element : elements )
	        {
	            add( element );
	        }
    	}
        
        return this;
    }
    
    public SetFactory<E> add( final Collection<E> elements )
    {
    	if( elements != null )
    	{
	        for( E element : elements )
	        {
	            add( element );
	        }
    	}
        
        return this;
    }
    
    public boolean remove( final E element )
    {
        boolean removed = false;
        
        if( element != null )
        {
            if( this.set != null )
            {
                removed = this.set.remove( element );
                
                if( this.set.size() == 1 )
                {
                    this.firstElement = this.set.iterator().next();
                    this.set = null;
                }
            }
            else if( this.firstElement != null && this.firstElement.equals( element ) )
            {
                removed = true;
                this.firstElement = null;
            }
        }
        
        return removed;
    }
    
    public boolean contains( final E element )
    {
        boolean contains = false;
        
        if( this.set != null )
        {
            contains = this.set.contains( element );
        }
        else if( this.firstElement != null && this.firstElement.equals( element ) )
        {
            contains = true;
        }
        
        return contains;
    }

    public int size()
    {
        final int size;
        
        if( this.set != null )
        {
            size = this.set.size();
        }
        else if( this.firstElement != null)
        {
            size = 1;
        }
        else
        {
            size = 0;
        }
        
        return size;
    }

    public Set<E> result()
    {
        if( this.exported )
        {
            throw new IllegalStateException();
        }
        
        this.exported = true;
        
        if( this.set != null )
        {
            return Collections.unmodifiableSet( this.set );
        }
        else if( this.firstElement != null )
        {
            return Collections.singleton( this.firstElement );
        }
        else
        {
            return Collections.emptySet();
        }
    }
    
}
