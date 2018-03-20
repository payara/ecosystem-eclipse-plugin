/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.payara.tools.GlassfishToolsPlugin;

/**
 * Class used to initialize default preference values.
 * 
 * @author  Ludovic Champenois
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
    public void initializeDefaultPreferences() {
		IPreferenceStore store = GlassfishToolsPlugin.getInstance().getPreferenceStore();
		store.setDefault(PreferenceConstants.ENABLE_LOG, false);
		store.setDefault(PreferenceConstants.ENABLE_START_VERBOSE, false);
		store.setDefault(PreferenceConstants.ENABLE_COLORS_CONSOLE, true);
	}

}
