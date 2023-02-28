/**
 * Copyright (c) 2020-2022 Payara Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package fish.payara.eclipse.tools.micro.ui;

import static fish.payara.eclipse.tools.micro.ui.wizards.MicroProjectWizard.ARCHETYPE_MICRO_VERSIONS;
import static fish.payara.eclipse.tools.micro.MicroConstants.ATTR_BUILD_ARTIFACT;
import static fish.payara.eclipse.tools.micro.MicroConstants.ATTR_CONTEXT_PATH;
import static fish.payara.eclipse.tools.micro.MicroConstants.ATTR_DEBUG_PORT;
import static fish.payara.eclipse.tools.micro.MicroConstants.ATTR_MICRO_VERSION;
import static fish.payara.eclipse.tools.micro.MicroConstants.ATTR_RELOAD_ARTIFACT;
import static fish.payara.eclipse.tools.micro.MicroConstants.AUTO_DEPLOY_ARTIFACT;
import static fish.payara.eclipse.tools.micro.MicroConstants.DEFAULT_DEBUG_PORT;
import static fish.payara.eclipse.tools.micro.MicroConstants.EXPLODED_WAR_BUILD_ARTIFACT;
import static fish.payara.eclipse.tools.micro.MicroConstants.HOT_DEPLOY_ARTIFACT;
import static fish.payara.eclipse.tools.micro.MicroConstants.JAVA_HOME_ENV_VAR;
import static fish.payara.eclipse.tools.micro.MicroConstants.UBER_JAR_BUILD_ARTIFACT;
import static fish.payara.eclipse.tools.micro.MicroConstants.WAR_BUILD_ARTIFACT;
import static org.eclipse.core.externaltools.internal.IExternalToolConstants.ATTR_BUILD_SCOPE;
import static org.eclipse.core.externaltools.internal.IExternalToolConstants.ATTR_LOCATION;
import static org.eclipse.core.externaltools.internal.IExternalToolConstants.ATTR_TOOL_ARGUMENTS;
import static org.eclipse.core.externaltools.internal.IExternalToolConstants.ATTR_WORKING_DIRECTORY;
import static org.eclipse.debug.core.ILaunchManager.ATTR_ENVIRONMENT_VARIABLES;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.core.ui.internal.MavenImages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import fish.payara.eclipse.tools.micro.BuildTool;
import fish.payara.eclipse.tools.micro.ui.wizards.Messages;

public class MicroProjectTab extends AbstractJavaMainTab {

	private Text contextPathText, debugPortText;
	private Combo microVersionText, buildArtifactCombo, reloadArtifactCombo;

	@Override
	public void createControl(Composite parent) {
		Composite mainComposite = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
		createProjectEditor(mainComposite);

		Group group = SWTFactory.createGroup(mainComposite, Messages.contextPathComponentLabel, 1, 1,
				GridData.FILL_HORIZONTAL);
		contextPathText = SWTFactory.createSingleText(group, 1);
		contextPathText.addModifyListener(getDefaultListener());

		group = SWTFactory.createGroup(mainComposite, Messages.microVersionComponentLabel, 1, 1,
				GridData.FILL_HORIZONTAL);
                microVersionText = SWTFactory.createCombo(group, SWT.READ_ONLY, 1, ARCHETYPE_MICRO_VERSIONS);
		microVersionText.addModifyListener(getDefaultListener());

		group = SWTFactory.createGroup(mainComposite, Messages.buildArtifactComponentLabel, 1, 1,
				GridData.FILL_HORIZONTAL);
		buildArtifactCombo = SWTFactory.createCombo(group, SWT.READ_ONLY, 1, new String[] { EMPTY_STRING,
				WAR_BUILD_ARTIFACT, EXPLODED_WAR_BUILD_ARTIFACT, UBER_JAR_BUILD_ARTIFACT });
		buildArtifactCombo.addModifyListener(getDefaultListener());

		group = SWTFactory.createGroup(mainComposite, Messages.debugPortComponentLabel, 1, 1, GridData.FILL_HORIZONTAL);
		debugPortText = SWTFactory.createSingleText(group, 1);
		debugPortText.addModifyListener(getDefaultListener());

		group = SWTFactory.createGroup(mainComposite, Messages.reloadArtifactComponentLabel, 1, 1,
				GridData.FILL_HORIZONTAL);
		reloadArtifactCombo = SWTFactory.createCombo(group, SWT.READ_ONLY, 1,
				new String[] { EMPTY_STRING, AUTO_DEPLOY_ARTIFACT, HOT_DEPLOY_ARTIFACT });
		reloadArtifactCombo.addModifyListener(getDefaultListener());
		reloadArtifactCombo.setToolTipText(Messages.reloadArtifactComponentTooltip);

		setControl(mainComposite);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		updateMicroSettingsFromConfig(config);
		super.initializeFrom(config);
	}

	private void updateMicroSettingsFromConfig(ILaunchConfiguration config) {
		String contextPath = EMPTY_STRING;
		try {
			contextPath = config.getAttribute(ATTR_CONTEXT_PATH, contextPath);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		contextPathText.setText(contextPath);

		String microVersion = EMPTY_STRING;
		try {
			microVersion = config.getAttribute(ATTR_MICRO_VERSION, microVersion);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		microVersionText.setText(microVersion);

		String buildType = EMPTY_STRING;
		try {
			buildType = config.getAttribute(ATTR_BUILD_ARTIFACT, buildType);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		buildArtifactCombo.setText(buildType);

		String debugPort = String.valueOf(DEFAULT_DEBUG_PORT);
		try {
			debugPort = config.getAttribute(ATTR_DEBUG_PORT, debugPort);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		debugPortText.setText(debugPort);

		String reloadType = EMPTY_STRING;
		try {
			reloadType = config.getAttribute(ATTR_RELOAD_ARTIFACT, reloadType);
		} catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		reloadArtifactCombo.setText(reloadType);
	}

	public Image getImage() {
		return MavenImages.IMG_LAUNCH_MAIN;
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		String name = fProjText.getText().trim();
		if (name.isEmpty()) {
			setErrorMessage(LauncherMessages.JavaMainTab_missing_project);
			return false;
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validateName(name, IResource.PROJECT);
		if (!status.isOK()) {
			setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_19, new String[] { status.getMessage() }));
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		if (!project.exists()) {
			setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_20, new String[] { name }));
			return false;
		}
		if (!project.isOpen()) {
			setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_21, new String[] { name }));
			return false;
		}
		try {
			BuildTool.getToolSupport(project).getExecutableHome();
		} catch (FileNotFoundException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement javaElement = getContext();
		if (javaElement != null) {
			initializeJavaProject(javaElement, config);
		} else {
			config.setAttribute(ATTR_PROJECT_NAME, EMPTY_STRING);
		}
		config.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, "fish.payara.eclipse.tools.micro.processFactory");
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		String projectName = fProjText.getText().trim();
		try {
			if (!projectName.isEmpty()) {
				IProject project = getWorkspaceRoot().getProject(projectName);
				if (!project.exists()) {
					setErrorMessage(NLS.bind(LauncherMessages.JavaMainTab_20, new String[] { projectName }));
					return;
				}
				BuildTool buildTool = BuildTool.getToolSupport(project);
				if (buildTool == null) {
					setErrorMessage(Messages.projectBuildNotFound);
					throw new IllegalStateException(Messages.projectBuildNotFound);
				}

				String debugPort = debugPortText.getText();
				if (debugPort.isEmpty()) {
					debugPort = String.valueOf(DEFAULT_DEBUG_PORT);
				}
				config.setAttribute(ATTR_CONTEXT_PATH, contextPathText.getText());
				config.setAttribute(ATTR_MICRO_VERSION, microVersionText.getText());
				config.setAttribute(ATTR_BUILD_ARTIFACT, buildArtifactCombo.getText());
				config.setAttribute(ATTR_DEBUG_PORT, debugPort);
				config.setAttribute(ATTR_RELOAD_ARTIFACT, reloadArtifactCombo.getText());
				config.setAttribute(ATTR_PROJECT_NAME, projectName);
				config.setAttribute(ATTR_WORKING_DIRECTORY, project.getLocation().toOSString());
				config.setAttribute(ATTR_BUILD_SCOPE, "${projects:" + project.getName() + "}");
				Map<String, String> env = config.getAttribute(ATTR_ENVIRONMENT_VARIABLES, Collections.emptyMap());
				if (env.isEmpty()) {
					config.setAttribute(ATTR_ENVIRONMENT_VARIABLES, env = new HashMap<>());
				}
				if (!env.containsKey(JAVA_HOME_ENV_VAR)) {
					env.put(JAVA_HOME_ENV_VAR, getJavaHome(project));
				}
				config.setAttribute(ATTR_LOCATION, buildTool.getExecutableHome());
				boolean hotDeploy = HOT_DEPLOY_ARTIFACT.equals(reloadArtifactCombo.getText());
				List<String> startCmd = buildTool.getStartCommand(contextPathText.getText(), microVersionText.getText(),
						buildArtifactCombo.getText(), debugPort, hotDeploy);
				config.setAttribute(ATTR_TOOL_ARGUMENTS, String.join(" ", startCmd));
			}
		} catch (FileNotFoundException ex) {
			setErrorMessage(ex.getMessage());
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static String getJavaHome(IProject project) throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		IVMInstall install = JavaRuntime.getVMInstall(javaProject);
		return install.getInstallLocation().getAbsolutePath();
	}

	@Override
	public String getName() {
		return Messages.microProjectTabTitle;
	}

}
