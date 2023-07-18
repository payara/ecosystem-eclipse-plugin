/** ****************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ***************************************************************************** */
/** ****************************************************************************
 * Copyright (c) 2018-2023 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ***************************************************************************** */
package fish.payara.eclipse.tools.server.facets.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;

import fish.payara.eclipse.tools.server.facets.IPayaraWebDeploymentDescriptor;
import fish.payara.eclipse.tools.server.facets.IPayaraEjbDeploymentDescriptor;

public class PayaraDeploymentDescriptorFactory {

    static final String WEB_INF = "WEB-INF";
    static final String META_INF = "META-INF";

    static final String WEB_DEPLOYMENT_DESCRIPTOR_NAME = "payara-web.xml";
    static final String EJB_DEPLOYMENT_DESCRIPTOR_NAME = "glassfish-ejb-jar.xml";

    public static IPayaraWebDeploymentDescriptor getWebDeploymentDescriptor(IProject project) {
        IVirtualComponent comp = ComponentCore.createComponent(project);
        IPath projectPath = comp.getRootFolder().getUnderlyingFolder()
                .getProjectRelativePath();

        return new PayaraWebDeploymentDescriptor(
                project.getFile(projectPath.append(WEB_INF).append(WEB_DEPLOYMENT_DESCRIPTOR_NAME)));
    }

    public static IPayaraEjbDeploymentDescriptor getEjbDeploymentDescriptor(IProject project) {
        IVirtualComponent comp = ComponentCore.createComponent(project);
        IPath projectPath = comp.getRootFolder().getUnderlyingFolder()
                .getProjectRelativePath();

        return new PayaraEjbDeploymentDescriptor(
                project.getFile(projectPath.append(META_INF).append(EJB_DEPLOYMENT_DESCRIPTOR_NAME)));
    }

}
