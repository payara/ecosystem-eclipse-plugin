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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.concurrent.TimeUnit;
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
import org.eclipse.glassfish.tools.exceptions.GlassfishLaunchException;
import org.eclipse.glassfish.tools.exceptions.HttpPortUpdateException;
import org.eclipse.glassfish.tools.internal.GlassfishStateResolver;
import org.eclipse.glassfish.tools.internal.ServerStateListener;
import org.eclipse.glassfish.tools.internal.ServerStatusMonitor;
import org.eclipse.glassfish.tools.log.GlassfishConsoleManager;
import org.eclipse.glassfish.tools.log.IGlassFishConsole;
import org.eclipse.glassfish.tools.sdk.GlassFishIdeException;
import org.eclipse.glassfish.tools.sdk.TaskState;
import org.eclipse.glassfish.tools.sdk.admin.Command;
import org.eclipse.glassfish.tools.sdk.admin.CommandAddResources;
import org.eclipse.glassfish.tools.sdk.admin.CommandDeploy;
import org.eclipse.glassfish.tools.sdk.admin.CommandGetProperty;
import org.eclipse.glassfish.tools.sdk.admin.CommandRedeploy;
import org.eclipse.glassfish.tools.sdk.admin.CommandStopDAS;
import org.eclipse.glassfish.tools.sdk.admin.CommandTarget;
import org.eclipse.glassfish.tools.sdk.admin.CommandTargetName;
import org.eclipse.glassfish.tools.sdk.admin.CommandUndeploy;
import org.eclipse.glassfish.tools.sdk.admin.CommandVersion;
import org.eclipse.glassfish.tools.sdk.admin.ResultMap;
import org.eclipse.glassfish.tools.sdk.admin.ResultProcess;
import org.eclipse.glassfish.tools.sdk.admin.ResultString;
import org.eclipse.glassfish.tools.sdk.admin.ServerAdmin;
import org.eclipse.glassfish.tools.sdk.data.IdeContext;
import org.eclipse.glassfish.tools.sdk.server.FetchLogSimple;
import org.eclipse.glassfish.tools.sdk.server.ServerTasks;
import org.eclipse.glassfish.tools.sdk.server.ServerTasks.StartMode;
import org.eclipse.glassfish.tools.utils.ResourceUtils;
import org.eclipse.glassfish.tools.utils.Utils;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.launching.JavaRemoteApplicationLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.internal.ProgressUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.PublishHelper;

@SuppressWarnings("restriction")
public final class GlassFishServerBehaviour extends
		ServerBehaviourDelegate implements ServerStateListener {
	
	// not used yet private GlassFishV2DeployFacility gfv2depl=null;//lazy
	// initialized
	protected boolean needARedeploy = true; // by default, will be calculated..
	
	private GlassfishStateResolver stateResolver = new GlassfishStateResolver();
	
	private ServerStatusMonitor statusMonitor;
	
	private static final ExecutorService asyncJobsService = Executors.newCachedThreadPool();
	
	private static JavaRemoteApplicationLaunchConfigurationDelegate REMOTE_JAVA_APP_LAUNCH_DELEGATE =
	        new JavaRemoteApplicationLaunchConfigurationDelegate();

	/** Creates a new instance of SunAppServerBehaviour */
	public GlassFishServerBehaviour() {
		GlassfishToolsPlugin.logMessage("in SunAppServerBehaviour CTOR ");

	}

	protected void initialize(IProgressMonitor monitor) {
		super.initialize(monitor);
		GlassfishToolsPlugin.logMessage("in Behaviour initialize for " + getGlassfishServerDelegate().getName());
		final GlassFishServer sunserver = getGlassfishServerDelegate();
		
		statusMonitor = ServerStatusMonitor.getInstance(sunserver, this);
		statusMonitor.start();
	}

	
	@Override
	public void dispose() {
		super.dispose();
		statusMonitor.stop();
		GlassfishToolsPlugin.logMessage("in Behaviour dispose for " + getGlassfishServerDelegate().getName());
	}
	
	@Override
	public IStatus canRestart(String mode) {
		if( this.getGlassfishServerDelegate().isRemote() && !mode.equals( ILaunchManager.DEBUG_MODE))
			return new Status(IStatus.ERROR, GlassfishToolsPlugin.SYMBOLIC_NAME,"Restart remote Glassfish server is not supported");
		return super.canRestart(mode);
	}

	@Override
	public IStatus canStart(String launchMode) {
		if( this.getGlassfishServerDelegate().isRemote() )
			return new Status(IStatus.ERROR, GlassfishToolsPlugin.SYMBOLIC_NAME,"Start remote Glassfish server is not supported");
		return super.canStart(launchMode);
	}
	
	/**
     * @see org.eclipse.wst.server.core.model.ServerBehaviourDelegate#canStop()
     */
    public IStatus canStop() {
        if( !getGlassfishServerDelegate().isRemote() )
            return Status.OK_STATUS;
        else
        	return new Status(IStatus.ERROR, GlassfishToolsPlugin.SYMBOLIC_NAME, "Start remote Glassfish server is not supported");
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
	
	private void serverStateChanged(int serverState) {
		switch (serverState) {
		case IServer.STATE_STARTED:
			try {
				updateHttpPort();
				tryAttachDebug();
			} catch (HttpPortUpdateException e) {
				GlassfishToolsPlugin.logError("Unable to update HTTP port for server started"
						+ "outside of IDE!", e);
			}
			break;
		case IServer.STATE_STOPPED:
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
					if (launch != null ) {
						String mode = launch.getLaunchMode();
						if( mode.equals( ILaunchManager.DEBUG_MODE)){
							//config.launch(ILaunchManager.DEBUG_MODE, null);
						}
					}
				} catch (CoreException e) {
					GlassfishToolsPlugin.logMessage("Unable to attach debugger, running in normal mode");
				}
			}
			
		};
		
		t.start();
	}

	public ServerStatus getServerStatus(boolean forceUpdate) {
		return statusMonitor.getServerStatus(forceUpdate);
	}

	/*
	 * get the correct adapter for the GlassFish server
	 */
	public GlassFishServer getGlassfishServerDelegate() {
		// return (SunAppServer)getServer().getAdapter(SunAppServer.class);
		GlassFishServer sunserver = (GlassFishServer) getServer()
				.getAdapter(GlassFishServer.class);
		if (sunserver == null) {
			sunserver = (GlassFishServer) getServer().loadAdapter(
					GlassFishServer.class, new NullProgressMonitor());
		}
		return sunserver;
	}
	
	ResultProcess launchServer(StartupArgsImpl gfStartArguments, StartMode launchMode, 
			IProgressMonitor monitor) throws TimeoutException, InterruptedException, ExecutionException, HttpPortUpdateException {
		setGFServerState(IServer.STATE_STARTING);
		ResultProcess p = null;
		StartJob j = new StartJob(gfStartArguments, launchMode);
		Future<ResultProcess> res = asyncJobsService.submit(j);
		try {
			p = res.get(getServer().getStartTimeout(), TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			res.cancel(true);
			throw e;
		}
		updateHttpPort();
		return p;
	}
	
	/**
	 * Called to attempt to attach debugger to running glassfish.
	 * 
	 * @param launch
	 * @param config
	 * @param monitor
	 * @throws CoreException
	 */
	void attach(ILaunch launch, ILaunchConfigurationWorkingCopy config, IProgressMonitor monitor) throws CoreException {
		GlassFishServer serverDelegate = getGlassfishServerDelegate();
		int debugPort = serverDelegate.getDebugPort();
		debugPort = debugPort == -1 ? GlassFishServer.DEFAULT_DEBUG_PORT : debugPort;
		attach(launch, config, monitor, debugPort);
	}
	
	void attach(final ILaunch launch, ILaunchConfigurationWorkingCopy config, IProgressMonitor monitor, int debugPort) throws CoreException {
		setDebugArgument(config, "hostname", getServer().getHost());
		setDebugArgument(config, "port", String.valueOf(debugPort));
		REMOTE_JAVA_APP_LAUNCH_DELEGATE.launch(config, ILaunchManager.DEBUG_MODE, launch, ProgressUtil.getMonitorFor(monitor));
		
		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
			
			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				for( DebugEvent event : events ){
					//System.err.println( "Debugger element terminated " + event.getSource() );
					if( event.getKind() == DebugEvent.TERMINATE && event.getSource() instanceof JDIDebugTarget){
						JDIDebugTarget debugTarget = (JDIDebugTarget)event.getSource();
						if(debugTarget!=null && debugTarget.getLaunch().getLaunchConfiguration()==launch.getLaunchConfiguration()) {
							DebugPlugin.getDefault().removeDebugEventListener(this);
							((Server)getServer()).setMode( ILaunchManager.RUN_MODE);
							((Server)getServer()).setServerStatus(new Status(IStatus.OK, GlassfishToolsPlugin.SYMBOLIC_NAME,""));
							return;
						}
					}
				}
			}
		}); 		
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setDebugArgument(ILaunchConfigurationWorkingCopy config, String key, String arg) {
        try {
            Map args = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, (Map)null);
            
            if (args!=null)
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
	 * This is the only modification point of server state.
	 * 
	 * @param state
	 */
	protected synchronized void setGFServerState(int state) {
		//System.out.println("Setting server state " + state + " for server " + this.getServer().getName());
		setServerState(state);
	}

	public GlassFishRuntime getRuntimeDelegate() {
		return (GlassFishRuntime) getServer().getRuntime().loadAdapter(
				GlassFishRuntime.class, null);
	}

	public static String getVersion(GlassFishServer server) throws GlassFishIdeException {
		Command command = new CommandVersion();
		IdeContext ide = new IdeContext();
		Future<ResultString> future = ServerAdmin.exec(server,
				command, ide);
		try {
			ResultString result = future.get(30, TimeUnit.SECONDS);
			return result.getValue();
		} catch (InterruptedException e) {
			throw new GlassFishIdeException("Exception by calling getVersion",
					e);
		} catch (java.util.concurrent.ExecutionException e) {
			throw new GlassFishIdeException("Exception by calling getVersion",
					e);
		} catch (TimeoutException e) {
			throw new GlassFishIdeException(
					"Timeout for getting version command exceeded", e);
		}
	}
	
	@Override
	public void restart(final String launchMode) throws CoreException {
		if( this.getGlassfishServerDelegate().isRemote() && launchMode.equals( ILaunchManager.DEBUG_MODE))
			((Server)getServer()).setServerStatus(new Status(IStatus.OK, GlassfishToolsPlugin.SYMBOLIC_NAME, "Attaching to remote server..."));
				
		GlassfishToolsPlugin.logMessage("in GlassfishServerBehaviourDelegate restart");
		stopServer(false);
		Thread thread = new Thread("Synchronous server start") {
			public void run() {
				try {
					//setServerState(IServer.STATE_STARTING);
					// SunAppSrvPlugin.logMessage("in !!!!!!!SunAppServerBehaviour restart");
					getServer().getLaunchConfiguration(true, null).launch(launchMode, new NullProgressMonitor());
					//getServer().start(launchMode, new NullProgressMonitor());
					GlassfishToolsPlugin
							.logMessage("GlassfishServerBehaviourDelegate restart done");
					
				} catch (Exception e) {
					GlassfishToolsPlugin.logError(
							"in SunAppServerBehaviour restart", e);
				}
			}
		};
		
		thread.setDaemon(true);
		thread.start();

	}

	@Override
	public void stop(boolean force) {
		GlassfishToolsPlugin.logMessage("in GlassfishServerBehaviourDelegate stop");

		stopServer(true);
	}

	/**
	 * 
	 * @stop GlassFish v3 or v3 prelude via http command
	 */
	protected void stopServer(boolean stopLogging) {
		final GlassFishServer server = getGlassfishServerDelegate();
		//Shouldn't allow stop remote server
		if( server.isRemote() )
			return;
		stopImpl(server);
		if (stopLogging)
			GlassfishConsoleManager.getStandardConsole(server).stopLogging(3);
	}
	
	private void stopImpl(GlassFishServer server) {
		setGFServerState(IServer.STATE_STOPPING);
		StopJob j = new StopJob();
		Future<?> res = asyncJobsService.submit(j);
		// TODO how to let user know about possible failures
		try {
			res.get(getServer().getStopTimeout(), TimeUnit.SECONDS);
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

	protected String getConfigTypeID() {
		return GlassfishToolsPlugin.SYMBOLIC_NAME
				+ ".SunAppServerLaunchConfigurationType";
	}

	public void undeploy(String moduleName, IProgressMonitor monitor)
			throws CoreException {
		try {

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
			// if we were able to map module name to IModule, set publish state
			// to Full to tell
			// a full deploy would be needed
			if (module != null) {
				setModulePublishState(module, IServer.PUBLISH_STATE_FULL);
			}

		} finally {
		}
	}

	public void undeploy(String moduleName) throws CoreException {
		CommandUndeploy cmd = new CommandUndeploy(moduleName, null);
		try {
			Future<ResultString> future =
                    ServerAdmin.<ResultString>exec(getGlassfishServerDelegate(), cmd, new IdeContext());
                ResultString result = future.get(520, TimeUnit.SECONDS);
                if (!TaskState.COMPLETED.equals(result.getState())) {
                	GlassfishToolsPlugin.logMessage("undeploy is failing=" + result.getValue());
					throw new Exception("undeploy is failing=" + result.getValue());
                }
		} catch (Exception ex) {
			GlassfishToolsPlugin.logError("Undeploy is failing=", ex);
			throw new CoreException(new Status(IStatus.ERROR,
					GlassfishToolsPlugin.SYMBOLIC_NAME, 0, "cannot UnDeploy "
							+ moduleName, ex));
		}
	}

	/**
	 * Checks if the Ant publisher actually needs to publish. For ear modules it
	 * also checks if any of the children modules requires publishing.
	 * 
	 * @return true if ant publisher needs to publish.
	 */
	protected boolean publishNeeded(int kind, int deltaKind, IModule[] module) {
		if( this.getServer().getServerPublishState() == IServer.PUBLISH_CLEAN )
			return true;
		
		if (kind != IServer.PUBLISH_INCREMENTAL && kind != IServer.PUBLISH_AUTO)
			return true;
		if (deltaKind != ServerBehaviourDelegate.NO_CHANGE)
			return true;
		if( module[0] instanceof DeletedModule )
			return false;
		
		if (AssembleModules.isModuleType(module[0], "jst.ear")) { //$NON-NLS-1$
			IEnterpriseApplication earModule = (IEnterpriseApplication) module[0]
					.loadAdapter(IEnterpriseApplication.class,
							new NullProgressMonitor());
			IModule[] childModules = earModule.getModules();
			for (int i = 0; i < childModules.length; i++) {
				IModule m = childModules[i];
				IModule[] modules = { module[0], m };
				if (IServer.PUBLISH_STATE_NONE != getGlassfishServerDelegate().getServer()
						.getModulePublishState(modules))
					return true;
			}
		}else{
			int publishState = getGlassfishServerDelegate().getServer().getModulePublishState(module);
			if (IServer.PUBLISH_STATE_NONE != publishState )
				return true;
		}
		return false;
	}
	
	protected void publishDeployedDirectory(int deltaKind, Properties p,
			IModule module[], IProgressMonitor monitor) throws CoreException {
		// ludo using PublishHelper now to control the temp area to be
		// in the same file system of the deployed apps so that the mv operation
		// Eclipse is doing sometimes can work.
		PublishHelper helper = new PublishHelper(new Path(
				getGlassfishServerDelegate().getDomainPath() + "/eclipseAppsTmp").toFile());

		if (deltaKind == REMOVED) {
			String publishPath = (String) p.get(module[0].getId());
			GlassfishToolsPlugin.logMessage("REMOVED in publishPath" + publishPath);
			String name = Utils.simplifyModuleID(module[0].getName());
			try{
				undeploy(name);
			}catch(Exception e){
				//Bug 16876200 - UNABLE TO CLEAN GF SERVER INSTANCE
				//In case undeploy with asadmin failed,
				//catch the exception and try delete the app directory from server directly next 
			}

			if (publishPath != null) {
				try {
					File pub = new File(publishPath);
					if (pub.exists()) {
						GlassfishToolsPlugin
								.logMessage("PublishUtil.deleteDirectory called");
						IStatus[] stat = PublishHelper.deleteDirectory(pub,
								monitor);
						analyseReturnedStatus(stat);
					}
				} catch (Exception e) {
					throw new CoreException(new Status(IStatus.WARNING,
							GlassfishToolsPlugin.SYMBOLIC_NAME, 0, "cannot remove "
									+ module[0].getName(), e));
				}
			}
		} else {
			IPath path = new Path(getGlassfishServerDelegate().getDomainPath()
					+ "/eclipseApps/" + module[0].getName());

			// IModuleResource[] moduleResource = getResources(module);
			// SunAppSrvPlugin.logMessage("IModuleResource len="+moduleResource.length);
			// for (int j=0;j<moduleResource.length ;j++
			// SunAppSrvPlugin.logMessage("IModuleResource n="+moduleResource[j].getName()+"-----"+moduleResource[j].getModuleRelativePath());

			String contextRoot = null;
			AssembleModules assembler = new AssembleModules(module, path,
					getGlassfishServerDelegate(), helper);
			GlassfishToolsPlugin.logMessage("Deploy direcotry " + path.toFile().getAbsolutePath());

			if( module[0] instanceof DeletedModule )
				return;
			
			// either ear or web.
			if (AssembleModules.isModuleType(module[0], "jst.web")) {
				GlassfishToolsPlugin.logMessage("is WEB");
				assembler.assembleWebModule(monitor);

				needARedeploy = assembler.needsARedeployment();
				String projectContextRoot = ComponentUtilities
						.getServerContextRoot(module[0].getProject());
				contextRoot = (((projectContextRoot != null) && (projectContextRoot
						.length() > 0)) ? projectContextRoot : module[0]
						.getName());
			} else if (AssembleModules.isModuleType(module[0], "jst.ear")) {
				GlassfishToolsPlugin.logMessage("is EAR");
				assembler.assembleDirDeployedEARModule(monitor);
				needARedeploy = assembler.needsARedeployment();

			} else {// default
				assembler.assembleNonWebOrNonEARModule(monitor);
				needARedeploy = assembler.needsARedeployment();

			}

			// deploy the sun resource file if there in path:
			registerSunResource(module, p, path);

			String spath = "" + path;
			// /BUG NEED ALSO to test if it has been deployed
			// once...isDeployed()
			if (needARedeploy) {
				String name = Utils.simplifyModuleID(module[0].getName());

				Map<String, String> properties = new HashMap<String, String>();
				boolean keepSession =getGlassfishServerDelegate().getKeepSessions();
				String preserveSessionKey = getGlassfishServerDelegate().computePreserveSessions();
				if(preserveSessionKey!=null)
					properties.put(preserveSessionKey, Boolean.toString( getGlassfishServerDelegate().getKeepSessions()) );
				
				File[] libraries = new File[0];
				CommandTarget command = null;
				
				if( deltaKind == ADDED ){
					command = new CommandDeploy(name, null, new File(spath), contextRoot, properties, libraries);
				}else{
					command = new CommandRedeploy(name, null, contextRoot, properties, libraries, keepSession);
				}
				
				
				try {
					Future<ResultString> future =
		                    ServerAdmin.<ResultString>exec(getGlassfishServerDelegate(), command, new IdeContext());
		                ResultString result = future.get(300, TimeUnit.SECONDS);
		                if (!TaskState.COMPLETED.equals(result.getState())) {
		                	GlassfishToolsPlugin.logMessage("deploy is failing=" + result.getValue());
							throw new Exception("deploy is failing=" + result.getValue());
		                }
				} catch (Exception ex) {
					GlassfishToolsPlugin.logError("deploy is failing=", ex);
					throw new CoreException(new Status(IStatus.ERROR,
							GlassfishToolsPlugin.SYMBOLIC_NAME, 0, "cannot Deploy "
									+ name, ex));
				}
			} else {
				GlassfishToolsPlugin
						.logMessage("optimal: NO NEED TO TO A REDEPLOYMENT, !!!");

			}
		}
	}
	
	//public abstract ServerStatus getServerStatus();

	public void updateHttpPort() throws HttpPortUpdateException {
		GlassFishServer server = (GlassFishServer)getServer().createWorkingCopy().loadAdapter(GlassFishServer.class, null);	
		CommandGetProperty cgp = new CommandGetProperty("*.server-config.*.http-listener-1.port");
		Future<ResultMap<String, String>> future = ServerAdmin.<ResultMap<String, String>>exec(getGlassfishServerDelegate(), cgp, new IdeContext());
        ResultMap<String, String> result = null;
        try {
			result = future.get(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			GlassfishToolsPlugin.logError("Unable to retrieve server http port for server ", e);
			throw new HttpPortUpdateException(e);
		} catch (java.util.concurrent.ExecutionException e) {
			GlassfishToolsPlugin.logError("Unable to retrieve server http port for target ", e);
			throw new HttpPortUpdateException(e);
		} catch (TimeoutException e) {
			GlassfishToolsPlugin.logError("Unable to retrieve server http port for target ", e);
			throw new HttpPortUpdateException(e);
		}
        
        if ((result != null) && TaskState.COMPLETED.equals(result.getState())) {
        	for (Entry<String, String> entry : result.getValue().entrySet()) {
                String val = entry.getValue();
                try {
                    if (null != val && val.trim().length() > 0) {
                        server.setPort(Integer.parseInt(val));
                        server.getServerWorkingCopy().save(true, null);
                        break;
                    }
                } catch (NumberFormatException nfe) {
                	throw new HttpPortUpdateException(nfe);
                } catch (CoreException ce) {
                	throw new HttpPortUpdateException(ce);
                }
            }
        }
	}
	
	protected void registerSunResource(IModule module[], Properties p,
			IPath path) throws CoreException {
		// Get correct location for sun-resources.xml
		IProject project = module[0].getProject();
		String location = ResourceUtils.getRuntimeResourceLocation(project);
		if (location != null) {
			if (location.trim().length() > 0) {
				location = location + File.separatorChar
						+ ResourceUtils.RESOURCE_FILE_NAME;
			} else {
				location = ResourceUtils.RESOURCE_FILE_NAME;
			}
		}
		File sunResource = new File("" + path, location);
		if (sunResource.exists()) {
			ResourceUtils.checkUpdateServerResources(sunResource,
					getGlassfishServerDelegate());
			CommandAddResources command = new CommandAddResources(sunResource, null);
			try {
				Future<ResultString> future =
	                    ServerAdmin.<ResultString>exec(getGlassfishServerDelegate(), command, new IdeContext());
	                ResultString result = future.get(120, TimeUnit.SECONDS);
	                if (!TaskState.COMPLETED.equals(result.getState())) {
	                	GlassfishToolsPlugin.logMessage("register resource is failing=" + result.getValue());
						throw new Exception("register resource is failing=" + result.getValue());
	                }
			} catch (Exception ex) {
				GlassfishToolsPlugin.logError(
						"deploy of sun-resources is failing ", ex);
				throw new CoreException(new Status(IStatus.ERROR,
						GlassfishToolsPlugin.SYMBOLIC_NAME, 0,
						"cannot register sun-resource.xml for "
								+ module[0].getName(), ex));
			}
		}
		p.put(module[0].getId(), path.toOSString());

	}

	protected void analyseReturnedStatus(IStatus[] status) throws CoreException {

		if (status == null || status.length == 0) {
			return;
		}
		/*
		 * if (status.length == 1) { throw new CoreException(status[0]); }
		 * String message = "GlassFish: Error Deploying"; MultiStatus ms = new
		 * MultiStatus(SunAppSrvPlugin.SUNPLUGIN_ID, 0, status, message, null);
		 * throw new CoreException(ms);
		 */
		for (IStatus s : status) {
			GlassfishToolsPlugin.logMessage("analyseReturnedStatus: "
					+ s.getMessage());

		}
	}
	
	public void updateServerStatus(){
		ServerStatus status = getServerStatus(true);
		updateServerStatus(status);
	}
	
	/**
	 * Updates server status.
	 */
	private void updateServerStatus(ServerStatus status) {
		Server server2 = ((Server)getServer());
		if( status != ServerStatus.RUNNING_DOMAIN_MATCHING ){
			//server2.setServerState(IServer.STATE_STOPPED);
			String statusMsg = null;
			switch(status){
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
					if( server2.getServerState()!=IServer.STATE_STOPPED)
						statusMsg = Messages.connectionError;
					break;
				default:
					server2.setServerStatus(null);
					
			}
			
			if (statusMsg != null) {
				server2.setServerStatus(
						GlassfishToolsPlugin.createErrorStatus(statusMsg, null));//$NON-NLS-1$);
			}
			//server2.setServerState(IServer.STATE_STOPPED);
				
		} else {
			server2.setServerStatus(null);
		}
	}

	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		IModule[] modules = this.getServer().getModules();
        boolean allpublished = true;
        for (int i = 0; i < modules.length; i++) {
        	if (this.getServer().getModulePublishState(new IModule[]{modules[i]}) != IServer.PUBLISH_STATE_NONE)
                allpublished = false;
        }
        if (allpublished)
            setServerPublishState(IServer.PUBLISH_STATE_NONE);
	}

	
    public void publishModule(int kind, int deltaKind, IModule[] module,
            IProgressMonitor monitor) throws CoreException {

        // first, test if the server is still existing
        File serverloc = new File(getGlassfishServerDelegate()
                .getServerInstallationDirectory());
        if (!serverloc.exists()) {
            GlassfishToolsPlugin.logError(
                    NLS.bind(Messages.serverDirectoryGone,
                            serverloc.getAbsolutePath()), null);
            return;

        }

        needARedeploy = true; // by default

        long t = System.currentTimeMillis();
        if (module.length > 1) {// only publish root modules, i.e web modules
            setModulePublishState(module, IServer.PUBLISH_STATE_NONE);
        } else {
            publishModuleForGlassFishV3(kind, deltaKind, module, monitor);
            GlassfishToolsPlugin.logMessage("done publishModule in "
                    + (System.currentTimeMillis() - t) + " ms");
        }

    }
    
    /*
     * Publishes for Web apps only in V3 prelude
     */
    protected void publishModuleForGlassFishV3(int kind, int deltaKind,
            IModule[] module, IProgressMonitor monitor) throws CoreException {

        if (module.length > 1) {// only publish root modules, i.e web modules
            setModulePublishState(module, IServer.PUBLISH_STATE_NONE);
            return;
        }
        if (!publishNeeded(kind, deltaKind, module) || monitor.isCanceled()) {
            return;
        }
        IPath path = getTempDirectory().append("publish.txt");
        // SunAppSrvPlugin.logMessage("in PATH" +path +"module length======="
        // +module.length);

        FileInputStream fis = null;
        Properties prop = new Properties();
        try {
            fis = new FileInputStream(path.toFile());
            prop.load(fis);
        } catch (Exception e) {
        } finally {
            try {
                fis.close();
            } catch (Exception ex) {
            }
        }
        
        boolean isRemote = getGlassfishServerDelegate().isRemote();
        boolean isJarDeploy = getGlassfishServerDelegate().getJarDeploy(); 
        if ((! isRemote && ! isJarDeploy )) {
            publishDeployedDirectory(deltaKind, prop, module, monitor);
        } else {
            publishJarFile(kind, deltaKind, prop, module, monitor);

        }

        setModulePublishState(module, IServer.PUBLISH_STATE_NONE);
        FileOutputStream fos = null;
        try {
            prop.store(fos = new FileOutputStream(path.toFile()), "GlassFish 3");
        } catch (Exception e) {
            GlassfishToolsPlugin.logError(" error in PUBLISH_STATE_NONE", e);
        } finally {
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException e) {
                    // Auto-generated catch block
                }
        }

    }

    
    private void publishJarFile(int kind, int deltaKind, Properties p,
            IModule[] module, IProgressMonitor monitor) throws CoreException {
        // first try to see if we need to undeploy:

        if (deltaKind == ServerBehaviourDelegate.REMOVED) {
            // same logic as directory undeploy
            publishDeployedDirectory(deltaKind, p, module, monitor);

        } else {

            try {
                File archivePath = ExportJavaEEArchive.export(module[0], monitor);
                GlassfishToolsPlugin.logMessage("Deploy archive " + archivePath.getAbsolutePath());
                String name = Utils.simplifyModuleID(module[0].getName());
                String contextRoot = null;

                if (AssembleModules.isModuleType(module[0], "jst.web")) {
                    String projectContextRoot = ComponentUtilities
                            .getServerContextRoot(module[0].getProject());
                    contextRoot = (((projectContextRoot != null) && (projectContextRoot
                            .length() > 0)) ? projectContextRoot : module[0]
                            .getName());
                }
                Map<String, String> properties = new HashMap<String, String>();
                boolean keepSession =getGlassfishServerDelegate().getKeepSessions();
                String preserveSessionKey = getGlassfishServerDelegate().computePreserveSessions();
                if(preserveSessionKey!=null)
                    properties.put(preserveSessionKey, Boolean.toString( keepSession ) );
                
                
                File[] libraries = new File[0];

                // keepSession state is NOT supported in redeploy as JAR
                CommandTargetName command = null;
                command = new CommandDeploy(name, null, archivePath, contextRoot, properties, libraries);
                
                try {
                    Future<ResultString> future =
                            ServerAdmin.<ResultString>exec(getGlassfishServerDelegate(), command, new IdeContext());
                        ResultString result = future.get(520, TimeUnit.SECONDS);
                        if (!TaskState.COMPLETED.equals(result.getState())) {
                            GlassfishToolsPlugin.logMessage("deploy is failing=" + result.getValue());
                            throw new Exception("deploy is failing=" + result.getValue());
                        }
                } catch (Exception ex) {
                    GlassfishToolsPlugin.logError("deploy is failing=", ex);
                    throw new CoreException(new Status(IStatus.ERROR,
                            GlassfishToolsPlugin.SYMBOLIC_NAME, 0, "cannot Deploy "
                                    + name, ex));
                }
            } catch (org.eclipse.core.commands.ExecutionException e) {
                e.printStackTrace();
            }

        }
    }

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
				process = ServerTasks.startServer(getGlassfishServerDelegate(),
					args, mode);
			} catch (GlassFishIdeException e) {
				throw new GlassfishLaunchException("Exception in startup library.", e);
			}
			
			Process gfProcess = process.getValue().getProcess();
			// read process output to prevent process'es blocking
			IGlassFishConsole startupConsole = GlassfishConsoleManager
					.getStartupProcessConsole(getGlassfishServerDelegate(), gfProcess);
			startupConsole.startLogging(
					new FetchLogSimple(gfProcess.getInputStream()),
					new FetchLogSimple(gfProcess.getErrorStream()));

			synchronized (GlassFishServerBehaviour.this) {
				check_server_status:
				while (true) {
					ServerStatus status = getServerStatus(false);
					//System.out.println("Launch process got server status: " + status);
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
						throw new GlassfishLaunchException("BAD GATEWAY response code returned. Check your proxy settings. Killing startup process.", gfProcess);
					case RUNNING_CREDENTIAL_PROBLEM:
						startupConsole.stopLogging();
						gfProcess.destroy();
						throw new GlassfishLaunchException("Wrong user name or password. Killing startup process.", gfProcess);
					case RUNNING_DOMAIN_MATCHING:
						startupConsole.stopLogging();
						break check_server_status;
					default:
						break;

					}
					// wait for notification when server state changes
					try {
						//System.out.println("Entering wait");
						// limit waiting so we can check process exit code again
						GlassFishServerBehaviour.this.wait(5000);
						//System.out.println("Wait exited");
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
			Server server2 = ((Server)getServer());
			server2.setServerStatus(null);
			return null;
		}
		
	}
}
