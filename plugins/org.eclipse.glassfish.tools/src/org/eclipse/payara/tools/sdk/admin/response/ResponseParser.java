/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin.response;

import java.io.InputStream;

/**
 * Interface for various implementations of parsing response functionality.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface ResponseParser {

    /**
     * Method parses the response and returns it's object representation - <code>ActionReport</code>.
     * <p>
     *
     * @param in - input stream object
     * @return <i>ActionReport</i> object that represents the response.
     */
    public ActionReport parse(InputStream in);

}
