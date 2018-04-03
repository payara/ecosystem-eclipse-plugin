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
 * <code>module</code> Java EE configuration XML element reader.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JavaEEModuleReader extends AbstractReader {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes //
    ////////////////////////////////////////////////////////////////////////////

    /** Java EE module values from XML element. */
    public class Module {

        /** Java EE module type. */
        final String type;

        /** Java EE module check reference. */
        final String check;

        /**
         * Creates an instance of Java EE module values from XML element.
         * <p/>
         *
         * @param type Java EE module type.
         * @param check Java EE module check reference.
         */
        Module(final String type, final String check) {
            this.type = type;
            this.check = check;
        }

        /**
         * Get Java EE module type.
         * <p/>
         *
         * @return Java EE module type.
         */
        public String getType() {
            return type;
        }

        /**
         * Get Java EE module check reference.
         * <p/>
         *
         * @return Java EE module check reference.
         */
        public String getCheck() {
            return check;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>javaee</code> XML element name. */
    private static final String NODE = "module";

    /** <code>type</code> XML element attribute name. */
    private static final String TYPE_ATTR = "type";

    /** <code>check</code> XML element attribute name. */
    private static final String CHECK_ATTR = "check";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Modules retrieved from XML elements. */
    private List<Module> modules;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of <code>module</code> Java EE configuration XML element reader.
     * <p/>
     *
     * @param pathPrefix Tree parser path prefix to be prepended before current XML element.
     */
    JavaEEModuleReader(final String pathPrefix) throws ServerConfigException {
        super(pathPrefix, NODE);
        modules = new LinkedList<>();
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
        modules.add(new Module(attributes.getValue(TYPE_ATTR),
                attributes.getValue(CHECK_ATTR)));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get modules retrieved from XML elements.
     * <p/>
     *
     * @return Modules retrieved from XML elements.
     */
    public List<Module> getModules() {
        return modules;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    public void reset() {
        modules = new LinkedList<>();
    }

}
