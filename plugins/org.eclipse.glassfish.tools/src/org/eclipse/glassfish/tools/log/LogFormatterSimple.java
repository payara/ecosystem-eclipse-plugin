/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.log;

import java.util.Formatter;

import org.eclipse.glassfish.tools.log.AbstractLogFilter.GlassfishLogFields;
import org.eclipse.glassfish.tools.log.AbstractLogFilter.ILogFormatter;

public class LogFormatterSimple implements ILogFormatter {

	private GlassfishLogFields[] fields;
	private String format;
	StringBuilder s = new StringBuilder(1024);
	
	public LogFormatterSimple() {
		format = "%s|%s: %s";
		fields = new GlassfishLogFields[] {GlassfishLogFields.DATETIME, GlassfishLogFields.LEVEL, GlassfishLogFields.MESSAGE};
	}
	
	public LogFormatterSimple(String delimeter, GlassfishLogFields[] fields) {
		this.fields = fields;
	}
	 
	@Override
	public String formatLogRecord(LogRecord record) {
		s.setLength(0);
		Formatter f = new Formatter(s);
		f.format(format, record.getRecordFieldValues(fields));
		f.close();
		return s.toString();
	}

}
