/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sapphire;

import static org.eclipse.payara.tools.utils.JdtUtil.validateJvm;
import static org.eclipse.payara.tools.utils.WtpUtil.findUniqueServerName;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.osgi.util.NLS;
import org.eclipse.payara.tools.Messages;
import org.eclipse.payara.tools.server.GlassFishRuntime;
import org.eclipse.payara.tools.server.GlassFishServer;
import org.eclipse.payara.tools.utils.GlassFishLocationUtils;
import org.eclipse.payara.tools.utils.JavaLocationDefaultValueService;
import org.eclipse.payara.tools.utils.JavaLocationValidationService;
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
import org.eclipse.wst.server.core.ServerCore;

public final class GlassfishServerConfigServices {
	
	public static final class UniqueRuntimeNameValidationService extends ValidationService {

		@Override
		protected Status compute() {
			final Value<?> name = context(Value.class);

			if (!name.empty()) {
				IRuntime thisRuntime = name.element().adapt(IRuntime.class);

				if (thisRuntime instanceof IRuntimeWorkingCopy) {
					thisRuntime = ((IRuntimeWorkingCopy) thisRuntime).getOriginal();
				}

				for (final IRuntime r : ServerCore.getRuntimes()) {
					if (r != thisRuntime && name.text().equals(r.getName())) {
						return Status.createErrorStatus(NLS.bind(Messages.duplicateRuntimeName, name.text()));
					}
				}
			}

			return Status.createOkStatus();
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
			final Value<?> name = context(Value.class);

			if (!name.empty()) {
				final IServerWorkingCopy thisServerWorkingCopy = name.element().adapt(IServerWorkingCopy.class);
				final IServer thisServer = thisServerWorkingCopy.getOriginal();

				for (final IServer s : ServerCore.getServers()) {
					if (s != thisServer && name.text().equals(s.getName())) {
						return Status.createErrorStatus(duplicateServerName.format(name.text()));
					}
				}
			}

			return Status.createOkStatus();
		}
	}

	public static final class DomainLocationDefaultValueService extends DefaultValueService {

		private static final String DEFAULT_DOMAINS_DIR = "domains";
		private static final String DEFAULT_DOMAIN_NAME = "domain1";

		@Override
		protected String compute() {
			IServerWorkingCopy wc = context(Value.class).element().adapt(IServerWorkingCopy.class);
			IRuntime runtime = wc.getRuntime();
			IPath serverLocation = runtime.getLocation();
			return serverLocation.append(DEFAULT_DOMAINS_DIR).append(DEFAULT_DOMAIN_NAME).toString();
		}
	}

	public static final class JdkDefaultValueService extends JavaLocationDefaultValueService {
		@Override
		protected void initDefaultValueService() {
			super.initDefaultValueService();

			// There is no need to detach the listener as the life cycle of the JDK and
			// GlassFish
			// location properties is the same.

			context(IGlassfishRuntimeModel.class).getServerRoot().attach(new FilteredListener<PropertyEvent>() {
				@Override
				protected void handleTypedEvent(final PropertyEvent event) {
					refresh();
				}
			});
		}

		@Override
		protected boolean acceptable(final IVMInstall jvm) {
			if (context(IGlassfishRuntimeModel.class).getServerRoot().validation().ok()) {
				final IRuntime r = context(Value.class).element().adapt(IRuntime.class);
				final GlassFishRuntime gf = (GlassFishRuntime) r.loadAdapter(GlassFishRuntime.class, null);
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
			// GlassFish
			// location properties is the same.

			context(IGlassfishRuntimeModel.class).getServerRoot().attach(new FilteredListener<PropertyContentEvent>() {
				@Override
				protected void handleTypedEvent(final PropertyContentEvent event) {
					refresh();
				}
			});
		}

		@Override
		protected Status validate(final File location) {
			final IRuntime r = context(Value.class).element().adapt(IRuntime.class);
			final GlassFishRuntime gf = (GlassFishRuntime) r.loadAdapter(GlassFishRuntime.class, null);
			return validateJvm(location).jdk().version(gf.getJavaVersionConstraint()).result();
		}
	}

	public static final class ServerLocationValidationService extends ValidationService {

		@Override
		protected Status compute() {
			IRuntime r = context(Value.class).element().adapt(IRuntime.class);
			GlassFishRuntime runtimeDelegate = (GlassFishRuntime) r.loadAdapter(GlassFishRuntime.class, null);
			IStatus s = runtimeDelegate.validateServerLocation();
			if (!s.isOK()) {
                return StatusBridge.create(s);
            }
			return StatusBridge.create(runtimeDelegate.validateVersion());
		}
	}

	public static final class ServerLocationListener extends Listener {
		private static final List<Path> subFoldersToSearch = ListFactory.<Path>start().add(new Path("glassfish"))
				.add(new Path("glassfish4/glassfish")).add(new Path("glassfish3/glassfish")).result();

		@Override
		public void handle(final Event event) {
			final IGlassfishRuntimeModel model = ((PropertyEvent) event).property()
					.nearest(IGlassfishRuntimeModel.class);

			Version gfVersion = null;

			final Path location = model.getServerRoot().content();

			if (location != null) {
				GlassFishLocationUtils gfInstall = GlassFishLocationUtils.find(location.toFile());

				if (gfInstall == null) {
					for (final Path sf : subFoldersToSearch) {
						final Path p = location.append(sf);
						gfInstall = GlassFishLocationUtils.find(p.toFile());

						if (gfInstall != null) {
							model.setServerRoot(p);
							break;
						}
					}
				}

				if (gfInstall != null) {
					gfVersion = gfInstall.version();
				}
			}

			model.setName(GlassFishRuntime.createDefaultRuntimeName(gfVersion));
		}
	}

	public static final class DomainLocationValidationService extends ValidationService {

		@Override
		protected Status compute() {
			IServerWorkingCopy wc = context(Value.class).element().adapt(IServerWorkingCopy.class);
			GlassFishServer serverDelegate = (GlassFishServer) wc.loadAdapter(GlassFishServer.class, null);

			return StatusBridge.create(serverDelegate.validate());
		}

	}

	public static final class DomainLocationListener extends Listener {
		@Override
		public void handle(final Event event) {
			final Property property = ((PropertyEvent) event).property();
			final IServerWorkingCopy wc = property.element().adapt(IServerWorkingCopy.class);
			final IGlassfishServerModel model = property.nearest(IGlassfishServerModel.class);

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
