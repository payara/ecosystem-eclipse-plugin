/**
 * Copyright (c) 2020 Payara Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.payara.tools.micro;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.externaltools.internal.launchConfigurations.ProgramLaunchDelegate;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import static org.eclipse.core.runtime.IStatus.ERROR;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP;
import static org.eclipse.payara.tools.micro.MicroConstants.DEFAULT_DEBUG_PORT;
import static org.eclipse.payara.tools.micro.MicroConstants.DEFAULT_HOST;
import static org.eclipse.payara.tools.micro.MicroConstants.PLUGIN_ID;
import static org.eclipse.payara.tools.micro.MicroConstants.ATTR_HOST_NAME;
import static org.eclipse.payara.tools.micro.MicroConstants.ATTR_PORT;
import static org.eclipse.payara.tools.micro.MicroConstants.ATTR_DEBUG_PORT;

public class MicroLaunchDelegate extends ProgramLaunchDelegate {

    private static final byte[] JWDP_HANDSHAKE = "JDWP-Handshake".getBytes(StandardCharsets.US_ASCII);
    private static final int MAX_WAIT_FOR_CONNECTION = 90 * 1000;
    private static final String DEBUG_MODE = "debug";

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        super.launch(configuration, mode, launch, monitor);
        IProcess tool = launch.getProcesses()[0];
        if (DEBUG_MODE.equals(mode) && tool instanceof MicroRuntimeProcess) {
            ((MicroRuntimeProcess) tool).setDebuggerConnection(createDebugConfiguration(configuration, monitor));
        }
    }

    private ILaunch createDebugConfiguration(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
        String debugPort = configuration.getAttribute(ATTR_DEBUG_PORT, String.valueOf(DEFAULT_DEBUG_PORT));
    	waitForDebuggerConnection(debugPort, monitor);
        String projectName = configuration.getAttribute(ATTR_PROJECT_NAME, (String) null);
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        String name = "Payara Micro " + project.getName();
        ILaunchConfigurationWorkingCopy config = DebugPlugin.getDefault()
                .getLaunchManager()
                .getLaunchConfigurationType(ID_REMOTE_JAVA_APPLICATION)
                .newInstance(null, name);
        config.setAttribute(ATTR_PROJECT_NAME, project.getName());
        config.setAttribute(ATTR_ALLOW_TERMINATE, false);
        config.setAttribute(ATTR_VM_CONNECTOR, ID_SOCKET_ATTACH_VM_CONNECTOR);
        Map<String, String> connectAttrs = new HashMap<>();
        connectAttrs.put(ATTR_HOST_NAME, DEFAULT_HOST);
        connectAttrs.put(ATTR_PORT, debugPort);
        config.setAttribute(ATTR_CONNECT_MAP, connectAttrs);
        return config.launch(DEBUG_MODE, monitor);
    }

    private void waitForDebuggerConnection(String debugPort, IProgressMonitor monitor) throws CoreException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < MAX_WAIT_FOR_CONNECTION && !monitor.isCanceled()) {
            try (Socket socket = new Socket(DEFAULT_HOST, Integer.valueOf(debugPort))) {
                socket.getOutputStream().write(JWDP_HANDSHAKE);
                return;
            } catch (ConnectException ex) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e1) {
                    throw new CoreException(new Status(ERROR, PLUGIN_ID, ex.getMessage()));
                }
            } catch (IOException ex) {
                throw new CoreException(new Status(ERROR, PLUGIN_ID, ex.getMessage()));
            }
        }
        throw new CoreException(new Status(ERROR, PLUGIN_ID, "Unable to connect to the JVM"));
    }
}
