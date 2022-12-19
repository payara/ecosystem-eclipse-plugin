/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.sdk.data;

import java.util.List;
import java.util.Map;

/**
 * GlassFish Server Entity.
 * <p/>
 * Local GlassFish Server entity instance which is used when not defined in IDE.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class StartupArgsEntity implements StartupArgs {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Command line arguments passed to bootstrap jar. */
    private List<String> glassfishArgs;

    /** Command line arguments passed to JVM. */
    private List<String> javaArgs;

    /** Environment variables set before JVM execution. */
    private Map<String, String> environmentVars;

    /** Installation home of Java SDK used to run GlassFish. */
    private String javaHome;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs empty class instance. No default values are set.
     */
    public StartupArgsEntity() {
    }

    /**
     * Constructs class instance with all values supplied.
     * <p/>
     *
     * @param glassfishArgs Command line arguments passed to bootstrap jar.
     * @param javaArgs Command line arguments passed to JVM.
     * @param environmentVars Environment variables set before JVM execution.
     * @param javaHome Installation home of Java SDK used to run GlassFish.
     */
    public StartupArgsEntity(List<String> glassfishArgs, List<String> javaArgs,
            Map<String, String> environmentVars, String javaHome) {
        this.glassfishArgs = glassfishArgs;
        this.javaArgs = javaArgs;
        this.environmentVars = environmentVars;
        this.javaHome = javaHome;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get command line arguments passed to bootstrap jar.
     * <p/>
     *
     * @return Command line arguments passed to bootstrap jar.
     */
    @Override
    public List<String> getGlassfishArgs() {
        return glassfishArgs;
    }

    /**
     * Set command line arguments passed to bootstrap jar.
     * <p/>
     *
     * @param glassfishArgs Command line arguments passed to bootstrap jar.
     */
    public void setGlassfishArgs(List<String> glassfishArgs) {
        this.glassfishArgs = glassfishArgs;
    }

    /**
     * Get command line arguments passed to JVM.
     * <p/>
     *
     * @return Command line arguments passed to JVM.
     */
    @Override
    public List<String> getJavaArgs() {
        return javaArgs;
    }

    /**
     * Set command line arguments passed to JVM.
     * <p/>
     *
     * @param javaArgs Command line arguments passed to JVM.
     */
    public void getJavaArgs(List<String> javaArgs) {
        this.javaArgs = javaArgs;
    }

    /**
     * Get environment variables set before JVM execution.
     * <p/>
     *
     * @return Environment variables set before JVM execution.
     */
    @Override
    public Map<String, String> getEnvironmentVars() {
        return environmentVars;
    }

    /**
     * Set environment variables set before JVM execution.
     * <p/>
     *
     * @param environmentVars Environment variables set before JVM execution.
     */
    public void setEnvironmentVars(Map<String, String> environmentVars) {
        this.environmentVars = environmentVars;
    }

    /**
     * Get installation home of Java SDK used to run GlassFish.
     * <p/>
     *
     * @return Installation home of Java SDK used to run GlassFish.
     */
    @Override
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * Set installation home of Java SDK used to run GlassFish.
     * <p/>
     *
     * @param javaHome Installation home of Java SDK used to run GlassFish.
     */
    public void getJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

}
