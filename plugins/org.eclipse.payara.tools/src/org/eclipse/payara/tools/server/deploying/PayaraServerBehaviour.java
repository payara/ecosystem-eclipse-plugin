/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.server.deploying;

import static java.io.File.separatorChar;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.OK;
import static org.eclipse.core.runtime.IStatus.WARNING;
import static org.eclipse.debug.core.DebugEvent.TERMINATE;
import static org.eclipse.debug.core.ILaunchManager.DEBUG_MODE;
import static org.eclipse.debug.core.ILaunchManager.RUN_MODE;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP;
import static org.eclipse.osgi.util.NLS.bind;
import static org.eclipse.payara.tools.Messages.connectionError;
import static org.eclipse.payara.tools.Messages.invalidCredentials;
import static org.eclipse.payara.tools.Messages.serverDirectoryGone;
import static org.eclipse.payara.tools.Messages.serverNotMatchingLocal;
import static org.eclipse.payara.tools.Messages.serverNotMatchingRemote;
import static org.eclipse.payara.tools.PayaraToolsPlugin.SYMBOLIC_NAME;
import static org.eclipse.payara.tools.PayaraToolsPlugin.createErrorStatus;
import static org.eclipse.payara.tools.PayaraToolsPlugin.logError;
import static org.eclipse.payara.tools.PayaraToolsPlugin.logMessage;
import static org.eclipse.payara.tools.log.PayaraConsoleManager.getStandardConsole;
import static org.eclipse.payara.tools.sdk.TaskState.COMPLETED;
import static org.eclipse.payara.tools.sdk.admin.CommandStopDAS.stopDAS;
import static org.eclipse.payara.tools.sdk.admin.ServerAdmin.exec;
import static org.eclipse.payara.tools.server.PayaraServer.DEFAULT_DEBUG_PORT;
import static org.eclipse.payara.tools.server.ServerStatus.RUNNING_DOMAIN_MATCHING;
import static org.eclipse.payara.tools.server.ServerStatus.STOPPED_NOT_LISTENING;
import static org.eclipse.payara.tools.server.archives.AssembleModules.isModuleType;
import static org.eclipse.payara.tools.server.archives.ExportJavaEEArchive.export;
import static org.eclipse.payara.tools.utils.ResourceUtils.RESOURCE_FILE_NAME;
import static org.eclipse.payara.tools.utils.ResourceUtils.checkUpdateServerResources;
import static org.eclipse.payara.tools.utils.Utils.isEmpty;
import static org.eclipse.payara.tools.utils.Utils.simplifyModuleID;
import static org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities.getServerContextRoot;
import static org.eclipse.wst.server.core.IServer.PUBLISH_AUTO;
import static org.eclipse.wst.server.core.IServer.PUBLISH_CLEAN;
import static org.eclipse.wst.server.core.IServer.PUBLISH_FULL;
import static org.eclipse.wst.server.core.IServer.PUBLISH_INCREMENTAL;
import static org.eclipse.wst.server.core.IServer.PUBLISH_STATE_FULL;
import static org.eclipse.wst.server.core.IServer.PUBLISH_STATE_NONE;
import static org.eclipse.wst.server.core.IServer.STATE_STARTED;
import static org.eclipse.wst.server.core.IServer.STATE_STARTING;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPED;
import static org.eclipse.wst.server.core.IServer.STATE_STOPPING;
import static org.eclipse.wst.server.core.internal.ProgressUtil.getMonitorFor;
import static org.eclipse.wst.server.core.util.PublishHelper.deleteDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.launching.JavaRemoteApplicationLaunchConfigurationDelegate;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.payara.tools.exceptions.HttpPortUpdateException;
import org.eclipse.payara.tools.internal.PayaraStateResolver;
import org.eclipse.payara.tools.internal.ServerStateListener;
import org.eclipse.payara.tools.internal.ServerStatusMonitor;
import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.sdk.admin.CommandAddResources;
import org.eclipse.payara.tools.sdk.admin.CommandDeploy;
import org.eclipse.payara.tools.sdk.admin.CommandGetProperty;
import org.eclipse.payara.tools.sdk.admin.CommandRedeploy;
import org.eclipse.payara.tools.sdk.admin.CommandTarget;
import org.eclipse.payara.tools.sdk.admin.CommandUndeploy;
import org.eclipse.payara.tools.sdk.admin.CommandVersion;
import org.eclipse.payara.tools.sdk.admin.ResultMap;
import org.eclipse.payara.tools.sdk.admin.ResultProcess;
import org.eclipse.payara.tools.sdk.admin.ResultString;
import org.eclipse.payara.tools.sdk.admin.ServerAdmin;
import org.eclipse.payara.tools.sdk.server.ServerTasks.StartMode;
import org.eclipse.payara.tools.server.PayaraRuntime;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.payara.tools.server.ServerStatus;
import org.eclipse.payara.tools.server.archives.AssembleModules;
import org.eclipse.payara.tools.server.starting.PayaraServerLaunchDelegate;
import org.eclipse.payara.tools.server.starting.PayaraStartJob;
import org.eclipse.payara.tools.server.starting.StartupArgsImpl;
import org.eclipse.payara.tools.utils.ResourceUtils;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.PublishHelper;

/**
 * This behavior class is called back by WTP to perform the standard operations on the Payara /
 * GlassFish server such as deploy/undeploy etc. Starting/restarting is delegated to
 * {@link PayaraServerLaunchDelegate}.
 *
 * <p>
 * This class is registered in <code>plug-in.xml</code> in the
 * <code>org.eclipse.wst.server.core.serverTypes</code> extension point.
 * </p>
 *
 */
@SuppressWarnings("restriction")
public final class PayaraServerBehaviour extends ServerBehaviourDelegate implements ServerStateListener {
    
    private static final IStatus OK_STATUS = new Status(OK, SYMBOLIC_NAME, "");

    // initialized
    protected boolean needARedeploy = true; // by default, will be calculated..

    private PayaraStateResolver stateResolver = new PayaraStateResolver();

    private ServerStatusMonitor statusMonitor;

    private static final ExecutorService asyncJobsService = Executors.newCachedThreadPool();

    private static JavaRemoteApplicationLaunchConfigurationDelegate REMOTE_JAVA_APP_LAUNCH_DELEGATE = new JavaRemoteApplicationLaunchConfigurationDelegate();

    public PayaraServerBehaviour() {
        logMessage("in PayaraServerBehaviour CTOR ");
    }

    @Override
    protected void initialize(IProgressMonitor monitor) {
        super.initialize(monitor);
        logMessage("in Behaviour initialize for " + getPayaraServerDelegate().getName());

        statusMonitor = ServerStatusMonitor.getInstance(getPayaraServerDelegate(), this);
        statusMonitor.start();
    }

    // ### Life-cycle methods called by Eclipse WTP

    @Override
    public IStatus canStart(String launchMode) {
        if (getPayaraServerDelegate().isRemote()) {
            return new Status(ERROR, SYMBOLIC_NAME, "Start remote Glassfish server is not supported");
        }

        return super.canStart(launchMode);
    }
    
    @Override
    public boolean canRestartModule(IModule[] module) {
        // Holds for both start, stop and restart
        return true;
    }

    @Override
    public void stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
        if (getServer().getModuleState(module) != STATE_STARTED) {
            // Nothing to stop at this moment
            return;
        }
        undeploy(module);
    }
    
    @Override
    public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
        publishModule(PUBLISH_FULL, module, ADDED, monitor);
    }
    
    @Override
    public IStatus canRestart(String mode) {
        if (getPayaraServerDelegate().isRemote() && !mode.equals(DEBUG_MODE)) {
            return new Status(ERROR, SYMBOLIC_NAME, "Restart remote Glassfish server is not supported");
        }

        return super.canRestart(mode);
    }

    @Override
    public void restart(final String launchMode) throws CoreException {
        if (getPayaraServerDelegate().isRemote() && launchMode.equals(DEBUG_MODE)) {
            ((Server) getServer()).setServerStatus(
                    new Status(OK, SYMBOLIC_NAME, "Attaching to remote server..."));
        }

        logMessage("in PayaraServerBehaviourDelegate restart");
        stopServer(false);

        Thread thread = new Thread("Synchronous server start") {
            @Override
            public void run() {
                try {
                    getServer().getLaunchConfiguration(true, null).launch(launchMode, new NullProgressMonitor());
                    logMessage("PayaraServerBehaviourDelegate restart done");

                } catch (Exception e) {
                    logError("in PayaraServerBehaviourDelegate restart", e);
                }
            }
        };

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {

        // First, test if the server still exists
        File serverloc = new File(getPayaraServerDelegate().getServerInstallationDirectory());
        if (!serverloc.exists()) {
            logError(bind(serverDirectoryGone, serverloc.getAbsolutePath()), null);
            return;
        }

        long publishStartTime = System.currentTimeMillis();
        
        publishModuleForPayara(kind, deltaKind, module, monitor);
        
        logMessage("done publishModule in " + (System.currentTimeMillis() - publishStartTime) + " ms");
    }

    @Override
    public void serverStatusChanged(ServerStatus newStatus) {
        synchronized (this) {
            int currentState = getServer().getServerState();
            int nextState = stateResolver.resolve(newStatus, currentState);
            
            if (currentState != nextState) {
                setPayaraServerState(nextState);
                serverStateChanged(nextState);
                updateServerStatus(newStatus);
            }
            
            notify();
        }
    }

    @Override
    protected void publishFinish(IProgressMonitor monitor) throws CoreException {
        boolean allpublished = true;
        
        for (IModule module : getServer().getModules()) {
            if (getServer().getModulePublishState(new IModule[] { module }) != PUBLISH_STATE_NONE) {
                allpublished = false;
            }
        }

        if (allpublished) {
            setServerPublishState(PUBLISH_STATE_NONE);
        }
    }

    @Override
    public IStatus canStop() {
        if (!getPayaraServerDelegate().isRemote()) {
            return OK_STATUS;
        }

        return new Status(ERROR, SYMBOLIC_NAME, "Start remote Glassfish server is not supported");
    }

    @Override
    public void stop(boolean force) {
        logMessage("in PayaraServerBehaviourDelegate stop");
        stopServer(true);
    }

    @Override
    public void dispose() {
        super.dispose();
        statusMonitor.stop();
        logMessage("in Behaviour dispose for " + getPayaraServerDelegate().getName());
    }
    

    // #### API for external callers

    /*
     * get the correct adapter for the Payara server
     */
    public PayaraServer getPayaraServerDelegate() {
        PayaraServer payaraServer = getServer().getAdapter(PayaraServer.class);
        if (payaraServer == null) {
            payaraServer = (PayaraServer) getServer().loadAdapter(PayaraServer.class, new NullProgressMonitor());
        }

        return payaraServer;
    }

    public ServerStatus getServerStatus(boolean forceUpdate) {
        return statusMonitor.getServerStatus(forceUpdate);
    }

    public static String getVersion(PayaraServer server) throws PayaraIdeException {
        Future<ResultString> future = ServerAdmin.exec(server, new CommandVersion());
        
        try {
            return future.get(30, SECONDS).getValue();
        } catch (InterruptedException | ExecutionException e) {
            throw new PayaraIdeException("Exception by calling getVersion", e);
        } catch (TimeoutException e) {
            throw new PayaraIdeException("Timeout for getting version command exceeded", e);
        }
    }

    public void updateServerStatus() {
        updateServerStatus(getServerStatus(true));
    }
    
    public String getModuleDeployPath(IModule module) {
    	return (String) loadPublishProperties().get(module.getId());
    }

    public void undeploy(String moduleName, IProgressMonitor monitor) throws CoreException {
        undeploy(moduleName);

        // Retrieve the Module for the module name we want to undeploy
        IModule[] undeployModule = null;
        for (IModule[] module : getAllModules()) {
            if (module.length == 1 && module[0].getName().equals(moduleName)) {
                undeployModule = module;
                break;
            }
        }

        // If we were able to map module name to IModule, set publish state
        // to Full to tell a full deploy would be needed
        if (undeployModule != null) {
            setModulePublishState(undeployModule, PUBLISH_STATE_FULL);
        }
    }

    public PayaraRuntime getRuntimeDelegate() {
        return (PayaraRuntime) getServer().getRuntime().loadAdapter(PayaraRuntime.class, null);
    }

    /**
     * This is the only modification point of server state.
     *
     * @param state
     */
    public synchronized void setPayaraServerState(int state) {
        setServerState(state);
    }
    
    /**
     * Sets the server status.
     *
     * @param status the status of the server
     */
    public void setPayaraServerStatus(IStatus status) {
        setServerStatus(status);
    }
    
    /**
     * Sets the server publish state.
     *
     * @param state the publish state of the server
     */
    public void setPayaraServerPublishState(int state) {
        setServerPublishState(state);
    }
    
    /**
     * Sets the server mode.
     *
     * @param mode the mode of the server
     */
    public void setPayaraServerMode(String mode) {
        setMode(mode);
    }

    /**
     * Called to attempt to attach debugger to running Payara.
     *
     * @param launch
     * @param config
     * @param monitor
     * @throws CoreException
     */
    public void attach(ILaunch launch, ILaunchConfigurationWorkingCopy config, IProgressMonitor monitor) throws CoreException {
        int debugPort = getPayaraServerDelegate().getDebugPort();
        
        attach(launch, config, monitor, debugPort == -1 ? DEFAULT_DEBUG_PORT : debugPort);
    }

    public void attach(ILaunch launch, ILaunchConfigurationWorkingCopy config, IProgressMonitor monitor, int debugPort) throws CoreException {
        setDebugArgument(config, "hostname", getServer().getHost());
        setDebugArgument(config, "port", String.valueOf(debugPort));

        REMOTE_JAVA_APP_LAUNCH_DELEGATE.launch(config, DEBUG_MODE, launch, getMonitorFor(monitor));

        DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {

            @Override
            public void handleDebugEvents(DebugEvent[] events) {
                for (DebugEvent event : events) {
                    if (event.getKind() == TERMINATE && event.getSource() instanceof JDIDebugTarget) {
                        JDIDebugTarget debugTarget = (JDIDebugTarget) event.getSource();
                        
                        if (debugTarget == null) {
                            continue;
                        }
                        
                        if (debugTarget.getLaunch().getLaunchConfiguration() == launch.getLaunchConfiguration()) {
                            
                            DebugPlugin.getDefault().removeDebugEventListener(this);
                            
                            setPayaraServerMode(RUN_MODE);
                            setPayaraServerStatus(OK_STATUS);

                            return;
                        }
                    }
                }
            }
        });
    }

    // #### Implementation of life-cycle methods defined above

    public void serverStateChanged(int serverState) {
        switch (serverState) {
            case STATE_STARTED:
                try {
                    updateHttpPort();
                } catch (HttpPortUpdateException e) {
                    logError("Unable to update HTTP port for server started outside of IDE!", e);
                }
                break;
            case STATE_STOPPED:
                setLaunch(null);
                break;
            default:
                break;
            }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void setDebugArgument(ILaunchConfigurationWorkingCopy config, String key, String arg) {
        try {
            Map<String, String> args = config.getAttribute(ATTR_CONNECT_MAP, (Map) null);

            if (args != null) {
                args = new HashMap<>(args);
            } else {
                args = new HashMap<>();
            }
            args.put(key, String.valueOf(arg));

            config.setAttribute(ATTR_CONNECT_MAP, args);
        } catch (CoreException ce) {
            logError("Error when setting debug argument for remote Payara", ce);
        }
    }

    /**
     * Checks if the Ant publisher actually needs to publish. For ear modules it also checks if any of
     * the children modules requires publishing.
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
            
            for (IModule m : earModule.getModules()) {
                IModule[] modules = { module[0], m };
                if (PUBLISH_STATE_NONE != getPayaraServerDelegate().getServer().getModulePublishState(modules)) {
                    return true;
                }
            }
        } else if (PUBLISH_STATE_NONE != getPayaraServerDelegate().getServer().getModulePublishState(module)) {
            return true;
        }
        
        return false;
    }

    private void publishModuleForPayara(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
        if (module.length > 1) {// only publish root modules, i.e web modules
            setModulePublishState(module, PUBLISH_STATE_NONE);
            return;
        }

        if (!publishNeeded(kind, deltaKind, module) || monitor.isCanceled()) {
            return;
        }

        Properties publishProperties = loadPublishProperties();

        boolean isRemote = getPayaraServerDelegate().isRemote();
        boolean isJarDeploy = getPayaraServerDelegate().getJarDeploy();
        
        if ((!isRemote && !isJarDeploy)) {
            publishDeployedDirectory(kind, deltaKind, publishProperties, module, monitor);
        } else {
            publishJarFile(kind, deltaKind, publishProperties, module, monitor);

        }

        setModulePublishState(module, PUBLISH_STATE_NONE);
        savePublishProperties(publishProperties);
    }
    
    private Properties loadPublishProperties() {
    	Properties publishProperties = new Properties();
        
        try (FileInputStream fis = new FileInputStream(getPublishPropertiesFile())) {
            publishProperties.load(fis);
        } catch (Exception e) {
            // Ignore
        }
        
        return publishProperties;
    }
    
    private void savePublishProperties(Properties publishProperties) {
        try (FileOutputStream fos = new FileOutputStream(getPublishPropertiesFile())) {
            publishProperties.store(fos, "GlassFish 3");
        } catch (Exception e) {
            logError("Error in PUBLISH_STATE_NONE", e);
        }
    }
    
    private File getPublishPropertiesFile() {
        return getTempDirectory().append("publish.txt").toFile();
    }

    private void publishDeployedDirectory(int kind, int deltaKind, Properties publishProperties, IModule module[], IProgressMonitor monitor) throws CoreException {
      
        if (deltaKind == REMOVED) {
        	
        	// Undeploy
        	
            String publishPath = (String) publishProperties.get(module[0].getId());
            logMessage("REMOVED in publishPath" + publishPath);
            
            try {
                undeploy(module);
            } catch (Exception e) {
                // Bug 16876200 - UNABLE TO CLEAN PAYARA SERVER INSTANCE
                // In case undeploy with asadmin failed, catch the exception and 
            	// try delete the app directory from server directly next
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
        	
        	// Deploy
        	
        	if (module[0] instanceof DeletedModule) {
                return;
            }
        	
            IPath path = new Path(getPayaraServerDelegate().getDomainPath() + "/eclipseApps/" + module[0].getName());
            
            // Using PublishHelper to control the temp area to be in the same file system of the deployed apps 
            // so that the move operation Eclipse is doing sometimes can work.
            PublishHelper helper = new PublishHelper(
                new Path(getPayaraServerDelegate().getDomainPath() + "/eclipseAppsTmp").toFile());

            
            AssembleModules assembler = new AssembleModules(module, path, getPayaraServerDelegate(), helper);
            logMessage("Deploy direcotry " + path.toFile().getAbsolutePath());
            
            String contextRoot = null;

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
            
            if (kind == PUBLISH_INCREMENTAL || kind == PUBLISH_AUTO) {
                needARedeploy = assembler.needsARedeployment();
            } else {
                needARedeploy = true;
            }

            // deploy the sun resource file if there is one in path:
            registerSunResource(module, publishProperties, path);

            // BUG NEED ALSO to test if it has been deployed
            // once...isDeployed()
            if (needARedeploy) {

                String name = simplifyModuleID(module[0].getName());
                Map<String, String> properties = getDeploymentProperties();
                boolean keepSession = getPayaraServerDelegate().getKeepSessions();

                CommandTarget command = null;
                if (deltaKind == ADDED) {
                    command = new CommandDeploy(name, null, new File("" + path), contextRoot, properties, new File[0]);
                } else {
                    command = new CommandRedeploy(name, null, contextRoot, properties, new File[0], keepSession);
                }

                try {
                    ServerAdmin.executeOn(getPayaraServerDelegate())
                            .command(command)
                            .onNotCompleted(result -> {
                                logMessage("deploy is failing=" + result.getValue());
                                throw new IllegalStateException("deploy is failing=" + result.getValue());
                            })
                            .get();
                    
                    setModuleState(module, STATE_STARTED);

                } catch (Exception ex) {
                    setModuleState(module, STATE_STOPPED);
                    logError("deploy is failing=", ex);
                    throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, 0, "cannot Deploy " + name, ex));
                }
            } else {
                logMessage("optimal: NO NEED TO DO A REDEPLOYMENT, !!!");
            }
        }
    }

    private void publishJarFile(int kind, int deltaKind, Properties p, IModule[] module, IProgressMonitor monitor) throws CoreException {
        // first try to see if we need to undeploy:

        if (deltaKind == REMOVED) {

            // Same logic as directory undeploy
            publishDeployedDirectory(kind, deltaKind, p, module, monitor);

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
                    ServerAdmin.executeOn(getPayaraServerDelegate())
                            .command(new CommandDeploy(name, null, archivePath, contextRoot, getDeploymentProperties(), new File[0]))
                            .timeout(520)
                            .onNotCompleted(result -> {
                                logMessage("deploy is failing=" + result.getValue());
                                throw new IllegalStateException("deploy is failing=" + result.getValue());
                            })
                            .get();

                } catch (Exception ex) {
                    logError("deploy is failing=", ex);
                    throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, 0, "cannot Deploy " + name, ex));
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
            checkUpdateServerResources(sunResource, getPayaraServerDelegate());
            try {
                Future<ResultString> future = ServerAdmin.<ResultString>exec(getPayaraServerDelegate(),
                        new CommandAddResources(sunResource, null));
                ResultString result = future.get(120, SECONDS);

                if (!COMPLETED.equals(result.getState())) {
                    throw new Exception("register resource is failing=" + result.getValue());
                }
            } catch (Exception ex) {
                throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, 0,
                        "cannot register sun-resource.xml for " + module[0].getName(), ex));
            }
        }

        properties.put(module[0].getId(), path.toOSString());
    }

    private void undeploy(IModule module[]) throws CoreException {
        setModuleState(module, STATE_STOPPING);
        undeploy(simplifyModuleID(module[0].getName()));
        setModuleState(module, STATE_STOPPED);
    }
    
    private void undeploy(String moduleName) throws CoreException {
        try {
            ServerAdmin.executeOn(getPayaraServerDelegate())
                       .command(new CommandUndeploy(moduleName))
                       .timeout(520)
                       .onNotCompleted(result -> {
                           throw new IllegalStateException("undeploy is failing=" + result.getValue());
                        })
                       .get();
        } catch (Exception ex) {
            throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, 0, "cannot UnDeploy " + moduleName, ex));
        }
    }

    public void updateHttpPort() throws HttpPortUpdateException {
        PayaraServer server = (PayaraServer) getServer().createWorkingCopy().loadAdapter(PayaraServer.class, null);
        
        Future<ResultMap<String, String>> future = exec(getPayaraServerDelegate(), new CommandGetProperty("*.server-config.*.http-listener-1.port"));
        
        ResultMap<String, String> result = null;

        try {
            result = future.get(20, SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            
            throw new HttpPortUpdateException(e);
        }

        if (result != null && COMPLETED.equals(result.getState())) {
            for (Entry<String, String> entry : result.getValue().entrySet()) {
                String val = entry.getValue();
                try {
                    if (val != null && !val.trim().isEmpty()) {
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

        return !isEmpty(projectContextRoot) ? projectContextRoot : module[0].getName();
    }

    private Map<String, String> getDeploymentProperties() {
        Map<String, String> properties = new HashMap<>();

        String preserveSessionKey = getPayaraServerDelegate().computePreserveSessions();
        if (preserveSessionKey != null) {
            properties.put(preserveSessionKey, Boolean.toString(getPayaraServerDelegate().getKeepSessions()));
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
        Server server = ((Server) getServer());
        
        if (status != RUNNING_DOMAIN_MATCHING) {
            String statusMsg = null;
            
            switch (status) {
            case RUNNING_CREDENTIAL_PROBLEM:
                statusMsg = invalidCredentials;
                break;
            case STOPPED_DOMAIN_NOT_MATCHING:
                if (!getPayaraServerDelegate().isRemote()) {
                    statusMsg = serverNotMatchingLocal;
                } else {
                    statusMsg = serverNotMatchingRemote;
                }
                break;
            case RUNNING_CONNECTION_ERROR:
                if (server.getServerState() != STATE_STOPPED) {
                    statusMsg = connectionError;
                }
                break;
            default:
                server.setServerStatus(null);
            }

            if (statusMsg != null) {
                server.setServerStatus(createErrorStatus(statusMsg));
            }

        } else {
            server.setServerStatus(null);
        }
    }

    /**
     *
     * @stop GlassFish v3 or v3 prelude via http command
     */
    private void stopServer(boolean stopLogging) {
        PayaraServer server = getPayaraServerDelegate();

        // Shouldn't allow stop remote server
        if (server.isRemote()) {
            return;
        }

        stopImpl(server);

        if (stopLogging) {
            getStandardConsole(server).stopLogging(3);
        }
    }

    private void stopImpl(PayaraServer server) {
        setPayaraServerState(STATE_STOPPING);
        
        Future<ResultString> futureStop = asyncJobsService.submit(new StopJob());

        // TODO how to let user know about possible failures
        try {
            futureStop.get(getServer().getStopTimeout(), SECONDS);
            setPayaraServerState(STATE_STOPPED);
        } catch (InterruptedException e) {
            currentThread().interrupt();
            e.printStackTrace();
        } catch (ExecutionException e) {
            logError("Stop server could not be finished because of exception.", e);
        } catch (TimeoutException e1) {
            futureStop.cancel(true);
            logMessage("Stop server could not be finished in time.");
        }

        setLaunch(null);
    }
    

    // ### Jobs

    
    private class StopJob implements Callable<ResultString> {

        @Override
        public ResultString call() throws Exception {
            ResultString result = stopDAS(getPayaraServerDelegate());

            // Check if server is stopped
            if (!COMPLETED.equals(result.getState())) {
                throw new Exception("Stop call failed. Reason: " + result.getValue());
            }
            
            // Check if server is *really* stopped
            while (!getServerStatus(true).equals(STOPPED_NOT_LISTENING)) {
                Thread.sleep(100);
            }
            
            ((Server) getServer()).setServerStatus(null);
            
            return result;
        }

    }
}
