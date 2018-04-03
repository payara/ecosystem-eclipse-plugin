/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.utils;

import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;

@SuppressWarnings("restriction")
public class ModuleUtil {

    public static boolean isEARModule(IModule[] modulePath) {
        return matchModuleType(modulePath, IModuleConstants.JST_EAR_MODULE);
    }

    public static boolean isWebModule(IModule[] modulePath) {
        return matchModuleType(modulePath, IModuleConstants.JST_WEB_MODULE);
    }

    public static boolean isEJBModule(IModule[] modulePath) {
        return matchModuleType(modulePath, IModuleConstants.JST_EJB_MODULE);
    }

    public static boolean isUtilityModule(IModule[] modulePath) {
        return matchModuleType(modulePath, IModuleConstants.JST_UTILITY_MODULE);
    }

    public static boolean isConnectorModule(IModule[] modulePath) {
        return matchModuleType(modulePath, IModuleConstants.JST_CONNECTOR_MODULE);
    }

    public static boolean isClientModule(IModule[] modulePath) {
        return matchModuleType(modulePath, IModuleConstants.JST_APPCLIENT_MODULE);
    }

    public static boolean isEARModule(IModule module) {
        return matchModuleType(module, IModuleConstants.JST_EAR_MODULE);
    }

    public static boolean isWebModule(IModule module) {
        return matchModuleType(module, IModuleConstants.JST_WEB_MODULE);
    }

    public static boolean isEJBModule(IModule module) {
        return matchModuleType(module, IModuleConstants.JST_EJB_MODULE);
    }

    public static boolean isUtilityModule(IModule module) {
        return matchModuleType(module, IModuleConstants.JST_UTILITY_MODULE);
    }

    public static boolean isWebFragmentModule(IModule module) {
        return matchModuleType(module, IModuleConstants.JST_WEBFRAGMENT_MODULE);
    }

    public static boolean isConnectorModule(IModule module) {
        return matchModuleType(module, IModuleConstants.JST_CONNECTOR_MODULE);
    }

    public static boolean isClientModule(IModule module) {
        return matchModuleType(module, IModuleConstants.JST_APPCLIENT_MODULE);
    }

    public static String getContextRoot(IModule module) {
        return J2EEProjectUtilities.getServerContextRoot(module.getProject());
    }

    private static boolean matchModuleType(IModule[] modulePath, String typeId) {
        IModule module = modulePath[modulePath.length - 1];
        IModuleType type = module.getModuleType();
        if (type == null) {
            return false;
        }
        return typeId.equals(type.getId());
    }

    private static boolean matchModuleType(IModule module, String typeId) {
        IModuleType type = module.getModuleType();
        if (type == null) {
            return false;
        }
        return typeId.equals(type.getId());
    }
}
