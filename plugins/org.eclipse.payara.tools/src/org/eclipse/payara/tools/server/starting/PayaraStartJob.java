/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.payara.tools.server.starting;

import static org.eclipse.payara.tools.PayaraToolsPlugin.logMessage;
import static org.eclipse.payara.tools.log.PayaraConsoleManager.getStandardConsole;
import static org.eclipse.payara.tools.log.PayaraConsoleManager.getStartupProcessConsole;
import static org.eclipse.payara.tools.log.PayaraConsoleManager.showConsole;
import static org.eclipse.payara.tools.sdk.server.ServerTasks.getDebugPort;
import static org.eclipse.payara.tools.sdk.server.ServerTasks.startServer;
import static org.eclipse.payara.tools.sdk.server.ServerTasks.StartMode.DEBUG;
import static org.eclipse.payara.tools.server.starting.PayaraServerLaunchDelegate.WORK_STEP;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.payara.tools.exceptions.PayaraLaunchException;
import org.eclipse.payara.tools.log.IPayaraConsole;
import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.sdk.admin.ResultProcess;
import org.eclipse.payara.tools.sdk.server.FetchLogSimple;
import org.eclipse.payara.tools.sdk.server.ServerTasks.StartMode;
import org.eclipse.payara.tools.server.deploying.PayaraServerBehaviour;

public class PayaraStartJob implements Callable<ResultProcess> {

    private PayaraServerBehaviour payaraServerBehaviour;
    private StartupArgsImpl args;
    private StartMode mode;
    private ILaunchConfiguration configuration;
    private ILaunch launch;
    private IProgressMonitor monitor;

    public PayaraStartJob(PayaraServerBehaviour payaraServerBehaviour, StartupArgsImpl args, StartMode mode, ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) {
        super();
        this.payaraServerBehaviour = payaraServerBehaviour;
        this.args = args;
        this.mode = mode;
        this.configuration = configuration;
        this.launch = launch;
        this.monitor = monitor;
    }

    @Override
    public ResultProcess call() throws Exception {
        
        boolean earlyAttach = payaraServerBehaviour.getPayaraServerDelegate().getAttachDebuggerEarly();
        
        // Create the process that starts the server
        ResultProcess process = startPayara(earlyAttach);

        Process payaraProcess = process.getValue().getProcess();
        
        // Read process std output to prevent process'es blocking
        IPayaraConsole startupConsole = startLogging(payaraProcess);
        
        IPayaraConsole filelogConsole = getStandardConsole(payaraServerBehaviour.getPayaraServerDelegate());

        synchronized (payaraServerBehaviour) {
            
            boolean attached = false;
            boolean hasLogged = false;
            boolean hasLoggedPayara = false;
            
            // Query the process status in a loop
            
            check_server_status: while (true) {
                
                switch (payaraServerBehaviour.getServerStatus(false)) {
                    case STOPPED_NOT_LISTENING:
                        try {
                            if (payaraProcess.isAlive()) {
                                
                                // Server is not (yet) listening.
                                // Check if we need to attach the debugger for it to continue.
                                // This happens when the server is started in debug with halt on start
                                
                                if (earlyAttach && mode == DEBUG && !attached) {
                                    try {
                                        payaraServerBehaviour.attach(launch, configuration.getWorkingCopy(), null, getDebugPort(process));
                                        checkMonitorAndProgress(monitor, WORK_STEP);
                                        attached = true;
                                    } catch (CoreException e) {
                                        // Process may not have reached the point where it waits for a remote connection
                                        logMessage(e.getMessage());
                                    }
                                }
                            } else {
                                int exitCode = payaraProcess.exitValue();
                                
                                if (exitCode != 0) {
                                    // Something bad happened, show user startup console
                                    
                                    logMessage("launch failed with exit code " + exitCode);
                                    showConsole(startupConsole);
                                    
                                    throw new PayaraLaunchException("Launch process failed with exit code " + exitCode);
                                }
                            }
                            
                        } catch (IllegalThreadStateException e) { // still running, keep waiting
                        }
                        
                        break;
                    case RUNNING_PROXY_ERROR:
                        startupConsole.stopLogging();
                        payaraProcess.destroy();
                        
                        throw new PayaraLaunchException("BAD GATEWAY response code returned. Check your proxy settings. Killing startup process.", payaraProcess);
                    case RUNNING_CREDENTIAL_PROBLEM:
                        startupConsole.stopLogging();
                        payaraProcess.destroy();
                        
                        throw new PayaraLaunchException("Wrong user name or password. Killing startup process.", payaraProcess);
                    case RUNNING_DOMAIN_MATCHING:
                        startupConsole.stopLogging();
                        break check_server_status;
                    default:
                        break;
                }
                
                // Wait for notification when server state changes
                try {
                    checkMonitor(monitor);
                    
                    // Limit waiting so we can check process exit code again
                    payaraServerBehaviour.wait(500);
                    
                    if (!hasLogged && (startupConsole.hasLogged() || filelogConsole.hasLogged())) {
                        // Something has been logged meaning the JVM of the target
                        // process is activated. Could be JVM logging first
                        // like "waiting for connection", or the first log line of Payara starting
                        hasLogged = true;
                        checkMonitorAndProgress(monitor, WORK_STEP / 4);
                    }
                    
                    if (!hasLoggedPayara && filelogConsole.hasLoggedPayara()) {
                        
                        // A Payara logline has been written, meaning Payara is now starting up.
                        hasLoggedPayara = true;
                        checkMonitorAndProgress(monitor, WORK_STEP / 4);
                    }
                    
                } catch (InterruptedException e) {
                    startupConsole.stopLogging();
                    payaraProcess.destroy();
                    throw e;
                }
            }
        }

        return process;
    }
    
    private ResultProcess startPayara(boolean earlyAttach) throws PayaraLaunchException {
        try {
            // Process the arguments and call the CommandStartDAS command which will initiate
            // starting the Payara server
            return startServer(payaraServerBehaviour.getPayaraServerDelegate(), args, mode, earlyAttach);
        } catch (PayaraIdeException e) {
            throw new PayaraLaunchException("Exception in startup library.", e);
        }
    }
    
    private IPayaraConsole startLogging(Process payaraProcess) {
        IPayaraConsole startupConsole = getStartupProcessConsole(payaraServerBehaviour.getPayaraServerDelegate(), payaraProcess);
        
        startupConsole.startLogging(
                new FetchLogSimple(payaraProcess.getInputStream()),
                new FetchLogSimple(payaraProcess.getErrorStream()));
        
        return startupConsole;
    }
    
    private void checkMonitor(IProgressMonitor monitor) throws InterruptedException {
        if (monitor.isCanceled()) {
            throw new InterruptedException();
        }
    }
    
    private void checkMonitorAndProgress(IProgressMonitor monitor, int work) throws InterruptedException {
        checkMonitor(monitor);
        monitor.worked(work);
    }

}
