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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class StateLogFilterV3 extends AbstractLogFilter {

    private int state;

    StateLogFilterV3() {
        super();
    }

    @Override
    public String process(String line) {
        if (line.equals(logRecordDelimeter) && !isReadingUserMessage()) {
            reset();
        }

        String result = null;
        for (char c : line.toCharArray()) {
            result = process(c);
        }
        process('\n');
        return result;// process('\n');
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
    public String process(char c) {
        String result = null;

        switch (state) {
        case 0:
            if (c == '[') {
                state = 1;
            } else {
                if (c == '\n') {
                    if (buffer.length() > 0) {
                        buffer.append(c);
                        result = buffer.toString();
                        buffer.setLength(0);
                    }
                } else if (c != '\r') {
                    buffer.append(c);
                }
            }
            break;
        case 1:
            if (c == '#') {
                state = 2;
            } else {
                state = 0;
                if (c == '\n') {
                    if (buffer.length() > 0) {
                        buffer.append(c);
                        result = buffer.toString();
                        buffer.setLength(0);
                    }
                } else if (c != '\r') {
                    buffer.append('[');
                    buffer.append(c);
                }
            }
            break;
        case 2:
            if (c == '|') {
                state = 3;
                buffer.setLength(0);
            } else {
                if (c == '\n') {
                    if (buffer.length() > 0) {
                        buffer.append(c);
                        result = buffer.toString();
                        buffer.setLength(0);
                    }
                } else if (c != '\r') {
                    state = 0;
                    buffer.append('[');
                    buffer.append('#');
                    buffer.append(c);
                }
            }
            break;
        case 3:
            if (c == '|') {
                state = 4;
                record.setTime(buffer.toString());
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
            break;
        case 4:
            if (c == '|') {
                state = 5;
                record.setLevel(levelResolver.resolve(buffer.toString()));
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
            break;
        case 5:
            if (c == '|') {
                state = 6;
                record.setVersion(buffer.toString());
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
            break;
        case 6:
            if (c == '|') {
                state = 7;
                record.setClassInfo(buffer.toString());
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
            break;
        case 7:
            if (c == '|') {
                state = 8;
                record.setThreadInfo(buffer.toString());
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
            break;
        case 8: // reading message
            if (c == '|') {
                state = 9;
                record.setMessage(buffer.toString());
            } else if (c == '\n') {
                if (buffer.length() > 0) { // suppress blank lines in multiline messages
                    buffer.append('\n');
                    // result = !multiline ? type + ": " + buffer.toString() : buffer.toString();
                    // multiline = true;
                    // buffer.setLength(0);
                }
            } else if (c != '\r') {
                buffer.append(c);
            }
            break;
        case 9:
            if (c == '#') {
                state = 10;
            } else {
                state = 8;
                buffer.append('|');
                buffer.append(c);
            }
            break;
        case 10:
            if (c == ']') {
                state = 0;
                result = formatter.formatLogRecord(record);// (multiline ? message : type + ": " + message) + '\n';
                reset();
            } else {
                state = 8;
                buffer.append('|');
                buffer.append('#');
                buffer.append(c);
            }
            break;
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        final InputStream stream = new FileInputStream("src/oracle/eclipse/tools/glassfish/log/logv4.txt");

        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            PatternLogFilterV4 filter = new PatternLogFilterV4();
            // StateLogFilterV3 filter = new StateLogFilterV3();

            for (String line = null; (line = reader.readLine()) != null;) {
                line = filter.process(line);
                if (line != null) {
                    // output.println("line:");
                    System.out.println(line);
                }
            }

            reader.close();
        } finally {
            try {
                stream.close();
            } catch (final IOException e) {
            }
        }
    }

    @Override
    protected boolean isReadingUserMessage() {
        return state == 8;
    }

}
