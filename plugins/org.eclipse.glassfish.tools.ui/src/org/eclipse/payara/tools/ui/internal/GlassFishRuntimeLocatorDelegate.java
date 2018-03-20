/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.internal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.payara.tools.GlassfishToolsPlugin;
import org.eclipse.payara.tools.sapphire.IGlassfishRuntimeModel;
import org.eclipse.payara.tools.server.GlassFishRuntime;
import org.eclipse.payara.tools.utils.GlassFishLocationUtils;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.swt.SapphireDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ResourceManager;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.model.RuntimeLocatorDelegate;

@SuppressWarnings("restriction")
public final class GlassFishRuntimeLocatorDelegate extends RuntimeLocatorDelegate {

	private static final IRuntimeType RUNTIME_TYPE = ServerCore.findRuntimeType("payara.runtime");

	@Override
	public void searchForRuntimes(IPath path, final IRuntimeSearchListener listener, IProgressMonitor monitor) {
		search(path.toFile(), listener, monitor);
	}

	private void search(final File f, final IRuntimeSearchListener listener, final IProgressMonitor monitor) {
		if (monitor.isCanceled() || !f.isDirectory() || f.isHidden()) {
			return;
		}

		try {
			IRuntime rt = create(f);
			if (rt != null) {
				IRuntimeWorkingCopy wc = rt.createWorkingCopy();
				listener.runtimeFound(wc);
				return;
			}
		} catch (final CoreException e) {
			GlassfishToolsPlugin.log(e);
			return;
		}

		final File[] children = f.listFiles();

		if (children != null) {
			for (final File child : children) {
				search(child, listener, monitor);
			}
		}
	}

	private static IRuntime create(final File gfhome) throws CoreException {
		GlassFishLocationUtils install = GlassFishLocationUtils.find(gfhome);
		if (install == null)
			return null;

		if (findRuntime(gfhome) != null) {
			return null;
		}

		final String name = GlassFishRuntime.createDefaultRuntimeName(install.version());

		final IRuntimeWorkingCopy created = RUNTIME_TYPE.createRuntime(name, null);
		created.setLocation(new Path(gfhome.getAbsolutePath()));
		created.setName(name);

		final RuntimeWorkingCopy rwc = (RuntimeWorkingCopy) created;

		final Map<String, String> props = new HashMap<String, String>();
		props.put("sunappserver.rootdirectory", rwc.getLocation().toPortableString());
		rwc.setAttribute("generic_server_instance_properties", props);

		final GlassFishRuntime gf = (GlassFishRuntime) rwc.loadAdapter(GlassFishRuntime.class, null);
		final IGlassfishRuntimeModel gfmodel = gf.getModel();
		final Value<org.eclipse.sapphire.modeling.Path> javaRuntimeEnvironmentProperty = gfmodel
				.getJavaRuntimeEnvironment();

		if (javaRuntimeEnvironmentProperty.content() == null) {
			final Display display = Display.getDefault();

			display.syncExec(new Runnable() {
				public void run() {
					new SapphireDialog(display.getActiveShell(), gfmodel,
							DefinitionLoader.sdef(GlassFishRuntimeLocatorDelegate.class).dialog()).open();
				}
			});

			if (javaRuntimeEnvironmentProperty.content() == null) {
				rwc.dispose();
				return null;
			} else {
				// Force JVM definition to be created

				gf.getVMInstall();

				// Clear the explicit JVM location as the DefaultValueService will now pick it
				// up

				javaRuntimeEnvironmentProperty.clear();
			}
		}

		final IStatus validationResult = created.validate(null);

		if (validationResult.getSeverity() != IStatus.ERROR) {
			created.save(true, null);
			return created.getOriginal();
		}

		return null;
	}

	private static IRuntime findRuntime(final File location) {
		for (final IRuntime runtime : ResourceManager.getInstance().getRuntimes()) {
			if (RUNTIME_TYPE == runtime.getRuntimeType() && location.equals(runtime.getLocation().toFile())) {
				return runtime;
			}
		}

		return null;
	}

}
