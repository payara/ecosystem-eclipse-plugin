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

/**
 * Represents response returned from server after command execution.
 * <p>
 * Inspired by ActionReport class from module GF Admin Rest Service. In our case the interface
 * allows just read-only access.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface ActionReport {

    public enum ExitCode {
        SUCCESS, WARNING, FAILURE
    }

    public ExitCode getExitCode();

    public String getMessage();

    public String getCommand();

}
