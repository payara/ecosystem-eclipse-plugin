/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.utils;

import static org.eclipse.core.runtime.jobs.Job.SHORT;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Utility class for running jobs
 * 
 * @author Arjan Tijms
 *
 */
public class Jobs {
    
    /**
     * Schedules the <code>runnable</code> to be run via a <code>Job</code> 
     * 
     * <p>
     * The job is added to a queue of waiting jobs with the priority for short background jobs, 
     * and will be run when it arrives at the beginning of the queue.
     * 
     * <p>
     * The monitor is started and set at 100 steps, and closed after the <code>runnable</code> executes.
     * 
     * @param name the name of the job
     * @param runnable  the runnable to execute
     */
    public static void scheduleShortJob(String name, ICoreRunnable runnable) {
        Job job = Job.create(name, 
            monitor -> {
                try {
                    monitor.beginTask(name, 100);
                    runnable.run(monitor);
                } finally {
                    monitor.done();
                }
           });
        
        job.setPriority(SHORT);
        job.schedule();
    }

}
