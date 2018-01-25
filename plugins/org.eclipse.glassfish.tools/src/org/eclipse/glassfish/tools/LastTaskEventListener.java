/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools;

import org.eclipse.glassfish.tools.sdk.TaskEvent;
import org.eclipse.glassfish.tools.sdk.TaskState;
import org.eclipse.glassfish.tools.sdk.TaskStateListener;

/**
 * This listener stores the last task event that occurred
 * during command execution. It's return value is well defined only
 * after corresponding Future.get method returned. It can
 * also return null if no event was observed or execution timed
 * out.
 * Do not put the same instance into two exec calls that run
 * concurrently, it is not thread safe.
 * 
 * @author Peter Benedikovic
 *
 */
public class LastTaskEventListener implements TaskStateListener {

	private TaskEvent lastEvent = null;
	
	@Override
	public void operationStateChanged(TaskState newState, TaskEvent event,
			String... args) {
		lastEvent = event;
	}
	
	public TaskEvent getLastEvent() {
		return lastEvent;
	}

}
