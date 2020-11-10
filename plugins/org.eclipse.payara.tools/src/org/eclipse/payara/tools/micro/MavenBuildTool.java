/**
 * Copyright (c) 2020 Payara Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.payara.tools.micro;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import static org.eclipse.payara.tools.micro.MicroConstants.DEFAULT_DEBUG_PORT;

public class MavenBuildTool extends BuildTool {

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
            throw new FileNotFoundException("Maven home path not found.");
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
    public String getStartCommand(String contextPath, String microVersion, String debugPort) {
        StringBuilder sb = new StringBuilder();
        sb.append("package payara-micro:start");
        if (contextPath != null && !contextPath.trim().isEmpty()) {
            sb.append(" -DcontextRoot=").append(contextPath);
        }
        if (microVersion != null && !microVersion.trim().isEmpty()) {
            sb.append(" -DpayaraVersion=").append(microVersion);
        }
        sb.append(" -Ddebug=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=")
        .append(debugPort);
        return sb.toString();
    }
}
