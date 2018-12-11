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

package org.eclipse.payara.tools.sdk.admin.response;

import org.eclipse.payara.tools.sdk.PayaraIdeException;

/**
 * Factory that returns appropriate response parser implementation based on content type of the
 * response.
 * <p>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResponseParserFactory {

    private static RestXMLResponseParser xmlParser;

    // private static RestJSONResponseParser jsonParser;

    public static synchronized RestResponseParser getRestParser(ResponseContentType contentType) {
        switch (contentType) {
        case APPLICATION_XML:
            if (xmlParser == null) {
                xmlParser = new RestXMLResponseParser();
            }
            return xmlParser;
        case APPLICATION_JSON:
            // RestJSONResponseParser is not used in Eclipse Payara Tools and has dependency on
            // com.googlecode.json-simple 1.1.1, which we don't want to bundle
            // if (jsonParser == null) {
            // jsonParser = new RestJSONResponseParser();
            // }
            // return jsonParser;
        case TEXT_PLAIN:
            return null;
        default:
            throw new PayaraIdeException("Not supported content type. Cannot create response parser!");
        }
    }

}
