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

package fish.payara.eclipse.tools.server.sdk.server.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <code>fileset</code> library configuration XML element reader.
 * <p/>
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class FilesetReader extends TreeParser.NodeListener {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** File sets retrieved from XML elements. */
    private Map<String, List<String>> filesets = new HashMap<>();

    /** File set being actually processed. */
    private List<String> actualFileset = null;

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
        String dirName = attributes.getValue("dir");
        if ((actualFileset = filesets.get(dirName)) == null) {
            actualFileset = new ArrayList<>();
            filesets.put(dirName, actualFileset);
        }
    }

    /**
     * Process child elements from current XML element.
     * <p/>
     *
     * @param qname Not used.
     * @param attributes List of XML attributes.
     * @throws SAXException When any problem occurs.
     */
    @Override
    public void readChildren(final String qname, final Attributes attributes)
            throws SAXException {
        actualFileset.add(attributes.getValue("name"));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get file sets retrieved from XML elements.
     * <p/>
     *
     * @return File sets retrieved from XML elements.
     */
    public Map<String, List<String>> getFilesets() {
        return filesets;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    public void reset() {
        filesets = new HashMap<>();
        actualFileset = null;
    }

}
