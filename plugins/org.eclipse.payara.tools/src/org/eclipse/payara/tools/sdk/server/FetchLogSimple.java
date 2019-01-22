/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server;

import java.io.InputStream;

/**
 * Fetch GlassFish log from provided stream.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class FetchLogSimple extends FetchLog {

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server log fetcher using provided stream.
     * <p/>
     * Super class constructor will not call <code>initInputStream</code> method so this method should
     * be ignored. Old log lines are never skipped so whole log is always available in
     * <code>InputStream</code>
     * <p/>
     *
     * @param in Input stream to access server log.
     */
    public FetchLogSimple(InputStream in) {
        super(in, false);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor callback makes no sense in this child class.
     * <p/>
     * This method throws an exception when called.
     * <p/>
     *
     * @return <code>FileInputStream</code> where log lines received from server will be available to
     * read.
     */
    @Override
    InputStream initInputStream() {
        throw new UnsupportedOperationException(
                "Method initInputStream should not be called in " +
                        "FetchLogSimple class!");
    }
}
