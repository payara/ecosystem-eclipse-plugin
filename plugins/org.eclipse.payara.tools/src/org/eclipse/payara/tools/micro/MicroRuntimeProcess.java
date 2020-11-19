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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.RuntimeProcess;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MicroRuntimeProcess extends RuntimeProcess {

    private ILaunch debuggerConnection;

    private static final String ERROR_MESSAGE = "Error occurred while terminating payara-micro";

    private static final Logger LOG = Logger.getLogger(MicroRuntimeProcess.class.getName());

    public MicroRuntimeProcess(ILaunch launch, Process process, String name, Map<String, String> attributes) {
        super(launch, process, name, attributes);
    }

    ILaunch getDebuggerConnection() {
        return debuggerConnection;
    }

    void setDebuggerConnection(ILaunch debuggerConnection) {
        this.debuggerConnection = debuggerConnection;
    }

    @Override
    public void terminate() throws DebugException {
        terminateInstance();
        super.terminate();
        disconnectDebuggerConnection();
    }

    private void disconnectDebuggerConnection() throws DebugException {
    	if (debuggerConnection != null 
        		&& debuggerConnection instanceof IDisconnect 
        		&& ((IDisconnect) debuggerConnection).canDisconnect()) {
            ((IDisconnect) debuggerConnection).disconnect();
        }
    }
    
    private void terminateInstance() {
        try {
            Process process = (Process) getSystemProcess();
            long pid;
            try {
                Method method = process.getClass().getDeclaredMethod("pid");
                method.setAccessible(true);
                pid = (long) method.invoke(process);
            } catch (NoSuchMethodException ex) {
                Field field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                long handleValue = field.getLong(process);
                Kernel32 kernel = Kernel32.INSTANCE;
                WinNT.HANDLE handle = new WinNT.HANDLE();
                handle.setPointer(Pointer.createConstant(handleValue));
                pid = kernel.GetProcessId(handle);
            }
            killProcess(String.valueOf(pid));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ERROR_MESSAGE, ex);
        }
    }

    private void killProcess(String processId) throws IOException, InterruptedException {
        String command;
        final Runtime re = Runtime.getRuntime();
        if (Platform.OS_WIN32.equals(Platform.getOS())) {
            command = "taskkill /F /T /PID " + processId;
        } else {
            command = "kill " + processId;
        }
        Process killProcess = re.exec(command);
        int result = killProcess.waitFor();
        if (result != 0) {
            LOG.severe(ERROR_MESSAGE);
        }
    }
}
