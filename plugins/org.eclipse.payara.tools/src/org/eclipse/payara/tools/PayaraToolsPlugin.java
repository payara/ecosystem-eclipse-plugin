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

package org.eclipse.payara.tools;

import static java.lang.Runtime.getRuntime;
import static java.nio.charset.Charset.defaultCharset;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.INFO;
import static org.eclipse.payara.tools.preferences.PreferenceConstants.ENABLE_LOG;
import static org.eclipse.wst.server.core.ServerCore.addRuntimeLifecycleListener;
import static org.eclipse.wst.server.core.ServerCore.addServerLifecycleListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.payara.tools.server.events.RuntimeLifecycleListener;
import org.eclipse.payara.tools.server.events.ServerLifecycleListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.core.IRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Payara Tools Server Plugin. This is used as the OSGi bundle activator, as well as the central
 * place to do logging.
 */
public class PayaraToolsPlugin extends AbstractUIPlugin {

    public static final String RUNTIME_TYPE = "payara.runtime"; //$NON-NLS-1$

    public static final String GF_SERVER_IMG = "gf-server.img";

    public static final String SYMBOLIC_NAME = "org.eclipse.payara.tools";
    public static final Bundle BUNDLE = Platform.getBundle(SYMBOLIC_NAME);
    private static final ILog LOG = Platform.getLog(BUNDLE);

    private static PayaraToolsPlugin singleton;
    private static HashSet<String[]> commandsToExecuteAtExit = new HashSet<>();

    public PayaraToolsPlugin() {
        singleton = this;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        addRuntimeLifecycleListener(new RuntimeLifecycleListener());
        addServerLifecycleListener(new ServerLifecycleListener());
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        reg.put(GF_SERVER_IMG, ImageDescriptor.createFromURL(getBundle().getEntry("icons/obj16/payara-blue.png")));
    }

    @Override
    public void stop(BundleContext v) throws Exception {
        logMessage("STOP IS CALLED!!!!!!!!!!!!!!!!");

        for (String[] command : commandsToExecuteAtExit) {
            try {
                logMessage(">>> " + command[0]);
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        getRuntime().exec(command).getInputStream(), defaultCharset()));
                
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

    public static PayaraToolsPlugin getInstance() {
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

    public static void logMessage(String message) {
        if (getInstance().getPreferenceStore().getBoolean(ENABLE_LOG)) {
            log(new Status(INFO, SYMBOLIC_NAME, 1, "Payara: " + message, null));
        }
    }
    
    public static void logError(String message, Exception e) {
        log(createErrorStatus(message, e));
    }
    
    public static void logError(String message) {
        logError(message, null);
    }

    public static void log(Exception e) {
        log(createErrorStatus(e));
    }
    
    public static IStatus createErrorStatus(String message) {
        return createErrorStatus(message, null);
    }

    public static IStatus createErrorStatus(Exception e) {
        return createErrorStatus(null, e);
    }

    public static IStatus createErrorStatus(String message, Exception e) {
        return new Status(ERROR, SYMBOLIC_NAME, 0, message == null ? e.getMessage() : message, e);
    }
    
    public static void log(final IStatus status) {
        LOG.log(status);
    }

    public static boolean is31OrAbove(IRuntime runtime) {
        return runtime.getRuntimeType().getId().equals(RUNTIME_TYPE);
    }

}
