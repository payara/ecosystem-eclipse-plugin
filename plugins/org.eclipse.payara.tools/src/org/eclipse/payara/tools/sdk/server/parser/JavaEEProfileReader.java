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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.payara.tools.sdk.server.config.ServerConfigException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <code>profile</code> Java EE configuration XML element reader.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JavaEEProfileReader extends AbstractReader {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes //
    ////////////////////////////////////////////////////////////////////////////

    /** Java EE profile values from XML element. */
    public class Profile {

        /** Java EE profile version. */
        final String version;

        /** Java EE profile type. */
        final String type;

        /** Java EE profile check reference. */
        final String check;

        /**
         * Creates an instance of Java EE profile values from XML element.
         * <p/>
         *
         * @param version Java EE profile version.
         * @param type Java EE profile type.
         * @param check Java EE profile check reference.
         */
        Profile(final String version, final String type, final String check) {
            this.version = version;
            this.type = type;
            this.check = check;
        }

        /**
         * Get Java EE profile version.
         * <p/>
         *
         * @return Java EE profile version.
         */
        public String getVersion() {
            return version;
        }

        /**
         * Get Java EE profile type.
         * <p/>
         *
         * @return Java EE profile type.
         */
        public String getType() {
            return type;
        }

        /**
         * Get Java EE profile check reference.
         * <p/>
         *
         * @return Java EE profile check reference.
         */
        public String getCheck() {
            return check;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>javaee</code> XML element name. */
    private static final String NODE = "profile";

    /** <code>version</code> XML element attribute name. */
    private static final String VERSION_ATTR = "version";

    /** <code>type</code> XML element attribute name. */
    private static final String TYPE_ATTR = "type";

    /** <code>check</code> XML element attribute name. */
    private static final String CHECK_ATTR = "check";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Profiles retrieved from XML elements. */
    private List<Profile> profiles;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of <code>profile</code> Java EE configuration XML element reader.
     * <p/>
     *
     * @param pathPrefix Tree parser path prefix to be prepended before current XML element.
     */
    JavaEEProfileReader(final String pathPrefix) throws ServerConfigException {
        super(pathPrefix, NODE);
        profiles = new LinkedList<>();
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
        profiles.add(new Profile(attributes.getValue(VERSION_ATTR),
                attributes.getValue(TYPE_ATTR),
                attributes.getValue(CHECK_ATTR)));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get profiles retrieved from XML elements.
     * <p/>
     *
     * @return Profiles retrieved from XML elements.
     */
    public List<Profile> getProfiles() {
        return profiles;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    public void reset() {
        profiles = new LinkedList<>();
    }

}
