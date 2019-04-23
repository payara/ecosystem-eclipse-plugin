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

package org.eclipse.corundum.listing;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.corundum.AntTaskOperationContext;
import org.eclipse.corundum.FileSystemExcludes;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class GenFolderListingTask extends Task {
    private File folder;
    private final FileSystemExcludes excludes = new FileSystemExcludes();

    public void setFolder(final File folder) {
        this.folder = folder;
    }

    public FileSystemExcludes createExcludes() {
        return this.excludes;
    }

    @Override
    public void execute() throws BuildException {
        if (this.folder == null) {
            throw new BuildException("The \"folder\" attribute must be specified.");
        }

        final GenFolderListingOp op = new GenFolderListingOp();
        op.setFolder(this.folder);
        op.getExcludes().addAll(this.excludes.list());
        op.execute(new AntTaskOperationContext(this));
    }

}
