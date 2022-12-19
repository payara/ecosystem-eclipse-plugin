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

package fish.payara.eclipse.tools.server.sdk.admin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.admin.response.MessagePart;
import fish.payara.eclipse.tools.server.sdk.logging.Logger;

/**
 * Command runner that executes get property command.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestGetProperty extends RunnerRest {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerRestGetProperty.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Result object - contains list of JDBC resources names. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultMap<String, String> result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using REST interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestGetProperty(final PayaraServer server,
            final Command command) {
        super(server, command, "/command/", null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create <code>ResultMap</code> object corresponding to server get property command execution value
     * to be returned.
     */
    @Override
    protected ResultMap<String, String> createResult() {
        return result = new ResultMap<>();
    }

    @Override
    protected boolean processResponse() {
        final String METHOD = "processResponse";
        List<MessagePart> childMessages = report.getTopMessagePart().getChildren();
        if ((childMessages == null) || childMessages.isEmpty()) {
            return false;
        }
        result.value = new HashMap<>(childMessages.size());

        for (MessagePart msg : childMessages) {
            String message = msg.getMessage();
            int equalsIndex = message.indexOf('=');
            if (equalsIndex >= 0) {
                String keyPart = message.substring(0, equalsIndex);
                String valuePart = message.substring(equalsIndex + 1);
                try {
                    // Around Sept. 2008... 3.x servers were double encoding their
                    // responces. It appears that has stopped
                    // (See http://netbeans.org/bugzilla/show_bug.cgi?id=195015)
                    // The open question is, "When did 3.x stop doing the double
                    // encode?" since we don't know... this strategy will work
                    // for us
                    // Belt and suspenders, like
                    result.value.put(keyPart, valuePart); // raw form
                    result.value.put(keyPart, URLDecoder.decode(valuePart,
                            "UTF-8")); // single decode
                    result.value.put(keyPart, URLDecoder.decode(result.value.get(keyPart), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    LOGGER.log(Level.INFO, METHOD,
                            "unsupportedEncoding", result.value.get(keyPart));
                } catch (IllegalArgumentException iae) {
                    LOGGER.log(Level.INFO, METHOD, "illegalArgument",
                            new Object[] { valuePart, result.value.get(keyPart) });
                }
            } else {
                LOGGER.log(Level.INFO, METHOD, "emptyString", message);
                result.value.put(message, "");
            }
        }
        return true;
    }

    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
        OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
        CommandGetProperty gpCommand = (CommandGetProperty) command;
        StringBuilder data = new StringBuilder();
        data.append("pattern=").append(gpCommand.propertyPattern);
        wr.write(data.toString());
        wr.flush();
        wr.close();
    }

}
