/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.log;

import static java.util.logging.Level.ALL;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import fish.payara.eclipse.tools.server.log.AbstractLogFilter.ILevelResolver;

class LevelResolver implements ILevelResolver {

    private Map<String, String> localizedLevels;

    LevelResolver() {
        Locale defaultLocale = null;

        try {
            Locale logLocale = getLogLocale();
            if (!logLocale.equals(Locale.getDefault())) {
                defaultLocale = Locale.getDefault();
                Locale.setDefault(logLocale);
            }

            localizedLevels = new HashMap<>();
            for (Level level : new Level[] { ALL, CONFIG, FINE, FINER, FINEST, INFO, SEVERE, WARNING }) {
                localizedLevels.put(level.getName(), level.getLocalizedName());
            }
        } finally {
            if (defaultLocale != null) {
                Locale.setDefault(defaultLocale);
            }
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

    @Override
    public String resolve(String level) {
        String localizedLevel = localizedLevels.get(level);
        return localizedLevel != null ? localizedLevel : level;
    }

}
