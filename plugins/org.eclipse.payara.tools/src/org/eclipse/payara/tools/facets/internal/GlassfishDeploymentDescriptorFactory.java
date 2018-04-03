/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.facets.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.payara.tools.facets.IGlassfishEjbDeploymentDescriptor;
import org.eclipse.payara.tools.facets.IGlassfishWebDeploymentDescriptor;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;

public class GlassfishDeploymentDescriptorFactory {

    static final String WEB_INF = "WEB-INF";
    static final String META_INF = "META-INF";

    static final String WEB_DEPLOYMENT_DESCRIPTOR_NAME = "glassfish-web.xml";
    static final String EJB_DEPLOYMENT_DESCRIPTOR_NAME = "glassfish-ejb-jar.xml";

    public static IGlassfishWebDeploymentDescriptor getWebDeploymentDescriptor(IProject project) {
        IVirtualComponent comp = ComponentCore.createComponent(project);
        IPath projectPath = comp.getRootFolder().getUnderlyingFolder()
                .getProjectRelativePath();

        try {
            return new GlassfishWebDeploymentDescriptorSapphire(
                    project.getFile(projectPath.append(WEB_INF).append(WEB_DEPLOYMENT_DESCRIPTOR_NAME)));
        } catch (ResourceStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static IGlassfishEjbDeploymentDescriptor getEjbDeploymentDescriptor(IProject project) {
        IVirtualComponent comp = ComponentCore.createComponent(project);
        IPath projectPath = comp.getRootFolder().getUnderlyingFolder()
                .getProjectRelativePath();

        try {
            return new GlassfishEjbDeploymentDescriptorSapphire(
                    project.getFile(projectPath.append(META_INF).append(EJB_DEPLOYMENT_DESCRIPTOR_NAME)));
        } catch (ResourceStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

}
