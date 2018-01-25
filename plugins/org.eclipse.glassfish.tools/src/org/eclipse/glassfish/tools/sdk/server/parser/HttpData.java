/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.server.parser;

/**
 * Data holder for port informations from domain.xml.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class HttpData {

	private final String id;
    private final int port;
    private final boolean secure;
    
    public HttpData(String id, int port, boolean secure) {
        this.id = id;
        this.port = port;
        this.secure = secure;
    }
    
    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public boolean isSecure() {
        return secure;
    }
    
    @Override
    public String toString() {
        return "{ " + id + ", " + port + ", " + secure + " }";	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
