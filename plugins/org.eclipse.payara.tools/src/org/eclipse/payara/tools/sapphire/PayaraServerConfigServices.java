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

package org.eclipse.payara.tools.sapphire;

import static org.eclipse.osgi.util.NLS.bind;
import static org.eclipse.payara.tools.Messages.duplicateRuntimeName;
import static org.eclipse.payara.tools.server.PayaraRuntime.createDefaultRuntimeName;
import static org.eclipse.payara.tools.utils.JdtUtil.validateJvm;
import static org.eclipse.payara.tools.utils.PayaraLocationUtils.find;
import static org.eclipse.payara.tools.utils.WtpUtil.findUniqueServerName;
import static org.eclipse.sapphire.modeling.Status.createErrorStatus;
import static org.eclipse.sapphire.modeling.Status.createOkStatus;
import static org.eclipse.wst.server.core.ServerCore.getRuntimes;
import static org.eclipse.wst.server.core.ServerCore.getServers;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.payara.tools.server.PayaraRuntime;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.payara.tools.utils.JavaLocationDefaultValueService;
import org.eclipse.payara.tools.utils.JavaLocationValidationService;
import org.eclipse.payara.tools.utils.PayaraLocationUtils;
import org.eclipse.sapphire.DefaultValueService;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.platform.StatusBridge;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;

public final class PayaraServerConfigServices {

    public static final class UniqueRuntimeNameValidationService extends ValidationService {

        @Override
        protected Status compute() {
            Value<?> name = context(Value.class);

            if (!name.empty()) {
                IRuntime thisRuntime = name.element().adapt(IRuntime.class);

                if (thisRuntime instanceof IRuntimeWorkingCopy) {
                    thisRuntime = ((IRuntimeWorkingCopy) thisRuntime).getOriginal();
                }

                for (final IRuntime runtime : getRuntimes()) {
                    if (runtime != thisRuntime && name.text().equals(runtime.getName())) {
                        return createErrorStatus(bind(duplicateRuntimeName, name.text()));
                    }
                }
            }

            return createOkStatus();
        }
    }

    public static final class UniqueServerNameValidationService extends ValidationService {
        @Text("Server name {0} is already in use")
        private static LocalizableText duplicateServerName;

        static {
            LocalizableText.init(UniqueServerNameValidationService.class);
        }

        @Override
        protected Status compute() {
            Value<?> name = context(Value.class);

            if (!name.empty()) {
                final IServerWorkingCopy thisServerWorkingCopy = name.element().adapt(IServerWorkingCopy.class);
                final IServer thisServer = thisServerWorkingCopy.getOriginal();

                for (final IServer server : getServers()) {
                    if (server != thisServer && name.text().equals(server.getName())) {
                        return createErrorStatus(duplicateServerName.format(name.text()));
                    }
                }
            }

            return createOkStatus();
        }
    }

    public static final class DomainLocationDefaultValueService extends DefaultValueService {

        private static final String DEFAULT_DOMAINS_DIR = "domains";
        private static final String DEFAULT_DOMAIN_NAME = "domain1";

        @Override
        protected String compute() {
            return context(Value.class)
                    .element()
                    .adapt(IServerWorkingCopy.class)
                    .getRuntime()
                    .getLocation()
                    .append(DEFAULT_DOMAINS_DIR)
                    .append(DEFAULT_DOMAIN_NAME)
                    .toString();
        }
    }

    public static final class JdkDefaultValueService extends JavaLocationDefaultValueService {
        @Override
        protected void initDefaultValueService() {
            super.initDefaultValueService();

            // There is no need to detach the listener as the life cycle of the JDK and
            // Payara location properties is the same.

            context(IPayaraRuntimeModel.class).getServerRoot().attach(new FilteredListener<PropertyEvent>() {
                @Override
                protected void handleTypedEvent(final PropertyEvent event) {
                    refresh();
                }
            });
        }

        @Override
        protected boolean acceptable(final IVMInstall jvm) {
            if (context(IPayaraRuntimeModel.class).getServerRoot().validation().ok()) {
                final IRuntime r = context(Value.class).element().adapt(IRuntime.class);
                final PayaraRuntime gf = (PayaraRuntime) r.loadAdapter(PayaraRuntime.class, null);
                return validateJvm(jvm).jdk().version(gf.getJavaVersionConstraint()).result().ok();
            }

            return false;
        }
    }

    public static final class JdkValidationService extends JavaLocationValidationService {
        @Override
        protected void initValidationService() {
            super.initValidationService();

            // There is no need to detach the listener as the life cycle of the JDK and
            // Payara location properties is the same.

            context(IPayaraRuntimeModel.class).getServerRoot().attach(new FilteredListener<PropertyContentEvent>() {
                @Override
                protected void handleTypedEvent(final PropertyContentEvent event) {
                    refresh();
                }
            });
        }

        @Override
        protected Status validate(final File location) {
            final IRuntime r = context(Value.class).element().adapt(IRuntime.class);
            final PayaraRuntime gf = (PayaraRuntime) r.loadAdapter(PayaraRuntime.class, null);
            return validateJvm(location).jdk().version(gf.getJavaVersionConstraint()).result();
        }
    }

    public static final class ServerLocationValidationService extends ValidationService {

        @Override
        protected Status compute() {
            IRuntime runtime = context(Value.class).element().adapt(IRuntime.class);
            PayaraRuntime runtimeDelegate = (PayaraRuntime) runtime.loadAdapter(PayaraRuntime.class, null);
            IStatus status = runtimeDelegate.validateServerLocation();
            
            if (!status.isOK()) {
                return StatusBridge.create(status);
            }
            
            return StatusBridge.create(runtimeDelegate.validateVersion());
        }
    }

    public static final class ServerLocationListener extends Listener {
        private static final List<Path> subFoldersToSearch = ListFactory.<Path>start()
                .add(new Path("glassfish"))
                .add(new Path("glassfish4/glassfish"))
                .add(new Path("glassfish3/glassfish")).result();

        @Override
        public void handle(final Event event) {
            IPayaraRuntimeModel model = ((PropertyEvent) event)
                    .property()
                    .nearest(IPayaraRuntimeModel.class);

            Version payaraVersion = null;

            Path payaraRootLocation = model.getServerRoot().content();

            if (payaraRootLocation != null) {
                PayaraLocationUtils payaraInstall = find(payaraRootLocation.toFile());

                if (payaraInstall == null) {
                    for (Path subFolder : subFoldersToSearch) {
                        Path potentialRootLocation = payaraRootLocation.append(subFolder);
                        payaraInstall = PayaraLocationUtils.find(potentialRootLocation.toFile());

                        if (payaraInstall != null) {
                            model.setServerRoot(potentialRootLocation);
                            break;
                        }
                    }
                }

                if (payaraInstall != null) {
                    payaraVersion = payaraInstall.version();
                }
            }

            model.setName(createDefaultRuntimeName(payaraVersion));
        }
    }

    public static final class DomainLocationValidationService extends ValidationService {

        @Override
        protected Status compute() {
            IServerWorkingCopy wc = context(Value.class).element().adapt(IServerWorkingCopy.class);
            PayaraServer serverDelegate = (PayaraServer) wc.loadAdapter(PayaraServer.class, null);

            return StatusBridge.create(serverDelegate.validate());
        }

    }

    public static final class DomainLocationListener extends Listener {
        @Override
        public void handle(final Event event) {
            final Property property = ((PropertyEvent) event).property();
            final IServerWorkingCopy wc = property.element().adapt(IServerWorkingCopy.class);
            final IPayaraServerModel model = property.nearest(IPayaraServerModel.class);

            String name = wc.getRuntime().getName() + " [";

            if (model.getRemote().content()) {
                name = name + model.getHostName().content();
            } else {
                name = name + model.getDomainPath().content().lastSegment();
            }

            name = name + "]";

            model.setName(findUniqueServerName(name));
        }
    }

}
