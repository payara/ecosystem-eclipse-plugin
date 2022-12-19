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

package fish.payara.eclipse.tools.server.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.server.core.IRuntime;

import fish.payara.eclipse.tools.server.PayaraServerPlugin;

@SuppressWarnings("restriction")
public class WizardUtil {

    public static boolean isWebOrEJBProjectWithGF3Runtime(IProject project) {
        try {
            boolean result = project.isAccessible() &&
                    project.hasNature(IModuleConstants.MODULE_NATURE_ID) &&
                    (JavaEEProjectUtilities.isDynamicWebProject(project) ||
                            JavaEEProjectUtilities.isEJBProject(project));

            if (result) {
                return hasGF3Runtime(project);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isWebProjectWithGF3Runtime(IProject project) {
        try {
            boolean result = (project.isAccessible() &&
                    project.hasNature(IModuleConstants.MODULE_NATURE_ID) &&
                    JavaEEProjectUtilities.isDynamicWebProject(project));

            if (result) {
                return hasGF3Runtime(project);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean hasGF3Runtime(IProject project) {
        try {
            IRuntime runtime = J2EEProjectUtilities.getServerRuntime(project);
            if ((runtime != null) && PayaraServerPlugin.is31OrAbove(runtime)) {
                return true;
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return false;
    }
}
