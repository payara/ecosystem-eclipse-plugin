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

package org.eclipse.payara.tools.sdk.server.parser;

import org.eclipse.payara.tools.sdk.server.parser.TreeParser.NodeListener;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Marks that the xml parser is currently inside config element with give name. This information is
 * used by descendants of this class.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
class TargetConfigReader extends NodeListener {

    public static final String CONFIG_PATH = "/domain/configs/config";

    public static final String DEFAULT_TARGET = "server";

    protected static boolean readData = false;

    private String targetConfigName = null;

    public TargetConfigReader(String targetConfigName) {
        this.targetConfigName = targetConfigName;
        // TODO all parsing has to be rewritten at some point
        TargetConfigReader.readData = false;
    }

    class TargetConfigMarker extends NodeListener {

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            if ((targetConfigName != null) && attributes.getValue("name").equalsIgnoreCase(targetConfigName)) {
                readData = true;
            }
        }

        @Override
        public void endNode(String qname) throws SAXException {
            if ("config".equals(qname)) {
                readData = false;
            }
        }

    }

}
