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

package fish.payara.eclipse.tools.server.sdk.server.parser;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import fish.payara.eclipse.tools.server.sdk.server.config.ServerConfigException;

/**
 * <code>platform</code> Java SE configuration XML element reader.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JavaSEPlatformReader extends AbstractReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>platform</code> XML element name. */
    private static final String NODE = "platform";

    /** <code>type</code> XML element attribute name. */
    private static final String VERSION_ATTR = "version";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Platforms retrieved from XML elements. */
    private List<String> platforms;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of <code>platform</code> Java EE configuration XML element reader.
     * <p/>
     *
     * @param pathPrefix Tree parser path prefix to be prepended before current XML element.
     */
    JavaSEPlatformReader(final String pathPrefix) throws ServerConfigException {
        super(pathPrefix, NODE);
        platforms = new LinkedList<>();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tree parser methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Process attributes from current XML element.
     * <p/>
     *
     * @param qname Not used.
     * @param attributes List of XML attributes.
     * @throws SAXException When any problem occurs.
     */
    @Override
    public void readAttributes(final String qname, final Attributes attributes)
            throws SAXException {
        platforms.add(attributes.getValue(VERSION_ATTR));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get platforms retrieved from XML elements.
     * <p/>
     *
     * @return Platforms retrieved from XML elements.
     */
    public List<String> getPlatforms() {
        return platforms;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    public void reset() {
        platforms = new LinkedList<>();
    }

}
