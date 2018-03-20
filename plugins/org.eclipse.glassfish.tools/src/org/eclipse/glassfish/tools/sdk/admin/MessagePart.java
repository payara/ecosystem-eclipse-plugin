/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class MessagePart {

    Properties props = new Properties();
    String message;

    List<MessagePart> children = new ArrayList<MessagePart>();

    public List<MessagePart> getChildren() {
        return children;
    }

    public String getMessage() {
        return message;
    }

    public Properties getProps() {
        return props;
    }
}
