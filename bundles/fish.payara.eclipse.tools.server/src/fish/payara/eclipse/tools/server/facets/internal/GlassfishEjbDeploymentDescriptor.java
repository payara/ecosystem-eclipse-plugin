/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.facets.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import fish.payara.eclipse.tools.server.facets.IGlassfishEjbDeploymentDescriptor;

class GlassfishEjbDeploymentDescriptor extends
        AbstractGlassfishDeploymentDescriptor implements IGlassfishEjbDeploymentDescriptor {

    private IFile file;

    GlassfishEjbDeploymentDescriptor(IFile file) {
        this.file = file;
    }

    @Override
    protected void prepareDescriptor() {

    }

    @Override
    protected boolean isPossibleToCreate() {
        // check for existence of older sun descriptor
        IPath sunDescriptor = file.getLocation().removeLastSegments(1)
                .append(IGlassfishEjbDeploymentDescriptor.SUN_EJB_DEPLOYMENT_DESCRIPTOR_NAME);
        if (sunDescriptor.toFile().exists()) {
            return false;
        }
        return true;
    }

    @Override
    protected void save() {
    }

}
