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
 * Java EE platform check configuration XML element reader.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JavaEEProfileCheckReader
        extends AbstractReader implements XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes //
    ////////////////////////////////////////////////////////////////////////////

    /** Java EE platform check values from XML element. */
    public class Check {

        /** Java EE platform check name (unique ID). */
        final String name;

        /** Java EE platform check files. */
        List<String> files;

        /**
         * Creates an instance of Java EE platform check values from XML element.
         * <p/>
         * Internal file list to check is initialized as an empty list.
         * <p/>
         *
         * @param name Java EE platform check name (unique ID).
         */
        Check(final String name) {
            this.name = name;
            this.files = null;
        }

        /**
         * Set Java EE platform check files.
         * <p/>
         *
         * @param files Java EE platform check files.
         */
        void setFiles(final List<String> files) {
            this.files = files;
        }

        /**
         * Get Java EE platform check name (unique ID).
         * <p/>
         *
         * @return Java EE platform check name (unique ID).
         */
        public String getName() {
            return name;
        }

        /**
         * Get Java EE platform check files.
         * <p/>
         *
         * @return Java EE platform check files.
         */
        public List<String> getFiles() {
            return files;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>check</code> XML element name. */
    static final String NODE = "check";

    /** <code>name</code> XML element attribute name. */
    private static final String NAME_ATTR = "name";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Java EE platform check value from XML element. */
    Check currentCheck;

    /** All Java EE platform check values from XML elements on this level. */
    List<Check> checks;

    /** <code>file</code> XML element reader. */
    final PathReader pathReader;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE platform check configuration XML element reader.
     * <p/>
     *
     * @param pathPrefix Tree parser path prefix to be prepended before current XML element.
     */
    JavaEEProfileCheckReader(final String pathPrefix) {
        super(pathPrefix, NODE);
        pathReader = new PathReader(path);
        checks = new LinkedList<>();
        currentCheck = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get all Java EE platform check values from XML elements on this level.
     * <p/>
     *
     * @return All Java EE platform check values from XML elements on this level.
     */
    List<Check> getChecks() {
        return checks;
    }

    ////////////////////////////////////////////////////////////////////////////
    // XML reader methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Provide paths to listen on.
     * <p/>
     * Sets readers for <code>javaee</code> element and it's content.
     * <p/>
     *
     * @return Paths that the reader listens to.
     */
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new TreeParser.Path(path, this));
        paths.add(new TreeParser.Path(pathReader.getPath(), pathReader));
        return paths;
    }

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
        currentCheck = new Check(attributes.getValue(NAME_ATTR));
    }

    /**
     * Finish <code>javaee</code> element processing.
     * <p/>
     *
     * @param qname Current XML element name.
     * @throws ServerConfigException when more than one <code>javaee</code> XML elements were found.
     */
    @Override
    public void endNode(final String qname) throws SAXException {
        if (NODE.equals(qname)) {
            currentCheck.setFiles(pathReader.getPaths());
            checks.add(currentCheck);
            localReset();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader to allow reading of next element on the same level.
     */
    private void localReset() {
        currentCheck = null;
        pathReader.reset();
    }

    /**
     * Full reset of XML element reader.
     */
    public void reset() {
        localReset();
        checks = new LinkedList<>();
    }
}
