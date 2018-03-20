/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.server.archives;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;
import org.eclipse.glassfish.tools.server.GlassFishServer;
import org.eclipse.glassfish.tools.server.deploying.GlassFishServerBehaviour;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IJ2EEModule;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.jst.server.generic.core.internal.CorePlugin;
import org.eclipse.jst.server.generic.core.internal.publishers.ModulePackager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.eclipse.wst.server.core.util.PublishHelper;

/* assemble modules (i.e if a web app depends on a utility lib, we need to create the jar file for this utility and
 * put it in the web-inf/lib area of the web app.
 * Later for v3 we need to also handle ear files
 */
@SuppressWarnings("restriction")
public class AssembleModules {

	protected IModule[] modulePath; // Full path of the module. We need the path to get publish state and query
									// resource delta, etc
	protected IModule module; // Module to be assembled
	protected IPath assembleRoot;
	protected PublishHelper publishHelper;
	protected GlassFishServer server;
	protected boolean childNeedsARedeployment = false;

	public AssembleModules(IModule[] modulePath, IPath assembleRoot, GlassFishServer server, PublishHelper helper) {
		this.modulePath = modulePath;
		this.module = modulePath[modulePath.length - 1]; // last segment of the module path
		this.assembleRoot = assembleRoot;
		this.server = server;
		this.publishHelper = helper;
		GlassfishToolsPlugin.logMessage("AssembleModules assembleRoot=" + assembleRoot);

	}

	public IPath assembleWebModule(IProgressMonitor monitor) throws CoreException {

		IPath parent = assembleRoot;
		boolean shouldCopy = (IServer.PUBLISH_STATE_NONE != server.getServer().getModulePublishState(modulePath));
		if (shouldCopy)
			copyModule(module, monitor);

		IWebModule webModule = (IWebModule) module.loadAdapter(IWebModule.class, monitor);
		IModule[] childModules = webModule.getModules();
		for (int i = 0; i < childModules.length; i++) {
			IModule childModule = childModules[i];
			// packModule(module, webModule.getURI(module), parent);
			String uri = webModule.getURI(childModule);
			if (uri == null) { // The bad memories of WTP 1.0
				IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0,
						"unable to assemble module null uri", null); //$NON-NLS-1$
				throw new CoreException(status);
			}
			IJ2EEModule jeeModule = (IJ2EEModule) childModule.loadAdapter(IJ2EEModule.class, monitor);
			if (jeeModule != null && jeeModule.isBinary()) { // Binary module
				ProjectModule pm = (ProjectModule) childModule.loadAdapter(ProjectModule.class, null);
				IModuleResource[] resources = pm.members();
				publishHelper.publishToPath(resources, parent.append(uri), monitor);
			} else { // Project module
				// ludo 2010 packModule(module, uri, parent);
				String version = GlassFishServerBehaviour.getVersion(server);
				if (version.indexOf(" 3.1") == -1) {
					packModule(childModule, uri, parent);
				} else {

					if (shouldRepack(childModule)) {

						final IModule[] childModulePath = new IModule[modulePath.length + 1];
						System.arraycopy(modulePath, 0, childModulePath, 0, modulePath.length);
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
		if (module.getModuleType() != null && moduleTypeId.equals(module.getModuleType().getId())) {
			return true;
		}
		return false;
	}

	protected void packModule(IModule module, String deploymentUnitName, IPath destination) throws CoreException {

		String dest = destination.append(deploymentUnitName).toString();
		GlassfishToolsPlugin.logMessage("AssembleModules dest=" + dest);

		ModulePackager packager = null;
		try {
			packager = new ModulePackager(dest, false);
			ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, null);
			IModuleResource[] resources = pm.members();
			for (int i = 0; i < resources.length; i++) {
				GlassfishToolsPlugin.logMessage("AssembleModules resources=" + resources[i]);

				doPackModule(resources[i], packager);
			}
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, GlassfishToolsPlugin.SYMBOLIC_NAME, 0,
					"unable to assemble module", e); //$NON-NLS-1$
			throw new CoreException(status);
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
			GlassfishToolsPlugin.logMessage("AssembleModules  doPackModule IModuleFolder=" + mFolder);
			GlassfishToolsPlugin.logMessage("AssembleModules  doPackModule resource.getModuleRelativePath()="
					+ resource.getModuleRelativePath());
			GlassfishToolsPlugin.logMessage(
					"AssembleModules  resource.getModuleRelativePath().append(resource.getName()).toPortableString()="
							+ resource.getModuleRelativePath().append(resource.getName()).toPortableString());

			packager.writeFolder(resource.getModuleRelativePath().append(resource.getName()).toPortableString());

			for (int i = 0; resources != null && i < resources.length; i++) {
				GlassfishToolsPlugin.logMessage("AssembleModules resources[i]=" + resources[i]);

				doPackModule(resources[i], packager);
			}
		} else {
			String destination = resource.getModuleRelativePath().append(resource.getName()).toPortableString();
			IFile file = (IFile) resource.getAdapter(IFile.class);
			if (file != null) {
				packager.write(file, destination);
			} else {
				File file2 = (File) resource.getAdapter(File.class);
				packager.write(file2, destination);
			}
		}
	}

	protected IPath copyModule(IModule module, IProgressMonitor monitor) throws CoreException {
		ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, monitor);
		// SunAppSrvPlugin.logMessage("AssembleModules copyModule ProjectModule
		// is="+pm);
		IPath[] jarPaths = null;
		if (module.getModuleType().getId().equals("jst.web")) {//$NON-NLS-1$
			// IModuleResource[] mr = getResources(module);
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
			for (int i = 0; i < status.length; i++) {
				GlassfishToolsPlugin.logMessage("warning copying module: " + status[i].getMessage());
			}
		}

		return assembleRoot;
	}

	protected IPath copyEarModule(IModule module, IProgressMonitor monitor) throws CoreException {
		ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, monitor);
		IEnterpriseApplication earModule = (IEnterpriseApplication) module.loadAdapter(IEnterpriseApplication.class,
				monitor);
		// get publish paths of child modules so we do not delete them with publishSmart
		// call
		IModule[] childModules = earModule.getModules();
		GlassfishToolsPlugin.logMessage("copyEarModule childModules.length=" + childModules.length);
		ArrayList<IPath> ignorePaths = new ArrayList<IPath>(childModules.length);
		for (int i = 0; i < childModules.length; i++) {

			IModule childModule = childModules[i];
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
			for (int i = 0; i < status.length; i++) {
				GlassfishToolsPlugin.logMessage("warning copying module: " + status[i].getMessage());
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
		// copy ear root to the temporary assembly directory
		IPath parent = assembleRoot;

		boolean shouldCopy = (IServer.PUBLISH_STATE_NONE != server.getServer().getModulePublishState(modulePath));
		if (shouldCopy)
			copyModule(module, monitor);
		IEnterpriseApplication earModule = (IEnterpriseApplication) module.loadAdapter(IEnterpriseApplication.class,
				monitor);
		IModule[] childModules = earModule.getModules();
		for (int i = 0; i < childModules.length; i++) {
			IModule module = childModules[i];
			String uri = earModule.getURI(module);
			if (uri == null) {
				IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0,
						"unable to assemble module null uri", null); //$NON-NLS-1$
				throw new CoreException(status);
			}
			IJ2EEModule jeeModule = (IJ2EEModule) module.loadAdapter(IJ2EEModule.class, monitor);
			if (jeeModule != null && jeeModule.isBinary()) {// Binary module just copy
				ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, null);
				IModuleResource[] resources = pm.members();
				// ludo nbew publishHelper.publishFull(resources, parent, monitor);
				//
				//
				//
				publishHelper.publishToPath(resources, parent.append(uri), monitor);
				//
				//
				//
				//

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
	/*
	 * private boolean shouldRepack( IModule module ) { final Server _server =
	 * (Server) server.getServer(); final IModule[] modules ={module};
	 * IModuleResourceDelta[] deltas = _server.getPublishedResourceDelta( modules );
	 * 
	 * return deltas.length > 0; }
	 */
	private boolean shouldRepack(IModule lmodule) {
		final IModule[] childModulePath = new IModule[modulePath.length + 1];
		System.arraycopy(modulePath, 0, childModulePath, 0, modulePath.length);
		childModulePath[childModulePath.length - 1] = lmodule;

		boolean repack = (IServer.PUBLISH_STATE_NONE != server.getServer().getModulePublishState(childModulePath));
		repack |= (IServer.PUBLISH_STATE_NONE != server.getServer().getModulePublishState(modulePath));
		return repack;
	}

	/*
	 * returns true is a deploy command has to be run. for example a simple JSP
	 * change does not need a redeployment as the file is already been copied by the
	 * assembly in the correct directory
	 */
	public boolean needsARedeployment() {
		final Server _server = (Server) server.getServer();
		IModuleResourceDelta[] deltas = _server.getPublishedResourceDelta(modulePath);
		return (childNeedsARedeployment || criticalResourceChangeThatNeedsARedeploy(deltas));
	}

	/*
	 * return true is a module resource change requires a redeploy command for
	 * example, web.xml or a .class file change needs a redepploy. a jsp or html
	 * change just needs a file copy not a redeploy command.
	 */
	private boolean criticalResourceChangeThatNeedsARedeploy(IModuleResourceDelta[] deltas) {
		if (deltas == null) {
			return false;
		}

		for (int i = 0; i < deltas.length; i++) {
			if (deltas[i].getModuleResource().getName().endsWith(".class")) {// class file
				GlassfishToolsPlugin.logMessage(
						"Class Changed in AssembleModules criticalResourceChangeThatNeedsARedeploy DELTA IS="
								+ deltas[i].getKind() + deltas[i].getModuleResource().getName());
				return true;
			}
			if (deltas[i].getModuleResource().getName().endsWith(".properties")) {// properties file
				return true;
			}
			if (deltas[i].getModuleResource().getName().endsWith(".xml")) {// all XML files, including DD files or
																			// config files
				GlassfishToolsPlugin
						.logMessage("XML Changed in AssembleModules criticalResourceChangeThatNeedsARedeploy DELTA IS="
								+ deltas[i].getKind() + deltas[i].getModuleResource().getName());
				return true;
			}
			if (deltas[i].getModuleResource().getName().equalsIgnoreCase("manifest.mf")) {
				GlassfishToolsPlugin.logMessage(
						"MANIFEST FIle  Changed in AssembleModules criticalResourceChangeThatNeedsARedeploy DELTA IS="
								+ deltas[i].getKind() + deltas[i].getModuleResource().getName());
				return true;
			}
			GlassfishToolsPlugin.logMessage("AssembleModules neither class manifest or xml file");

			IModuleResourceDelta[] childrenDeltas = deltas[i].getAffectedChildren();
			if (criticalResourceChangeThatNeedsARedeploy(childrenDeltas)) {
				return true;
			}
		}

		return false;

	}

	protected void packModuleEARModule(IModule module, String deploymentUnitName, IPath destination)
			throws CoreException {
		GlassfishToolsPlugin
				.logMessage("AssembleModules packModuleEARModule=" + module.getId() + " " + module.getName());
		GlassfishToolsPlugin.logMessage("AssembleModules deploymentUnitName=" + deploymentUnitName); // ie foo.war or
																										// myejbs.jar
		// need to replace the , with_ ie _war or _jar as the dirname for dir deploy
		GlassfishToolsPlugin.logMessage("AssembleModules destination=" + destination);
		if (module.getModuleType().getId().equals("jst.web")) {//$NON-NLS-1$

			AssembleModules assembler = new AssembleModules(modulePath, assembleRoot, server, publishHelper);
			IPath webAppPath = assembler.assembleWebModule(new NullProgressMonitor());
			String realDestination = destination.append(deploymentUnitName).toString();
			GlassfishToolsPlugin.logMessage("AssembleModules realDestination=" + realDestination);
			ModulePackager packager = null;
			try {
				packager = new ModulePackager(realDestination, false);
				packager.pack(webAppPath.toFile(), webAppPath.toOSString());

			} catch (IOException e) {
				IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0, "unable to assemble module", e); //$NON-NLS-1$
				throw new CoreException(status);
			} finally {
				if (packager != null) {
					try {
						packager.finished();
					} catch (IOException e) {
					}
				}
			}

		} else {
			/* ludo super. */packModule(module, deploymentUnitName, destination);
		}

	}

	public IPath assembleDirDeployedEARModule(IProgressMonitor monitor) throws CoreException {
		// copy ear root to the temporary assembly directory
		IPath parent = assembleRoot;

		boolean shouldCopy = (IServer.PUBLISH_STATE_NONE != server.getServer().getModulePublishState(modulePath));
		if (shouldCopy)
			copyEarModule(module, monitor);
		IEnterpriseApplication earModule = (IEnterpriseApplication) module.loadAdapter(IEnterpriseApplication.class,
				monitor);
		IModule[] childModules = earModule.getModules();
		GlassfishToolsPlugin.logMessage("assembleDirDeployedEARModule childModules.length=" + childModules.length);
		for (int i = 0; i < childModules.length; i++) {

			IModule childModule = childModules[i];
			String uri = earModule.getURI(childModule);
			if (uri == null) {
				IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0,
						"unable to assemble module null uri", null); //$NON-NLS-1$
				throw new CoreException(status);
			}
			IJ2EEModule jeeModule = (IJ2EEModule) childModule.loadAdapter(IJ2EEModule.class, monitor);
			if (jeeModule != null && jeeModule.isBinary()) {// Binary module just copy
				ProjectModule pm = (ProjectModule) childModule.loadAdapter(ProjectModule.class, null);
				IModuleResource[] resources = pm.members();
				publishHelper.publishToPath(resources, parent.append(uri), monitor);
				// was publishHelper.publishSmart(resources, parent, monitor);
				continue;// done! no need to go further
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
				// packModuleEARModule(module,uri, parent);
				final IModule[] childModulePath = new IModule[modulePath.length + 1];
				System.arraycopy(modulePath, 0, childModulePath, 0, modulePath.length);
				childModulePath[childModulePath.length - 1] = childModule;

				if (childModule.getModuleType().getId().equals("jst.web")) {//$NON-NLS-1$
					AssembleModules assembler = new AssembleModules(childModulePath, assembleRoot.append(uri), server,
							publishHelper);
					childNeedsARedeployment = (childNeedsARedeployment || assembler.needsARedeployment());
					assembler.assembleWebModule(new NullProgressMonitor());
				} else {
					// /*ludo super.*/packModule(module, uri, parent);
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
