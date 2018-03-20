/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.internal;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.payara.tools.server.GlassFishServer;
import org.eclipse.payara.tools.server.ServerStatus;
import org.eclipse.payara.tools.utils.ServerStatusHelper;

public class ServerStatusMonitor implements Runnable {

	private static final int DEFAULT_DELAY_IN_SEC = 5;
	
	private ScheduledExecutorService scheduler;
	
	private GlassFishServer server;
	
	private int delay;
	
	private ScheduledFuture<?> scheduledTask;
	
	private volatile ServerStatus status = ServerStatus.NOT_DEFINED;
	
	private CopyOnWriteArrayList<ServerStateListener> listeners = null;
	
	private ServerStatusMonitor(GlassFishServer server) {
		this(server, DEFAULT_DELAY_IN_SEC);
	}
	
	private ServerStatusMonitor(GlassFishServer server, ServerStateListener... listeners) {
		this(server, DEFAULT_DELAY_IN_SEC, listeners);
	}
	
	private ServerStatusMonitor(GlassFishServer server, int checkInterval, ServerStateListener... listeners) {
		this.server = server;
		this.delay = checkInterval;
		this.listeners = new CopyOnWriteArrayList<ServerStateListener>(listeners);
	}
	
	public static ServerStatusMonitor getInstance(GlassFishServer server) {
		return new ServerStatusMonitor(server);
	}
	
	public static ServerStatusMonitor getInstance(GlassFishServer server, ServerStateListener... listeners) {
		return new ServerStatusMonitor(server, listeners);
	}
	
	public static ServerStatusMonitor getInstance(GlassFishServer server, int checkInterval) {
		return new ServerStatusMonitor(server, checkInterval);
	}
	
	
	public void start() {
		//System.out.println("ServerStatusMonitor for " + server.getName() + " started");
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduledTask = scheduler.scheduleWithFixedDelay(this, 0, delay, TimeUnit.SECONDS);
	}
	
	public void stop() {
		//System.out.println("ServerStatusMonitor for " + server.getName() + " stopped");
		scheduledTask.cancel(true);
		scheduler.shutdown();
	}

	@Override
	public void run() {
		//System.out.println("ServerStatusMonitor for " + server.getName() + " run");
		
		//Check server version
		//long t0 = System.currentTimeMillis();
		status = ServerStatusHelper.checkServerStatus(server);
		//long t1 = System.currentTimeMillis();
		//System.out.println("ServerStatusMonitor for " + server.getName() + " check, took " + (t1 - t0));
		notifyListeners(status);
		//t1 = System.currentTimeMillis();
		//System.out.println("ServerStatusMonitor for " + server.getName() + " run, took " + (t1 - t0));
	}
	
	public ServerStatus getServerStatus(boolean forceUpdate) {
		if (forceUpdate) {
			Future<?> f = scheduler.submit(this);
			try {
				f.get();
			} catch (InterruptedException e) {}
			catch (ExecutionException e) {}
		}
		return status; 
	}
	
	public ServerStatus getServerStatus() {
		return getServerStatus(false);
	}
	
	private void notifyListeners(ServerStatus newStatus) {
		//System.out.println("Notifying listeners about new status of " + server.getName());
		for (ServerStateListener listener : listeners) {
			listener.serverStatusChanged(newStatus);
		}
	}
	
	public void registerServerStatusListener(ServerStateListener listener) {
		listeners.add(listener);
	}
	
}
