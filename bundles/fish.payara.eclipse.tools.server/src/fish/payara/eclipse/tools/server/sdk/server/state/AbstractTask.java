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

package fish.payara.eclipse.tools.server.sdk.server.state;

import fish.payara.eclipse.tools.server.sdk.TaskEvent;
import fish.payara.eclipse.tools.server.sdk.TaskState;
import fish.payara.eclipse.tools.server.sdk.TaskStateListener;
import fish.payara.eclipse.tools.server.sdk.data.GlassFishStatusCheck;
import fish.payara.eclipse.tools.server.sdk.logging.Logger;

/**
 * Abstract task for server status verification.
 * <p/>
 *
 * @author Tomas Kraus
 */
public abstract class AbstractTask implements Runnable {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(AbstractTask.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Server status check job internal data. */
    final StatusJob job;

    /** Individual status check task data. */
    final StatusJob.Task task;

    /** Internal job status when this task was created. */
    final StatusJobState jobState;

    /** Server status check type. */
    final GlassFishStatusCheck type;

    /** Listeners that want to know about command state. */
    final TaskStateListener[] stateListeners;

    /** Cancellation notification. */
    boolean cancelled;

    /**
     * Creates an instance of abstract task for server status verification.
     * <p/>
     *
     * @param job Server status check job internal data.
     * @param task Individual status check task data.
     * @param type Server status check type.
     */
    AbstractTask(final StatusJob job, final StatusJob.Task task,
            final GlassFishStatusCheck type) {
        this.job = job;
        this.task = task;
        this.jobState = job.getState();
        this.type = type;
        this.stateListeners = task.getListeners();
        this.cancelled = false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Mark this task as canceled.
     * <p/>
     * Listeners won't be notified about server status verification task state change after task was
     * canceled.
     */
    void cancel() {
        cancelled = true;
    }

    /**
     * Notify all registered task state listeners server status verification task state change.
     * <p/>
     * This method should be used after task is submitted into <code>ExecutorService</code>.
     * <p/>
     *
     * @param taskState New task execution state.
     * @param taskEvent Event related to execution state change.
     * @param args Additional arguments.
     */
    void handleStateChange(final TaskState taskState,
            final TaskEvent taskEvent, final String... args) {
        if (stateListeners != null && !cancelled) {
            for (TaskStateListener stateListener : stateListeners) {
                if (stateListener != null) {
                    stateListener.operationStateChanged(taskState,
                            taskEvent, args);
                }
            }
        }
    }

}
