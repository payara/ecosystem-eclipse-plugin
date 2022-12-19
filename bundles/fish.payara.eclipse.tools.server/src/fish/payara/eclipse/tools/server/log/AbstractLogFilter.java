/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.log;

import fish.payara.eclipse.tools.server.PayaraServer;

/**
 * Common base class for log filters. They are used to transform standard GF log records to more
 * human readable format.
 *
 * @author Peter Benedikovic
 */
public abstract class AbstractLogFilter implements ILogFilter {

    private static final String DEFAULT_DELIMETER = "";

    protected StringBuilder buffer;

    protected String logRecordDelimeter = "";

    protected ILevelResolver levelResolver;

    protected LogRecord record;

    protected ILogFormatter formatter;

    protected AbstractLogFilter() {
        this(new LogFormatterSimple(), new LevelResolver(), DEFAULT_DELIMETER);
    }

    protected AbstractLogFilter(ILogFormatter formatter, ILevelResolver resolver) {
        this(formatter, resolver, DEFAULT_DELIMETER);
    }

    protected AbstractLogFilter(ILogFormatter formatter, ILevelResolver resolver,
            String logRecordDelimeter) {
        buffer = new StringBuilder(1024); // 1 kB
        record = new LogRecord();

        this.formatter = formatter;
        this.levelResolver = resolver;
        this.logRecordDelimeter = logRecordDelimeter;
    }

    /**
     * Resets log filter after reading complete log record.
     */
    @Override
    public void reset() {
        record.reset();
        buffer.setLength(0);
    }

    protected abstract boolean isReadingUserMessage();

    /**
     * Processes read line.
     *
     * @param line - mustn't contain new line character
     * @return Complete log record or null if the read line haven't completed the log record.
     */
    @Override
    public abstract String process(String line);

    public static AbstractLogFilter createFilter(PayaraServer server) {
        if (server.getVersion().matches("[4")) {
            return new PatternLogFilterV4();
        } else {
            return new StateLogFilterV3();
        }
    }

    public static interface ILevelResolver {

        public String resolve(String level);

    }

    public static interface ILogFormatter {

        public String formatLogRecord(LogRecord record);

    }

    public enum GlassfishLogFields {

        DATETIME, LEVEL, VERSION, CLASSINFO, THREADINFO, MESSAGE;
    }

}
