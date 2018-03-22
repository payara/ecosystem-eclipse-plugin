/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sapphire;

import java.io.File;

import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

public class DomainNameValidationService extends ValidationService {

    @Override
    protected Status compute() {
        ICreateGlassfishDomainOp op = context(ICreateGlassfishDomainOp.class);
        Path gfInstallPath = op.getLocation().content();
        if (gfInstallPath != null && gfInstallPath.toFile().exists()) {
            String name = op.getName().content();
            if (name != null && name.trim().length() > 0) {

                if (name.indexOf(' ') > 0) {
                    return Status.createErrorStatus("Invalid value for domain name."); //$NON-NLS-1$
                }

                File domainRoot = new File(gfInstallPath.toFile(), "domains");
                File domainsDir = new File(domainRoot, name);
                if (domainsDir.exists()) {
                    return Status.createErrorStatus("A domain already exists at the specified location."); //$NON-NLS-1$
                }
                return Status.createOkStatus();
            }
        }
        return Status.createOkStatus();
    }

}
