/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.project.facet.core.ActionConfig;
import org.eclipse.wst.common.project.facet.core.IActionConfigFactory;


public class FacetActionConfigFactory implements IActionConfigFactory{

	public Object create() throws CoreException {
		return new RealConfig();
	}	
	public static class RealConfig extends ActionConfig{		
	}	
}
