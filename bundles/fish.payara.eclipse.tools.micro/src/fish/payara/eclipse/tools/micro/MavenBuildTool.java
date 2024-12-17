/**
 * Copyright (c) 2020-2024 Payara Foundation
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

public class MavenBuildTool extends BuildTool {

	public static String START_COMMAND = "dev";
	private static final String PLUGIN = " fish.payara.maven.plugins:payara-micro-maven-plugin:";

	public MavenBuildTool(IProject project) {
		super(project);
	}

	@Override
	public String getExecutableHome() throws FileNotFoundException {
		String mavenHome = System.getenv("M2_HOME");
		if (mavenHome == null) {
			mavenHome = System.getenv("MAVEN_HOME");
		}

		if (mavenHome == null) {
			throw new FileNotFoundException("set MAVEN_HOME the environment variable to maven installation folder");
		}

		boolean mavenHomeEndsWithPathSep = mavenHome.charAt(mavenHome.length() - 1) == File.separatorChar;
		String mavenExecStr = null;
		String executor = mavenHome;
		if (!mavenHomeEndsWithPathSep) {
			executor += File.separatorChar;
		}
		executor += "bin" + File.separatorChar + "mvn";
		if (Platform.OS_WIN32.contentEquals(Platform.getOS())) {
			if (Paths.get(executor + ".bat").toFile().exists()) {
				mavenExecStr = executor + ".bat";
			} else if (Paths.get(executor + ".cmd").toFile().exists()) {
				mavenExecStr = executor + ".cmd";
			} else {
				throw new FileNotFoundException(String.format("Maven executable %s.cmd not found.", executor));
			}
		} else if (Paths.get(executor).toFile().exists()) {
			mavenExecStr = executor;
		}
		// Maven executable should exist.
		if (mavenExecStr == null || !Paths.get(mavenExecStr).toFile().exists()) {
			throw new FileNotFoundException(String.format("Maven executable [%s] not found", mavenExecStr));
		}
		return mavenExecStr;
	}

	@Override
	public List<String> getStartCommand(String contextPath, String microVersion, String buildType, String debugPort,
			boolean hotDeploy) {

		List<String> commands = new ArrayList<>();
		if (WAR_BUILD_ARTIFACT.equals(buildType)) {
			commands.add("resources:resources");
			commands.add("compiler:compile");
			commands.add("war:war");
			commands.add("-DdeployWar=true");
		} else if (EXPLODED_WAR_BUILD_ARTIFACT.equals(buildType)) {
			commands.add("resources:resources");
			commands.add("compiler:compile");
			commands.add("war:exploded");
			commands.add("-DdeployWar=true");
			commands.add("-Dexploded=true");
		} else if (UBER_JAR_BUILD_ARTIFACT.equals(buildType)) {
			commands.add("package");
			commands.add(PLUGIN + "bundle");
			commands.add("-DuseUberJar=true");
		} else {
			commands.add("package");
		}
		commands.add(PLUGIN + START_COMMAND);
		if (contextPath != null && !contextPath.trim().isEmpty()) {
			commands.add("-DcontextRoot=" + contextPath);
		}
		if (microVersion != null && !microVersion.trim().isEmpty()) {
			commands.add("-DpayaraVersion=" + microVersion);
		}
		if (hotDeploy) {
			commands.add("-DhotDeploy=true");
		}
		commands.add("-Ddebug=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + debugPort);
		return commands;
	}

	public List<String> getReloadCommand(boolean hotDeploy, List<String> sourcesChanged, boolean metadataChanged) {

		List<String> commands = new ArrayList<>();
		commands.add("resources:resources");
		commands.add("compiler:compile");
		commands.add("war:exploded");
		commands.add("payara-micro:reload");

		if (hotDeploy) {
			commands.add("-DhotDeploy=true");
			if (metadataChanged) {
				commands.add("-DmetadataChanged=true");
			}
			if (!sourcesChanged.isEmpty()) {
				commands.add("-DsourcesChanged=" + String.join(",", sourcesChanged));
			}
		}
		return commands;
	}

	public static void setStartCommand(String cmd) {
		START_COMMAND = cmd;
	}
}
