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
public class WSDesc {
    
    private final String testUrl;
    private final String wsdlUrl;
    private final String name;
    
    public WSDesc(final String name, final String wsdlUrl, final String testUrl) {
        this.name = name;
        this.testUrl = testUrl;
        this.wsdlUrl = wsdlUrl;
    }

    public String getName() {
        return name;
    }
    
    public String getTestURL() {
        return testUrl;
    }
    
    public String getWsdlUrl() {
        return wsdlUrl;
    }

}
