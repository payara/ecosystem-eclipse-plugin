/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.facets;

public interface IGlassfishWebDeploymentDescriptor extends IGlassfishDeploymentDescriptor {

	static final String SUN_WEB_DEPLOYMENT_DESCRIPTOR_NAME = "sun-web.xml";

	public void setContext(String context);

	public String getContext();
}
