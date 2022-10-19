/**
 * Copyright (c) 2020-2022 Payara Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package fish.payara.eclipse.micro;

import java.io.FileNotFoundException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public abstract class BuildTool {

	protected final IProject project;

	public static final String MAVEN_NATURE = "org.eclipse.m2e.core.maven2Nature";

	protected BuildTool(IProject project) {
		this.project = project;
	}

	public abstract String getExecutableHome() throws FileNotFoundException;

	public abstract List<String> getStartCommand(String contextPath, String microVersion, String buildType,
			String debugPort, boolean hotDeploy);

	public abstract List<String> getReloadCommand(boolean hotDeploy, List<String> sourcesChanged,
			boolean metadataChanged);

	public static boolean isMavenProject(IProject project) {
		try {
			return project.hasNature(MAVEN_NATURE);
		} catch (CoreException e) {
			return false;
		}
	}

	public static BuildTool getToolSupport(IProject project) {
		if (isMavenProject(project)) {
			return new MavenBuildTool(project);
		} else {
			return new GradleBuildTool(project);
		}
	}
}
