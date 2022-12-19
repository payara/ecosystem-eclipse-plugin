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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ListFactory<E>
{
    private Filter<E> filter;
    private E firstElement = null;
    private ArrayList<E> list = null;
    private boolean exported = false;

    private ListFactory() {}

    public static <E> List<E> empty()
    {
        return Collections.emptyList();
    }

    public static <E> List<E> singleton( final E element )
    {
        return Collections.singletonList( element );
    }

    @SafeVarargs

    public static <E> List<E> unmodifiable( final E... elements )
    {
        return ListFactory.<E>start().add( elements ).result();
    }

    public static <E> List<E> unmodifiable( final Collection<E> elements )
    {
        return ListFactory.<E>start().add( elements ).result();
    }

    public static <E> ListFactory<E> start()
    {
        return new ListFactory<>();
    }

    public ListFactory<E> filter( final Filter<E> filter )
    {
        if( this.exported )
        {
            throw new IllegalStateException();
        }

        this.filter = filter;

        if( this.filter != null )
        {
            if( this.list != null )
            {
                for( Iterator<E> itr = this.list.iterator(); itr.hasNext(); )
                {
                    if( ! this.filter.allows( itr.next() ) )
                    {
                        itr.remove();
                    }
                }

                final int size = this.list.size();

                if( size == 1 )
                {
                    this.firstElement = this.list.get( 0 );
                    this.list = null;
                }
                else if( size == 0 )
                {
                    this.list = null;
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

    public ListFactory<E> add( final E element )
    {
        if( this.exported )
        {
            throw new IllegalStateException();
        }

        if( element != null && ( this.filter == null || this.filter.allows( element ) ) )
        {
            if( this.list != null )
            {
                this.list.add( element );
            }
            else if( this.firstElement != null )
            {
                this.list = new ArrayList<>();
                this.list.add( this.firstElement );
                this.list.add( element );
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

    public final ListFactory<E> add( final E... elements )
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

    public ListFactory<E> add( final Collection<E> elements )
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

    public E remove( final int index )
    {
        final int size = size();

        if( index < 0 || index >= size )
        {
            throw new IllegalArgumentException();
        }

        E removed;

        if( this.list != null )
        {
            if( size == 2 )
            {
                removed = this.list.get( index );
                this.firstElement = this.list.get( index == 0 ? 1 : 0 );
                this.list = null;
            }
            else
            {
                removed = this.list.remove( index );
            }
        }
        else if( this.firstElement != null )
        {
            removed = this.firstElement;
            this.firstElement = null;
        }
        else
        {
            throw new IllegalStateException();
        }

        return removed;
    }

    public E get( final int index )
    {
        if( index < 0 || index >= size() )
        {
            throw new IllegalArgumentException();
        }

        E element;

        if( this.list != null )
        {
            element = this.list.get( index );
        }
        else if( this.firstElement != null )
        {
            element = this.firstElement;
        }
        else
        {
            throw new IllegalStateException();
        }

        return element;
    }

    public boolean contains( final E element )
    {
        boolean contains = false;

        if( this.list != null )
        {
            contains = this.list.contains( element );
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

        if( this.list != null )
        {
            size = this.list.size();
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

    public List<E> result()
    {
        if( this.exported )
        {
            throw new IllegalStateException();
        }

        this.exported = true;

        if( this.list != null )
        {
            this.list.trimToSize();
            return Collections.unmodifiableList( this.list );
        }
        else if( this.firstElement != null )
        {
            return Collections.singletonList( this.firstElement );
        }
        else
        {
            return Collections.emptyList();
        }
    }

}
