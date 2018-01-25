/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.handlers;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.wst.server.core.IServer;

public class UpdateCenterHandler extends AbstractGlassfishSelectionHandler {

	@Override
	public void processSelection(IStructuredSelection selection) {
		// only one server should be selected
		IServer server = (IServer) selection.getFirstElement();
		GlassFishServer serverAdapter = (GlassFishServer) server
				.loadAdapter(GlassFishServer.class, null);
		File installRoot = new File(
				serverAdapter.getServerInstallationDirectory()).getParentFile();
		if (!isUCInstalled(installRoot)) {
			showMessageDialog("GlassFish Update Tool is not yet installed. Please read the Eclipse console output and there, type 'y' to start the installation...");

		}
		File tool = getV3UpdateCenterLauncher(installRoot);
		// UC launcher was not found - tell it to the user
		if (tool == null) {
			showMessageDialog("Selected Glassfish server installation does not contain update center launcher.");
			return;
		}
		try {

			ILaunchManager manager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType programType = manager
					.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);

			ILaunchConfiguration cfg = programType.newInstance(null,
					"updatecentertool");
			ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
			wc.setAttribute(IExternalToolConstants.ATTR_LOCATION,
					tool.getAbsolutePath());
			wc.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY,
					installRoot.getAbsolutePath());
			wc.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "");
			cfg = wc.doSave();
			// ILaunch il = cfg.launch(ILaunchManager.RUN_MODE, null, false,
			// true);
			DebugUITools.launch(cfg, ILaunchManager.RUN_MODE);

			cfg.delete();
		} catch (CoreException e) {
			// TODO: log error
			e.printStackTrace();
		}
	}

	private boolean isUCInstalled(File installRoot) {
		return new File(installRoot, "updatetool/bin").exists();
	}

	/**
	 * Locate update center launcher within the glassfish installation
	 * [installRoot]/updatecenter/bin/updatetool[.BAT]
	 * 
	 * @param asInstallRoot
	 *            appserver install location
	 * @return File reference to launcher, or null if not found.
	 */
	private File getV3UpdateCenterLauncher(File installRoot) {
		File result = null;
		if (installRoot != null && installRoot.exists()) {
			File updateCenterBin = new File(installRoot, "bin"); // NOI18N
			if (updateCenterBin.exists()) {
				if (File.separator.equals("\\")) {
					File launcherPath = new File(updateCenterBin,
							"updatetool.exe"); // NOI18N
					if (launcherPath.exists()) {
						result = launcherPath;
					} else {
						launcherPath = new File(updateCenterBin,
								"updatetool.bat"); // NOI18N
						result = (launcherPath.exists()) ? launcherPath : null;
					}
				} else {
					File launcherPath = new File(updateCenterBin, "updatetool"); // NOI18N
					result = (launcherPath.exists()) ? launcherPath : null;
					if (result != null) {
						result.setExecutable(true);
					}
				}
			}
		}
		return result;
	}

}
