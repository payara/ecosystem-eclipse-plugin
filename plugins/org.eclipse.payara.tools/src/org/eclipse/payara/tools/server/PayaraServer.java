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

package org.eclipse.payara.tools.server;

import static java.io.File.separator;
import static java.net.URLEncoder.encode;
import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities.getServerContextRoot;
import static org.eclipse.jst.server.core.FacetUtil.getRuntime;
import static org.eclipse.jst.server.core.internal.J2EEUtil.getEnterpriseApplications;
import static org.eclipse.osgi.util.NLS.bind;
import static org.eclipse.payara.tools.Messages.facetNotSupported;
import static org.eclipse.payara.tools.Messages.invalidPortNumbers;
import static org.eclipse.payara.tools.Messages.pathDoesNotExist;
import static org.eclipse.payara.tools.Messages.pathNotDirectory;
import static org.eclipse.payara.tools.Messages.pathNotValidDomain;
import static org.eclipse.payara.tools.Messages.pathNotWritable;
import static org.eclipse.payara.tools.Messages.serverWithSameDomainPathExisting;
import static org.eclipse.payara.tools.PayaraToolsPlugin.SYMBOLIC_NAME;
import static org.eclipse.payara.tools.PayaraToolsPlugin.createErrorStatus;
import static org.eclipse.payara.tools.PayaraToolsPlugin.logError;
import static org.eclipse.payara.tools.PayaraToolsPlugin.logMessage;
import static org.eclipse.payara.tools.facets.internal.GlassfishDeploymentDescriptorFactory.getWebDeploymentDescriptor;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_ATTACH_DEBUGGER_DEFAULT;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_ATTACH_DEBUGGER_EARLY;
import static org.eclipse.payara.tools.sdk.server.parser.TreeParser.readXml;
import static org.eclipse.payara.tools.utils.ModuleUtil.isEARModule;
import static org.eclipse.payara.tools.utils.ModuleUtil.isEJBModule;
import static org.eclipse.payara.tools.utils.ModuleUtil.isWebModule;
import static org.eclipse.payara.tools.utils.PayaraLocationUtils.find;
import static org.eclipse.payara.tools.utils.Utils.canWrite;
import static org.eclipse.payara.tools.utils.Utils.getAppWebContextRoot;
import static org.eclipse.payara.tools.utils.Utils.getHttpListenerProtocol;
import static org.eclipse.payara.tools.utils.Utils.hasProjectFacet;
import static org.eclipse.payara.tools.utils.WtpUtil.load;
import static org.eclipse.wst.common.componentcore.internal.util.IModuleConstants.JST_WEB_MODULE;
import static org.eclipse.wst.common.project.facet.core.ProjectFacetsManager.getProjectFacet;
import static org.eclipse.wst.server.core.ServerUtil.getModules;
import static org.eclipse.wst.server.core.util.SocketUtil.isLocalhost;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.payara.tools.Messages;
import org.eclipse.payara.tools.sapphire.IPayaraServerModel;
import org.eclipse.payara.tools.sapphire.PayaraServerModelWorkingCopyAdapter;
import org.eclipse.payara.tools.sdk.data.GlassFishAdminInterface;
import org.eclipse.payara.tools.sdk.server.parser.HttpData;
import org.eclipse.payara.tools.sdk.server.parser.HttpListenerReader;
import org.eclipse.payara.tools.sdk.server.parser.NetworkListenerReader;
import org.eclipse.payara.tools.sdk.server.parser.TargetConfigNameReader;
import org.eclipse.payara.tools.server.deploying.PayaraServerBehaviour;
import org.eclipse.payara.tools.utils.PayaraLocationUtils;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.internal.IMonitoredServerPort;
import org.eclipse.wst.server.core.internal.IServerMonitorManager;
import org.eclipse.wst.server.core.internal.ServerMonitorManager;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.core.model.ServerDelegate;

/**
 * This class represents the specific type of server that we implement; Payara / GlassFish.
 *
 * <p>
 * A few methods from ServerDelegate are overridden here, while a whole slew of central server like
 * functionality is put here as well.
 * </p>
 *
 * <p>
 * This class is registered in <code>plug-in.xml</code> in the
 * <code>org.eclipse.wst.server.core.serverTypes</code> extension point.
 * </p>
 *
 */
@SuppressWarnings("restriction")
public final class PayaraServer extends ServerDelegate implements IURLProvider {

    public static final String TYPE_ID = "payara.server";
    public static final IServerType TYPE = ServerCore.findServerType(TYPE_ID);

    private static final String DEFAULT_SERVER_DIR_NAME = "glassfish"; // $NON-NLS-N$
    private static final String DEFAULT_DOMAIN_DIR_NAME = "domains"; // $NON-NLS-N$
    private static final String DEFAULT_DOMAIN_NAME = "domain1"; // $NON-NLS-N$
    public static final int DEFAULT_DEBUG_PORT = 9009;
    public static final String ATTR_SERVERPORT = "glassfish.serverportnumber"; //$NON-NLS-1$
    public static final String ATTR_ADMINPORT = "glassfish.adminserverportnumber"; //$NON-NLS-1$
    public static final String ATTR_DEBUG_PORT = "glassfish.debugport";
    public static final String ATTR_USECUSTOMTARGET = "glassfish.usecustomtarget";
    public static final String ATTR_DOMAINPATH = "glassfish.domainpath"; //$NON-NLS-1$
    public static final String ATTR_ADMIN = "glassfish.adminname"; //$NON-NLS-1$
    public static final String ATTR_ADMINPASS = "glassfish.adminpassword"; //$NON-NLS-1$
    public static final String ATTR_KEEPSESSIONS = "glassfish.keepSessions"; //$NON-NLS-1$
    public static final String ATTR_JARDEPLOY = "glassfish.jarDeploy"; //$NON-NLS-1$
    public static final String ATTR_USEANONYMOUSCONNECTIONS = "glassfish.useAnonymousConnection"; //$NON-NLS-1$

    private List<PropertyChangeListener> propChangeListeners;

    private IPayaraServerModel model;

    // #### ServerDelegate overridden methods

    @Override
    protected void initialize() {
        logMessage("in PayaraServer initialize" + this.getServer().getName()); //$NON-NLS-1$

        if (getServerWorkingCopy() != null) {
            readDomainConfig();
        }

        model = IPayaraServerModel.TYPE.instantiate(new PayaraServerModelWorkingCopyAdapter(getServerWorkingCopy()));
    }

    /**
     * @see org.eclipse.wst.server.core.model.ServerDelegate#modifyModules(org.eclipse.wst.server.core.IModule[],
     * org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {

    }

    @Override
    public ServerPort[] getServerPorts() {
        try {
            return new ServerPort[] {
                new ServerPort("adminserver", "Admin Server Port", getAdminPort(), "HTTP"),
                new ServerPort("server", "Server Port", getPort(), "HTTP")
            };
        } catch (Exception e) {
            return new ServerPort[0];
        }
    }

    // #### IURLProvider implementation methods

    @Override
    public URL getModuleRootURL(IModule module) {
        String protocol = getHttpListenerProtocol(getHost(), getPort());
        String path = getModuleRootPath(module);
        int serverPort = getMonitorPort(getPort());
        String hostname = getHost();

        try {
            return new URL(protocol, hostname, serverPort, path);
        } catch (MalformedURLException e) {
            // Shouldn't happen
            e.printStackTrace();
        }
        
        return null;
    }

    // #### Other public methods

    public PayaraServerBehaviour getServerBehaviourAdapter() {
        PayaraServerBehaviour serverBehavior = getServer().getAdapter(PayaraServerBehaviour.class);

        if (serverBehavior == null) {
            serverBehavior = load(getServer(), PayaraServerBehaviour.class);
        }

        return serverBehavior;
    }

    public IPayaraServerModel getModel() {
        return this.model;
    }

    public static IPath getDefaultDomainDir(IPath serverLocation) {
        if (DEFAULT_SERVER_DIR_NAME.equals(serverLocation.lastSegment())) {
            return serverLocation
                    .append(DEFAULT_DOMAIN_DIR_NAME)
                    .append(DEFAULT_DOMAIN_NAME);
        }

        return serverLocation
                .append(DEFAULT_SERVER_DIR_NAME)
                .append(DEFAULT_DOMAIN_DIR_NAME)
                .append(DEFAULT_DOMAIN_NAME);
    }

    public static String createServerNameWithDomain(String serverName, Path domain) {
        int domainStartPos = serverName.lastIndexOf("[");
        if (domainStartPos == -1) {
            return serverName + " [" + domain.lastSegment() + "]";
        }

        return serverName.substring(0, domainStartPos) + "[" + domain.lastSegment() + "]";
    }

    public boolean isRemote() {
        return getServer().getServerType().supportsRemoteHosts() && !isLocalhost(getServer().getHost());
    }

    public String getDebugOptions(int debugPort) {
        Version version = getVersion();

        if (version.matches("[4")) {
            return "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + debugPort;
        }

        return "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + debugPort;
    }

    private void readDomainConfig() {
        if (!isRemote()) {
            if (readServerConfiguration(new File(getDomainsFolder() + separator + getDomainName() + "/config/domain.xml"))) { //$NON-NLS-1$

                logMessage("In Payara initialize done readServerConfiguration"); //$NON-NLS-1$

                syncHostAndPortsValues();

                // This is mainly so serversection can listen and repopulate,
                // but it is not working as intended because the sunserver
                // instance to which the prop change listener is attached is a different one
                // than is seeing the changes.
                //
                // In fact, we have multiple instances of this object and the glassfishBehaviour 
                // object per server - see issue 140
                
                // firePropertyChangeEvent(DOMAINUPDATE, null, null);
            } else {
                logMessage("In Payara could not readServerConfiguration - probably invalid domain"); //$NON-NLS-1$
            }
        }
    }

    public String validateDomainExists(String domainPath) {
        if (isRemote()) {
            return null;
        }

        if ((domainPath != null) && (!domainPath.startsWith("${"))) { // only if we are correctly setup... //$NON-NLS-1$
            File domainPathFile = new File(domainPath);

            if (!domainPathFile.exists()) {
                return format(pathDoesNotExist, domainPathFile.getAbsolutePath());
            }

            if (!domainPathFile.isDirectory()) {
                return format(pathNotDirectory, domainPathFile.getAbsolutePath());
            }

            if (!canWrite(domainPathFile)) {
                return format(pathNotWritable, domainPathFile.getAbsolutePath());
            }

            File configDir = new File(domainPathFile, "config");
            if (!configDir.exists()) {
                return format(pathDoesNotExist, configDir.getAbsolutePath());
            }

            if (!configDir.canWrite()) {
                return format(pathNotWritable, configDir.getAbsolutePath());
            }

            File domain = new File(domainPathFile, "config/domain.xml"); //$NON-NLS-1$
            if (!domain.exists()) {
                return format(pathNotValidDomain, domain.getAbsolutePath());
            }

            return null;
        }
        
        return Messages.missingDomainLocation;
    }

    IStatus validateDomainLocation() {
        if (isRemote()) {
            return OK_STATUS;
        }

        String domainPath = getDomainPath();
        String domainConfigPath = domainPath + separator + "config" + separator + "domain.xml";

        File domainConfigLocation = new File(domainConfigPath);
        if (!domainConfigLocation.exists()) {
            return new Status(ERROR, SYMBOLIC_NAME, pathNotValidDomain);
        }

        // Check if domain and config dir are writable
        File domainLocation = domainConfigLocation.getParentFile().getParentFile();
        if (!canWrite(domainLocation)) {
            return new Status(ERROR, SYMBOLIC_NAME, bind(pathNotWritable, domainLocation.getAbsolutePath()));
        }

        File domainConfigDir = domainConfigLocation.getParentFile();
        if (!canWrite(domainConfigDir)) {
            return new Status(ERROR, SYMBOLIC_NAME, bind(pathNotWritable, domainConfigDir.getAbsolutePath()));
        }

        return OK_STATUS;
    }

    /*
     * not yet, ui nor working well for generic validation
     */
    public IStatus validate() {

        logMessage("in  validate");
        IStatus status = null;
        if (!isRemote()) {

            // validate domain before reading domain.xml
            if (!(status = validateDomainLocation()).isOK()) {
                return status;
            }

            for (IServer server : ServerCore.getServers()) {
                if (server.getId().equals(this.getServer().getId())) {
                    continue;
                }

                if (server.getServerType() == this.getServer().getServerType()) {
                    PayaraServer payaraServer = (PayaraServer) server.loadAdapter(PayaraServer.class, null);
                    File otherDomainPath = new File(getDomainPath());
                    File payaraDomainPath = new File(payaraServer.getDomainPath());

                    if (otherDomainPath.equals(payaraDomainPath)) {
                        return new Status(ERROR, SYMBOLIC_NAME, serverWithSameDomainPathExisting);
                    }
                }
            }

            // reads ports from domain
            readDomainConfig();

            // validate ports
            if (getAdminPort() == -1) {
                return new Status(ERROR, SYMBOLIC_NAME, invalidPortNumbers);
            }

            // refresh model
            getModel().setAdminPort(getAdminPort());
            getModel().setServerPort(getPort());

        }

        return OK_STATUS;
    }

    private void syncHostAndPortsValues() {
        System.err.println("syncHostAndPortsValues");
    }

    /**
     * Add a property change listener to this server.
     *
     * @param listener java.beans.PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (propChangeListeners == null) {
            propChangeListeners = new ArrayList<>();
        }

        propChangeListeners.add(listener);
    }

    /**
     * Remove a property change listener from this server.
     *
     * @param listener java.beans.PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (propChangeListeners != null) {
            propChangeListeners.remove(listener);
        }
    }

    /**
     * Fire a property change event.
     *
     * @param propertyName a property name
     * @param oldValue the old value
     * @param newValue the new value
     */
    public void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        if (propChangeListeners == null) {
            return;
        }

        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        try {
            Iterator<PropertyChangeListener> iterator = propChangeListeners.iterator();
            while (iterator.hasNext()) {
                try {
                    PropertyChangeListener listener = iterator.next();
                    listener.propertyChange(event);
                } catch (Exception e) {
                    logError("Error firing property change event", e); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            logError("Error in property event", e); //$NON-NLS-1$
        }
    }

    public static PayaraServer getGlassfishServerDelegate(IServerWorkingCopy server) {
        PayaraServer glassfishDelegate = server.getOriginal().getAdapter(PayaraServer.class);
        if (glassfishDelegate == null) {
            glassfishDelegate = (PayaraServer) server.getOriginal().loadAdapter(PayaraServer.class,
                    new NullProgressMonitor());
        }

        return glassfishDelegate;
    }

    protected boolean readServerConfiguration(File domainXml) {
        boolean result = false;

        final Map<String, HttpData> httpMap = new LinkedHashMap<>();

        if (domainXml.exists()) {
            TargetConfigNameReader configNameReader = new TargetConfigNameReader();
            readXml(domainXml, configNameReader);
            String configName = configNameReader.getTargetConfigName();
            if (configName == null) {
                return false;
            }

            HttpListenerReader httpListenerReader = new HttpListenerReader(configName);
            NetworkListenerReader networkListenerReader = new NetworkListenerReader(configName);
            try {
                readXml(domainXml, httpListenerReader, networkListenerReader);

                httpMap.putAll(httpListenerReader.getResult());
                httpMap.putAll(networkListenerReader.getResult());
                
                // !PW This is probably more convoluted than it had to be, but while
                // http-listeners are usually named "http-listener-1",  "http-listener-2", ...
                // technically they could be named anything.
                //
                // For now, the logic is as follows:
                // admin port is the one named "admin-listener"
                // http port is the first non-secure enabled port - typically
                // http-listener-1
                // https port is the first secure enabled port - typically
                // http-listener-2
                // disabled ports are ignored.
                //
                HttpData adminData = httpMap.remove("admin-listener"); //$NON-NLS-1$
                int adminPort = adminData != null ? adminData.getPort() : -1;
                setAttribute(ATTR_ADMINPORT, String.valueOf(adminPort));
                logMessage("reading from domain.xml adminServerPortNumber=" + getAdminPort()); //$NON-NLS-1$

                HttpData httpPortData = httpMap.remove("http-listener-1"); //$NON-NLS-1$
                int httpPort = httpPortData != null ? httpPortData.getPort() : -1;
                setAttribute(ATTR_SERVERPORT, String.valueOf(httpPort));

                result = adminPort != -1;
            } catch (IllegalStateException ex) {
                logError("error IllegalStateException ", ex); //$NON-NLS-1$
            }
        }
        
        return result;
    }

    public String getDomainConfigurationFilePath() {
        return getDomainPath().trim() + "/config/domain.xml";
    }

    public int getDebugPort() {
        return getAttribute(ATTR_DEBUG_PORT, -1);
    }

    /*
     * *************Implementation of adapter methods used by tooling SDK library.
     */
    
    public int getAdminPort() {
        return getAttribute(ATTR_ADMINPORT, -1);
    }

    public String getAdminUser() {
        return getAttribute(ATTR_ADMIN, "admin");
    }

    public String getDomainsFolder() {
        if (!isRemote()) {
            return new File(getDomainPath()).getParent();
        }

        return null;
    }

    public String getDomainName() {
        return getDomainPath() != null ? new File(getDomainPath()).getName() : null;
    }
    
    public String getDomainPath() {
        return getAttribute(ATTR_DOMAINPATH, "");
    }

    public String getHost() {
        return getServer().getHost();
    }

    public String getName() {
        return getServer().getName();
    }

    public int getPort() {
        return getAttribute(ATTR_SERVERPORT, 8080);
    }

    public String getUrl() {
        return null;
    }

    public Version getVersion() {
        final IPath location = getServer().getRuntime().getLocation();

        if (location != null) {
            PayaraLocationUtils payaraInstall = find(location.toFile());

            if (payaraInstall != null) {
                return payaraInstall.version();
            }
        }

        return null;
    }

    public GlassFishAdminInterface getAdminInterface() {
        return GlassFishAdminInterface.HTTP;
    }

    public String getServerHome() {
        return new File(getServerInstallationDirectory()).getAbsolutePath();
    }

    public String getServerRoot() {
        return null;
    }

    // *********************************************************

    public boolean getKeepSessions() {
        return getAttribute(ATTR_KEEPSESSIONS, true);
    }
    
    public boolean getAttachDebuggerEarly() {
        return getAttribute(PROP_ATTACH_DEBUGGER_EARLY.name(), PROP_ATTACH_DEBUGGER_DEFAULT);
    }

    public String getAdminPassword() {
        return getAttribute(ATTR_ADMINPASS, "");
    }

    public void setAdminPassword(String value) {
        setAttribute(ATTR_ADMINPASS, value);
    }

    public String computePreserveSessions() {
        if (!getKeepSessions()) {
            return null;
        }

        return "keepstate";
    }

    /*
     * JAR deploy for v3
     */
    public boolean getJarDeploy() {
        if (isRemote()) {
            return true;
        }

        return getAttribute(ATTR_JARDEPLOY, false);
    }

    public void setPort(int port) {
        setAttribute(ATTR_SERVERPORT, port);
    }
    public boolean useAnonymousConnections() {
        return getAttribute(ATTR_USEANONYMOUSCONNECTIONS, true);
    }

    public String getServerInstallationDirectory() {
        IPath baseLocation = getServer().getRuntime().getLocation();
        if (DEFAULT_SERVER_DIR_NAME.equals(baseLocation.lastSegment())) {
            return baseLocation.toString();

        }

        return baseLocation.append(DEFAULT_SERVER_DIR_NAME).toString();
    }

    @Override
    public IStatus canModifyModules(IModule[] add, IModule[] remove) {
        if (add == null || add.length == 0) {
            return OK_STATUS;
        }

        for (IModule module : add) {
            if (!isModuleSupported(module)) {
                return createErrorStatus("Module is not supported on this server", null);
            }

            IStatus status = checkModule(module);
            if (status.getSeverity() == ERROR) {
                return status;
            }

            IModule[] root = doGetParentModules(module);
            if (root != null && root.length > 0 && root[0] != module) {
                return createErrorStatus(
                        "Web module which is part of an Ear cannot be added as top level module to this server", null);
            }
        }

        return OK_STATUS;
    }

    protected boolean isModuleSupported(IModule module) {
        return isEARModule(module) || isWebModule(module) || isEJBModule(module);
    }

    @Override
    public IModule[] getChildModules(IModule[] modulePath) {
        if ((modulePath == null) || (modulePath.length == 0)) {
            return new IModule[0];
        }
        
        IModule module = modulePath[modulePath.length - 1];
        if (module != null && module.getModuleType() != null) {
            IModuleType moduleType = module.getModuleType();
            if (moduleType != null && "jst.ear".equals(moduleType.getId())) { //$NON-NLS-1$
                IEnterpriseApplication enterpriseApplication = (IEnterpriseApplication) module
                        .loadAdapter(IEnterpriseApplication.class, null);
                if (enterpriseApplication != null) {
                    IModule[] earModules = enterpriseApplication.getModules();
                    if (earModules != null) {
                        return earModules;
                    }
                }
            } else if (moduleType != null && "jst.web".equals(moduleType.getId())) { //$NON-NLS-1$
                IWebModule webModule = (IWebModule) module.loadAdapter(IWebModule.class, null);
                if (webModule != null) {
                    IModule[] modules = webModule.getModules();
                    return modules;
                }
            }
        }
        
        return new IModule[0];
    }

    @Override
    public IModule[] getRootModules(IModule module) throws CoreException {
        if (!isModuleSupported(module)) {
            return null;
        }
        
        IModule[] parents = doGetParentModules(module);
        if (parents.length > 0) {
            return parents;
        }
        
        return new IModule[] { module };
    }

    private IModule[] doGetParentModules(IModule module) {
        ArrayList<IModule> list = new ArrayList<>();
        
        for (IModule earModule : getModules("jst.ear")) {
            IEnterpriseApplication ear = (IEnterpriseApplication) earModule.loadAdapter(IEnterpriseApplication.class, null);
            
            for (IModule child : ear.getModules()) {
                if (child.equals(module)) {
                    list.add(earModule);
                }
            }
        }
        
        return list.toArray(new IModule[list.size()]);
    }

    protected IStatus checkModule(final IModule module) {
        return canSupportModule(module);
    }

    public IStatus canSupportModule(final IModule module) {
        IProject project = module.getProject();

        if (project == null) {
            return createErrorStatus("module type not supported", null);
        }

        try {
            IFacetedProject facetedProject = ProjectFacetsManager.create(module.getProject());

            if (facetedProject != null) {
                IRuntime runtime = getRuntime(getServer().getRuntime());

                if (runtime == null) {
                    return createErrorStatus("cannot bridge runtimes", null);
                }

                for (Object element : facetedProject.getProjectFacets()) {
                    IProjectFacetVersion facetVersion = (IProjectFacetVersion) element;

                    if (!runtime.supports(facetVersion)) {
                        return createErrorStatus(bind(facetNotSupported, facetVersion.toString()), null);
                    }
                }
            }
        } catch (CoreException e) {
            return e.getStatus();
        }

        for (IModule child : getChildModules(new IModule[] { module })) {
            IStatus status = canSupportModule(child);

            if (status.getSeverity() == ERROR) {
                return status;
            }
        }

        return OK_STATUS;
    }

    private String getModuleRootPath(IModule module) {
        if (module == null || module.getProject() == null) {
            return "/";
        }

        // If we are dealing with a web module, look if there is a root ear module

        if (hasProjectFacet(module, getProjectFacet(JST_WEB_MODULE))) {
            IModule[] rootEars = getRootEarModulesOnThisServer(module);
            if (rootEars != null && rootEars.length > 0) {
                return getModuleRootPath(module, rootEars[0]);
            }

            // Try to get context root from glassfish-web.xml

            String path = getWebDeploymentDescriptor(module.getProject()).getContext();
            if (path != null) {
                return path;
            }

        }

        return "/" + getServerContextRoot(module.getProject());
    }

    /**
     * Return the context root of a web module in ther parent EAR.
     *
     * @param module: the web module
     * @param parent: the EAR module
     * @return
     */
    private String getModuleRootPath(IModule module, IModule parent) {
        String context = getAppWebContextRoot(parent, module);

        if (context != null && context.length() > 0) {
            try {
                return "/" + encode(context, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        
        return "/";
    }

    /**
     * Return only the root modules already being added to this Glassfish server
     */
    private IModule[] getRootEarModulesOnThisServer(IModule module) {
        // Determine the root
        IModule[] ear = getEnterpriseApplications(module, null);
        if (ear != null && ear.length > 0) {
            ArrayList<IModule> rootEarModules = new ArrayList<>();
            
            // Return only the EAR modules on current server.
            
            HashSet<IModule> allmodules = new HashSet<>(asList(getServer().getModules()));
            for (IModule element : ear) {
                if (allmodules.contains(element)) {
                    rootEarModules.add(element);
                }
            }
            
            return rootEarModules.toArray(new IModule[rootEarModules.size()]);
        }
        
        return null;
    }

    private int getMonitorPort(int configedPort) {
        IServerMonitorManager manager = ServerMonitorManager.getInstance();
        
        for (IMonitoredServerPort port : manager.getMonitoredPorts(getServer())) {
            if (port.getServerPort().getPort() == configedPort) {
                return port.getMonitorPort();
            }
        }
        
        return configedPort;
    }

}
