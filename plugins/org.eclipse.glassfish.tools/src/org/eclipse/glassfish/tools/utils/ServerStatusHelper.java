/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.glassfish.tools.GlassFishRuntime;
import org.eclipse.glassfish.tools.GlassFishServer;
import org.eclipse.glassfish.tools.GlassFishServerBehaviour;
import org.eclipse.glassfish.tools.GlassfishToolsPlugin;
import org.eclipse.glassfish.tools.LastTaskEventListener;
import org.eclipse.glassfish.tools.ServerStatus;
import org.eclipse.glassfish.tools.sdk.TaskEvent;
import org.eclipse.glassfish.tools.sdk.admin.CommandLocation;
import org.eclipse.glassfish.tools.sdk.admin.CommandVersion;
import org.eclipse.glassfish.tools.sdk.admin.ResultMap;
import org.eclipse.glassfish.tools.sdk.admin.ResultString;
import org.eclipse.glassfish.tools.sdk.admin.ServerAdmin;
import org.eclipse.glassfish.tools.sdk.utils.ServerUtils;
import org.eclipse.wst.server.core.IServer;

public class ServerStatusHelper {
	
	public static ServerStatus checkServerStatus(GlassFishServer server) {
		try {
			Thread.sleep(Math.round(Math.random() * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
			return ServerStatus.NOT_DEFINED;
		}

		if (!ServerUtils.isAdminPortListening(server)) {
			return ServerStatus.STOPPED_NOT_LISTENING;
		} 
		
		if (server.isRemote()) {
			IServer server1 = server.getServer();
			String remoteServerVersion = GlassFishServerBehaviour.getVersion(server); 				
			GlassFishRuntime gfRuntime = (GlassFishRuntime) server1.getRuntime().loadAdapter(GlassFishRuntime.class, null);
			String thisServerVersion = gfRuntime.getVersion().toString();
			int n = thisServerVersion.indexOf(".X");
			if( n>0 )
				thisServerVersion = thisServerVersion.substring(0,n+1);
			if( remoteServerVersion!=null && remoteServerVersion.indexOf(thisServerVersion) <0 ){
				return ServerStatus.STOPPED_DOMAIN_NOT_MATCHING;
			}
		}
		
		//System.out.println("ServerStatusMonitor for " + server.getName() + " location");
		CommandLocation command = new CommandLocation();
		LastTaskEventListener listener = new LastTaskEventListener();
		ResultMap<String, String> result = null;
		Future<ResultMap<String, String>> locationTask = null;
		try {
			//t0 = System.currentTimeMillis();
			locationTask = ServerAdmin
					.<ResultMap<String, String>> exec(server, command, 
							 listener);
			result = locationTask.get(10, TimeUnit.SECONDS);
			//t1 = System.currentTimeMillis();
			//System.out.println("ServerStatusMonitor for " + server.getName() + " location took " + (t1-t0));
		} catch (InterruptedException e) {
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location interrupted");
		} catch (ExecutionException e) {
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location throws exception");
			e.printStackTrace();
		} catch (TimeoutException e) {
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location timed out");
		} 
		finally {
			if (result == null) {
				if (locationTask != null)
					locationTask.cancel(true);
				return ServerStatus.RUNNING_CONNECTION_ERROR;
			}
		}
		
		ServerStatus s = null;
		switch (result.getState()) {
		case COMPLETED:
			try {
				if (domainMatching(server, result.getValue())) {
					s = ServerStatus.RUNNING_DOMAIN_MATCHING;
				} else {
					s = ServerStatus.STOPPED_DOMAIN_NOT_MATCHING;
				}
			} catch (IOException e) {
				
			}
			break;
		case FAILED:
			if (isAuthException(listener.getLastEvent(), result)) {
				s = ServerStatus.RUNNING_CREDENTIAL_PROBLEM;
			} else if (isRemoteAdminException(result)) {
				s = ServerStatus.RUNNING_REMOTE_NOT_SECURE;
			} else if (listener.getLastEvent().equals(TaskEvent.BAD_GATEWAY)) {
				s = ServerStatus.RUNNING_PROXY_ERROR;
			} else {
				s = ServerStatus.RUNNING_CONNECTION_ERROR;
			}
			break;
		case RUNNING:
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location takes long time...");
			locationTask.cancel(true);
			s = ServerStatus.NOT_DEFINED;
			break;
		default:
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location in ready state");
			s = ServerStatus.NOT_DEFINED;
			break;
		}
//		if (!s.equals(ServerStatus.RUNNING_DOMAIN_MATCHING)) {
//			logLocationResult(result);
//		}
		return s;
	}
	
	private static ServerStatus checkServerStatusLocal(GlassFishServer server) {
		//System.out.println("ServerStatusMonitor for " + server.getName() + " location");
				CommandLocation command = new CommandLocation();
				LastTaskEventListener listener = new LastTaskEventListener();
				ResultMap<String, String> result = null;
				Future<ResultMap<String, String>> locationTask = null;
				try {
					//t0 = System.currentTimeMillis();
					locationTask = ServerAdmin
							.<ResultMap<String, String>> exec(server, command, 
									 listener);
					result = locationTask.get(10, TimeUnit.SECONDS);
					//t1 = System.currentTimeMillis();
					//System.out.println("ServerStatusMonitor for " + server.getName() + " location took " + (t1-t0));
				} catch (InterruptedException e) {
					GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location interrupted");
				} catch (ExecutionException e) {
					GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location throws exception");
					e.printStackTrace();
				} catch (TimeoutException e) {
					GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location timed out");
				} 
				finally {
					if (result == null) {
						if (locationTask != null)
							locationTask.cancel(true);
						return ServerStatus.RUNNING_CONNECTION_ERROR;
					}
				}
				
				ServerStatus s = null;
				switch (result.getState()) {
				case COMPLETED:
					try {
						if (domainMatching(server, result.getValue())) {
							s = ServerStatus.RUNNING_DOMAIN_MATCHING;
						} else {
							s = ServerStatus.STOPPED_DOMAIN_NOT_MATCHING;
						}
					} catch (IOException e) {
						
					}
					break;
				case FAILED:
					if (isAuthException(listener.getLastEvent(), result)) {
						s = ServerStatus.RUNNING_CREDENTIAL_PROBLEM;
					} else if (isRemoteAdminException(result)) {
						s = ServerStatus.RUNNING_REMOTE_NOT_SECURE;
					} else if (listener.getLastEvent().equals(TaskEvent.BAD_GATEWAY)) {
						s = ServerStatus.RUNNING_PROXY_ERROR;
					} else {
						s = ServerStatus.RUNNING_CONNECTION_ERROR;
					}
					break;
				case RUNNING:
					GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location takes long time...");
					locationTask.cancel(true);
					s = ServerStatus.NOT_DEFINED;
					break;
				default:
					GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location in ready state");
					s = ServerStatus.NOT_DEFINED;
					break;
				}
//				if (!s.equals(ServerStatus.RUNNING_DOMAIN_MATCHING)) {
//					logLocationResult(result);
//				}
				return s;
	}
	
	private static ServerStatus checkServerStatusRemote(GlassFishServer server) {
		CommandVersion command = new CommandVersion();
		LastTaskEventListener listener = new LastTaskEventListener();
		ResultString result = null;
		Future<ResultString> locationTask = null;
		try {
			//t0 = System.currentTimeMillis();
			locationTask = ServerAdmin
					.<ResultString> exec(server, command,
							 listener);
			result = locationTask.get(10, TimeUnit.SECONDS);
			//t1 = System.currentTimeMillis();
			//System.out.println("ServerStatusMonitor for " + server.getName() + " location took " + (t1-t0));
		} catch (InterruptedException e) {
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location interrupted");
		} catch (ExecutionException e) {
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location throws exception");
			e.printStackTrace();
		} catch (TimeoutException e) {
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location timed out");
		} 
		finally {
			if (result == null) {
				if (locationTask != null)
					locationTask.cancel(true);
				return ServerStatus.RUNNING_CONNECTION_ERROR;
			}
		}
		
		ServerStatus s = null;
		switch (result.getState()) {
		case COMPLETED:
			if (versionMatching(server, result.getValue())) {
				s = ServerStatus.RUNNING_DOMAIN_MATCHING;
			} else {
				s = ServerStatus.STOPPED_DOMAIN_NOT_MATCHING;
			}
			break;
		case FAILED:
			if (isAuthException(listener.getLastEvent(), result)) {
				s = ServerStatus.RUNNING_CREDENTIAL_PROBLEM;
			} else if (isRemoteAdminException(result)) {
				s = ServerStatus.RUNNING_REMOTE_NOT_SECURE;
			} else if (listener.getLastEvent().equals(TaskEvent.BAD_GATEWAY)) {
				s = ServerStatus.RUNNING_PROXY_ERROR;
			} else {
				s = ServerStatus.RUNNING_CONNECTION_ERROR;
			}
			break;
		case RUNNING:
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location takes long time...");
			locationTask.cancel(true);
			s = ServerStatus.NOT_DEFINED;
			break;
		default:
			GlassfishToolsPlugin.logMessage("ServerStatusMonitor for " + server.getName() + " location in ready state");
			s = ServerStatus.NOT_DEFINED;
			break;
		}
//		if (!s.equals(ServerStatus.RUNNING_DOMAIN_MATCHING)) {
//			logLocationResult(result);
//		}
		return s;
	}
	
	private static boolean versionMatching(GlassFishServer server, String version) {
		IServer server1 = server.getServer();
		GlassFishRuntime gfRuntime =  server1.getRuntime().getAdapter(GlassFishRuntime.class);
		String thisServerVersion = gfRuntime.getVersion().toString();
		int n = thisServerVersion.indexOf(".X");
		if( n>0 )
			thisServerVersion = thisServerVersion.substring(0,n+1);
		if( version !=null && version.indexOf(thisServerVersion) <0 ){
			return false;
		}
		return true;
	}
	
	private static boolean domainMatching(GlassFishServer server, Map<String, String> locationResult) throws IOException {
		if (server.isRemote())
			return true;
		String expectedDomainRoot = server.getDomainsFolder() + File.separator
				+ server.getDomainName();
		String actualDomainRoot = locationResult.get(
				"Domain-Root_value");
		if ((expectedDomainRoot != null) && (actualDomainRoot != null)) {
			File expected = new File(expectedDomainRoot);
			File actual = new File(actualDomainRoot);
			if (expected.getCanonicalPath().equals(actual.getCanonicalPath())) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean isAuthException(TaskEvent event, ResultMap<String, String> result) {
		// for now handle remote admin access exception as auth issue
		return event.equals(TaskEvent.AUTH_FAILED) ||
						((result.getValue() != null) && (result.getValue().get("message") != null) && (
								result.getValue().get("message").contains("javax.security.auth.login.LoginException")));
	}
	
	private static boolean isAuthException(TaskEvent event, ResultString result) {
		// for now handle remote admin access exception as auth issue
		return event.equals(TaskEvent.AUTH_FAILED) ||
						((result.getValue() != null) && (result.getValue().contains("javax.security.auth.login.LoginException")));
	}
	
	private static boolean isRemoteAdminException(ResultMap<String, String> result) {
		return (result.getValue() != null) && 
				(result.getValue().get("message") != null) &&
				result.getValue().get("message").contains("org.glassfish.internal.api.RemoteAdminAccessException");
	}
	
	private static boolean isRemoteAdminException(ResultString result) {
		return (result.getValue() != null) && 
				(result.getValue() != null) &&
				result.getValue().contains("org.glassfish.internal.api.RemoteAdminAccessException");
	}
	
//	private static void logLocationResult(ResultMap<String, String> res) {
//		GlassfishToolsPlugin.logMessage("Location command result state: " + res.getState());
//		if (res.getValue() != null) {
//			for (String key : res.getValue().keySet()) {
//				GlassfishToolsPlugin.logMessage(key + ": "
//						+ res.getValue().get(key));
//			}
//		}
//		
//	}

}
