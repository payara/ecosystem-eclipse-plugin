/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.utils;

/**
 *
 * @author vbk
 */
public class PartialCompletionException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String failedUpdates;

    PartialCompletionException(String itemsNotUpdated) {
        // throw new UnsupportedOperationException("Not yet implemented");
        failedUpdates = itemsNotUpdated;
    }

    @Override
    public String getMessage() {
        return "Failed to update: " + failedUpdates;
    }

}
