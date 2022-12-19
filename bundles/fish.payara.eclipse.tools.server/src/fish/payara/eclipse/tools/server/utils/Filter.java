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
 * Generic filter interface that can be parameterized for different element types.
 *
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface Filter<E>
{
    /**
     * Evaluates whether the given element passes the criteria implemented by the filter.
     *
     * @param element the element to evaluate or null
     * @return true if the given element is allowed by the filter, false otherwise
     */

    boolean allows( E element );

}
