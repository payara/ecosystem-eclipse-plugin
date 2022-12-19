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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.admin.response.ResponseContentType;
import fish.payara.eclipse.tools.server.sdk.logging.Logger;

/**
 * GlassFish Server <code>view-log</code> Administration Command Execution using HTTP interface.
 * <p/>
 * Class implements GlassFish server administration functionality trough HTTP interface.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestFetchLogData extends RunnerRest {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Extracts query string from passed View Log command entity. <p.>
     *
     * @param command View Log command entity.
     * @return Query string for given command.
     */
    private static String query(Command command) {
        if (command instanceof CommandFetchLogData) {
            return ((CommandFetchLogData) command).paramsAppendNext;
        } else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * GlassFish log lines.
     * <p/>
     * <code>List&ltString&gt lines</code> instance is internal server response holder. Instance life
     * cycle is started in <code>readResponse</code> method where log returned from server is read and
     * stored internally.
     */
    private List<String> lines;

    /**
     * Content of HTTP header <code>X-Text-Append-Next</code>.
     * <p/>
     * This header contains the entire URL to pass to the GET method to return the changes since the
     * last call. You can use this header in client applications to get all log entries that were added
     * in particular interval.
     */
    private URL headerAppendNext;

    /**
     * GlassFish administration command result containing server log.
     * <p/>
     * Result instance life cycle is started with submitting task into <code>ExecutorService</code>'s
     * queue. method <code>call()</code> is responsible for correct <code>TaskState</code> and
     * receiveResult value handling.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultLog result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    // TODO: Make this "/management/domain/" command v3 only after
    // NetBeans 7.3 release.
    /**
     * Constructs an instance of administration command executor using HTTP interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestFetchLogData(final PayaraServer server,
            final Command command) {
        super(server, command, "/management/domain/", query(command));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create <code>ResultLog</code> object corresponding to server log command execution value to be
     * returned.
     */
    @Override
    protected Result createResult() {
        return result = new ResultLog();
    }

    @Override
    public boolean getDoOutput() {
        return false;
    }

    @Override
    protected String getRequestMethod() {
        return "GET";
    }

    /**
     * Inform whether this runner implementation accepts gzip format.
     * <p/>
     *
     * @return <code>true</code> when gzip format is accepted, <code>false</code> otherwise.
     */
    @Override
    public boolean acceptsGzip() {
        return false;
    }

    /**
     * Reads response from server and stores it into internal objects.
     * <p/>
     * Retrieved lines of server log are stored in internal <code>lines</code> <code>List</code>.
     * Content of HTTP header <code>X-Text-Append-Next</code> is stored in internal
     * <code>headerAppendNext</code> variable.
     * <p/>
     * It's not necessary close the stream parameter when finished. Caller will take care of that. But
     * this method uses additional stream handlers for <code>gzip</code> compression and buffered
     * reading so it should close them.
     * <p/>
     *
     * @param in Stream to read data from.
     * @return <code>true</code> if response <code>X-Text-Append-Next</code> HTTP header contains some
     * parameters (e.g. ?start=&lt;number&gt;) or <code>false</code> otherwise.
     * @throws CommandException in case of stream error.
     */
    @Override
    public boolean readResponse(InputStream in, HttpURLConnection hconn) {
        lines = new LinkedList<>();
        String ce = hconn.getContentEncoding();
        BufferedReader br = null;
        String line = null;
        try {
            InputStream cooked = null != ce && ce.contains("gzip")
                    ? new GZIPInputStream(in)
                    : in;
            br = new BufferedReader(new java.io.InputStreamReader(cooked));
            while ((line = br.readLine()) != null) {
                if (line != null) {
                    lines.add(line);
                }
            }
        } catch (IOException ioe) {
            throw new CommandException(CommandException.HTTP_RESP_IO_EXCEPTION,
                    ioe);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                Logger.log(Level.WARNING, ioe.getLocalizedMessage(), ioe);
            }
        }

        try {
            headerAppendNext = new URL(hconn.getHeaderField("X-Text-Append-Next"));
        } catch (MalformedURLException mue) {
            Logger.log(Level.WARNING, mue.getLocalizedMessage(), mue);
            headerAppendNext = null;
            return false;
        }
        String queryAppendNext = headerAppendNext.getQuery();
        return queryAppendNext != null;
    }

    /**
     * Extracts result value from internal <code>Manifest</code> object. Value of <i>message</i>
     * attribute in <code>Manifest</code> object is stored as <i>value</i> into
     * <code>ResultString</code> result object.
     * <p/>
     *
     * @return true if result was extracted correctly. <code>null</code> <i>message</i>value is
     * considered as failure.
     */
    @Override
    protected boolean processResponse() {
        // Make ArrayList copy of stored lines. ArrayList allows better access
        // to log values.
        List logLines = new ArrayList(lines.size());
        for (String line : lines) {
            logLines.add(line);
        }
        result.value = new ValueLog(logLines, headerAppendNext.getQuery());
        return true;
    }

    @Override
    protected ResponseContentType getResponseType() {
        return ResponseContentType.TEXT_PLAIN;
    }

}
