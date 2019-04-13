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

package org.eclipse.payara.tools.internal;

import static java.util.Collections.unmodifiableList;
import static org.eclipse.payara.tools.PayaraToolsPlugin.SYMBOLIC_NAME;
import static org.eclipse.payara.tools.utils.PluginUtil.findExtensions;
import static org.eclipse.payara.tools.utils.PluginUtil.findRequiredAttribute;
import static org.eclipse.payara.tools.utils.PluginUtil.getTopLevelElements;
import static org.eclipse.payara.tools.utils.PluginUtil.instantiate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.payara.tools.PayaraToolsPlugin;
import org.eclipse.payara.tools.utils.PluginUtil.InvalidExtensionException;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.server.core.IRuntime;

/**
 * Contains the logic for processing the <code>runtimeComponentProviders</code> extension point.
 *
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */
public final class RuntimeComponentProvidersExtensionPoint {
    public static final String EXTENSION_POINT_ID = "runtimeComponentProviders";
    private static final String EL_RUNTIME_COMPONENT_PROVIDER = "runtime-component-provider";
    private static final String ATTR_CLASS = "class";

    private static List<RuntimeComponentProvider> providers;

    public static List<IRuntimeComponent> getRuntimeComponents(IRuntime runtime) {
        List<IRuntimeComponent> components = new ArrayList<>();

        for (RuntimeComponentProvider provider : getProviders()) {
            try {
                List<IRuntimeComponent> runtimeComponents = provider.getRuntimeComponents(runtime);

                if (runtimeComponents != null) {
                    components.addAll(runtimeComponents);
                }
            } catch (final Exception e) {
                PayaraToolsPlugin.log(e);
            }
        }

        return components;
    }

    private static synchronized List<RuntimeComponentProvider> getProviders() {
        if (providers == null) {
            List<RuntimeComponentProvider> modifiableProviders = new ArrayList<>();

            for (ProviderDef providerDef : readExtensions()) {
                RuntimeComponentProvider provider = instantiate(providerDef.pluginId, providerDef.className, RuntimeComponentProvider.class);

                if (provider != null) {
                    modifiableProviders.add(provider);
                }
            }

            providers = unmodifiableList(modifiableProviders);
        }

        return providers;
    }

    private static List<ProviderDef> readExtensions() {
        List<ProviderDef> providers = new ArrayList<>();

        for (IConfigurationElement element : getTopLevelElements(findExtensions(SYMBOLIC_NAME, EXTENSION_POINT_ID))) {
            
            String pluginId = element.getContributor().getName();

            if (element.getName().equals(EL_RUNTIME_COMPONENT_PROVIDER)) {
                try {
                    providers.add(new ProviderDef(pluginId, findRequiredAttribute(element, ATTR_CLASS)));
                } catch (final InvalidExtensionException e) {
                    // Continue. The problem has been reported to the user via the log.
                }
            }
        }

        return providers;
    }

    private static final class ProviderDef {
        public final String pluginId;
        public final String className;

        public ProviderDef(String pluginId, String className) {
            this.pluginId = pluginId;
            this.className = className;
        }
    }

}
