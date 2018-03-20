/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.internal;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.payara.tools.GlassfishToolsPlugin;
import org.eclipse.payara.tools.sapphire.ICreateGlassfishDomainOp;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;

@SuppressWarnings("restriction")
public class CreateGlassfishDomainOpMethods {

    public static Status execute(ICreateGlassfishDomainOp op, ProgressMonitor mon) {
        Path root = op.getLocation().content();
        File asadmin = new File(new File(root.toFile(), "bin"),
                Platform.getOS().equals(Platform.OS_WIN32) ? "asadmin.bat" : "asadmin");
        ;
        if (asadmin.exists()) {
            String javaExecutablePath = asadmin.getAbsolutePath();
            String[] cmdLine = new String[] { javaExecutablePath, "create-domain",
                    "--nopassword=true",
                    "--portbase", String.valueOf(op.getPortBase().content()),
                    "--domaindir", op.getDomainDir().content().toPortableString(),
                    op.getName().content() };
            Process p = null;
            try {
                final StringBuilder output = new StringBuilder();
                final StringBuilder errOutput = new StringBuilder();
                output.append(Arrays.toString(cmdLine) + "\n");

                // Set AS_JAVA location which will be used to run asadmin
                String envp[] = new String[1];
                envp[0] = "AS_JAVA=" + op.getJavaLocation().content();

                p = DebugPlugin.exec(cmdLine, null, envp);
                IProcess process = DebugPlugin.newProcess(new Launch(null, ILaunchManager.RUN_MODE, null), p, "GlassFish asadmin"); //$NON-NLS-1$

                // Log output
                process.getStreamsProxy().getOutputStreamMonitor().addListener((text, monitor) -> output.append(text));

                process.getStreamsProxy().getErrorStreamMonitor().addListener((text, monitor) -> errOutput.append(text));

                for (int i = 0; i < 600; i++) {
                    // Wait no more than 30 seconds (600 * 50 milliseconds)
                    if (process.isTerminated()) {
                        String msg = output.toString() + "\n" + errOutput.toString();
                        org.eclipse.core.runtime.Status status = new org.eclipse.core.runtime.Status(IStatus.INFO,
                                GlassfishToolsPlugin.SYMBOLIC_NAME, 1, msg, null);
                        GlassfishToolsPlugin.getInstance().getLog().log(status);
                        break;
                    }
                    try {
                        Thread.sleep(50);
                        mon.worked(10);
                    } catch (InterruptedException e) {
                    }
                }

                File f = new File(op.getDomainDir().content().toFile(), op.getName().content());
                if (!f.exists()) {
                    return Status.createErrorStatus(errOutput.toString());
                }
            } catch (CoreException ioe) {
                LaunchingPlugin.log(ioe);
            } finally {
                if (p != null) {
                    p.destroy();
                }
            }
        }

        return Status.createOkStatus();
    }

}
