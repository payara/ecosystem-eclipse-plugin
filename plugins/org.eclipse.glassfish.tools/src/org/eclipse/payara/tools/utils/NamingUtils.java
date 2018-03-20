/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.utils;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.payara.tools.exceptions.UniqueNameNotFound;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;

/**
 * Set of utility methods for creating (unique) names.
 *
 */
public class NamingUtils {

	public static String createUniqueRuntimeName(String runtimeName) throws UniqueNameNotFound {
		IRuntime[] runtimes = ServerCore.getRuntimes();
		HashSet<String> takenNames = new HashSet<String>(runtimes.length);
		for (IRuntime runtime : runtimes) {
			takenNames.add(runtime.getName());
		}
		return createUniqueName(runtimeName, takenNames);
	}

	private static String createUniqueName(String candidadeName, Set<String> takenNames) throws UniqueNameNotFound {
		if (!takenNames.contains(candidadeName)) {
			return candidadeName;
		}

		for (int i = 2; i < Integer.MAX_VALUE; i++) {
			String candidadeNameWithSuffix = candidadeName + " (" + i + ")";
			if (!takenNames.contains(candidadeNameWithSuffix)) {
				return candidadeNameWithSuffix;
			}
		}

		throw new UniqueNameNotFound();
	}
	
}
