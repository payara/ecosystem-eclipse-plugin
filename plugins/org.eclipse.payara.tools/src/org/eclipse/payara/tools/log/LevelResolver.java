/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.eclipse.payara.tools.log.AbstractLogFilter.ILevelResolver;

class LevelResolver implements ILevelResolver {

    private Map<String, String> localizedLevels;

    LevelResolver() {
        Locale logLocale = getLogLocale();
        String logBundle = getLogBundle();
        localizedLevels = new HashMap<>();
        for (Level l : new Level[] { Level.ALL, Level.CONFIG, Level.FINE,
                Level.FINER, Level.FINEST, Level.INFO, Level.SEVERE, Level.WARNING }) {
            String name = l.getName();
            localizedLevels.put(name, getLocalized(name, logBundle, logLocale));
        }
    }

    private Locale getLogLocale() {
        // XXX detect and use server language/country/variant instead of IDE's.
        String language = System.getProperty("user.language");
        if (language != null) {
            return new Locale(language, System.getProperty("user.country", ""), System.getProperty("user.variant", ""));
        }
        return Locale.getDefault();
    }

    private String getLogBundle() {
        return Level.INFO.getResourceBundleName();
    }

    private String getLocalized(String text, String logBundleName, Locale logLocale) {
        ResourceBundle bundle = ResourceBundle.getBundle(logBundleName, logLocale);
        String localized = bundle.getString(text);
        return localized;
    }

    @Override
    public String resolve(String level) {
        String localizedLevel = localizedLevels.get(level);
        return localizedLevel != null ? localizedLevel : level;
    }

}
