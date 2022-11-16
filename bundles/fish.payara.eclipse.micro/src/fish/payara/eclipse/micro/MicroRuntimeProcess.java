/**
 * Copyright (c) 2020-2022 Payara Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package fish.payara.eclipse.micro;

import static fish.payara.eclipse.micro.Messages.errorInInitializingMicroWatcher;
import static fish.payara.eclipse.micro.Messages.errorInReloadingMicro;
import static fish.payara.eclipse.micro.Messages.errorInTerminatingMicro;
import static fish.payara.eclipse.micro.MicroConstants.ATTR_RELOAD_ARTIFACT;
import static fish.payara.eclipse.micro.MicroConstants.HOT_DEPLOY_ARTIFACT;
import static fish.payara.eclipse.micro.MicroConstants.PROJECT_NAME_ATTR;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.RuntimeProcess;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

public class MicroRuntimeProcess extends RuntimeProcess {

	private ILaunch debuggerConnection;

	private IResourceChangeListener sourceChangeListener;

	private static final Logger LOG = Logger.getLogger(MicroRuntimeProcess.class.getName());

	public MicroRuntimeProcess(ILaunch launch, Process process, String name, Map<String, String> attributes) {
		super(launch, process, name, attributes);
		watchResources(launch.getLaunchConfiguration(), process);
	}

	ILaunch getDebuggerConnection() {
		return debuggerConnection;
	}

	void setDebuggerConnection(ILaunch debuggerConnection) {
		this.debuggerConnection = debuggerConnection;
	}

	private void watchResources(ILaunchConfiguration config, Process process) {
		try {
			final String projectName = config.getAttribute(PROJECT_NAME_ATTR, "");
			String reloadArtifact = config.getAttribute(ATTR_RELOAD_ARTIFACT, "");
			if (!reloadArtifact.isEmpty()) {
				this.sourceChangeListener = new IResourceChangeListener() {
					public void resourceChanged(IResourceChangeEvent event) {
						List<IFile> filesChanged = getAllAffectedResources(event.getDelta(), IFile.class,
								IResourceDelta.CHANGED, projectName);
						boolean hotDeploy = reloadArtifact.equals(HOT_DEPLOY_ARTIFACT);
						List<String> sourcesChanged = new ArrayList<>();
						boolean metadataChanged = false;
						if (hotDeploy) {
							for (IFile fileChanged : filesChanged) {
								String path = fileChanged.getFullPath().toString();
								String ext = fileChanged.getFileExtension();
								if (!path.endsWith("class")) {
									sourcesChanged.add(path);
								}
								if (ext.equals("xml") || ext.equals("properties")) {
									metadataChanged = true;
								}
							}
						}
						if (!filesChanged.isEmpty()) {
							try {
								IProject project = filesChanged.get(0).getProject();
								BuildTool tool = BuildTool.getToolSupport(project);
								List<String> commands = new ArrayList<>();
								commands.add(tool.getExecutableHome());
								commands.addAll(tool.getReloadCommand(hotDeploy, sourcesChanged, metadataChanged));
								ProcessBuilder pb = new ProcessBuilder(commands);
								pb.directory(new File(project.getLocationURI()));
								pb.redirectErrorStream(true);
								pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
								pb.start();
							} catch (Exception ex) {
								LOG.log(Level.SEVERE, errorInReloadingMicro, ex);
							}
						}
					}
				};
				ResourcesPlugin.getWorkspace().addResourceChangeListener(this.sourceChangeListener,
						IResourceChangeEvent.POST_BUILD);
			}
		} catch (CoreException ex) {
			LOG.log(Level.SEVERE, errorInInitializingMicroWatcher, ex);
		}
	}

	private <T> List<T> getAllAffectedResources(IResourceDelta delta, Class<T> clazz, int deltaKind,
			String projectName) {
		List<T> files = new ArrayList<T>();

		for (IResourceDelta child : delta.getAffectedChildren()) {
			IResource resource = child.getResource();

			if (resource.getProject().getName().equals(projectName)) {

				if (resource != null && clazz.isAssignableFrom(resource.getClass())) {
					if ((child.getKind() & deltaKind) != 0) {
						files.add((T) resource);
					}
				} else {
					files.addAll(getAllAffectedResources(child, clazz, deltaKind, projectName));
				}
			}
		}
		return files;
	}

	@Override
	public void terminate() throws DebugException {
		terminateInstance();
		super.terminate();
		disconnectDebuggerConnection();
		if (this.sourceChangeListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this.sourceChangeListener);
		}
	}

	private void disconnectDebuggerConnection() throws DebugException {
		if (debuggerConnection != null && debuggerConnection instanceof IDisconnect
				&& ((IDisconnect) debuggerConnection).canDisconnect()) {
			((IDisconnect) debuggerConnection).disconnect();
		}
	}

	private void terminateInstance() {
		try {
			Process process = (Process) getSystemProcess();
			long pid;
			try {
				Method method = process.getClass().getDeclaredMethod("pid");
				method.setAccessible(true);
				pid = (long) method.invoke(process);
			} catch (NoSuchMethodException ex) {
				Field field = process.getClass().getDeclaredField("handle");
				field.setAccessible(true);
				long handleValue = field.getLong(process);
				Kernel32 kernel = Kernel32.INSTANCE;
				WinNT.HANDLE handle = new WinNT.HANDLE();
				handle.setPointer(Pointer.createConstant(handleValue));
				pid = kernel.GetProcessId(handle);
			}
			killProcess(String.valueOf(pid));
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, errorInTerminatingMicro, ex);
		}
	}

	private void killProcess(String processId) throws IOException, InterruptedException {
		String command;
		final Runtime re = Runtime.getRuntime();
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			command = "taskkill /F /T /PID " + processId;
		} else {
			command = "kill " + processId;
		}
		Process killProcess = re.exec(command);
		int result = killProcess.waitFor();
		if (result != 0) {
			LOG.severe(errorInTerminatingMicro);
		}
	}
}
