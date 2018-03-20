/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.exceptions;

public class GlassfishLaunchException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3931653934641477601L;

	private Process gfProcess;

	public GlassfishLaunchException() {
		super();
	}

	public GlassfishLaunchException(String message, Throwable cause) {
		this(message, cause, null);
	}

	public GlassfishLaunchException(String message, Process gfProcess) {
		this(message, null, gfProcess);
	}

	public GlassfishLaunchException(String message, Throwable cause, Process gfProcess) {
		super(message, cause);
		this.gfProcess = gfProcess;
	}

	public GlassfishLaunchException(String message) {
		this(message, null, null);
	}

	public GlassfishLaunchException(Throwable cause) {
		this(null, cause, null);
	}

	public Process getStartedProcess() {
		return gfProcess;
	}

}
