/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.server.starting;

import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.OK;
import static org.eclipse.debug.core.DebugPlugin.ATTR_CAPTURE_OUTPUT;
import static org.eclipse.debug.core.ILaunchManager.DEBUG_MODE;
import static org.eclipse.debug.core.ILaunchManager.RUN_MODE;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR;
import static org.eclipse.jface.dialogs.MessageDialog.openError;
import static org.eclipse.payara.tools.GlassfishToolsPlugin.SYMBOLIC_NAME;
import static org.eclipse.payara.tools.GlassfishToolsPlugin.logError;
import static org.eclipse.payara.tools.GlassfishToolsPlugin.logMessage;
import static org.eclipse.payara.tools.Messages.abortLaunchMsg;
import static org.eclipse.payara.tools.Messages.badGateway;
import static org.eclipse.payara.tools.Messages.canntCommunicate;
import static org.eclipse.payara.tools.Messages.checkVpnOrProxy;
import static org.eclipse.payara.tools.Messages.domainNotMatch;
import static org.eclipse.payara.tools.Messages.wrongUsernamePassword;
import static org.eclipse.payara.tools.log.GlassfishConsoleManager.getStandardConsole;
import static org.eclipse.payara.tools.log.GlassfishConsoleManager.showConsole;
import static org.eclipse.payara.tools.sdk.server.ServerTasks.StartMode.DEBUG;
import static org.eclipse.payara.tools.sdk.server.ServerTasks.StartMode.START;
import static org.eclipse.payara.tools.sdk.utils.ServerUtils.GFV3_JAR_MATCHER;
import static org.eclipse.payara.tools.sdk.utils.ServerUtils.getJarName;
import static org.eclipse.payara.tools.sdk.utils.Utils.quote;
import static org.eclipse.payara.tools.utils.WtpUtil.load;
import static org.eclipse.wst.server.core.IServer.STATE_STARTED;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPED;
import static org.eclipse.wst.server.core.ServerUtil.getServer;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.payara.tools.Messages;
import org.eclipse.payara.tools.exceptions.HttpPortUpdateException;
import org.eclipse.payara.tools.log.IGlassFishConsole;
import org.eclipse.payara.tools.sdk.admin.ResultProcess;
import org.eclipse.payara.tools.sdk.server.FetchLogPiped;
import org.eclipse.payara.tools.sdk.server.ServerTasks.StartMode;
import org.eclipse.payara.tools.server.GlassFishServer;
import org.eclipse.payara.tools.server.deploying.GlassFishServerBehaviour;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerDelegate;

/**
 * This class takes care of actually starting (launching) the Payara / GlassFish server.
 *
 * <p>
 * This class is registered in <code>plug-in.xml</code> in the
 * <code>org.eclipse.debug.core.launchConfigurationTypes</code> extension point.
 * </p>
 *
 */
@SuppressWarnings("restriction")
public class GlassfishServerLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {

    public static final String GFV3_MODULES_DIR_NAME = "modules"; //$NON-NLS-1$

    private static final int MONITOR_TOTAL_WORK = 1000;
    private static final int WORK_STEP = 200;
    private static Pattern debugPortPattern = Pattern.compile("-\\S+jdwp[:=]\\S*address=([0-9]+)");

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

        logMessage("in Payara launch"); //$NON-NLS-1$

        monitor.beginTask("Starting Payara", MONITOR_TOTAL_WORK);

        IServer server = getServer(configuration);
        if (server == null) {
            abort("missing Server", null, ERR_INTERNAL_ERROR); //$NON-NLS-1$
        }

        GlassFishServerBehaviour serverBehavior = load(server, GlassFishServerBehaviour.class);
        GlassFishServer serverAdapter = load(server, GlassFishServer.class);

        serverBehavior.setLaunch(launch);

        try {
            checkMonitorAndProgress(monitor, WORK_STEP);
        } catch (InterruptedException e1) {
            return;
        }

        // Find out if our server is really running and ready
        boolean isRunning = isRunning(serverBehavior);

        // If server is running and the mode is debug, try to attach the debugger
        if (isRunning) {
            logMessage("Server is already started!");
            if (DEBUG_MODE.equals(mode)) {
                try {
                    serverBehavior.attach(launch, configuration.getWorkingCopy(), monitor);
                } catch (final CoreException e) {
                    Display.getDefault().asyncExec(() -> openError(
                            Display.getDefault().getActiveShell(),
                            "Error",
                            "Error attaching to GlassFish Server. Please make sure the server is started in debug mode."));

                    logError("Not able to attach debugger, running in normal mode", e);
                    ((Server) serverBehavior.getServer()).setMode(RUN_MODE);

                    throw e;
                }
                ((Server) serverBehavior.getServer())
                        .setServerStatus(new Status(OK, SYMBOLIC_NAME, "Debugging"));
            }
        }

        try {
            if (serverAdapter.isRemote()) {
                if (!isRunning) {
                    abort(
                            "GlassFish Remote Servers cannot be start from this machine.", null,
                            ERR_INTERNAL_ERROR);
                }
            } else {
                if (!isRunning) {
                    startDASAndTarget(serverAdapter, serverBehavior, configuration, launch, mode, monitor);
                }
            }

        } catch (InterruptedException e) {
            getStandardConsole(serverAdapter).stopLogging(3);
            logError("Server start interrupted.", e);

            serverBehavior.setGFServerState(STATE_STOPPED);
            abort("Unable to start server due interruption.", e, ERR_INTERNAL_ERROR);
        } catch (CoreException e) {
            getStandardConsole(serverAdapter).stopLogging(3);
            serverBehavior.setGFServerState(STATE_STOPPED);
            throw e;
        } finally {
            monitor.done();
        }

        ((Server) serverBehavior.getServer()).setMode(mode);
    }

    @Override
    protected void abort(String message, Throwable exception, int code) throws CoreException {
        throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, code, message, exception));
    }

    // #### Private methods

    private void startDASAndTarget(GlassFishServer serverAdapter, GlassFishServerBehaviour serverBehavior,
            ILaunchConfiguration configuration, ILaunch launch, String mode, IProgressMonitor monitor)
            throws CoreException, InterruptedException {

        String domain = serverAdapter.getDomainName();
        String domainAbsolutePath = serverAdapter.getDomainPath();

        File bootstrapJar = getJarName(serverAdapter.getServerInstallationDirectory(), GFV3_JAR_MATCHER);

        if (bootstrapJar == null) {
            abort("bootstrap jar not found", null, ERR_INTERNAL_ERROR);
        }

        // TODO which java to use? for now ignore the one from launch config
        AbstractVMInstall/* IVMInstall */ vm = (AbstractVMInstall) serverBehavior.getRuntimeDelegate().getVMInstall();

        if (vm == null || vm.getInstallLocation() == null) {
            abort("Invalid Java VM location for server " + serverAdapter.getName(), null, ERR_INTERNAL_ERROR);
        }

        StartupArgsImpl startArgs = new StartupArgsImpl();
        startArgs.setJavaHome(vm.getInstallLocation().getAbsolutePath());

        // Program & VM args
        String programArgs = getProgramArguments(configuration);
        String vmArgs = getVMArguments(configuration);

        // Bug 22543277 - required by ADF support on GF 3.1.x
        if (vmArgs.indexOf("-Doracle.mds.cache=simple") < 0) { //$NON-NLS-1$
            ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
            wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
                    vmArgs + " -Doracle.mds.cache=simple ");//$NON-NLS-1$
            configuration = wc.doSave();
        }

        StartMode startMode = DEBUG_MODE.equals(mode) ? DEBUG : START;
        addJavaOptions(serverAdapter, mode, startArgs, vmArgs);
        startArgs.addGlassfishArgs(programArgs);
        startArgs.addGlassfishArgs("--domain " + domain);
        startArgs.addGlassfishArgs("--domaindir " + quote(domainAbsolutePath));

        setDefaultSourceLocator(launch, configuration);

        checkMonitorAndProgress(monitor, WORK_STEP);

        Process processGF = null;

        checkMonitorAndProgress(monitor, WORK_STEP);
        startLogging(serverAdapter, serverBehavior);
        ResultProcess process = null;

        try {
            process = serverBehavior.launchServer(startArgs, startMode, monitor);
            processGF = process.getValue().getProcess();
            launch.setAttribute(ATTR_CAPTURE_OUTPUT, "false");

            new RuntimeProcess(launch, processGF, "Payara Application Server", null);
        } catch (TimeoutException e) {
            abort("Unable to start server on time.", e, ERR_INTERNAL_ERROR);
        } catch (ExecutionException e) {
            abort("Unable to start server due following issues:", e.getCause(), ERR_INTERNAL_ERROR);
        } catch (HttpPortUpdateException e) {
            abort("Unable to update http port. Server shut down.", e, ERR_INTERNAL_ERROR);
        }

        try {
            checkMonitorAndProgress(monitor, WORK_STEP);
        } catch (InterruptedException e) {
            killProcesses(processGF);
            throw e;
        }

        setDefaultSourceLocator(launch, configuration);

        if (DEBUG_MODE.equals(mode)) {
            Integer debugPort = null;
            try {
                debugPort = getDebugPort(process.getValue().getArguments());
            } catch (IllegalArgumentException e) {
                killProcesses(processGF);
                abort("Server run in debug mode but the debug port couldn't be determined!", e, ERR_INTERNAL_ERROR);
            }

            serverBehavior.attach(launch, configuration.getWorkingCopy(), monitor, debugPort);
        }

    }

    private void addJavaOptions(GlassFishServer serverAdapter, String mode, StartupArgsImpl args, String vmArgs) {

        // Debug port was specified by user, use it
        if (DEBUG_MODE.equals(mode)) {
            args.addJavaArgs(vmArgs);
            int debugPort = serverAdapter.getDebugPort();
            if (debugPort != -1) {
                args.addJavaArgs(serverAdapter.getDebugOptions(debugPort));
            }
        } else {
            args.addJavaArgs(ignoreDebugArgs(vmArgs));
        }
    }

    private String ignoreDebugArgs(String vmArgs) {
        StringBuilder args = new StringBuilder(vmArgs.length());
        for (String a : vmArgs.split("\\s")) {
            if ("-Xdebug".equalsIgnoreCase(a) || (a.startsWith("-agentlib")) || a.startsWith("-Xrunjdwp")) {
                break;
            }
            args.append(a);
            args.append(" ");
        }
        return args.toString();
    }

    private void checkMonitorAndProgress(IProgressMonitor monitor, int work) throws InterruptedException {
        if (monitor.isCanceled()) {
            throw new InterruptedException();
        }

        monitor.worked(work);
    }

    private boolean isRunning(GlassFishServerBehaviour serverBehavior) throws CoreException {
        IServer thisServer = serverBehavior.getServer();

        for (IServer server : ServerCore.getServers()) {

            if (server != thisServer && server.getServerState() == STATE_STARTED) {
                ServerDelegate delegate = load(server, ServerDelegate.class);
                if (delegate instanceof GlassFishServer) {
                    GlassFishServer runingGfServer = (GlassFishServer) delegate;

                    if (runingGfServer.isRemote()) {
                        continue;
                    }

                    GlassFishServer thisGfServer = (GlassFishServer) (load(thisServer, ServerDelegate.class));
                    if (runingGfServer.getPort() == thisGfServer.getPort()
                            || runingGfServer.getAdminPort() == thisGfServer.getAdminPort()) {
                        abort(canntCommunicate, new RuntimeException(domainNotMatch), ERR_INTERNAL_ERROR);
                        return false;
                    }

                }
            }
        }

        switch (serverBehavior.getServerStatus(true)) {
        case RUNNING_CONNECTION_ERROR:
            abort(canntCommunicate, new RuntimeException(Messages.abortLaunchMsg + domainNotMatch + checkVpnOrProxy), ERR_INTERNAL_ERROR);
            break;
        case RUNNING_CREDENTIAL_PROBLEM:
            abort(canntCommunicate, new RuntimeException(Messages.abortLaunchMsg + wrongUsernamePassword), ERR_INTERNAL_ERROR);
            break;
        case RUNNING_DOMAIN_MATCHING:
            return true;
        case RUNNING_PROXY_ERROR:
            abort(canntCommunicate, new RuntimeException(abortLaunchMsg + badGateway), ERR_INTERNAL_ERROR);
            break;
        case STOPPED_DOMAIN_NOT_MATCHING:
            abort(canntCommunicate, new RuntimeException(domainNotMatch), ERR_INTERNAL_ERROR);
            break;
        case STOPPED_NOT_LISTENING:
            return false;
        default:
            break;
        }

        return false;
    }

    private void startLogging(final GlassFishServer serverAdapter, final GlassFishServerBehaviour serverBehavior) {
        try {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    File logFile = new File(serverAdapter.getDomainPath() + "/logs/server.log"); //$NON-NLS-1$
                    try {
                        logFile.createNewFile();
                    } catch (Exception e) {
                        // file probably exists
                        e.printStackTrace();
                    }

                    IGlassFishConsole console = getStandardConsole(serverAdapter);
                    showConsole(console);
                    if (!console.isLogging()) {
                        console.startLogging(FetchLogPiped.create(serverAdapter, true));
                    }
                }
            });
        } catch (Exception e) {
            logError("page.showView", e); //$NON-NLS-1$
        }
    }

    private static Integer getDebugPort(String startArgs) {
        Matcher debugPortMatcher = debugPortPattern.matcher(startArgs);
        if (debugPortMatcher.find()) {
            return Integer.parseInt(debugPortMatcher.group(1));
        }

        throw new IllegalArgumentException("Debug port not found in process args!");
    }

    private void killProcesses(Process... processes) {
        for (Process process : processes) {
            if (process != null) {
                process.destroy();
            }
        }
    }

    static class GlassfishServerDebugListener implements IDebugEventSetListener {

        private GlassFishServerBehaviour serverBehavior;
        private String debugTargetIdentifier;

        public GlassfishServerDebugListener(GlassFishServerBehaviour serverBehavior, String debugTargetIdentifier) {
            this.serverBehavior = serverBehavior;
            this.debugTargetIdentifier = debugTargetIdentifier;
        }

        @Override
        public void handleDebugEvents(DebugEvent[] events) {
            if (events != null) {
                int size = events.length;
                for (int i = 0; i < size; i++) {
                    if (events[i].getSource() instanceof JDIDebugTarget) {
                        JDIDebugTarget dt = (JDIDebugTarget) events[i].getSource();
                        try {

                            logMessage("JDIDebugTarget=" + dt.getName()); //$NON-NLS-1$
                            if ((dt.getName().indexOf(debugTargetIdentifier) != -1)
                                    && events[i].getKind() == DebugEvent.TERMINATE) { // $NON-NLS-1$
                                DebugPlugin.getDefault().removeDebugEventListener(this);

                                if (!dt.isTerminated()) {
                                    serverBehavior.stop(true);
                                }

                                // reset server status
                                Server server = (Server) serverBehavior.getServer();
                                server.setServerStatus(null);
                            }
                        } catch (DebugException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
