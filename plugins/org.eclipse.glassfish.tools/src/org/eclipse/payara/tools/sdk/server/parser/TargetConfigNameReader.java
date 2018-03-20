/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sdk.server.parser;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.payara.tools.sdk.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads the name of the configuration for given target (server
 * or cluster).
 * TODO now it reads only servers and not clusters...
 * @author Peter Benedikovic, Tomas Kraus
 */
public class TargetConfigNameReader extends TreeParser.NodeListener implements
        XMLReader {

    public static final String SERVER_PATH =
            "/domain/servers/server";

    public static final String DEFAULT_TARGET = "server";

    private String targetConfigName = null;

    private String targetName;

    public TargetConfigNameReader() {
        this(DEFAULT_TARGET);
    }

    public TargetConfigNameReader(String targetName) {
        this.targetName = targetName;
    }
    
    public String getTargetConfigName() {
        return targetConfigName;
    }

    @Override
    public void readAttributes(String qname, Attributes attributes) throws
            SAXException {
        if (attributes.getValue("name").equalsIgnoreCase(targetName)) {
            targetConfigName = attributes.getValue("config-ref");
        }
    }

    @Override
    public List<Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new Path(SERVER_PATH, this));
        return paths;
    }
}
