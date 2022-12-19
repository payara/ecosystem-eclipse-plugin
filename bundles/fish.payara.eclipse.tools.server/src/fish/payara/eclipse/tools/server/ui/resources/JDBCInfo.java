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

package fish.payara.eclipse.tools.server.ui.resources;

import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCDriverDefinitionConstants;

public class JDBCInfo {
    private IConnectionProfile connectionProfile;
    private UrlData urlDataParser;

    /**
     * Constructor for JDBCInfo.
     *
     * @param profile
     */
    public JDBCInfo(IConnectionProfile profile) {
        connectionProfile = profile;
        urlDataParser = new UrlData(getURL());
    }

    private String getProperty(String propName) {
        if (connectionProfile != null) {
            return connectionProfile.getBaseProperties().getProperty(propName);
        }
        return null;
    }

    public String getUserName() {
        return getProperty(IJDBCDriverDefinitionConstants.USERNAME_PROP_ID);
    }

    public String getUserPassword() {
        return getProperty(IJDBCDriverDefinitionConstants.PASSWORD_PROP_ID);
    }

    public String getURL() {
        return getProperty(IJDBCDriverDefinitionConstants.URL_PROP_ID);
    }

    public String getDriverClass() {
        return getProperty(IJDBCDriverDefinitionConstants.DRIVER_CLASS_PROP_ID);
    }

    public String getDatasourceClass() {
        return DriverMaps.getDSClassName(getURL());
    }

    public String getDatabaseVendor() {
        return getProperty(IJDBCDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID);
    }

    public String getPort() {
        return urlDataParser.getPort();
    }

    public String getServerName() {
        return urlDataParser.getHostName();
    }

    public String getDatabaseName() {
        return getProperty(IJDBCDriverDefinitionConstants.DATABASE_NAME_PROP_ID);
    }

    public String getAlternateDatabaseName() {
        return urlDataParser.getAlternateDBName();
    }
}
