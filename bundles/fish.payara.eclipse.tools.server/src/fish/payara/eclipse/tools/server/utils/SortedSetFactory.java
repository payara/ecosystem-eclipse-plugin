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
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import static fish.payara.eclipse.tools.server.utils.MiscUtil.equal;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SortedSetFactory<E>
{
    private final Comparator<E> comparator;
    private Filter<E> filter;
    private E firstElement = null;
    private SortedSet<E> set = null;
    private boolean exported = false;
    
    private SortedSetFactory( final Comparator<E> comparator )
    {
        this.comparator = comparator;
    }
    
    @SuppressWarnings( "unchecked" )
    
    public static <E> SortedSet<E> empty()
    {
        return (SortedSet<E>) EMPTY_SORTED_SET;
    }
    
    public static <E> SortedSet<E> singleton( final E element )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        return new SingletonSortedSet<E>( null, element );
    }

    @SafeVarargs
    
    public static <E> SortedSet<E> unmodifiable( final E... elements )
    {
        return SortedSetFactory.<E>start().add( elements ).result();
    }
    
    public static <E> SortedSet<E> unmodifiable( final Collection<E> elements )
    {
        return SortedSetFactory.<E>start().add( elements ).result();
    }
    
    public static <E> SortedSetFactory<E> start()
    {
        return start( null );
    }

    public static <E> SortedSetFactory<E> start( final Comparator<E> comparator )
    {
        return new SortedSetFactory<E>( comparator );
    }
    
    public SortedSetFactory<E> filter( final Filter<E> filter )
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
                    this.firstElement = this.set.first();
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
    
    public SortedSetFactory<E> add( final E element )
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
                this.set = new TreeSet<E>( this.comparator );
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
    
    public final SortedSetFactory<E> add( final E... elements )
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
    
    public SortedSetFactory<E> add( final Collection<E> elements )
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
    
    public E first()
    {
        E first;
        
        if( this.set != null )
        {
            first = this.set.first();
        }
        else if( this.firstElement != null )
        {
            first = this.firstElement;
        }
        else
        {
            throw new NoSuchElementException();
        }
        
        return first;
    }
    
    public E last()
    {
        E last;
        
        if( this.set != null )
        {
            last = this.set.last();
        }
        else if( this.firstElement != null )
        {
            last = this.firstElement;
        }
        else
        {
            throw new NoSuchElementException();
        }
        
        return last;
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

    public SortedSet<E> result()
    {
        if( this.exported )
        {
            throw new IllegalStateException();
        }
        
        this.exported = true;
        
        if( this.set != null )
        {
            return Collections.unmodifiableSortedSet( this.set );
        }
        else if( this.firstElement != null )
        {
            return new SingletonSortedSet<E>( this.comparator, this.firstElement );
        }
        else
        {
            return empty();
        }
    }
    
    private static final Iterator<Object> EMPTY_ITERATOR = new Iterator<Object>()
    {
        public boolean hasNext()
        {
            return false;
        }
        
        public Object next()
        {
            throw new NoSuchElementException();
        }
        
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    };
    
    private static final Object[] EMPTY_ARRAY = new Object[ 0 ];
    
    private static final SortedSet<Object> EMPTY_SORTED_SET = new SortedSet<Object>()
    {
        public int size()
        {
            return 0;
        }

        public boolean isEmpty()
        {
            return true;
        }

        public boolean contains( final Object object )
        {
            return false;
        }

        public Iterator<Object> iterator()
        {
            return EMPTY_ITERATOR;
        }

        public Object[] toArray()
        {
            return EMPTY_ARRAY;
        }

        public <T> T[] toArray( final T[] array )
        {
            if( array.length > 0 )
            {
                array[ 0 ] = null;
            }

            return array;
        }

        public boolean add( final Object object )
        {
            throw new UnsupportedOperationException();
        }

        public boolean remove( Object object )
        {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll( final Collection<?> collection )
        {
            return false;
        }

        public boolean addAll( final Collection<? extends Object> collection )
        {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll( final Collection<?> collection )
        {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll( final Collection<?> collection )
        {
            throw new UnsupportedOperationException();
        }

        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        public Comparator<? super Object> comparator()
        {
            return null;
        }

        public SortedSet<Object> subSet( final Object fromElement,
                                         final Object toElement )
        {
            return EMPTY_SORTED_SET;
        }

        public SortedSet<Object> headSet( final Object toElement )
        {
            return EMPTY_SORTED_SET;
        }

        public SortedSet<Object> tailSet( final Object fromElement )
        {
            return EMPTY_SORTED_SET;
        }

        public Object first()
        {
            return new NoSuchElementException();
        }

        public Object last()
        {
            return new NoSuchElementException();
        }
    };
    
    private static final class SingletonSortedSet<E> implements SortedSet<E>
    {
        private final Comparator<E> comparator;
        private final E entry;
        
        public SingletonSortedSet( final Comparator<E> comparator,
                                   final E entry )
        {
            this.comparator = comparator;
            this.entry = entry;
        }

        public int size()
        {
            return 1;
        }

        public boolean isEmpty()
        {
            return false;
        }

        @SuppressWarnings( "unchecked" )
        
        public boolean contains( final Object object )
        {
            return ( this.comparator == null ? equal( this.entry, object ) : this.comparator.compare( this.entry, (E) object ) == 0 );
        }

        public boolean containsAll( final Collection<?> collection )
        {
            for( Object object : collection )
            {
                if( ! contains( object ) )
                {
                    return false;
                }
            }
            
            return true;
        }

        public Iterator<E> iterator()
        {
            return new Iterator<E>()
            {
                private boolean hasNext = true;
                
                public boolean hasNext()
                {
                    return this.hasNext;
                }
                
                public E next()
                {
                    if( this.hasNext )
                    {
                        this.hasNext = false;
                        return SingletonSortedSet.this.entry;
                    }
                    
                    throw new NoSuchElementException();
                }
                
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public Object[] toArray()
        {
            return new Object[] { this.entry };
        }

        @SuppressWarnings( "unchecked" )
        
        public <T> T[] toArray( final T[] array )
        {
            T[] a = array;
            
            if( a.length == 0 )
            {
                a = (T[]) java.lang.reflect.Array.newInstance( a.getClass().getComponentType(), 1 );
                a[ 0 ] = (T) this.entry;
            }
            else
            {
                a[ 0 ] = (T) this.entry;
                
                if( a.length > 1 )
                {
                    a[ 1 ] = null;
                }
            }
            
            return a;
        }

        public boolean add( final E object )
        {
            throw new UnsupportedOperationException();
        }

        public boolean remove( final Object object )
        {
            throw new UnsupportedOperationException();
        }

        public boolean addAll( final Collection<? extends E> collection )
        {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll( final Collection<?> collection )
        {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll( final Collection<?> collection )
        {
            throw new UnsupportedOperationException();
        }

        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        public Comparator<? super E> comparator()
        {
            return null;
        }

        public SortedSet<E> subSet( final E fromElement,
                                    final E toElement )
        {
            return empty();
        }

        public SortedSet<E> headSet( final E toElement )
        {
            return empty();
        }

        public SortedSet<E> tailSet( final E fromElement )
        {
            if( contains( fromElement ) )
            {
                return this;
            }
            
            return empty();
        }

        public E first()
        {
            return this.entry;
        }

        public E last()
        {
            return this.entry;
        }

        @Override
        public String toString()
        {
            return "[" + this.entry.toString() + "]";
        }
    }
    
}
