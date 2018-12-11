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

package org.eclipse.payara.tools.sdk.admin.cloud;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.payara.tools.sdk.PayaraIdeException;
import org.eclipse.payara.tools.sdk.TaskStateListener;
import org.eclipse.payara.tools.sdk.admin.Command;
import org.eclipse.payara.tools.sdk.admin.ResultString;
import org.eclipse.payara.tools.sdk.admin.ServerAdmin;
import org.eclipse.payara.tools.server.PayaraServer;

/**
 * This class provides convenience methods for working with cloud (CPAS server).
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CloudTasks {

    /**
     * Deploy task that deploys application on server.
     *
     * @param server - server to deploy on
     * @param account - which account the application is deployed under
     * @param application - File object representing archive or directory where the application is
     * @param listener - listener, that listens to command execution events
     * @return result object with task status and message
     */
    public static ResultString deploy(final PayaraServer server,
            final String account, final File application,
            final TaskStateListener listener) {
        Command command = new CommandCloudDeploy(account, application);
        Future<ResultString> future = ServerAdmin.<ResultString>exec(server, command, listener);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PayaraIdeException(
                    "Instance or cluster stop failed.", e);
        }
    }
}
