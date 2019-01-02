/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.internal;

import java.util.List;

import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.server.core.IRuntime;

/**
 * This abstract class is used in conjunction with <code>runtimeComponentProviders</code> extension
 * point to extend the list of runtime components that make up a Payara Server runtime.
 *
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class RuntimeComponentProvider {

    /**
     * Returns additional components to add to the runtime that represents the provided GlassFish Server
     * installation.
     *
     * @param runtime the WTP server tools runtime definition
     * @return list of additional components or <code>null</code> to not contribute anything
     */
    public abstract List<IRuntimeComponent> getRuntimeComponents(IRuntime runtime);

}
