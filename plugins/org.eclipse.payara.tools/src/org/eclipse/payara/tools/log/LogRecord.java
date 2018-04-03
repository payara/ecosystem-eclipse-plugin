/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.log;

import org.eclipse.payara.tools.log.AbstractLogFilter.GlassfishLogFields;

class LogRecord {

    private String time;
    private String level;
    private String version;
    private String classinfo;
    private String threadinfo;
    private String message;

    LogRecord() {
        time = level = version = classinfo = threadinfo = message = "";
    }

    public void reset() {
        time = level = version = classinfo = threadinfo = message = "";
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getClassInfo() {
        return classinfo;
    }

    public void setClassInfo(String classinfo) {
        this.classinfo = classinfo;
    }

    public String getThreadInfo() {
        return threadinfo;
    }

    public void setThreadInfo(String threadinfo) {
        this.threadinfo = threadinfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRecordFieldValue(GlassfishLogFields field) {
        switch (field) {
        case CLASSINFO:
            return getClassInfo();
        case VERSION:
            return getVersion();
        case THREADINFO:
            return getThreadInfo();
        case DATETIME:
            return getTime();
        case MESSAGE:
            return getMessage();
        case LEVEL:
            return getLevel();

        default:
            throw new IllegalArgumentException();
        }
    }

    public String[] getRecordFieldValues(GlassfishLogFields... fields) {
        String[] res = new String[fields.length];
        int i = 0;
        for (GlassfishLogFields f : fields) {
            res[i++] = getRecordFieldValue(f);
        }
        return res;
    }
}
