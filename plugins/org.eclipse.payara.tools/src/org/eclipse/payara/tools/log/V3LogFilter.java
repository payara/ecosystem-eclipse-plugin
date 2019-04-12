/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.log;

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
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * author: Peter Williams
 */
public class V3LogFilter {

    private final Locale logLocale = getLogLocale();
    private final String logBundleName = getLogBundle();
    private final Map<String, String> localizedLevels = getLevelMap();

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

    private String getLocalized(String text) {
        ResourceBundle bundle = ResourceBundle.getBundle(logBundleName, logLocale);
        String localized = bundle.getString(text);
        return localized;
    }

    public Map<String, String> getLevelMap() {
        Map<String, String> levelMap = new HashMap<>();
        for (Level l : new Level[] { ALL, CONFIG, FINE, FINER, FINEST, INFO, SEVERE, WARNING }) {
            String name = l.getName();
            levelMap.put(name, getLocalized(name));
        }
        return levelMap;
    }

    public String getLocalizedLevel(String level) {
        String localizedLevel = localizedLevels.get(level);
        return localizedLevel != null ? localizedLevel : level;
    }

    public static interface Filter {

        public String process(char c);

    }

    public static final class LogFileFilter implements Filter {
        protected String message;

        protected int state;
        protected StringBuilder msg;

        private String time;
        private String type;
        private String version;
        private String classinfo;
        private String threadinfo;
        private boolean multiline;
        private final Map<String, String> typeMap;

        public LogFileFilter(Map<String, String> typeMap) {
            state = 0;
            msg = new StringBuilder(128);
            this.typeMap = typeMap;
            reset();
        }

        protected void reset() {
            message = "";
            time = "";
            type = "";
            version = "";
            classinfo = "";
            threadinfo = "";
            multiline = false;
        }

        private String getLocalizedType(String type) {
            String localizedType = typeMap.get(type);
            return localizedType != null ? localizedType : type;
        }

        /**
         * GlassFish server log entry format (unformatted), when read from file:
         *
         * [#| 2008-07-20T16:59:11.738-0700| INFO| GlassFish10.0| org.jvnet.hk2.osgiadapter|
         * _ThreadID=11;_ThreadName=Thread-6;org.glassfish.admin.config-api [1794];| Started bundle
         * org.glassfish.admin.config-api [1794] |#]
         *
         * !PW FIXME This parser should be checked for I18N stability.
         */
        @Override
        public String process(char c) {
            String result = null;

            switch (state) {
            case 0:
                if (c == '[') {
                    state = 1;
                } else {
                    if (c == '\n') {
                        if (msg.length() > 0) {
                            msg.append(c);
                            result = msg.toString();
                            msg.setLength(0);
                        }
                    } else if (c != '\r') {
                        msg.append(c);
                    }
                }
                break;
            case 1:
                if (c == '#') {
                    state = 2;
                } else {
                    state = 0;
                    if (c == '\n') {
                        if (msg.length() > 0) {
                            msg.append(c);
                            result = msg.toString();
                            msg.setLength(0);
                        }
                    } else if (c != '\r') {
                        msg.append('[');
                        msg.append(c);
                    }
                }
                break;
            case 2:
                if (c == '|') {
                    state = 3;
                    msg.setLength(0);
                } else {
                    if (c == '\n') {
                        if (msg.length() > 0) {
                            msg.append(c);
                            result = msg.toString();
                            msg.setLength(0);
                        }
                    } else if (c != '\r') {
                        state = 0;
                        msg.append('[');
                        msg.append('#');
                        msg.append(c);
                    }
                }
                break;
            case 3:
                if (c == '|') {
                    state = 4;
                    time = msg.toString();
                    msg.setLength(0);
                } else {
                    msg.append(c);
                }
                break;
            case 4:
                if (c == '|') {
                    state = 5;
                    type = getLocalizedType(msg.toString());
                    msg.setLength(0);
                } else {
                    msg.append(c);
                }
                break;
            case 5:
                if (c == '|') {
                    state = 6;
                    version = msg.toString();
                    msg.setLength(0);
                } else {
                    msg.append(c);
                }
                break;
            case 6:
                if (c == '|') {
                    state = 7;
                    classinfo = msg.toString();
                    msg.setLength(0);
                } else {
                    msg.append(c);
                }
                break;
            case 7:
                if (c == '|') {
                    state = 8;
                    threadinfo = msg.toString();
                    msg.setLength(0);
                } else {
                    msg.append(c);
                }
                break;
            case 8:
                if (c == '|') {
                    state = 9;
                    message = msg.toString();
                } else if (c == '\n') {
                    if (msg.length() > 0) { // suppress blank lines in multiline messages
                        msg.append('\n');
                        result = !multiline ? type + ": " + msg.toString() : msg.toString();
                        multiline = true;
                        msg.setLength(0);
                    }
                } else if (c != '\r') {
                    msg.append(c);
                }
                break;
            case 9:
                if (c == '#') {
                    state = 10;
                } else {
                    state = 8;
                    msg.append('|');
                    msg.append(c);
                }
                break;
            case 10:
                if (c == ']') {
                    state = 0;
                    msg.setLength(0);
                    result = (multiline ? message : type + ": " + message) + '\n';
                    reset();
                } else {
                    state = 8;
                    msg.append('|');
                    msg.append('#');
                    msg.append(c);
                }
                break;
            }
            return result;
        }
    }

}
