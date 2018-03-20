/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.serverview;


/**
 *
 * @author Peter Williams
 */
public class ResourceDesc implements Comparable<ResourceDesc> {
    
    private final String name;
    private final String cmdSuffix;
    
    public ResourceDesc(final String name, final String cmdSuffix) {
        this.name = name;
        this.cmdSuffix = cmdSuffix;
    }

    public String getName() {
        return name;
    }

    public String getCommandSuffix() {
        return cmdSuffix;
    }

    public int compareTo(ResourceDesc o) {
        int result = name.compareTo(o.name);
        if(result == 0) {
            result = cmdSuffix.compareTo(o.cmdSuffix);
        }
        return result;
    }
    
}
