/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.eclipse.wst.common.project.facet.ui.IRuntimeComponentLabelProvider;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin
 *         Komissarchik</a>
 */

public final class GlassFishRuntimeComponentLabelProviderFactory implements IAdapterFactory {
	private static final Class<?>[] ADAPTER_TYPES = { IRuntimeComponentLabelProvider.class };

	public <T> T getAdapter(Object adaptable, Class<T> adapterType) {
		return adapterType.cast(new GlassFishRuntimeComponentLabelProvider((IRuntimeComponent) adaptable));
	}

	public Class<?>[] getAdapterList() {
		return ADAPTER_TYPES;
	}

}