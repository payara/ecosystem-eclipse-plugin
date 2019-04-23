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

package org.eclipse.corundum;

import java.io.File;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DelegatingOperationContext extends OperationContext {
    private final OperationContext delegate;

    public DelegatingOperationContext(OperationContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public void log(String message) {
        delegate.log(message);
    }

    @Override
    public File file(File file) {
        return delegate.file(file);
    }

}