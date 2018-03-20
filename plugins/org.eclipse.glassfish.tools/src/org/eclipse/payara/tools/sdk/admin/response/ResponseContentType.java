/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin.response;

/**
 * Enum that represents possible content types that runners accept responses in.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum ResponseContentType {

    APPLICATION_XML("application/xml"), APPLICATION_JSON("application/json"), TEXT_PLAIN("text/plain");

    private String type;

    ResponseContentType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
