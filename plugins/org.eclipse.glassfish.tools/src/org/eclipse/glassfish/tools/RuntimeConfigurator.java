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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.glassfish.tools.exceptions.UniqueNameNotFound;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;

@SuppressWarnings("restriction")
public class RuntimeConfigurator {

	//
	private String serverID;
	private String runtimeName;
	private File serverLocation;
	private String domainName;

	/**
	 * @param serverID
	 */
	public RuntimeConfigurator(File serverLocation, String serverID,
			String runtimeName, String domainName) {
		this.serverID = serverID;
		this.serverLocation = serverLocation;
		this.runtimeName = runtimeName;
		this.domainName = domainName;
	}

	public void configure() throws CoreException {

		File glassfishLocation = new File(serverLocation, "glassfish");

		IRuntime alreadyThere = getRuntimeByLocation(glassfishLocation);
		if (alreadyThere != null) {
			GlassfishToolsPlugin.logMessage("Already Registered: "
					+ glassfishLocation);
			return;
		}
		GlassfishToolsPlugin.logMessage("Not  Registered yet : "
				+ glassfishLocation.getAbsolutePath());

		// deleteOldGlassFishInternalRuntimes(glassfishLocation);
		// GlassfishToolsPlugin.logMessage("done with deleting obsolete runtimes : ",
		// null);

		IServerType st = ServerCore.findServerType(serverID);// v3
		IRuntime runtime;
		try {
			runtime = createRuntime(runtimeName,
					glassfishLocation.getAbsolutePath());
		} catch (UniqueNameNotFound e) {
			throw new CoreException(new Status(IStatus.ERROR,
					GlassfishToolsPlugin.SYMBOLIC_NAME, NLS.bind(
							Messages.uniqueNameNotFound, "Runtime"), e));
		}
		IServer[] servers = ServerCore.getServers();

		for (IServer server : servers) {
			if (server.getRuntime() == null) {
				server.delete();
			}
			if (runtime != null 
					&& runtime.equals(server.getRuntime())) {
				// IRuntime ir = runtime.createWorkingCopy();
				// if (ir instanceof RuntimeWorkingCopy){
				// RuntimeWorkingCopy wc = (RuntimeWorkingCopy) ir;
				// wc.setLocation(new Path("/"));
				// wc.save(true, null);
				// }

				// / return ;
			}
		}

		IServerWorkingCopy wc = st.createServer(null, null, runtime, null);
		try {
			wc.setName(createUniqueServerName(runtime.getName()));
		} catch (UniqueNameNotFound e) {
			throw new CoreException(new Status(IStatus.ERROR,
					GlassfishToolsPlugin.SYMBOLIC_NAME, NLS.bind(
							Messages.uniqueNameNotFound, "Server"), e));
		}
		GlassFishServer sunAppServer = (GlassFishServer) wc
				.getAdapter(GlassFishServer.class);

		String expectedDomainLocation = getDomainLocation();
		File dom = new File(expectedDomainLocation);
		if (!dom.exists()) {
			copyDirectory(
					new File(serverLocation, "/glassfish/domains/domain1"),
					new File(expectedDomainLocation));
		} else {// domain exists!! clean the osgi dir as a precaution, it might
				// have been used by another server
			deleteOSGICacheDirectory(new File(dom, "osgi-cache"));
		}

		wc.setAttribute(GlassFishServer.ATTR_DOMAINPATH, expectedDomainLocation);
		wc.setAttribute(GlassFishServer.ATTR_USEANONYMOUSCONNECTIONS, true);
		wc.save(true, null);

		return;
	}

	private String getDomainLocation() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath location = root.getLocation();
		return "" + location + File.separator + domainName;
	}

	private IRuntime getRuntimeByLocation(File glassfishLocation) {

		IServerType st = ServerCore.findServerType(serverID);
		IRuntime[] runtimes = ServerCore.getRuntimes();
		ServerCore.getRuntimeTypes();
		ServerCore.getServers();
		for (IRuntime runtime : runtimes) {
			if (runtime != null
					&& runtime.getRuntimeType().equals(st.getRuntimeType())) {
				File currentlocation = new File("" + runtime.getLocation());
				if (currentlocation.equals(glassfishLocation))
					return runtime;
			}
		}
		return null;
	}

	static private boolean deleteOSGICacheDirectory(File osgicache) {
		if (osgicache.exists()) {
			File[] files = osgicache.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteOSGICacheDirectory(files[i]);
				} else {
					if( !files[i].delete() )
						return false;
				}
			}
		}
		return (osgicache.delete());
	}

	private IRuntime createRuntime(String runtimeName, String glassfishLocation)
			throws UniqueNameNotFound {
		try {
			IServerType st = ServerCore.findServerType(serverID);
			// IRuntime[] runtimes = ServerCore.getRuntimes();
			// ServerCore.getServers();
			// for (IRuntime runtime : runtimes) {
			// String fff = "" + runtime.getLocation();
			// GlassfishToolsPlugin.logMessage("loop in createRuntime : " + fff,
			// null);
			// if (runtime.getRuntimeType().equals(st.getRuntimeType())) {
			// if (fff.equals(glassfishLocation)) {
			// GlassfishToolsPlugin.logMessage("ALREREEEEEDDDD : "
			// + glassfishLocation, null);
			// return runtime;
			// }
			// }
			// }

			IRuntimeWorkingCopy wc;
			GlassfishToolsPlugin.logMessage("before Creating working copy : ");
			wc = st.getRuntimeType().createRuntime(null, null);
			wc.setName(createUniqueRuntimeName(runtimeName));
			GlassfishToolsPlugin.logMessage("Creating working copy : " + wc);

			GlassfishToolsPlugin.logMessage("Creating NEW : "
					+ glassfishLocation);
			
//			gRun.setServerDefinitionId(gRun.getRuntime().getRuntimeType()
//					.getId());
			wc.setLocation(new Path(glassfishLocation));
			GlassfishToolsPlugin.logMessage("pre saving new runtime : "
					+ glassfishLocation);
			return wc.save(true, null);
		} catch (CoreException e) {
			GlassfishToolsPlugin.logError("core exception : "
					+ glassfishLocation, e);

		}
		return null;
	}

	public static String createUniqueRuntimeName(String runtimeName)
			throws UniqueNameNotFound {
		IRuntime[] runtimes = ServerCore.getRuntimes();
		HashSet<String> takenNames = new HashSet<String>(runtimes.length);
		for (IRuntime runtime : runtimes) {
			takenNames.add(runtime.getName());
		}
		return createUniqueName(runtimeName, takenNames);
	}

	public static String createUniqueServerName(String serverName)
			throws UniqueNameNotFound {
		IServer[] servers = ServerCore.getServers();
		HashSet<String> takenNames = new HashSet<String>(servers.length);
		for (IServer server : servers) {
			takenNames.add(server.getName());
		}
		return createUniqueName(serverName, takenNames);
	}

	private static String createUniqueName(String candidadeName, Set<String> takenNames)
			throws UniqueNameNotFound {
		if (!takenNames.contains(candidadeName))
			return candidadeName;
		for (int i = 2; i < Integer.MAX_VALUE; i++) {
			String candidadeNameWithSuffix = candidadeName + " (" + i + ")";
			if (!takenNames.contains(candidadeNameWithSuffix))
				return candidadeNameWithSuffix;
		}
		throw new UniqueNameNotFound();
	}

	private void copyDirectory(File sourceLocation, File targetLocation) {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				boolean isCreated = targetLocation.mkdir();
				if(!isCreated)
					GlassfishToolsPlugin.logMessage("Failed to create : " + targetLocation.getAbsolutePath());
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = new FileInputStream(sourceLocation);
				out = new FileOutputStream(targetLocation);
				byte[] buf = new byte[10240];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} catch (IOException e) {
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
				}
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
				}
			}

		}

	}

}
