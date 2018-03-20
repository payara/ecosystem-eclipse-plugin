/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.logging.Level;

import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class RunnerHttpGetProperty extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerHttpGetProperty.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returned value is map where key-value pairs returned by server are stored.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultMap<String, String> result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpGetProperty(final GlassFishServer server,
            final Command command) {
        super(server, command,
                "pattern=" + ((CommandGetProperty) command).propertyPattern);
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
        if (manifest == null) {
            LOGGER.log(Level.WARNING, METHOD, "manifestNull", query);
            return false;
        }
        result.value = new HashMap<>();
        for (String encodedkey : manifest.getEntries().keySet()) {
            String key = "";
            try {
                if (null != encodedkey) {
                    key = encodedkey;
                    key = URLDecoder.decode(encodedkey, "UTF-8");
                }
            } catch (UnsupportedEncodingException uee) {
                LOGGER.log(Level.INFO, METHOD,
                        "unsupportedEncoding", encodedkey);
                LOGGER.log(Level.INFO, METHOD, "exceptionDetails", uee);
            } catch (IllegalArgumentException iae) {
                // Ignore this for now
            }
            int equalsIndex = key.indexOf('=');
            if (equalsIndex >= 0) {
                String keyPart = key.substring(0, equalsIndex);
                String valuePart = key.substring(equalsIndex + 1);
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
                LOGGER.log(Level.WARNING, METHOD, "emptyString", key);
                result.value.put(key, "");
            }
        }

        return true;
    }
}
