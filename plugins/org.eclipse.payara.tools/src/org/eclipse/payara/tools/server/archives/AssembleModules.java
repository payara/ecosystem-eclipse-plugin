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

package org.eclipse.payara.tools.server.archives;

import static java.lang.System.arraycopy;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.jst.server.generic.core.internal.CorePlugin.PLUGIN_ID;
import static org.eclipse.payara.tools.PayaraToolsPlugin.SYMBOLIC_NAME;
import static org.eclipse.payara.tools.PayaraToolsPlugin.logMessage;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_RESTART_PATTERN;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_RESTART_PATTERN_DEFAULT;
import static org.eclipse.payara.tools.utils.WtpUtil.load;
import static org.eclipse.wst.server.core.IServer.PUBLISH_STATE_NONE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IJ2EEModule;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.jst.server.generic.core.internal.CorePlugin;
import org.eclipse.jst.server.generic.core.internal.publishers.ModulePackager;
import org.eclipse.payara.tools.PayaraToolsPlugin;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.payara.tools.server.deploying.PayaraServerBehaviour;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.eclipse.wst.server.core.util.PublishHelper;

/* 
 * Assemble modules (i.e if a web app depends on a utility lib, we need to create the jar file for this utility and
 * put it in the web-inf/lib area of the web app.
 */
@SuppressWarnings("restriction")
public class AssembleModules {

    protected IModule[] modulePath; // Full path of the module. We need the path to get publish state and query
                                    // resource delta, etc
    protected IModule module; // Module to be assembled
    protected IPath assembleRoot;
    protected PublishHelper publishHelper;
    protected PayaraServer server;
    protected boolean childNeedsARedeployment;

    public AssembleModules(IModule[] modulePath, IPath assembleRoot, PayaraServer server, PublishHelper helper) {
        this.modulePath = modulePath;
        this.module = modulePath[modulePath.length - 1]; // last segment of the module path
        this.assembleRoot = assembleRoot;
        this.server = server;
        this.publishHelper = helper;
        
        logMessage("AssembleModules assembleRoot=" + assembleRoot);
    }

    public IPath assembleWebModule(IProgressMonitor monitor) throws CoreException {
        IPath parent = assembleRoot;
        
        if (PUBLISH_STATE_NONE != server.getServer().getModulePublishState(modulePath)) {
            copyModule(module, monitor);
        }

        IWebModule webModule = (IWebModule) module.loadAdapter(IWebModule.class, monitor);
        IModule[] childModules = webModule.getModules();
        for (IModule childModule : childModules) {
            String uri = webModule.getURI(childModule);
            if (uri == null) { // The bad memories of WTP 1.0
                throw new CoreException(new Status(ERROR, PLUGIN_ID, 0, "unable to assemble module null uri", null));
            }
            
            IJ2EEModule jeeModule = (IJ2EEModule) childModule.loadAdapter(IJ2EEModule.class, monitor);
            if (jeeModule != null && jeeModule.isBinary()) { // Binary module
                ProjectModule pm = (ProjectModule) childModule.loadAdapter(ProjectModule.class, null);
                IModuleResource[] resources = pm.members();
                publishHelper.publishToPath(resources, parent.append(uri), monitor);
            } else { // Project module
                String version = PayaraServerBehaviour.getVersion(server);
                if (version.indexOf(" 3.1") == -1) {
                    packModule(childModule, uri, parent);
                } else {

                    if (shouldRepack(childModule)) {

                        IModule[] childModulePath = new IModule[modulePath.length + 1];
                        arraycopy(modulePath, 0, childModulePath, 0, modulePath.length);
                        childModulePath[childModulePath.length - 1] = childModule;

                        AssembleModules assembler = new AssembleModules(childModulePath, assembleRoot.append(uri),
                                server, publishHelper);
                        childNeedsARedeployment = (childNeedsARedeployment || assembler.needsARedeployment());
                        assembler.copyModule(childModule, monitor);
                    }
                }
            }
        }
        
        return parent;
    }

    public static boolean isModuleType(IModule module, String moduleTypeId) {
        return module.getModuleType() != null && moduleTypeId.equals(module.getModuleType().getId()); 
    }

    protected void packModule(IModule module, String deploymentUnitName, IPath destination) throws CoreException {
        String dest = destination.append(deploymentUnitName).toString();
        logMessage("AssembleModules dest=" + dest);

        ModulePackager packager = null;
        try {
            packager = new ModulePackager(dest, false);
            for (IModuleResource resource : load(module, ProjectModule.class).members()) {
                logMessage("AssembleModules resources=" + resource);

                doPackModule(resource, packager);
            }
        } catch (IOException e) {
            throw new CoreException(new Status(ERROR, SYMBOLIC_NAME, 0,
                    "unable to assemble module", e));
        } finally {
            try {
                packager.finished();
            } catch (Exception e) {
            }
        }
    }

    private void doPackModule(IModuleResource resource, ModulePackager packager) throws CoreException, IOException {
        if (resource instanceof IModuleFolder) {
            IModuleFolder mFolder = (IModuleFolder) resource;
            IModuleResource[] resources = mFolder.members();
            
            logMessage("AssembleModules  doPackModule IModuleFolder=" + mFolder);
            logMessage("AssembleModules  doPackModule resource.getModuleRelativePath()=" + resource.getModuleRelativePath());
            logMessage("AssembleModules  resource.getModuleRelativePath().append(resource.getName()).toPortableString()="
                    + resource.getModuleRelativePath().append(resource.getName()).toPortableString());

            packager.writeFolder(resource.getModuleRelativePath().append(resource.getName()).toPortableString());

            for (int i = 0; resources != null && i < resources.length; i++) {
                logMessage("AssembleModules resources[i]=" + resources[i]);

                doPackModule(resources[i], packager);
            }
        } else {
            String destination = resource.getModuleRelativePath().append(resource.getName()).toPortableString();
            IFile file = resource.getAdapter(IFile.class);
            if (file != null) {
                packager.write(file, destination);
            } else {
                File file2 = resource.getAdapter(File.class);
                packager.write(file2, destination);
            }
        }
    }

    protected IPath copyModule(IModule module, IProgressMonitor monitor) throws CoreException {
        ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, monitor);
        
        IPath[] jarPaths = null;
        if (module.getModuleType().getId().equals("jst.web")) {
            IWebModule webModule = (IWebModule) module.loadAdapter(IWebModule.class, monitor);
            
            // Child module of the web project, e.g., Utility project added through
            // Deployment Assembly
            IModule[] childModules = webModule.getModules();
            if (childModules != null && childModules.length > 0) {
                jarPaths = new IPath[childModules.length];
                for (int i = 0; i < childModules.length; i++) {
                    jarPaths[i] = new Path(webModule.getURI(childModules[i]));
                }
            }

        }
        IStatus[] status = publishHelper.publishSmart(pm.members(), assembleRoot, jarPaths, monitor);
        if (status != null && status.length > 0) {
            // no need to emit an error like CoreException(status[0]); just log in the entry
            // see https://glassfishplugins.dev.java.net/issues/show_bug.cgi?id=268
            for (IStatus statu : status) {
                PayaraToolsPlugin.logMessage("warning copying module: " + statu.getMessage());
            }
        }

        return assembleRoot;
    }

    protected IPath copyEarModule(IModule module, IProgressMonitor monitor) throws CoreException {
        ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, monitor);
        IEnterpriseApplication earModule = (IEnterpriseApplication) module.loadAdapter(IEnterpriseApplication.class,
                monitor);
        
        // Get publish paths of child modules so we do not delete them with publishSmart
        // call
        IModule[] childModules = earModule.getModules();
        logMessage("copyEarModule childModules.length=" + childModules.length);
        ArrayList<IPath> ignorePaths = new ArrayList<>(childModules.length);
        for (IModule childModule2 : childModules) {

            IModule childModule = childModule2;
            String uri = earModule.getURI(childModule);
            if (uri == null) {
                IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0,
                        "unable to assemble module null uri", null); //$NON-NLS-1$
                throw new CoreException(status);
            }
            if (!childModule.getModuleType().getId().equals("jst.utility")) {//$NON-NLS-1$ see bug
                                                                             // https://glassfishplugins.dev.java.net/issues/show_bug.cgi?id=251
                if (uri.endsWith(".war")) {
                    uri = uri.substring(0, uri.length() - 4) + "_war";
                } else if (uri.endsWith(".jar")) {
                    uri = uri.substring(0, uri.length() - 4) + "_jar";
                } else if (uri.endsWith(".rar")) { // http://java.net/jira/browse/GLASSFISHPLUGINS-333
                    uri = uri.substring(0, uri.length() - 4) + "_rar";
                }
            }
            ignorePaths.add(new Path(uri));
        }
        IPath[] pathArr = new IPath[ignorePaths.size()];
        pathArr = ignorePaths.toArray(pathArr);
        IStatus[] status = publishHelper.publishSmart(pm.members(), assembleRoot, pathArr, monitor);
        if (status != null && status.length > 0) {
            // no need to emit an error like CoreException(status[0]); just log in the entry
            // see https://glassfishplugins.dev.java.net/issues/show_bug.cgi?id=268
            for (IStatus statu : status) {
                PayaraToolsPlugin.logMessage("warning copying module: " + statu.getMessage());
            }
        }

        return assembleRoot;
    }

    /*
     * not used for now... Would be ejb module when v3 has them
     *
     */
    public IPath assembleNonWebOrNonEARModule(IProgressMonitor monitor) throws CoreException {
        return copyModule(module, monitor);
    }

    public IPath assembleEARModule(IProgressMonitor monitor) throws CoreException {
        // Copy ear root to the temporary assembly directory
        IPath parent = assembleRoot;

        boolean shouldCopy = (IServer.PUBLISH_STATE_NONE != server.getServer().getModulePublishState(modulePath));
        if (shouldCopy) {
            copyModule(module, monitor);
        }
        
        IEnterpriseApplication earModule = (IEnterpriseApplication) module.loadAdapter(IEnterpriseApplication.class,
                monitor);
        
        IModule[] childModules = earModule.getModules();
        for (IModule module : childModules) {
            String uri = earModule.getURI(module);
            if (uri == null) {
                throw new CoreException(new Status(ERROR, PLUGIN_ID, 0,
                        "unable to assemble module null uri", null));
            }
            IJ2EEModule jeeModule = (IJ2EEModule) module.loadAdapter(IJ2EEModule.class, monitor);
            if (jeeModule != null && jeeModule.isBinary()) {// Binary module just copy
                ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, null);
                IModuleResource[] resources = pm.members();
                publishHelper.publishToPath(resources, parent.append(uri), monitor);

                continue;// done! no need to go further
            }
            if (shouldRepack(module)) {
                packModuleEARModule(module, uri, parent);
            }
        }
        
        return parent;

    }

    /**
     * Checks if there has been a change in the published resources.
     *
     * @param module
     * @return module changed
     */
    private boolean shouldRepack(IModule lmodule) {
        IModule[] childModulePath = new IModule[modulePath.length + 1];
        arraycopy(modulePath, 0, childModulePath, 0, modulePath.length);
        childModulePath[childModulePath.length - 1] = lmodule;

        boolean repack = PUBLISH_STATE_NONE != server.getServer().getModulePublishState(childModulePath);
        repack |= PUBLISH_STATE_NONE != server.getServer().getModulePublishState(modulePath);
        
        return repack;
    }

    /*
     * Returns true is a deploy command has to be run. for example a simple JSP change does not need a
     * redeployment as the file is already been copied by the assembly in the correct directory
     */
    public boolean needsARedeployment() {
        Server _server = (Server) server.getServer();
        
        return 
            childNeedsARedeployment ||
            criticalResourceChangeThatNeedsARedeploy(
                // The path that's going to be published
                _server.getPublishedResourceDelta(modulePath),
                
                // The pattern that denotes whether a restart is needed for that path
                Pattern.compile(_server.getAttribute(PROP_RESTART_PATTERN.name(), PROP_RESTART_PATTERN_DEFAULT)));
    }

    /*
     * return true is a module resource change requires a redeploy command for example, web.xml or a
     * .class file change needs a redepploy. a jsp or html change just needs a file copy not a redeploy
     * command.
     */
    private boolean criticalResourceChangeThatNeedsARedeploy(IModuleResourceDelta[] deltas, Pattern restartPattern) {
        if (deltas == null) {
            return false;
        }

        for (IModuleResourceDelta delta : deltas) {
            if (restartPattern.matcher(delta.getModuleResource().getName()).find()) {
                return true;
            }
            
            logMessage("AssembleModules no pattern matched.");

            if (criticalResourceChangeThatNeedsARedeploy(delta.getAffectedChildren(), restartPattern)) {
                return true;
            }
        }

        return false;
    }

    protected void packModuleEARModule(IModule module, String deploymentUnitName, IPath destination) throws CoreException {
        logMessage("AssembleModules packModuleEARModule=" + module.getId() + " " + module.getName());
        logMessage("AssembleModules deploymentUnitName=" + deploymentUnitName); // ie foo.war or myejbs.jar
        
        // Need to replace the , with_ ie _war or _jar as the dirname for dir deploy
        logMessage("AssembleModules destination=" + destination);
        if (module.getModuleType().getId().equals("jst.web")) {//$NON-NLS-1$

            AssembleModules assembler = new AssembleModules(modulePath, assembleRoot, server, publishHelper);
            IPath webAppPath = assembler.assembleWebModule(new NullProgressMonitor());
            String realDestination = destination.append(deploymentUnitName).toString();
            logMessage("AssembleModules realDestination=" + realDestination);
            
            ModulePackager packager = null;
            try {
                packager = new ModulePackager(realDestination, false);
                packager.pack(webAppPath.toFile(), webAppPath.toOSString());

            } catch (IOException e) {
                throw new CoreException(new Status(ERROR, PLUGIN_ID, 0, "unable to assemble module", e));
            } finally {
                if (packager != null) {
                    try {
                        packager.finished();
                    } catch (IOException e) {
                    }
                }
            }

        } else {
            packModule(module, deploymentUnitName, destination);
        }

    }

    public IPath assembleDirDeployedEARModule(IProgressMonitor monitor) throws CoreException {
        // Copy ear root to the temporary assembly directory
        IPath parent = assembleRoot;

        if (PUBLISH_STATE_NONE != server.getServer().getModulePublishState(modulePath)) {
            copyEarModule(module, monitor);
        }
        
        IEnterpriseApplication earModule = (IEnterpriseApplication) module.loadAdapter(IEnterpriseApplication.class,
                monitor);
        IModule[] childModules = earModule.getModules();
        logMessage("assembleDirDeployedEARModule childModules.length=" + childModules.length);
        for (IModule childModule2 : childModules) {

            IModule childModule = childModule2;
            String uri = earModule.getURI(childModule);
            if (uri == null) {
                throw new CoreException(new Status(ERROR, PLUGIN_ID, 0, "unable to assemble module null uri", null));
            }
            
            IJ2EEModule jeeModule = (IJ2EEModule) childModule.loadAdapter(IJ2EEModule.class, monitor);
            if (jeeModule != null && jeeModule.isBinary()) {// Binary module just copy
                publishHelper.publishToPath(
                    load(childModule, ProjectModule.class).members(), 
                    parent.append(uri), monitor);
                
                continue; // Done! no need to go further
            }
            
            if (!childModule.getModuleType().getId().equals("jst.utility")) {//$NON-NLS-1$ see bug
                                                                             // https://glassfishplugins.dev.java.net/issues/show_bug.cgi?id=251
                if (uri.endsWith(".war")) {
                    uri = uri.substring(0, uri.length() - 4) + "_war";
                } else if (uri.endsWith(".jar")) {
                    uri = uri.substring(0, uri.length() - 4) + "_jar";
                } else if (uri.endsWith(".rar")) { // http://java.net/jira/browse/GLASSFISHPLUGINS-333
                    uri = uri.substring(0, uri.length() - 4) + "_rar";
                }
            }

            if (shouldRepack(childModule)) {
                IModule[] childModulePath = new IModule[modulePath.length + 1];
                arraycopy(modulePath, 0, childModulePath, 0, modulePath.length);
                childModulePath[childModulePath.length - 1] = childModule;

                if (childModule.getModuleType().getId().equals("jst.web")) {//$NON-NLS-1$
                    AssembleModules assembler = new AssembleModules(childModulePath, assembleRoot.append(uri), server,
                            publishHelper);
                    childNeedsARedeployment = (childNeedsARedeployment || assembler.needsARedeployment());
                    assembler.assembleWebModule(new NullProgressMonitor());
                } else {
                    AssembleModules assembler = new AssembleModules(childModulePath, assembleRoot.append(uri), server,
                            publishHelper);
                    childNeedsARedeployment = (childNeedsARedeployment || assembler.needsARedeployment());
                    assembler.copyModule(childModule, monitor);
                }

            }
        }
        
        return parent;

    }
}
