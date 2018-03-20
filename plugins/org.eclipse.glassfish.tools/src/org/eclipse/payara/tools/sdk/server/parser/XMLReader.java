/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.parser;

import java.util.List;

/**
 * Interface for various implementations that read data from domain config (domain.xml).
 * 
 * 
 * @author Peter Benedikovic, Tomas Kraus
 */
public interface XMLReader {
    
    /**
     * Every implementation needs to provide path objects.
     * Path represents the xpath on which the reader wants to be notified.
     * 
     * @return paths that the reader listens to
     */
    public List<TreeParser.Path> getPathsToListen();
    
}
