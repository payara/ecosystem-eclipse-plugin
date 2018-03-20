/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sdk.admin.response;

import java.util.List;
import java.util.Properties;

/**
 * Class represents one part of REST server message.
 * <p>
 * This part can be repeated in server response.
 * It includes string message and can have other properties.
 * It can be nesting also other message parts.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class MessagePart {

    /** Message properties.*/
    Properties props;

    /** Message.*/
    String message;

    /** Nested messages.*/
    List<MessagePart> children;

    public List<MessagePart> getChildren() {
        return children;
    }

    public String getMessage() {
        return message;
    }

    public Properties getProperties() {
        return props;
    }

    public void setProperties(Properties props) {
        this.props = props;
    }

    void setMessage(String message) {
        this.message = message;
    }

}
