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

import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager.createRuntimeComponent;
import static org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager.getRuntimeComponentType;
import static org.eclipse.wst.server.core.ServerCore.getRuntimes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.common.project.facet.core.StandardJreRuntimeComponent;
import org.eclipse.payara.tools.server.PayaraRuntime;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.util.SetFactory;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeBridge;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentVersion;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Runtime;

@SuppressWarnings("restriction")
public final class PayaraRuntimeBridge implements IRuntimeBridge {

    @Override
    public Set<String> getExportedRuntimeNames() throws CoreException {
        final SetFactory<String> namesSetFactory = SetFactory.start();

        for (IRuntime runtime : getRuntimes()) {
            IRuntimeType type = runtime.getRuntimeType();

            if (type != null && "payara.runtime".equals(type.getId())) {
                namesSetFactory.add(runtime.getId());
            }
        }

        return namesSetFactory.result();
    }

    @Override
    public IStub bridge(final String name) throws CoreException {
        if (name == null) {
            throw new IllegalArgumentException();
        }

        return new Stub(name);
    }

    private static class Stub extends IRuntimeBridge.Stub {
        private String id;

        public Stub(String id) {
            this.id = id;
        }

        @Override
        public List<IRuntimeComponent> getRuntimeComponents() {
            List<IRuntimeComponent> components = new ArrayList<>(2);
            final IRuntime runtime = findRuntime(this.id);

            if (runtime == null) {
                return components;
            }

            final PayaraRuntime payaraRuntime = (PayaraRuntime) runtime.loadAdapter(PayaraRuntime.class, new NullProgressMonitor());

            if (payaraRuntime != null) {
                final Version payaraVersion = payaraRuntime.getVersion();

                if (payaraVersion != null) {
                    String payaraMainVersion;
                    if(payaraVersion.matches("[6")) {
                        payaraMainVersion = "6";
                    } else if(payaraVersion.matches("[5")) {
                        payaraMainVersion = "5";
                    } else if(payaraVersion.matches("[4")) {
                        payaraMainVersion = "4";
                    } else {
                        payaraMainVersion = "3.1";
                    }
                    IRuntimeComponentVersion payaraComponentVersion = getRuntimeComponentType("payara.runtime").getVersion(payaraMainVersion);

                    Map<String, String> properties = new HashMap<>(5);
                    if (runtime.getLocation() != null) {
                        properties.put("location", runtime.getLocation().toPortableString());
                    } else {
                        properties.put("location", "");
                    }

                    properties.put("name", runtime.getName());
                    properties.put("id", runtime.getId());
                    if (runtime.getRuntimeType() != null) {
                        properties.put("type", runtime.getRuntimeType().getName());
                        properties.put("type-id", runtime.getRuntimeType().getId());
                    }

                    components.add(createRuntimeComponent(payaraComponentVersion, properties));

                    // Java Runtime Environment

                    components.add(StandardJreRuntimeComponent.create(payaraRuntime.getVMInstall()));

                    // Other

                    components.addAll(RuntimeComponentProvidersExtensionPoint.getRuntimeComponents(runtime));
                }
            }

            return components;
        }

        @Override
        public Map<String, String> getProperties() {
            Map<String, String> properties = new HashMap<>();
            IRuntime runtime = findRuntime(id);
            if (runtime != null) {
                properties.put("id", runtime.getId());
                properties.put("localized-name", runtime.getName());
                String s = ((Runtime) runtime).getAttribute("alternate-names", (String) null);
                if (s != null) {
                    properties.put("alternate-names", s);
                }
            }
            
            return properties;
        }

        @Override
        public IStatus validate(final IProgressMonitor monitor) {
            final IRuntime runtime = findRuntime(this.id);
            if (runtime != null) {
                return runtime.validate(monitor);
            }
            return OK_STATUS;
        }

        private static final IRuntime findRuntime(final String id) {
            IRuntime[] runtimes = ServerCore.getRuntimes();
            int size = runtimes.length;

            for (int i = 0; i < size; i++) {
                if (runtimes[i].getId().equals(id)) {
                    return runtimes[i];
                }
                if (runtimes[i].getName().equals(id)) {
                    return runtimes[i];
                }

            }
            
            return null;
        }
    }

}