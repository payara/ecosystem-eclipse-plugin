/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.server.starting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.glassfish.tools.sdk.data.StartupArgs;

public class StartupArgsImpl implements StartupArgs {

	private String javaHome;
	private ArrayList<String> javaArgs;
	private ArrayList<String> glassfishArgs;

	public StartupArgsImpl() {

	}

	public void setJavaHome(String javaHome) {
		this.javaHome = javaHome;
	}

	/**
	 * Adds java arguments contained in <code>javaArgsString</code>. The parameter
	 * is parsed - the delimeter is defined as one or more whitespace characters
	 * followed by <code>-</code>.
	 * 
	 * @param javaArgsString
	 */
	public void addJavaArgs(String javaArgsString) {
		String[] args = javaArgsString.split("\\s+(?=-)");
		if (javaArgs == null) {
			javaArgs = new ArrayList<String>(args.length);
		}
		Collections.addAll(javaArgs, args);
	}

	/**
	 * Adds single argument for bootstrap jar. No processing is done.
	 * 
	 * @param glassfishArgsString
	 */
	public void addGlassfishArgs(String glassfishArgsString) {
		if (glassfishArgs == null) {
			glassfishArgs = new ArrayList<String>();
		}
		glassfishArgs.add(glassfishArgsString);
	}

	@Override
	public List<String> getGlassfishArgs() {
		return glassfishArgs;
	}

	@Override
	public List<String> getJavaArgs() {
		return javaArgs;
	}

	@Override
	public Map<String, String> getEnvironmentVars() {
		return Collections.emptyMap();
	}

	@Override
	public String getJavaHome() {
		return javaHome;
	}

}
