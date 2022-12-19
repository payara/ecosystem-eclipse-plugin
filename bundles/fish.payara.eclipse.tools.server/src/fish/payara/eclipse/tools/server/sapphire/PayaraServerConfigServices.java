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

package fish.payara.eclipse.tools.server.sapphire;

import static fish.payara.eclipse.tools.server.Messages.duplicateRuntimeName;
import static org.eclipse.osgi.util.NLS.bind;
//import static org.eclipse.sapphire.modeling.Status.createErrorStatus;
//import static org.eclipse.sapphire.modeling.Status.createOkStatus;
import static org.eclipse.wst.server.core.ServerCore.getRuntimes;


import org.eclipse.core.runtime.IStatus;
//import org.eclipse.sapphire.DefaultValueService;
//import org.eclipse.sapphire.Event;
//import org.eclipse.sapphire.Listener;
//import org.eclipse.sapphire.Value;
//import org.eclipse.sapphire.modeling.Status;
//import org.eclipse.sapphire.platform.StatusBridge;
//import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerWorkingCopy;

import fish.payara.eclipse.tools.server.PayaraRuntime;
import fish.payara.eclipse.tools.server.PayaraServer;

public final class PayaraServerConfigServices {



//    public static final class JdkDefaultValueService extends JavaLocationDefaultValueService {
//        @Override
//        protected void initDefaultValueService() {
//            super.initDefaultValueService();
//
//            // There is no need to detach the listener as the life cycle of the JDK and
//            // Payara location properties is the same.
//
//            context(IPayaraRuntimeModel.class).getServerRoot().attach(new FilteredListener<PropertyEvent>() {
//                @Override
//                protected void handleTypedEvent(final PropertyEvent event) {
//                    refresh();
//                }
//            });
//        }
//
//        @Override
//        protected boolean acceptable(final IVMInstall jvm) {
//            if (context(IPayaraRuntimeModel.class).getServerRoot().validation().ok()) {
//                final IRuntime r = context(Value.class).element().adapt(IRuntime.class);
//                final PayaraRuntime gf = (PayaraRuntime) r.loadAdapter(PayaraRuntime.class, null);
//                return validateJvm(jvm).jdk().version(gf.getJavaVersionConstraint()).result().ok();
//            }
//
//            return false;
//        }
    
//    import static fish.payara.eclipse.tools.server.utils.JdtUtil.newer;
//
//import org.eclipse.jdt.launching.AbstractVMInstall;
//import org.eclipse.jdt.launching.IVMInstall;
//import org.eclipse.jdt.launching.IVMInstallChangedListener;
//import org.eclipse.jdt.launching.IVMInstallType;
//import org.eclipse.jdt.launching.JavaRuntime;
//import org.eclipse.jdt.launching.PropertyChangeEvent;
//import org.eclipse.sapphire.DefaultValueService;
//    protected synchronized String compute() {
//        this.computing = true;
//
//        try {
//            IVMInstall jvm = null;
//
//            for (IVMInstallType vmInstallType : JavaRuntime.getVMInstallTypes()) {
//                for (IVMInstall vmInstall : vmInstallType.getVMInstalls()) {
//                    if (!internal(vmInstall) && acceptable(vmInstall)) {
//                        jvm = newer(jvm, vmInstall);
//                    }
//                }
//            }
//
//            return (jvm == null ? null : jvm.getInstallLocation().getAbsolutePath());
//        } finally {
//            this.computing = false;
//        }
//    }
//

//    }
//
//    public static final class JdkValidationService extends JavaLocationValidationService {
//    protected Status compute() {
//        final String location = context(Value.class).text();
//
//        if (location != null) {
//            final File locationFile = new File(location);
//
//            if (locationFile.exists() && locationFile.isDirectory()) {
//                return validate(locationFile);
//            }
//        }
//
//        return Status.createOkStatus();
//    }
//        protected void initValidationService() {
//            super.initValidationService();
//
//            // There is no need to detach the listener as the life cycle of the JDK and
//            // Payara location properties is the same.
//
//            context(IPayaraRuntimeModel.class).getServerRoot().attach(new FilteredListener<PropertyContentEvent>() {
//                @Override
//                protected void handleTypedEvent(final PropertyContentEvent event) {
//                    refresh();
//                }
//            });
//        }
//
//        @Override
//        protected Status validate(final File location) {
//            final IRuntime r = context(Value.class).element().adapt(IRuntime.class);
//            final PayaraRuntime gf = (PayaraRuntime) r.loadAdapter(PayaraRuntime.class, null);
//            return validateJvm(location).jdk().version(gf.getJavaVersionConstraint()).result();
//        }
//    }
//    public static final class ServerLocationListener extends Listener {
//        private static final List<Path> subFoldersToSearch = ListFactory.<Path>start()
//                .add(new Path("glassfish"))
//                .add(new Path("glassfish4/glassfish"))
//                .add(new Path("glassfish3/glassfish")).result();
//
//        @Override
//        public void handle(final Event event) {
//            IPayaraRuntimeModel model = ((PropertyEvent) event)
//                    .property()
//                    .nearest(IPayaraRuntimeModel.class);
//
//            Version payaraVersion = null;
//
//            Path payaraRootLocation = model.getServerRoot().content();
//
//            if (payaraRootLocation != null) {
//                PayaraLocationUtils payaraInstall = find(payaraRootLocation.toFile());
//
//                if (payaraInstall == null) {
//                    for (Path subFolder : subFoldersToSearch) {
//                        Path potentialRootLocation = payaraRootLocation.append(subFolder);
//                        payaraInstall = PayaraLocationUtils.find(potentialRootLocation.toFile());
//
//                        if (payaraInstall != null) {
//                            model.setServerRoot(potentialRootLocation);
//                            break;
//                        }
//                    }
//                }
//
//                if (payaraInstall != null) {
//                    payaraVersion = payaraInstall.version();
//                }
//            }
//
//            model.setName(createDefaultRuntimeName(payaraVersion));
//        }
//    }


}
