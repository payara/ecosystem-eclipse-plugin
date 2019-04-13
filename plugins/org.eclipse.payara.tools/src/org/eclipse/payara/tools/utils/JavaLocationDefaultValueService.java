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

package org.eclipse.payara.tools.utils;

import static org.eclipse.payara.tools.utils.JdtUtil.newer;

import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.eclipse.sapphire.DefaultValueService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class JavaLocationDefaultValueService extends DefaultValueService {
    private IVMInstallChangedListener listener;
    private boolean computing;

    @Override

    protected void initDefaultValueService() {
        this.listener = new IVMInstallChangedListener() {
            @Override

            public void vmRemoved(final IVMInstall vm) {
                update();
            }

            @Override

            public void vmChanged(final PropertyChangeEvent event) {
                update();
            }

            @Override

            public void vmAdded(final IVMInstall vm) {
                update();
            }

            @Override

            public void defaultVMInstallChanged(final IVMInstall previous, final IVMInstall current) {
                // Not relevant
            }
        };

        JavaRuntime.addVMInstallChangedListener(this.listener);
    }

    private synchronized void update() {
        if (!this.computing) {
            new Thread() {
                @Override
                public void run() {
                    refresh();
                }
            }.start();
        }
    }

    @Override

    protected synchronized String compute() {
        this.computing = true;

        try {
            IVMInstall jvm = null;

            for (IVMInstallType vmInstallType : JavaRuntime.getVMInstallTypes()) {
                for (IVMInstall vmInstall : vmInstallType.getVMInstalls()) {
                    if (!internal(vmInstall) && acceptable(vmInstall)) {
                        jvm = newer(jvm, vmInstall);
                    }
                }
            }

            return (jvm == null ? null : jvm.getInstallLocation().getAbsolutePath());
        } finally {
            this.computing = false;
        }
    }

    private static boolean internal(final IVMInstall jvm) {
        if (jvm instanceof AbstractVMInstall) {
            final String internal = ((AbstractVMInstall) jvm).getAttribute("internal");
            return "true".equals(internal);
        }

        return false;
    }

    protected abstract boolean acceptable(IVMInstall vminstall);

    @Override
    public void dispose() {
        super.dispose();

        if (this.listener != null) {
            JavaRuntime.removeVMInstallChangedListener(this.listener);
            this.listener = null;
        }
    }

}
