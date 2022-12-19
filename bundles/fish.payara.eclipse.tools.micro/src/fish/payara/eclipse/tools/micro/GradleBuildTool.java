/**
 * Copyright (c) 2020-2022 Payara Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package fish.payara.eclipse.tools.micro;

import static fish.payara.eclipse.tools.micro.MicroConstants.EXPLODED_WAR_BUILD_ARTIFACT;
import static fish.payara.eclipse.tools.micro.MicroConstants.UBER_JAR_BUILD_ARTIFACT;
import static fish.payara.eclipse.tools.micro.MicroConstants.WAR_BUILD_ARTIFACT;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;

public class GradleBuildTool extends BuildTool {

	public GradleBuildTool(IProject project) {
		super(project);
	}

	@Override
	public String getExecutableHome() throws FileNotFoundException {
		String gradleHome = System.getenv("GRADLE_HOME");
		if (gradleHome == null) {
			throw new FileNotFoundException("set GRADLE_HOME the environment variable to gradle installation folder");
		}

		boolean gradleHomeEndsWithPathSep = gradleHome.charAt(gradleHome.length() - 1) == File.separatorChar;
		String gradleExecStr = null;
		String executor = gradleHome;
		if (!gradleHomeEndsWithPathSep) {
			executor += File.separatorChar;
		}
		executor += "bin" + File.separatorChar + "gradle";
		if (Platform.OS_WIN32.contentEquals(Platform.getOS())) {
			if (Paths.get(executor + ".bat").toFile().exists()) {
				gradleExecStr = executor + ".bat";
			} else if (Paths.get(executor + ".cmd").toFile().exists()) {
				gradleExecStr = executor + ".cmd";
			} else {
				throw new FileNotFoundException(String.format("Gradle executable %s.cmd not found.", executor));
			}
		} else if (Paths.get(executor).toFile().exists()) {
			gradleExecStr = executor;
		}
		// Gradle executable should exist.
		if (gradleExecStr == null || !Paths.get(gradleExecStr).toFile().exists()) {
			throw new FileNotFoundException(String.format("Gradle executable [%s] not found", gradleExecStr));
		}
		return gradleExecStr;
	}

	@Override
	public List<String> getStartCommand(String contextPath, String microVersion, String buildType, String debugPort,
			boolean hotDeploy) {

		List<String> commands = new ArrayList<>();
		if (WAR_BUILD_ARTIFACT.equals(buildType)) {
			commands.add("war");
			commands.add("-DpayaraMicro.deployWar=true");
		} else if (EXPLODED_WAR_BUILD_ARTIFACT.equals(buildType)) {
			commands.add("warExplode");
			commands.add("-DpayaraMicro.deployWar=true");
			commands.add("-DpayaraMicro.exploded=true");
		} else if (UBER_JAR_BUILD_ARTIFACT.equals(buildType)) {
			commands.add("microBundle");
			commands.add("-DpayaraMicro.useUberJar=true");
		} else {
			commands.add("build");
		}
		commands.add("microStart");
		if (contextPath != null && !contextPath.trim().isEmpty()) {
			commands.add("-DpayaraMicro.contextRoot=" + contextPath);
		}
		if (microVersion != null && !microVersion.trim().isEmpty()) {
			commands.add("-DpayaraMicro.payaraVersion=" + microVersion);
		}
		if (hotDeploy) {
			commands.add("-DpayaraMicro.hotDeploy=true");
		}
		commands.add("-Ddebug=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + debugPort);
		return commands;
	}

	public List<String> getReloadCommand(boolean hotDeploy, List<String> sourcesChanged, boolean metadataChanged) {

		List<String> commands = new ArrayList<>();
		commands.add("warExplode");
		commands.add("microReload");

		if (hotDeploy) {
			commands.add("-DpayaraMicro.hotDeploy=true");
			if (metadataChanged) {
				commands.add("-DpayaraMicro.metadataChanged=true");
			}
			if (!sourcesChanged.isEmpty()) {
				commands.add("-DpayaraMicro.sourcesChanged=" + String.join(",", sourcesChanged));
			}
		}
		return commands;
	}

}
