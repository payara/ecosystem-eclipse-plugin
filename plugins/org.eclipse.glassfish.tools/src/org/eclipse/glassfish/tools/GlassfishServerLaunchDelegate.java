/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.glassfish.tools.exceptions.HttpPortUpdateException;
import org.eclipse.glassfish.tools.log.GlassfishConsoleManager;
import org.eclipse.glassfish.tools.log.IGlassFishConsole;
import org.eclipse.glassfish.tools.sdk.admin.ResultProcess;
import org.eclipse.glassfish.tools.sdk.server.FetchLogPiped;
import org.eclipse.glassfish.tools.sdk.server.ServerTasks.StartMode;
import org.eclipse.glassfish.tools.sdk.utils.ServerUtils;
import org.eclipse.glassfish.tools.sdk.utils.Utils;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerDelegate;

@SuppressWarnings("restriction")
public class GlassfishServerLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {

	public static final String GFV3_MODULES_DIR_NAME = "modules"; //$NON-NLS-1$

	private static final int MONITOR_TOTAL_WORK = 1000;

	private static final int WORK_STEP = 200;

	private static Pattern debugPortPattern = Pattern.compile("-\\S+jdwp[:=]\\S*address=([0-9]+)");

	public GlassfishServerLaunchDelegate() {
		// SunAppSrvPlugin.logMessage("in SUN SunAppServerLaunch ctor");
	}

	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, GlassfishToolsPlugin.SYMBOLIC_NAME, code, message, exception));
	}

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		GlassfishToolsPlugin.logMessage("in SUN SunAppServerLaunch launch"); //$NON-NLS-1$
		monitor.beginTask("Starting GlassFish", MONITOR_TOTAL_WORK);
		IServer server = ServerUtil.getServer(configuration);
		if (server == null) {
			abort("missing Server", null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}
		//IServerWorkingCopy server = s.createWorkingCopy();
		final GlassFishServerBehaviour serverBehavior = (GlassFishServerBehaviour) server.loadAdapter(
				GlassFishServerBehaviour.class, null);
		final GlassFishServer serverAdapter = (GlassFishServer) server.loadAdapter(
				GlassFishServer.class, new NullProgressMonitor());
		
		serverBehavior.setLaunch(launch);

		try {
			checkMonitorAndProgress(monitor, WORK_STEP);
		} catch (InterruptedException e1) {
			return;
		}

		// find out if our server is really running and ready
		boolean isRunning = isRunning(serverBehavior);
		// if server is running and the mode is debug, try to attach the debugger
		if (isRunning) {
			GlassfishToolsPlugin.logMessage("server is already started!!!");
			if (ILaunchManager.DEBUG_MODE.equals(mode)) {
				try {
					serverBehavior.attach(launch, configuration.getWorkingCopy(), monitor);
				} catch (final CoreException e) {
					Display.getDefault().asyncExec(new Runnable(){
						@Override
						public void run() {
							org.eclipse.jface.dialogs.MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", 
									"Error attaching to GlassFish Server. Please make sure the server is started in debug mode.");
							
						}
					});
					GlassfishToolsPlugin.logError("Not able to attach debugger, running in normal mode", e);
					((Server)serverBehavior.getServer()).setMode(ILaunchManager.RUN_MODE);
					throw e;
				}
				((Server)serverBehavior.getServer()).setServerStatus(new Status(IStatus.OK, GlassfishToolsPlugin.SYMBOLIC_NAME,"Debugging"));
			}
		}
		
		try {
			if (serverAdapter.isRemote()) {
				if (!isRunning) {
					abort("GlassFish Remote Servers cannot be start from this machine.", null,
							IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
				}
			} else {
				if (!isRunning) {
					startDASAndTarget(serverAdapter, serverBehavior, configuration, launch, mode, monitor);
				}
			}
		
		} catch (InterruptedException e) {
			IGlassFishConsole console = GlassfishConsoleManager.getStandardConsole(serverAdapter);
			console.stopLogging(3);
			GlassfishToolsPlugin.logError("Server start interrupted.", e);
			serverBehavior.setGFServerState(IServer.STATE_STOPPED);
			abort("Unable to start server due interruption.", e, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (CoreException e) {
			IGlassFishConsole console = GlassfishConsoleManager.getStandardConsole(serverAdapter);
			console.stopLogging(3);
			serverBehavior.setGFServerState(IServer.STATE_STOPPED);
			throw e;
		} finally {
			monitor.done();
		}
       	((Server)serverBehavior.getServer()).setMode(mode);
	}
	
	private void startDASAndTarget(final GlassFishServer serverAdapter,
			GlassFishServerBehaviour serverBehavior, ILaunchConfiguration configuration, ILaunch launch,
			String mode, IProgressMonitor monitor) throws CoreException, InterruptedException {
		String domain = serverAdapter.getDomainName();
		String domainAbsolutePath = serverAdapter.getDomainPath();

		File bootstrapJar = ServerUtils.getJarName(serverAdapter.getServerInstallationDirectory(),
				ServerUtils.GFV3_JAR_MATCHER);
		if (bootstrapJar == null) {
			abort("bootstrap jar not found", null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}

		// TODO which java to use? for now ignore the one from launch config
		AbstractVMInstall/* IVMInstall */vm = (AbstractVMInstall) serverBehavior.getRuntimeDelegate().getVMInstall();
		
		if( vm == null || vm.getInstallLocation()==null ){
			abort("Invalid Java VM location for server " + serverAdapter.getName(), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}

		// IVMInstall vm2 = verifyVMInstall(configuration);

		StartupArgsImpl startArgs = new StartupArgsImpl();
		startArgs.setJavaHome(vm.getInstallLocation().getAbsolutePath());
		// Program & VM args
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = getVMArguments(configuration);
		
		// Bug 22543277 - required by ADF support on GF 3.1.x 
		if( vmArgs.indexOf( "-Doracle.mds.cache=simple" ) <0 ) { //$NON-NLS-1$
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute( IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs + " -Doracle.mds.cache=simple ");//$NON-NLS-1$
			configuration = wc.doSave();
		}
		
		StartMode startMode = ILaunchManager.DEBUG_MODE.equals(mode) ? StartMode.DEBUG : StartMode.START;
		addJavaOptions(serverAdapter, mode, startArgs, vmArgs);
		startArgs.addGlassfishArgs(pgmArgs);
		startArgs.addGlassfishArgs("--domain " + domain);
		startArgs.addGlassfishArgs("--domaindir " + Utils.quote(domainAbsolutePath));

		// String[] envp = getEnvironment(configuration);

		IPreferenceStore store = GlassfishToolsPlugin.getInstance().getPreferenceStore();
		setDefaultSourceLocator(launch, configuration);

		checkMonitorAndProgress(monitor, WORK_STEP);

		Process processGF = null;

		checkMonitorAndProgress(monitor, WORK_STEP);
		startLogging(serverAdapter, serverBehavior);
		ResultProcess process = null;
		try {
			process = serverBehavior.launchServer(startArgs, startMode, monitor);
			processGF = process.getValue().getProcess();
			launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "false");
			new RuntimeProcess(launch, processGF, "GlassFish Application Server", null);
		} catch (TimeoutException e) {
			abort("Unable to start server on time.", e, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (ExecutionException e) {
			abort("Unable to start server due following issues:", e.getCause(),
					IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (HttpPortUpdateException e) {
			abort("Unable to update http port. Server shut down.", e,
					IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}

		try {
			checkMonitorAndProgress(monitor, WORK_STEP);
		} catch (InterruptedException e) {
			killProcesses(processGF);
			throw e;
		}


		setDefaultSourceLocator(launch, configuration);
		if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			Integer debugPort = null;
			try {
				debugPort = getDebugPort(process.getValue().getArguments());
			} catch (NumberFormatException e) {
				killProcesses(processGF);
				abort("Server run in debug mode but the debug port couldn't be determined!", e,
						IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			} catch (IllegalArgumentException e) {
				killProcesses(processGF);
				abort("Server run in debug mode but the debug port couldn't be determined!", e,
						IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
			serverBehavior.attach(launch, configuration.getWorkingCopy(), monitor, debugPort);
		}
//		if (mode.equals("debug")) { //$NON-NLS-1$
//
//			Map<String, String> arg = new HashMap<String, String>();
//			Integer debugPort = null;
//			try {
//				debugPort = getDebugPort(process.getValue().getArguments());
//			} catch (NumberFormatException e) {
//				killProcesses(processGF, processDB);
//				abort("Server run in debug mode but the debug port couldn't be determined!", e,
//						IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
//			} catch (IllegalArgumentException e) {
//				killProcesses(processGF, processDB);
//				abort("Server run in debug mode but the debug port couldn't be determined!", e,
//						IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
//			}
//
//			arg.put("hostname", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$
//			arg.put("port", debugPort.toString()); //$NON-NLS-1$ //$NON-NLS-2$
//			arg.put("timeout", "25000"); //$NON-NLS-1$ //$NON-NLS-2$
//			String connectorId = getVMConnectorId(configuration);
//			IVMConnector connector = null;
//			if (connectorId == null) {
//				connector = JavaRuntime.getDefaultVMConnector();
//			} else {
//				connector = JavaRuntime.getVMConnector(connectorId);
//			}
//			// connect to VM
//			connector.connect(arg, monitor, launch);
//
////			DebugPlugin.getDefault().addDebugEventListener(
////					new GlassfishServerDebugListener(serverBehavior, "localhost:" + debugPort));
//			
//			Server server = (Server)ServerUtil.getServer(configuration);
//			((Server)server).setServerStatus(new Status(IStatus.OK, GlassfishToolsPlugin.SYMBOLIC_NAME, "Debugging"));
//		}

	}

	private void addJavaOptions(GlassFishServer serverAdapter, String mode, 
			StartupArgsImpl args, String vmArgs) {
		
		// debug port was specified by user, use it
		if (ILaunchManager.DEBUG_MODE.equals(mode)) {
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
			if ("-Xdebug".equalsIgnoreCase(a) ||
				(a.startsWith("-agentlib")) || a.startsWith("-Xrunjdwp")) {
				break;
			}
			args.append(a);
			args.append(" ");
		}
		return args.toString();
	}

	private void killProcesses(Process... process) {
		for (Process p : process) {
			if (p != null)
				p.destroy();
		}
	}

	private boolean isRunning(GlassFishServerBehaviour serverBehavior) throws CoreException {
		IServer thisServer = serverBehavior.getServer();
		for( IServer server : ServerCore.getServers() ){
			
			if( server!= thisServer && 
					server.getServerState() == IServer.STATE_STARTED ){
				ServerDelegate delegate = (ServerDelegate)server.loadAdapter(ServerDelegate.class, null);
				if( delegate instanceof GlassFishServer ){
					GlassFishServer runingGfServer = (GlassFishServer)delegate;
					if( runingGfServer.isRemote() )
						continue;
					GlassFishServer thisGfServer = (GlassFishServer)( thisServer.loadAdapter(ServerDelegate.class, null));
					if( runingGfServer.getPort() == thisGfServer.getPort() || 
							runingGfServer.getAdminPort() == thisGfServer.getAdminPort()	){
						abort( Messages.canntCommunicate,
								new RuntimeException(
										Messages.domainNotMatch ), 
								IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
						return false;
					}
					
				}
			}
		} 
		
		
		
		ServerStatus status = serverBehavior.getServerStatus(true);
		switch (status) {
		case RUNNING_CONNECTION_ERROR:
			abort(Messages.canntCommunicate,
					new RuntimeException(
							Messages.abortLaunchMsg + Messages.domainNotMatch 
									+ Messages.checkVpnOrProxy ),
					IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			break;
		case RUNNING_CREDENTIAL_PROBLEM:
			abort(Messages.canntCommunicate,
					new RuntimeException( Messages.abortLaunchMsg 
							+ Messages.wrongUsernamePassword ), //$NON-NLS-1$
					IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			break;
		case RUNNING_DOMAIN_MATCHING:
			return true;
		case RUNNING_PROXY_ERROR:
			abort( Messages.canntCommunicate,
					new RuntimeException( Messages.abortLaunchMsg 
							+ Messages.badGateway ), 
					IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			break;
		case STOPPED_DOMAIN_NOT_MATCHING:
			abort( Messages.canntCommunicate,
					new RuntimeException(
							Messages.domainNotMatch ), 
					IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			break;
		case STOPPED_NOT_LISTENING:
			return false;
		default:
			break;

		}

		return false;
	}

	private void startLogging(final GlassFishServer serverAdapter,
			final GlassFishServerBehaviour serverBehavior) {
		try {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					File logFile = new File(serverAdapter.getDomainPath() + "/logs/server.log"); //$NON-NLS-1$
					try {
						logFile.createNewFile();
					} catch (Exception e) {
						// file probably exists
						e.printStackTrace();
					}
					IGlassFishConsole console = GlassfishConsoleManager.getStandardConsole(serverAdapter);
					GlassfishConsoleManager.showConsole(console);
					if (!console.isLogging())
						console.startLogging(FetchLogPiped.create(serverAdapter, true));
				}
			});
		} catch (Exception e) {
			GlassfishToolsPlugin.logError("page.showView", e); //$NON-NLS-1$
		}
	}

	private void checkMonitorAndProgress(IProgressMonitor monitor, int work) throws InterruptedException {
		if (monitor.isCanceled())
			throw new InterruptedException();
		monitor.worked(work);
	}

	private static Integer getDebugPort(String startArgs) {
		Matcher m = debugPortPattern.matcher(startArgs);
		if (m.find()) {
			return Integer.parseInt(m.group(1));
		} else {
			throw new IllegalArgumentException("Debug port not found in process args!");
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

							GlassfishToolsPlugin.logMessage("JDIDebugTarget=" + dt.getName()); //$NON-NLS-1$
							if ((dt.getName().indexOf(debugTargetIdentifier) != -1)
									&& events[i].getKind() == DebugEvent.TERMINATE) { //$NON-NLS-1$
								DebugPlugin.getDefault().removeDebugEventListener(this);
								
								if (!dt.isTerminated()){
									serverBehavior.stop(true);
								}
								//reset server status
								Server server = (Server)serverBehavior.getServer();
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
