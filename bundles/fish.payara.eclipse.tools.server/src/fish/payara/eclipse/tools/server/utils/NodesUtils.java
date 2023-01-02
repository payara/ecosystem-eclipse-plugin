/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.utils;

import static fish.payara.eclipse.tools.server.sdk.TaskState.COMPLETED;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Level.INFO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.TaskState;
import fish.payara.eclipse.tools.server.sdk.admin.Command;
import fish.payara.eclipse.tools.server.sdk.admin.CommandListComponents;
import fish.payara.eclipse.tools.server.sdk.admin.CommandListResources;
import fish.payara.eclipse.tools.server.sdk.admin.CommandListWebServices;
import fish.payara.eclipse.tools.server.sdk.admin.ResultList;
import fish.payara.eclipse.tools.server.sdk.admin.ResultMap;
import fish.payara.eclipse.tools.server.sdk.admin.ServerAdmin;
import fish.payara.eclipse.tools.server.serverview.AppDesc;
import fish.payara.eclipse.tools.server.serverview.ResourceDesc;
import fish.payara.eclipse.tools.server.serverview.WSDesc;

public class NodesUtils {

    public static List<ResourceDesc> getResources(PayaraServer server, String type) {
        List<String> result = Collections.emptyList();
        LinkedList<ResourceDesc> retVal = null;
        try {
            Command command = new CommandListResources(CommandListResources.command(type), null);
            Future<ResultList<String>> future = ServerAdmin.<ResultList<String>>exec(server, command);
            ResultList<String> res = future.get();
            if (COMPLETED.equals(res.getState())) {
                result = res.getValue();
            }
            retVal = new LinkedList<>();
            for (String rsc : result) {
                retVal.add(new ResourceDesc(rsc, type));
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);
        }
        return retVal;
    }

    public static Map<String, List<AppDesc>> getApplications(PayaraServer server, String container) {
        Map<String, List<String>> apps = Collections.emptyMap();
        Command command = new CommandListComponents(null);
        Future<ResultMap<String, List<String>>> future = ServerAdmin.<ResultMap<String, List<String>>>exec(server,
                command);
        ResultMap<String, List<String>> result = null;
        try {
            result = future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.getLogger("glassfish").log(Level.INFO, e.getMessage(), e); // NOI18N
        } catch (ExecutionException e) {
            Logger.getLogger("glassfish").log(Level.INFO, e.getMessage(), e); // NOI18N
        } catch (TimeoutException e) {
            Logger.getLogger("glassfish").log(Level.INFO, e.getMessage(), e); // NOI18N
        }
        if (result != null && result.getState().equals(TaskState.COMPLETED)) {
            apps = result.getValue();
        }
        if (apps == null || apps.isEmpty()) {
            return Collections.emptyMap();
        }

        return processApplications(apps);
    }

    private static Map<String, List<AppDesc>> processApplications(Map<String, List<String>> appsList) {
        Map<String, List<AppDesc>> result = new HashMap<>();
        for (final Map.Entry<String, List<String>> entry : appsList.entrySet()) {
            final String engine = entry.getKey();
            final List<String> apps = entry.getValue();
            for (int i = 0; i < apps.size(); i++) {
                String name = apps.get(i).trim();

                List<AppDesc> appList = result.get(engine);
                if (appList == null) {
                    appList = new ArrayList<>();
                    result.put(engine, appList);
                }
                appList.add(new AppDesc(name, null, null, engine));
            }
        }
        return result;
    }

    /**
     * Sends list-web-services command to server (synchronous)
     *
     * @return String array of names of deployed applications.
     */
    public static List<WSDesc> getWebServices(PayaraServer server) {
        List<String> wssList = null;

        Future<ResultList<String>> future = ServerAdmin.<ResultList<String>>exec(server, new CommandListWebServices());

        ResultList<String> result = null;
        try {
            result = future.get(10, SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Logger.getLogger("glassfish").log(INFO, e.getMessage(), e); // NOI18N
        }

        if (result != null && result.getState().equals(COMPLETED)) {
            wssList = result.getValue();
        }

        if (wssList == null || wssList.isEmpty()) {
            return emptyList();
        }

        return processWebServices(wssList);
    }

    private static List<WSDesc> processWebServices(List<String> wssList) {
        List<WSDesc> result = new ArrayList<>();
        for (String a : wssList) {
            result.add(new WSDesc(a, a + "?wsdl", a + "?Tester")); // NOI18N
        }
        return result;
    }

    public static Map<String, String> getResourceData(PayaraServer server, String name) {
        return getResourceData(server, name);
    }

    public static void putResourceData(PayaraServer server, Map<String, String> data)
            throws PartialCompletionException {
        putResourceData(server, data);
    }

}
