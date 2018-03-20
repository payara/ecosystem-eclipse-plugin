/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.admin;

import org.eclipse.payara.tools.sdk.logging.Logger;
import org.eclipse.payara.tools.sdk.utils.OsUtils;
import org.eclipse.payara.tools.server.GlassFishServer;

/**
 * Change administrator password command execution using local asadmin interface.
 * <p/>
 *
 * @author Tomas Kraus
 */
public class RunnerAsadminChangeAdminPassword extends RunnerAsadmin {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerAsadminChangeAdminPassword.class);

    /** Specifies the domain of the administrator user. */
    private static final String DOMAIN_NAME_PARAM = "--domain_name";

    /**
     * Specifies the parent directory of the domain specified in the --domain_name option.
     */
    private static final String DOMAINDIR_PARAM = "--domaindir";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds <code>change-admin-password</code> command query string.
     */
    private static String query(final GlassFishServer server,
            final Command command) {
        final String METHOD = "query";
        String domainsFolder = OsUtils.escapeString(server.getDomainsFolder());
        String domainName = OsUtils.escapeString(server.getDomainName());
        if (domainName == null || domainsFolder == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "nullValue"));
        }
        StringBuilder sb = new StringBuilder(
                DOMAIN_NAME_PARAM.length() + 1 + domainName.length() + 1
                        + DOMAINDIR_PARAM.length() + 1 + domainsFolder.length());
        sb.append(DOMAINDIR_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(domainsFolder);
        sb.append(PARAM_SEPARATOR);
        sb.append(DOMAIN_NAME_PARAM);
        sb.append(PARAM_ASSIGN_VALUE);
        sb.append(domainName);
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandChangeAdminPassword command;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using command line asadmin interface.
     * <p/>
     *
     * @param server GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerAsadminChangeAdminPassword(final GlassFishServer server,
            final Command command) {
        super(server, command, query(server, command));
        final String METHOD = "init";
        if (command instanceof CommandChangeAdminPassword) {
            this.command = (CommandChangeAdminPassword) command;
        } else {
            throw new CommandException(
                    LOGGER.excMsg(METHOD, "illegalInstance"));
        }
        passwordFile.setAdminNewPassword(this.command.password);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create internal <code>ProcessIOContent</code> object corresponding to command execution IO.
     */
    @Override
    protected ProcessIOContent createProcessIOContent() {
        ProcessIOContent processIOContent = new ProcessIOContent();
        processIOContent.addOutput(
                new String[] { "Command", "executed successfully" },
                new String[] { "Command change-admin-password failed" });
        return processIOContent;
    }

}
