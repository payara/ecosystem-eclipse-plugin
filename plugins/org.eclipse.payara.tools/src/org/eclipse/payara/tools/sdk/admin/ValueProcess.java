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

package org.eclipse.payara.tools.sdk.admin;

/**
 * GlassFish server process.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ValueProcess {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** The name of the executable to run. */
    private String processName;

    /** Arguments passed to the executable. */
    private String arguments;

    /** Process information. */
    private Process process;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish server process entity.
     * <p/>
     * Entity is initialized in <code>RunnerLocal</code> method <code>call</code>. method.
     * <p/>
     *
     * @param processName The name of the executable to run..
     * @param arguments Arguments passed to the executable.
     */
    ValueProcess(String processName, String arguments, Process process) {
        this.processName = processName;
        this.arguments = arguments;
        this.process = process;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get name of the executable to run.
     * <p/>
     *
     * @return Name of the executable to run.
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * Get arguments passed to the executable.
     * <p/>
     *
     * @return Arguments passed to the executable.
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Get process information.
     * <p/>
     *
     * @return Process information.
     */
    public Process getProcess() {
        return process;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert stored process information to <code>String</code>.
     * <p>
     *
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        int length = (processName != null ? processName.length() : 0) +
                (arguments != null ? arguments.length() : 0) + 1;
        StringBuilder sb = new StringBuilder(length);
        if (processName != null) {
            sb.append(processName);
        }
        sb.append(' ');
        if (arguments != null) {
            sb.append(arguments);
        }
        return sb.toString();
    }

}
