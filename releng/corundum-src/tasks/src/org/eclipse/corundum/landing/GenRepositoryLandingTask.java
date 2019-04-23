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

package org.eclipse.corundum.landing;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.corundum.AntTaskOperationContext;
import org.eclipse.corundum.FileSystemExcludes;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class GenRepositoryLandingTask extends Task {
    private File repository;
    private String name;
    private final FileSystemExcludes excludes = new FileSystemExcludes();

    public void setRepository(final File repository) {
        this.repository = repository;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileSystemExcludes createExcludes() {
        return excludes;
    }

    @Override
    public void execute() throws BuildException {
        if (repository == null) {
            throw new BuildException("The \"repository\" attribute must be specified.");
        }

        GenRepositoryLandingOp op = new GenRepositoryLandingOp();
        op.setRepository(repository);
        op.setName(name);
        op.getExcludes().addAll(excludes.list());
        op.execute(new AntTaskOperationContext(this));
    }

}
