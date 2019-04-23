/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.corundum;

import static org.apache.tools.ant.Project.MSG_ERR;
import static org.apache.tools.ant.Project.MSG_INFO;
import static org.apache.tools.ant.Project.MSG_WARN;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class AbstractTask extends Task {
    protected void info(String str) {
        log(str, MSG_INFO);
    }

    protected void warning(String str) {
        log(str, MSG_WARN);
    }

    protected void error(String str) {
        log(str, MSG_ERR);
    }

    protected void fail(String str) {
        error(str);
        throw new BuildException("Build failed.");
    }

    @Override
    public void log(final String str, final int level) {
        StringBuffer line = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            final char ch = str.charAt(i);

            if (ch == '\n') {
                if (line.length() == 0) {
                    line.append(' ');
                }
                super.log(line.toString(), level);
                line = new StringBuffer();
            } else {
                line.append(ch);
            }
        }

        if (line.length() > 0) {
            super.log(line.toString(), level);
        }
    }

}
