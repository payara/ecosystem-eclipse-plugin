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

package fish.payara.eclipse.tools.server.sdk.admin;

import static fish.payara.eclipse.tools.server.sdk.TaskEvent.CMD_COMPLETED;
import static fish.payara.eclipse.tools.server.sdk.TaskEvent.JAVA_VM_EXEC_FAILED;
import static fish.payara.eclipse.tools.server.sdk.TaskEvent.NO_JAVA_VM;
import static fish.payara.eclipse.tools.server.sdk.TaskState.COMPLETED;
import static fish.payara.eclipse.tools.server.sdk.TaskState.FAILED;
import static fish.payara.eclipse.tools.server.sdk.utils.JavaUtils.VM_CLASSPATH_OPTION;
import static fish.payara.eclipse.tools.server.sdk.utils.JavaUtils.javaVmExecutableFullPath;
import static fish.payara.eclipse.tools.server.sdk.utils.JavaUtils.javaVmVersion;
import static fish.payara.eclipse.tools.server.sdk.utils.OsUtils.parseParameters;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.GFV3_JAR_MATCHER;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.getDomainConfigPath;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.getJarName;
import static fish.payara.eclipse.tools.server.sdk.utils.Utils.quote;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.utils.JavaUtils;

/**
 * GlassFish server administration command execution using local file access interface.
 * <p/>
 * Class implements GlassFish server administration functionality trough local file access
 * interface.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerLocal extends RunnerJava {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish main class to be started when using classpath. */
    private static final String MAIN_CLASS = "com.sun.enterprise.glassfish.bootstrap.ASMain";

    private static final Logger LOGGER = Logger.getLogger(RunnerLocal.class.getName());

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    final CommandStartDAS command;

    /**
     * GlassFish admin command result containing process information.
     * <p/>
     * Result instance life cycle is started with submitting task into <code>ExecutorService</code>'s
     * queue. method <code>call()</code> is responsible for correct <code>TaskState</code> and value
     * handling.
     */
    ResultProcess result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using local file access interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish Server Administration Command Entity.
     */
    public RunnerLocal(PayaraServer server, Command command) {
        super(server, command);
        this.command = (CommandStartDAS) command;
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() method //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * GlassFish server administration command execution call. This is an entry point from
     * <code>executor<code>'s serialization queue.
    * <p/>
    * Attempts to start local GalssFish DAS directly using <code>java</code> executable.
     * <p/>
     *
     * @return Task execution state.
     */
    @Override
    public Result<ValueProcess> call() {
        String javaVmExe = javaVmExecutableFullPath(command.javaHome);
        File javaVmFile = new File(javaVmExe);

        // Java VM executable should exist.
        if (!javaVmFile.exists()) {
            LOGGER.log(INFO, "Java VM {0} executable for {1} was not found",
                    new Object[] { javaVmFile.getAbsolutePath(), server.getName() });

            return handleStateChange(FAILED, NO_JAVA_VM, command.getCommand(), server.getName());
        }

        // Java VM should be 1.6.0_0 or greater.
        checkJavaVersion(javaVmFile);

        String allArgs = buildJavaOptions(server, command);
        LOGGER.log(FINEST, "Starting {0} using Java VM {1} and arguments {2}",
                new Object[] { server.getName(), javaVmExe, allArgs });

        ProcessBuilder processBuilder = new ProcessBuilder(parseParameters(javaVmExe, allArgs));
        processBuilder.redirectErrorStream(true);
        setProcessCurrentDir(processBuilder);
        setJavaEnvironment(processBuilder.environment(), command);

        try {
            result.value = new ValueProcess(javaVmFile.getAbsolutePath(), allArgs, processBuilder.start());
            return handleStateChange(COMPLETED, CMD_COMPLETED, command.getCommand(), server.getName());

        } catch (IOException ex) {
            return handleStateChange(FAILED, JAVA_VM_EXEC_FAILED, command.getCommand(), server.getName());
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create <code>ResultString</code> object corresponding to <code>String</code>command execution
     * value to be returned.
     */
    @Override
    protected Result<ValueProcess> createResult() {
        return result = new ResultProcess();
    }

    /**
     * Reads response from server and stores internally.
     * <p/>
     *
     * @param in Stream to read data from.
     * @return Always returns <code>false</code>.
     * @throws CommandException in case of stream error.
     */
    @Override
    protected boolean readResponse(InputStream in, HttpURLConnection hconn) {
        return false;
    }

    /**
     * Extracts result value from internal storage.
     * <p/>
     *
     * @return Always returns <code>false</code>.
     */
    @Override
    protected boolean processResponse() {
        return false;
    }

    /**
     * Prepare Java VM options for Glassfish server execution.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish Server Administration Command Entity.
     * @return Java VM options for Glassfish server execution as <cpde>String</code>.
     */
    private static String buildJavaOptions(PayaraServer server, CommandStartDAS command) {
        // Java VM options
        StringBuilder javaOptionsBuilder = new StringBuilder();

        // Add classpath if exists.
        javaOptionsBuilder.append(VM_CLASSPATH_OPTION).append(' ');
        if (command.classPath != null && command.classPath.length() > 0) {
            javaOptionsBuilder.append(command.classPath);
        } else {
            javaOptionsBuilder.append(quote(getJarName(server.getServerHome(), GFV3_JAR_MATCHER).getAbsolutePath()));
        }
        javaOptionsBuilder.append(' ');

        // Add Java VM options.
        if (command.javaOpts != null && command.javaOpts.length() > 0) {
            javaOptionsBuilder.append(command.javaOpts);
            javaOptionsBuilder.append(' ');
        }

        // Add startup main class or jar.
        javaOptionsBuilder.append(MAIN_CLASS);
        javaOptionsBuilder.append(' ');

        // Add Glassfish specific options.
        if (command.glassfishArgs != null && command.glassfishArgs.length() > 0) {
            javaOptionsBuilder.append(command.glassfishArgs);
        }

        return javaOptionsBuilder.toString();
    }

    /**
     * Set server process current directory to domain directory if exists.
     * <p/>
     * No current directory will be set when domain directory does not exist.
     * <p/>
     *
     * @param processBuilder Process builder object where to set current directory.
     */
    @Override
    void setProcessCurrentDir(ProcessBuilder processBuilder) {
        if (command.domainDir != null && command.domainDir.length() > 0) {
            File currentDir = new File(getDomainConfigPath(command.domainDir));
            if (currentDir.exists()) {
                LOGGER.log(FINEST, "Setting {0} process current directory to {1}",
                        new Object[] { server.getName(), command.domainDir });
                processBuilder.directory(currentDir);
            }
        }
    }

    private void checkJavaVersion(File javaVmFile) {
        JavaUtils.JavaVersion javaVersion = javaVmVersion(javaVmFile);
        LOGGER.log(FINEST, "Java VM {0} executable version {1}",
                new Object[] { javaVmFile.getAbsolutePath(), javaVersion != null ? javaVersion.toString() : "null" });

        if (javaVersion == null || javaVersion.comapreTo(new JavaUtils.JavaVersion(1, 6, 0, 0)) == -1) {
            // Display warning message but try to run server anyway.
            LOGGER.log(Level.INFO,
                    "Java VM {0} executable version {1} can't be used with {2} " + "but trying to start server anyway.",
                    new Object[] { javaVmFile.getAbsolutePath(), javaVersion != null ? javaVersion.toString() : "null",
                            server.getName() });
        }
    }

}
