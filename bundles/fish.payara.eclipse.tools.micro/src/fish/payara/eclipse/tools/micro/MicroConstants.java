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

public interface MicroConstants {

	String PLUGIN_ID = "org.eclipse.payara.tools";

	String PROJECT_NAME_ATTR = "org.eclipse.jdt.launching.PROJECT_ATTR";

	String DEFAULT_HOST = "localhost";
	int DEFAULT_DEBUG_PORT = 9889;

	String ATTR_HOST_NAME = "hostname";
	String ATTR_PORT = "port";

	String JAVA_HOME_ENV_VAR = "JAVA_HOME";

	String ATTR_CONTEXT_PATH = "contextPath";
	String ATTR_MICRO_VERSION = "microVersion";
	String ATTR_BUILD_ARTIFACT = "buildArtifact";
	String ATTR_DEBUG_PORT = "debugPort";
	String ATTR_RELOAD_ARTIFACT = "reloadArtifact";

	String WAR_BUILD_ARTIFACT = "War";
	String EXPLODED_WAR_BUILD_ARTIFACT = "Exploded War";
	String UBER_JAR_BUILD_ARTIFACT = "Uber Jar";

	String AUTO_DEPLOY_ARTIFACT = "Auto Deploy";
	String HOT_DEPLOY_ARTIFACT = "Hot Deploy";

}
