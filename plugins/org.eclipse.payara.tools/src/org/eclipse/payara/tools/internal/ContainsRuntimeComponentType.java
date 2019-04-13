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

import java.util.Collection;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.payara.tools.PayaraToolsPlugin;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentType;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ContainsRuntimeComponentType extends PropertyTester {

    private static final String PROP_CONTAINS_RUNTIME_COMPONENT_TYPE = "containsRuntimeComponentType"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object value) {
        try {
            if (!property.equals(PROP_CONTAINS_RUNTIME_COMPONENT_TYPE)) {
                throw new IllegalStateException();
            }

            String val = (String) value;
            int colon = val.indexOf(':');

            String typeid;
            String vexpr;

            if (colon == -1 || colon == val.length() - 1) {
                typeid = val;
                vexpr = null;
            } else {
                typeid = val.substring(0, colon);
                vexpr = val.substring(colon + 1);
            }

            if (!RuntimeManager.isRuntimeComponentTypeDefined(typeid)) {
                return false;
            }

            IRuntimeComponentType type = RuntimeManager.getRuntimeComponentType(typeid);

            if (receiver instanceof IRuntime) {
                for (Object component : ((IRuntime) receiver).getRuntimeComponents()) {
                    if (match((IRuntimeComponent) component, type, vexpr)) {
                        return true;
                    }
                }

                return false;
            }
            
            if (receiver instanceof Collection) {
                for (Object obj : ((Collection<?>) receiver)) {
                    if (obj instanceof IRuntimeComponent) {
                        if (match((IRuntimeComponent) obj, type, vexpr)) {
                            return true;
                        }
                    } else if (obj instanceof IRuntime) {
                        if (test(obj, property, args, value)) {
                            return true;
                        }
                    } else {
                        throw new IllegalStateException();
                    }
                }

                return false;
            }
            
            throw new IllegalStateException();
            
        } catch (CoreException e) {
            PayaraToolsPlugin.log(e);
            return false;
        }
    }

    private static final boolean match(IRuntimeComponent component, IRuntimeComponentType type, String vexpr) throws CoreException {
        if (component.getRuntimeComponentType() == type) {
            if (vexpr == null) {
                return true;
            }
            
            return type.getVersions(vexpr)
                       .contains(component.getRuntimeComponentVersion());
        }

        return false;
    }

}
