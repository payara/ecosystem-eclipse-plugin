/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.payara.tools.preferences.PreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.core.IRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Payara Tools Server Plugin. This is used as the OSGi bundle activator, as well as the central
 * place to do logging.
 */
public class GlassfishToolsPlugin extends AbstractUIPlugin {

    public static final String RUNTIME_TYPE = "payara.runtime"; //$NON-NLS-1$

    public static final String GF_SERVER_IMG = "gf-server.img";

    public static final String SYMBOLIC_NAME = "org.eclipse.payara.tools";
    public static final Bundle BUNDLE = Platform.getBundle(SYMBOLIC_NAME);
    private static final ILog LOG = Platform.getLog(BUNDLE);

    private static GlassfishToolsPlugin singleton;
    private static HashSet<String[]> commandsToExecuteAtExit = new HashSet<>();

    public GlassfishToolsPlugin() {
        singleton = this;
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        reg.put(GF_SERVER_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/glassfishserver.gif")));
    }

    @Override
    public void stop(BundleContext v) throws Exception {
        logMessage("STOP IS CALLED!!!!!!!!!!!!!!!!");

        for (String[] command : commandsToExecuteAtExit) {
            try {
                logMessage(">>> " + command[0]);
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        Runtime.getRuntime().exec(command).getInputStream(), Charset.defaultCharset()));
                String line = null;
                while ((line = input.readLine()) != null) {
                    logMessage(">>> " + line);
                }
                input.close();
            } catch (Exception ex) {
                logMessage("Error executing process:\n" + ex);
            }
        }

        super.stop(v);
    }

    public static GlassfishToolsPlugin getInstance() {
        return singleton;
    }

    public void addCommandToExecuteAtExit(String command[]) {
        for (String[] com : commandsToExecuteAtExit) {
            if (Arrays.equals(com, command)) {
                logMessage("Command already there");

                return;
            }
        }
        commandsToExecuteAtExit.add(command);
        logMessage("addCommandToExecuteAtExit size=" + commandsToExecuteAtExit.size());
    }

    public static void logError(final String message) {
        logError(message, null);
    }

    public static void logError(final String message, final Exception e) {
        log(createErrorStatus(message, e));
    }

    public static void log(final Exception e) {
        log(createErrorStatus(e));
    }

    public static void log(final IStatus status) {
        LOG.log(status);
    }

    public static IStatus createErrorStatus(final String message) {
        return createErrorStatus(message, null);
    }

    public static IStatus createErrorStatus(final Exception e) {
        return createErrorStatus(null, e);
    }

    public static IStatus createErrorStatus(final String message, final Exception e) {
        final String msg = (message == null ? e.getMessage() + "" : message);
        return new Status(IStatus.ERROR, SYMBOLIC_NAME, 0, msg, e);
    }

    public static void logMessage(String mess) {

        IPreferenceStore store = getInstance().getPreferenceStore();
        boolean trace = store.getBoolean(PreferenceConstants.ENABLE_LOG);
        if (trace) {
            Status status = new Status(IStatus.INFO, SYMBOLIC_NAME, 1, "GlassFish: " + mess, null);
            log(status);
        }
    }

    public static boolean is31OrAbove(IRuntime runtime) {
        return runtime.getRuntimeType().getId().equals(RUNTIME_TYPE);
    }

}
