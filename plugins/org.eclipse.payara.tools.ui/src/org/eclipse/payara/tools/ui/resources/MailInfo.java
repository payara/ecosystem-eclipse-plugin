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

package org.eclipse.payara.tools.ui.resources;

public class MailInfo {

    private String jndiName;
    private String mailHost;
    private String mailFrom;
    private String mailUser;

    public MailInfo() {

    }

    /**
     * @param jndiName the jndiName to set
     */
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * @return the jndiName
     */
    public String getJndiName() {
        return jndiName;
    }

    /**
     * @param mailHost the mailHost to set
     */
    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    /**
     * @return the mailHost
     */
    public String getMailHost() {
        return mailHost;
    }

    /**
     * @param mailFrom the mailFrom to set
     */
    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    /**
     * @return the mailFrom
     */
    public String getMailFrom() {
        return mailFrom;
    }

    /**
     * @param mailUser the mailUser to set
     */
    public void setMailUser(String mailUser) {
        this.mailUser = mailUser;
    }

    /**
     * @return the mailUser
     */
    public String getMailUser() {
        return mailUser;
    }

}
