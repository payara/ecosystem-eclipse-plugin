/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.server.deploying;

import static java.io.File.separatorChar;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.OK;
import static org.eclipse.core.runtime.IStatus.WARNING;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.debug.core.DebugEvent.TERMINATE;
import static org.eclipse.debug.core.ILaunchManager.DEBUG_MODE;
import static org.eclipse.debug.core.ILaunchManager.RUN_MODE;
import static org.eclipse.payara.tools.GlassfishToolsPlugin.SYMBOLIC_NAME;
import static org.eclipse.payara.tools.GlassfishToolsPlugin.createErrorStatus;
import static org.eclipse.payara.tools.GlassfishToolsPlugin.logError;
import static org.eclipse.payara.tools.GlassfishToolsPlugin.logMessage;
import static org.eclipse.payara.tools.Messages.serverDirectoryGone;
import static org.eclipse.payara.tools.log.GlassfishConsoleManager.getStandardConsole;
import static org.eclipse.payara.tools.sdk.TaskState.COMPLETED;
import static org.eclipse.payara.tools.server.GlassFishServer.DEFAULT_DEBUG_PORT;
import static org.eclipse.payara.tools.server.archives.AssembleModules.isModuleType;
import static org.eclipse.payara.tools.server.archives.ExportJavaEEArchive.export;
import static org.eclipse.payara.tools.utils.ResourceUtils.RESOURCE_FILE_NAME;
import static org.eclipse.payara.tools.utils.ResourceUtils.checkUpdateServerResources;
import static org.eclipse.payara.tools.utils.Utils.isEmpty;
import static org.eclipse.payara.tools.utils.Utils.simplifyModuleID;
import static org.eclipse.osgi.util.NLS.bind;
import static org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities.getServerContextRoot;
import static org.eclipse.wst.server.core.IServer.PUBLISH_AUTO;
import static org.eclipse.wst.server.core.IServer.PUBLISH_CLEAN;
import static org.eclipse.wst.server.core.IServer.PUBLISH_INCREMENTAL;
import static org.eclipse.wst.server.core.IServer.PUBLISH_STATE_FULL;
import static org.eclipse.wst.server.core.IServer.PUBLISH_STATE_NONE;
import static org.eclipse.wst.server.core.IServer.STATE_STARTED;
import static org.eclipse.wst.server.core.IServer.STATE_STARTING;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPED;
import static org.eclipse.wst.server.core.internal.ProgressUtil.getMonitorFor;
import static org.eclipse.wst.server.core.util.PublishHelper.deleteDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.payara.tools.GlassfishToolsPlugin;
import org.eclipse.payara.tools.Messages;
import org.eclipse.payara.tools.exceptions.GlassfishLaunchException;
import org.eclipse.payara.tools.exceptions.HttpPortUpdateException;
import org.eclipse.payara.tools.internal.GlassfishStateResolver;
import org.eclipse.payara.tools.internal.ServerStateListener;
import org.eclipse.payara.tools.internal.ServerStatusMonitor;
import org.eclipse.payara.tools.log.GlassfishConsoleManager;
import org.eclipse.payara.tools.log.IGlassFishConsole;
import org.eclipse.payara.tools.sdk.GlassFishIdeException;
import org.eclipse.payara.tools.sdk.TaskState;
import org.eclipse.payara.tools.sdk.admin.CommandAddResources;
import org.eclipse.payara.tools.sdk.admin.CommandDeploy;
import org.eclipse.payara.tools.sdk.admin.CommandGetProperty;
import org.eclipse.payara.tools.sdk.admin.CommandRedeploy;
import org.eclipse.payara.tools.sdk.admin.CommandStopDAS;
import org.eclipse.payara.tools.sdk.admin.CommandTarget;
import org.eclipse.payara.tools.sdk.admin.CommandUndeploy;
import org.eclipse.payara.tools.sdk.admin.CommandVersion;
import org.eclipse.payara.tools.sdk.admin.ResultMap;
import org.eclipse.payara.tools.sdk.admin.ResultProcess;
import org.eclipse.payara.tools.sdk.admin.ResultString;
import org.eclipse.payara.tools.sdk.admin.ServerAdmin;
import org.eclipse.payara.tools.sdk.server.FetchLogSimple;
import org.eclipse.payara.tools.sdk.server.ServerTasks;
import org.eclipse.payara.tools.sdk.server.ServerTasks.StartMode;
import org.eclipse.payara.tools.server.GlassFishRuntime;
import org.eclipse.payara.tools.server.GlassFishServer;
import org.eclipse.payara.tools.server.ServerStatus;
import org.eclipse.payara.tools.server.archives.AssembleModules;
import org.eclipse.payara.tools.server.starting.GlassfishServerLaunchDelegate;
import org.eclipse.payara.tools.server.starting.StartupArgsImpl;
import org.eclipse.payara.tools.utils.ResourceUtils;
import org.eclipse.payara.tools.utils.Utils;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.launching.JavaRemoteApplicationLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.PublishHelper;

/**
 * This behavior class is called back by WTP to perform the standard operations on the Payara / GlassFish server 
 * such as deploy/undeploy etc. Starting/restarting is delegated to {@link GlassfishServerLaunchDelegate}.
 * 
 * <p>
 * This class is registered in <code>plug-in.xml</code> in the <code>org.eclipse.wst.server.core.serverTypes</code>
 * extension point.
 * </p>
 *
 */
@SuppressWarnings("restriction")
public final class GlassFishServerBehaviour extends ServerBehaviourDelegate implements ServerStateListener {

	// initialized
	protected boolean needARedeploy = true; // by default, will be calculated..

	private GlassfishStateResolver stateResolver = new GlassfishStateResolver();

	private ServerStatusMonitor statusMonitor;

	private static final ExecutorService asyncJobsService = Executors.newCachedThreadPool();

	private static JavaRemoteApplicationLaunchConfigurationDelegate REMOTE_JAVA_APP_LAUNCH_DELEGATE = new JavaRemoteApplicationLaunchConfigurationDelegate();

	public GlassFishServerBehaviour() {
		logMessage("in GlassFishServerBehaviour CTOR ");
	}

	protected void initialize(IProgressMonitor monitor) {
		super.initialize(monitor);
		logMessage("in Behaviour initialize for " + getGlassfishServerDelegate().getName());

		statusMonitor = ServerStatusMonitor.getInstance(getGlassfishServerDelegate(), this);
		statusMonitor.start();
	}
	
	
	// ### Life-cycle methods called by Eclipse WTP

	@Override
	public IStatus canStart(String launchMode) {
		if (getGlassfishServerDelegate().isRemote()) {
			return new Status(ERROR, SYMBOLIC_NAME,	"Start remote Glassfish server is not supported");
		}
		
		return super.canStart(launchMode);
	}
	
	@Override
	public IStatus canRestart(String mode) {
		if (getGlassfishServerDelegate().isRemote() && !mode.equals(DEBUG_MODE)) {
			return new Status(ERROR, SYMBOLIC_NAME, "Restart remote Glassfish server is not supported");
		}
		
		return super.canRestart(mode);
	}
	
	@Override
	public void restart(final String launchMode) throws CoreException {
		if (getGlassfishServerDelegate().isRemote() && launchMode.equals(DEBUG_MODE)) {
			((Server) getServer()).setServerStatus(
					new Status(OK, SYMBOLIC_NAME, "Attaching to remote server..."));
		}

		logMessage("in GlassfishServerBehaviourDelegate restart");
		stopServer(false);
		
		Thread thread = new Thread("Synchronous server start") {
			public void run() {
				try {
					getServer().getLaunchConfiguration(true, null).launch(launchMode, new NullProgressMonitor());
					logMessage("GlassfishServerBehaviourDelegate restart done");

				} catch (Exception e) {
					logError("in SunAppServerBehaviour restart", e);
				}
			}
		};

		thread.setDaemon(true);
		thread.start();
	}

	
	@Override
	public void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {

		// First, test if the server still exists
		File serverloc = new File(getGlassfishServerDelegate().getServerInstallationDirectory());
		if (!serverloc.exists()) {
			logError(bind(serverDirectoryGone, serverloc.getAbsolutePath()), null);
			return;
		}

		needARedeploy = true; // by default

		long t = System.currentTimeMillis();
		if (module.length > 1) {// only publish root modules, i.e web modules
			setModulePublishState(module, PUBLISH_STATE_NONE);
		} else {
			publishModuleForGlassFishV3(kind, deltaKind, module, monitor);
			logMessage("done publishModule in " + (System.currentTimeMillis() - t) + " ms");
		}
	}
	
	@Override
	public void serverStatusChanged(ServerStatus newStatus) {
		synchronized (this) {
			int currentState = getServer().getServerState();
			int nextState = stateResolver.resolve(newStatus, currentState);
			if (currentState != nextState) {
				setGFServerState(nextState);
				serverStateChanged(nextState);
				updateServerStatus(newStatus);
			}
			notify();
		}
	}
	
	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		IModule[] modules = getServer().getModules();
		boolean allpublished = true;
		for (int i = 0; i < modules.length; i++) {
			if (getServer().getModulePublishState(new IModule[] { modules[i] }) != PUBLISH_STATE_NONE) {
				allpublished = false;
			}
		}
		
		if (allpublished) {
			setServerPublishState(PUBLISH_STATE_NONE);
		}
	}

	@Override
	public IStatus canStop() {
		if (!getGlassfishServerDelegate().isRemote()) {
			return OK_STATUS;
		}
		
		return new Status(ERROR, SYMBOLIC_NAME, "Start remote Glassfish server is not supported");
	}
	
	@Override
	public void stop(boolean force) {
		logMessage("in GlassfishServerBehaviourDelegate stop");
		stopServer(true);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		statusMonitor.stop();
		logMessage("in Behaviour dispose for " + getGlassfishServerDelegate().getName());
	}
	
	
	
	// #### API for external callers
	
	/*
	 * get the correct adapter for the GlassFish server
	 */
	public GlassFishServer getGlassfishServerDelegate() {
		GlassFishServer payaraServer = (GlassFishServer) getServer().getAdapter(GlassFishServer.class);
		if (payaraServer == null) {
			payaraServer = (GlassFishServer) getServer().loadAdapter(GlassFishServer.class, new NullProgressMonitor());
		}
		
		return payaraServer;
	}
	
	public ServerStatus getServerStatus(boolean forceUpdate) {
		return statusMonitor.getServerStatus(forceUpdate);
	}
	
	public static String getVersion(GlassFishServer server) throws GlassFishIdeException {
		Future<ResultString> future = ServerAdmin.exec(server, new CommandVersion());
		try {
			ResultString result = future.get(30, SECONDS);
			return result.getValue();
		} catch (InterruptedException e) {
			throw new GlassFishIdeException("Exception by calling getVersion", e);
		} catch (java.util.concurrent.ExecutionException e) {
			throw new GlassFishIdeException("Exception by calling getVersion", e);
		} catch (TimeoutException e) {
			throw new GlassFishIdeException("Timeout for getting version command exceeded", e);
		}
	}
	
	public void updateServerStatus() {
		ServerStatus status = getServerStatus(true);
		updateServerStatus(status);
	}
	
	public void undeploy(String moduleName, IProgressMonitor monitor) throws CoreException {

		undeploy(moduleName);
		
		// Retrieve the IModule for the module name
		final List<IModule[]> moduleList = getAllModules();
		IModule[] module = null;
		for (IModule[] m : moduleList) {
			if (m.length == 1 && m[0].getName().equals(moduleName)) {
				module = m;
				break;
			}
		}
		
		// If we were able to map module name to IModule, set publish state
		// to Full to tell a full deploy would be needed
		if (module != null) {
			setModulePublishState(module, PUBLISH_STATE_FULL);
		}
	}
	
	public GlassFishRuntime getRuntimeDelegate() {
		return (GlassFishRuntime) getServer().getRuntime().loadAdapter(GlassFishRuntime.class, null);
	}

	public ResultProcess launchServer(StartupArgsImpl gfStartArguments, StartMode launchMode, IProgressMonitor monitor) 	throws TimeoutException, InterruptedException, ExecutionException, HttpPortUpdateException {
		setGFServerState(STATE_STARTING);

		ResultProcess p = null;
		StartJob j = new StartJob(gfStartArguments, launchMode);
		Future<ResultProcess> res = asyncJobsService.submit(j);
		try {
			p = res.get(getServer().getStartTimeout(), SECONDS);
		} catch (TimeoutException e) {
			res.cancel(true);
			throw e;
		}
		updateHttpPort();
		return p;
	}
	
	/**
	 * This is the only modification point of server state.
	 * 
	 * @param state
	 */
	public synchronized void setGFServerState(int state) {
		setServerState(state);
	}

	/**
	 * Called to attempt to attach debugger to running glassfish.
	 * 
	 * @param launch
	 * @param config
	 * @param monitor
	 * @throws CoreException
	 */
	public void attach(ILaunch launch, ILaunchConfigurationWorkingCopy config, IProgressMonitor monitor) throws CoreException {
		GlassFishServer serverDelegate = getGlassfishServerDelegate();
		int debugPort = serverDelegate.getDebugPort();
		debugPort = debugPort == -1 ? DEFAULT_DEBUG_PORT : debugPort;
		attach(launch, config, monitor, debugPort);
	}
	
	public void attach(final ILaunch launch, ILaunchConfigurationWorkingCopy config, IProgressMonitor monitor, int debugPort) throws CoreException {
		setDebugArgument(config, "hostname", getServer().getHost());
		setDebugArgument(config, "port", String.valueOf(debugPort));
		
		REMOTE_JAVA_APP_LAUNCH_DELEGATE.launch(config, DEBUG_MODE, launch, getMonitorFor(monitor));

		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {

			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				for (DebugEvent event : events) {
					if (event.getKind() == TERMINATE && event.getSource() instanceof JDIDebugTarget) {
						JDIDebugTarget debugTarget = (JDIDebugTarget) event.getSource();
						if (debugTarget != null && debugTarget.getLaunch().getLaunchConfiguration() == launch
								.getLaunchConfiguration()) {
							DebugPlugin.getDefault().removeDebugEventListener(this);
							((Server) getServer()).setMode(RUN_MODE);
							((Server) getServer()).setServerStatus(new Status(OK, SYMBOLIC_NAME, ""));
							
							return;
						}
					}
				}
			}
		});
	}
	
	
	
	
	
	// #### Implementation of life-cycle methods defined above
	
	
	private void serverStateChanged(int serverState) {
		switch (serverState) {
			case STATE_STARTED:
				try {
					updateHttpPort();
					tryAttachDebug();
				} catch (HttpPortUpdateException e) {
					logError("Unable to update HTTP port for server started" + "outside of IDE!", e);
				}
				break;
			case STATE_STOPPED:
				setLaunch(null);
				break;
			default:
				break;
		}
	}

	private void tryAttachDebug() {
		// try to launch configuration - if it fails, it means we are in run mode
		// if success - debug mode
		Thread t = new Thread("attach debugger to glassfish") {

			@Override
			public void run() {
				try {
					// this will not really run glassfish because it is already running
					// just tries to attach debugger
					ILaunchConfiguration config = getServer().getLaunchConfiguration(true, null);
					ILaunch launch = getServer().getLaunch();
					if (launch != null) {
						String mode = launch.getLaunchMode();
						if (mode.equals(ILaunchManager.DEBUG_MODE)) {
							// config.launch(ILaunchManager.DEBUG_MODE, null);
						}
					}
				} catch (CoreException e) {
					logMessage("Unable to attach debugger, running in normal mode");
				}
			}

		};

		t.start();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setDebugArgument(ILaunchConfigurationWorkingCopy config, String key, String arg) {
		try {
			Map args = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, (Map) null);

			if (args != null)
				args = new HashMap(args);
			else
				args = new HashMap();
			args.put(key, String.valueOf(arg));

			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, args);
		} catch (CoreException ce) {
			GlassfishToolsPlugin.logError("Error when setting debug argument for remote GF", ce);
		}
	}

	/**
	 * Checks if the Ant publisher actually needs to publish. For ear modules it
	 * also checks if any of the children modules requires publishing.
	 * 
	 * @return true if ant publisher needs to publish.
	 */
	private boolean publishNeeded(int kind, int deltaKind, IModule[] module) {
		if (getServer().getServerPublishState() == PUBLISH_CLEAN) {
			return true;
		}

		if (kind != PUBLISH_INCREMENTAL && kind != PUBLISH_AUTO) {
			return true;
		}
		
		if (deltaKind != NO_CHANGE) {
			return true;
		}
		
		if (module[0] instanceof DeletedModule) {
			return false;
		}

		if (AssembleModules.isModuleType(module[0], "jst.ear")) { //$NON-NLS-1$
			IEnterpriseApplication earModule = (IEnterpriseApplication) module[0]
					.loadAdapter(IEnterpriseApplication.class, new NullProgressMonitor());
			IModule[] childModules = earModule.getModules();
			for (int i = 0; i < childModules.length; i++) {
				IModule m = childModules[i];
				IModule[] modules = { module[0], m };
				if (PUBLISH_STATE_NONE != getGlassfishServerDelegate().getServer()
						.getModulePublishState(modules))
					return true;
			}
		} else {
			int publishState = getGlassfishServerDelegate().getServer().getModulePublishState(module);
			if (PUBLISH_STATE_NONE != publishState)
				return true;
		}
		return false;
	}
	
	/*
	 * Publishes for Web apps only in V3 prelude
	 */
	private void publishModuleForGlassFishV3(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor)
			throws CoreException {

		if (module.length > 1) {// only publish root modules, i.e web modules
			setModulePublishState(module, PUBLISH_STATE_NONE);
			return;
		}

		if (!publishNeeded(kind, deltaKind, module) || monitor.isCanceled()) {
			return;
		}

		IPath path = getTempDirectory().append("publish.txt");

		Properties prop = new Properties();
		try (FileInputStream fis = new FileInputStream(path.toFile())) {
			prop.load(fis);
		} catch (Exception e) {
			// Ignore
		}

		boolean isRemote = getGlassfishServerDelegate().isRemote();
		boolean isJarDeploy = getGlassfishServerDelegate().getJarDeploy();
		if ((!isRemote && !isJarDeploy)) {
			publishDeployedDirectory(deltaKind, prop, module, monitor);
		} else {
			publishJarFile(kind, deltaKind, prop, module, monitor);

		}

		setModulePublishState(module, PUBLISH_STATE_NONE);

		try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
			prop.store(fos, "GlassFish 3");
		} catch (Exception e) {
			logError("Error in PUBLISH_STATE_NONE", e);
		}

	}

	private void publishDeployedDirectory(int deltaKind, Properties p, IModule module[], IProgressMonitor monitor) throws CoreException {
		
		// ludo using PublishHelper now to control the temp area to be
		// in the same file system of the deployed apps so that the mv operation
		// Eclipse is doing sometimes can work.
		PublishHelper helper = new PublishHelper(
				new Path(getGlassfishServerDelegate().getDomainPath() + "/eclipseAppsTmp").toFile());

		if (deltaKind == REMOVED) {
			String publishPath = (String) p.get(module[0].getId());
			GlassfishToolsPlugin.logMessage("REMOVED in publishPath" + publishPath);
			String name = Utils.simplifyModuleID(module[0].getName());
			try {
				undeploy(name);
			} catch (Exception e) {
				// Bug 16876200 - UNABLE TO CLEAN GF SERVER INSTANCE
				// In case undeploy with asadmin failed,
				// catch the exception and try delete the app directory from server directly
				// next
			}

			if (publishPath != null) {
				try {
					File pub = new File(publishPath);
					if (pub.exists()) {
						logMessage("PublishUtil.deleteDirectory called");
						IStatus[] stat = deleteDirectory(pub, monitor);
						analyseReturnedStatus(stat);
					}
				} catch (Exception e) {
					throw new CoreException(new Status(WARNING, SYMBOLIC_NAME, 0, "cannot remove " + module[0].getName(), e));
				}
			}
		} else {
			IPath path = new Path(getGlassfishServerDelegate().getDomainPath() + "/eclipseApps/" + module[0].getName());

			String contextRoot = null;
			AssembleModules assembler = new AssembleModules(module, path, getGlassfishServerDelegate(), helper);
			logMessage("Deploy direcotry " + path.toFile().getAbsolutePath());

			if (module[0] instanceof DeletedModule) {
				return;
			}

			// Either web, ear or non of these
			if (isModuleType(module[0], "jst.web")) {
				logMessage("is WEB");
				
				assembler.assembleWebModule(monitor);
				contextRoot = getContextRoot(module);
			} else if (isModuleType(module[0], "jst.ear")) {
				logMessage("is EAR");
				
				assembler.assembleDirDeployedEARModule(monitor);
			} else {
				// default
				assembler.assembleNonWebOrNonEARModule(monitor);
			}
			
			needARedeploy = assembler.needsARedeployment();

			// deploy the sun resource file if there is one in path:
			registerSunResource(module, p, path);

			String spath = "" + path;
			
			// BUG NEED ALSO to test if it has been deployed
			// once...isDeployed()
			if (needARedeploy) {
				
				String name = simplifyModuleID(module[0].getName());
				Map<String, String> properties = getDeploymentProperties();
				boolean keepSession = getGlassfishServerDelegate().getKeepSessions();

				CommandTarget command = null;
				if (deltaKind == ADDED) {
					command = new CommandDeploy(name, null, new File(spath), contextRoot, properties, new File[0]);
				} else {
					command = new CommandRedeploy(name, null, contextRoot, properties, new File[0], keepSession);
				}

				try {
					ServerAdmin.executeOn(getGlassfishServerDelegate())
							   .command(command)
							   .onNotCompleted(result -> {
								   logMessage("deploy is failing=" + result.getValue());
								   throw new IllegalStateException("deploy is failing=" + result.getValue());})
							   .get();
					
				} catch (Exception ex) {
					logError("deploy is failing=", ex);
					throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, 0,	"cannot Deploy " + name, ex));
				}
			} else {
				logMessage("optimal: NO NEED TO TO A REDEPLOYMENT, !!!");

			}
		}
	}
	
	private void publishJarFile(int kind, int deltaKind, Properties p, IModule[] module, IProgressMonitor monitor) throws CoreException {
		// first try to see if we need to undeploy:

		if (deltaKind == REMOVED) {
			
			// Same logic as directory undeploy
			publishDeployedDirectory(deltaKind, p, module, monitor);

		} else {

			try {
				File archivePath = export(module[0], monitor);
				logMessage("Deploy archive " + archivePath.getAbsolutePath());
				
				String name = simplifyModuleID(module[0].getName());
				String contextRoot = null;

				if (isModuleType(module[0], "jst.web")) {
					contextRoot = getContextRoot(module);
				}

				// keepSession state is NOT supported in redeploy as JAR

				try {
					ServerAdmin.executeOn(getGlassfishServerDelegate())
					   .command(new CommandDeploy(name, null, archivePath, contextRoot, getDeploymentProperties(), new File[0]))
					   .timeout(520)
					   .onNotCompleted(result -> {
						   logMessage("deploy is failing=" + result.getValue());
						   throw new IllegalStateException("deploy is failing=" + result.getValue());})
					   .get();
					
				} catch (Exception ex) {
					logError("deploy is failing=", ex);
					throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, 0, 	"cannot Deploy " + name, ex));
				}
			} catch (org.eclipse.core.commands.ExecutionException e) {
				e.printStackTrace();
			}

		}
	}
	
	private void registerSunResource(IModule module[], Properties properties, IPath path) throws CoreException {
		// Get correct location for sun-resources.xml
		IProject project = module[0].getProject();
		String location = ResourceUtils.getRuntimeResourceLocation(project);
		if (location != null) {
			if (location.trim().length() > 0) {
				location = location + separatorChar + RESOURCE_FILE_NAME;
			} else {
				location = RESOURCE_FILE_NAME;
			}
		}
		
		File sunResource = new File("" + path, location);
		if (sunResource.exists()) {
			checkUpdateServerResources(sunResource, getGlassfishServerDelegate());
			try {
				Future<ResultString> future = ServerAdmin.<ResultString>exec(getGlassfishServerDelegate(), new CommandAddResources(sunResource, null));
				ResultString result = future.get(120, SECONDS);
				
				if (!COMPLETED.equals(result.getState())) {
					logMessage("register resource is failing=" + result.getValue());
					throw new Exception("register resource is failing=" + result.getValue());
				}
			} catch (Exception ex) {
				logError("deploy of sun-resources is failing ", ex);
				throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, 0,
						"cannot register sun-resource.xml for " + module[0].getName(), ex));
			}
		}
		
		properties.put(module[0].getId(), path.toOSString());
	}

	private void undeploy(String moduleName) throws CoreException {
		try {
			
			ServerAdmin.executeOn(getGlassfishServerDelegate())
			   .command(new CommandUndeploy(moduleName))
			   .timeout(520)
			   .onNotCompleted(result -> {
				   logMessage("undeploy is failing=" + result.getValue());
					throw new IllegalStateException("undeploy is failing=" + result.getValue());})
			   .get();
			
		} catch (Exception ex) {
			logError("Undeploy is failing=", ex);
			throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, 0, "cannot UnDeploy " + moduleName, ex));
		}
	}

	private void updateHttpPort() throws HttpPortUpdateException {
		GlassFishServer server = (GlassFishServer) getServer().createWorkingCopy().loadAdapter(GlassFishServer.class, null);
		CommandGetProperty cgp = new CommandGetProperty("*.server-config.*.http-listener-1.port");
		Future<ResultMap<String, String>> future = ServerAdmin.<ResultMap<String, String>>exec(getGlassfishServerDelegate(), cgp);
		ResultMap<String, String> result = null;
		
		try {
			result = future.get(20, SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			logError("Unable to retrieve server http port for server ", e);
			throw new HttpPortUpdateException(e);
		}

		if (result != null && COMPLETED.equals(result.getState())) {
			for (Entry<String, String> entry : result.getValue().entrySet()) {
				String val = entry.getValue();
				try {
					if (val != null && val.trim().length() > 0) {
						server.setPort(Integer.parseInt(val));
						server.getServerWorkingCopy().save(true, null);
						break;
					}
				} catch (NumberFormatException | CoreException nfe) {
					throw new HttpPortUpdateException(nfe);
				}
			}
		}
	}
	
	private String getContextRoot(IModule[] module) {
		String projectContextRoot = getServerContextRoot(module[0].getProject());
		
		return !isEmpty(projectContextRoot) ? projectContextRoot	: module[0].getName();
	}
	
	private Map<String, String> getDeploymentProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		
		String preserveSessionKey = getGlassfishServerDelegate().computePreserveSessions();
		if (preserveSessionKey != null) {
			properties.put(preserveSessionKey, Boolean.toString(getGlassfishServerDelegate().getKeepSessions()));
		}
		
		return properties;
	}
	
	

	private void analyseReturnedStatus(IStatus[] status) throws CoreException {

		if (status == null || status.length == 0) {
			return;
		}
		
		for (IStatus s : status) {
			logMessage("analyseReturnedStatus: " + s.getMessage());
		}
	}

	/**
	 * Updates server status.
	 */
	private void updateServerStatus(ServerStatus status) {
		Server server2 = ((Server) getServer());
		if (status != ServerStatus.RUNNING_DOMAIN_MATCHING) {
			// server2.setServerState(IServer.STATE_STOPPED);
			String statusMsg = null;
			switch (status) {
			case RUNNING_CREDENTIAL_PROBLEM:
				statusMsg = Messages.invalidCredentials;
				break;
			case STOPPED_DOMAIN_NOT_MATCHING:
				if (!getGlassfishServerDelegate().isRemote())
					statusMsg = Messages.serverNotMatchingLocal;
				else
					statusMsg = Messages.serverNotMatchingRemote;
				break;
			case RUNNING_CONNECTION_ERROR:
				if (server2.getServerState() != IServer.STATE_STOPPED)
					statusMsg = Messages.connectionError;
				break;
			default:
				server2.setServerStatus(null);

			}

			if (statusMsg != null) {
				server2.setServerStatus(createErrorStatus(statusMsg, null));// $NON-NLS-1$);
			}

		} else {
			server2.setServerStatus(null);
		}
	}
	
	/**
	 * 
	 * @stop GlassFish v3 or v3 prelude via http command
	 */
	private void stopServer(boolean stopLogging) {
		GlassFishServer server = getGlassfishServerDelegate();
		
		// Shouldn't allow stop remote server
		if (server.isRemote()) {
			return;
		}
		
		stopImpl(server);
		
		if (stopLogging) {
			getStandardConsole(server).stopLogging(3);
		}
	}

	private void stopImpl(GlassFishServer server) {
		setGFServerState(IServer.STATE_STOPPING);
		StopJob j = new StopJob();
		Future<?> res = asyncJobsService.submit(j);
		
		// TODO how to let user know about possible failures
		try {
			res.get(getServer().getStopTimeout(), SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			GlassfishToolsPlugin.logError("Stop server could not be finished because of exception.", e);
		} catch (TimeoutException e1) {
			res.cancel(true);
			GlassfishToolsPlugin.logMessage("Stop server could not be finished in time.");
		}
		
		setLaunch(null);
	}
	
	
	
	// ### Jobs

	private class StartJob implements Callable<ResultProcess> {

		private StartupArgsImpl args;
		private StartMode mode;

		StartJob(StartupArgsImpl args, StartMode mode) {
			super();
			this.args = args;
			this.mode = mode;
		}

		@Override
		public ResultProcess call() throws Exception {
			ResultProcess process = null;

			try {
				process = ServerTasks.startServer(getGlassfishServerDelegate(), args, mode);
			} catch (GlassFishIdeException e) {
				throw new GlassfishLaunchException("Exception in startup library.", e);
			}

			Process gfProcess = process.getValue().getProcess();
			// read process output to prevent process'es blocking
			IGlassFishConsole startupConsole = GlassfishConsoleManager
					.getStartupProcessConsole(getGlassfishServerDelegate(), gfProcess);
			startupConsole.startLogging(new FetchLogSimple(gfProcess.getInputStream()),
					new FetchLogSimple(gfProcess.getErrorStream()));

			synchronized (GlassFishServerBehaviour.this) {
				check_server_status: while (true) {
					ServerStatus status = getServerStatus(false);
					// System.out.println("Launch process got server status: " + status);
					switch (status) {
					case STOPPED_NOT_LISTENING:
						try {
							int exit_code = gfProcess.exitValue();
							System.out.println("Process exit code: " + exit_code);
							if (exit_code != 0) {
								// something bad happened, show user startup
								// console
								GlassfishToolsPlugin.logMessage("launch failed with exit code " + exit_code);
								GlassfishConsoleManager.showConsole(startupConsole);
								throw new GlassfishLaunchException("Launch process failed with exit code " + exit_code);
							}
						} catch (IllegalThreadStateException e) {// still running, keep waiting
						}
						break;
					case RUNNING_PROXY_ERROR:
						startupConsole.stopLogging();
						gfProcess.destroy();
						throw new GlassfishLaunchException(
								"BAD GATEWAY response code returned. Check your proxy settings. Killing startup process.",
								gfProcess);
					case RUNNING_CREDENTIAL_PROBLEM:
						startupConsole.stopLogging();
						gfProcess.destroy();
						throw new GlassfishLaunchException("Wrong user name or password. Killing startup process.",
								gfProcess);
					case RUNNING_DOMAIN_MATCHING:
						startupConsole.stopLogging();
						break check_server_status;
					default:
						break;

					}
					// wait for notification when server state changes
					try {
						// limit waiting so we can check process exit code again
						GlassFishServerBehaviour.this.wait(5000);
					} catch (InterruptedException e) {
						System.out.println("StartJob interrupted, killing startup process");
						startupConsole.stopLogging();
						gfProcess.destroy();
						throw e;
					}
				}
			}

			return process;
		}

	}

	private class StopJob implements Callable<Object> {

		@Override
		public Object call() throws Exception {
			ResultString result = null;

			try {
				result = CommandStopDAS.stopDAS(getGlassfishServerDelegate());
			} catch (GlassFishIdeException e) {
				GlassfishToolsPlugin.logMessage("Stop command failed in library code." + e.getMessage());
				throw e;
			}

			if (!TaskState.COMPLETED.equals(result.getState())) {
				GlassfishToolsPlugin.logMessage("Stop call failed. Reason: " + result.getValue()); //$NON-NLS-1$
				throw new Exception("Stop call failed. Reason: " + result.getValue());
			}

			synchronized (GlassFishServerBehaviour.this) {
				// wait until is really stopped
				while (!getServerStatus(false).equals(ServerStatus.STOPPED_NOT_LISTENING)) {
					GlassFishServerBehaviour.this.wait();
				}
			}
			Server server2 = ((Server) getServer());
			server2.setServerStatus(null);
			return null;
		}

	}
}
