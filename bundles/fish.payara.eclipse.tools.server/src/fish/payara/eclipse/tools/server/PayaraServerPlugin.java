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

package fish.payara.eclipse.tools.server;

import static fish.payara.eclipse.tools.server.preferences.PreferenceConstants.ENABLE_LOG;
import static java.lang.Runtime.getRuntime;
import static java.nio.charset.Charset.defaultCharset;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.INFO;
import static org.eclipse.jface.resource.ImageDescriptor.createFromURL;
import static org.eclipse.wst.server.core.ServerCore.addRuntimeLifecycleListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.internal.ResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import fish.payara.eclipse.tools.server.events.RuntimeLifecycleListener;
import fish.payara.eclipse.tools.server.events.ServerLifecycleListener;

/**
 * Payara Tools Server Plugin. This is used as the OSGi bundle activator, as well as the central
 * place to do logging.
 */
public class PayaraServerPlugin extends AbstractUIPlugin {
    protected Map imageDescriptors = new HashMap();

    public static final String RUNTIME_TYPE = "payara.runtime"; //$NON-NLS-1$

    public static final String GF_SERVER_IMG = "gf-server.img";
    public static final String EAR_MODULE_IMG = "ear.img";
    public static final String EJB_MODULE_IMG = "ejb.img";
    public static final String LOG_FILE_IMG = "log-file.img";
    public static final String WEB_MODULE_IMG = "web.img";
    public static final String WEBSERVICE_IMG = "webservice.img";
    public static final String RESOURCES_IMG = "resources.img";
    public static final String GF_WIZARD = "wizard.img";

    public static final String SYMBOLIC_NAME = "fish.payara.eclipse.tools.server";
    public static final Bundle BUNDLE = Platform.getBundle(SYMBOLIC_NAME);
    private static final ILog LOG = Platform.getLog(BUNDLE);

    private static PayaraServerPlugin singleton;
    private static HashSet<String[]> commandsToExecuteAtExit = new HashSet<>();

    public PayaraServerPlugin() {
        singleton = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        addRuntimeLifecycleListener(new RuntimeLifecycleListener());
        ResourceManager.getInstance().addServerLifecycleListener(new ServerLifecycleListener());
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        reg.put(GF_SERVER_IMG, createFromURL(getBundle().getEntry("icons/obj16/payara-blue.png")));
        reg.put(EAR_MODULE_IMG, createFromURL(getBundle().getEntry("icons/obj16/ear.gif")));
        reg.put(EJB_MODULE_IMG, createFromURL(getBundle().getEntry("icons/obj16/ejb_module.gif")));
        reg.put(LOG_FILE_IMG, createFromURL(getBundle().getEntry("icons/obj16/logfile.png")));
        reg.put(WEB_MODULE_IMG, createFromURL(getBundle().getEntry("icons/obj16/web_module.gif")));
        reg.put(WEBSERVICE_IMG, createFromURL(getBundle().getEntry("icons/obj16/webservice.png")));
        reg.put(RESOURCES_IMG, createFromURL(getBundle().getEntry("icons/obj16/resources.gif")));
        reg.put(GF_WIZARD, createFromURL(getBundle().getEntry("icons/wizard75x66.png")));
	}


    	/**
	 * Return the image with the given key from the image registry.
	 * @param key java.lang.String
	 * @return org.eclipse.jface.parts.IImage
	 */
    public static Image getImage(String key) {
        return getInstance().getImageRegistry().get(key);
    }

    	/**
	 * Return the image with the given key from the image registry.
	 * @param key java.lang.String
	 * @return org.eclipse.jface.parts.IImage
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		 return getInstance().getImageRegistry().getDescriptor(key);
	}

//        /**
//	 * Register an image with the registry.
//	 * @param key java.lang.String
//	 * @param partialURL java.lang.String
//	 */
//	private void registerImage(ImageRegistry registry, String key, String partialURL) {
//		if (ICON_BASE_URL == null) {
//			String pathSuffix = "icons/";
//			ICON_BASE_URL = singleton.getBundle().getEntry(pathSuffix);
//		}
//
//		try {
//			ImageDescriptor id = ImageDescriptor.createFromURL(new URL(ICON_BASE_URL, partialURL));
//			registry.put(key, id);
//			imageDescriptors.put(key, id);
//		} catch (Exception e) {
//			Trace.trace(Trace.WARNING, "Error registering image", e);
//		}
//	}

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

    public static PayaraServerPlugin getInstance() {
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
