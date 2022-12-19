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

package fish.payara.eclipse.tools.server.facets.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import fish.payara.eclipse.tools.server.facets.IGlassfishWebDeploymentDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

class GlassfishWebDeploymentDescriptor extends
        AbstractGlassfishDeploymentDescriptor implements IGlassfishWebDeploymentDescriptor {

    private static final Logger LOGGER = Logger.getLogger(GlassfishWebDeploymentDescriptor.class.getName());

    private final IFile file;
    private final Document document;

    GlassfishWebDeploymentDescriptor(IFile file) {
        this.file = file;
        this.document = readDocument(file);
    }

    @Override
    protected void prepareDescriptor() {

    }

    @Override
    protected boolean isPossibleToCreate() {
        // check for existence of older sun descriptor
        IPath sunDescriptor = file.getLocation().removeLastSegments(1)
                .append(IGlassfishWebDeploymentDescriptor.SUN_WEB_DEPLOYMENT_DESCRIPTOR_NAME);
        return !sunDescriptor.toFile().exists();
    }

    @Override
    protected void save() {
        saveDocument(document);
    }

    @Override
    public void setContext(String context) {
        Element rootElement = document.getDocumentElement();
        if (rootElement != null) {
            for (int i = 0; i < rootElement.getChildNodes().getLength(); i++) {
                Node node = rootElement.getChildNodes().item(i);
                if (node.getNodeName().equals("context-root")) {
                    node.setTextContent(context);
                    break;
                }
            }
        }
    }

    @Override
    public String getContext() {
        Element rootElement = document.getDocumentElement();
        if (rootElement != null) {
            for (int i = 0; i < rootElement.getChildNodes().getLength(); i++) {
                Node node = rootElement.getChildNodes().item(i);
                if (node.getNodeName().equals("context-root")) {
                    return node.getTextContent();
                }
            }
        }
        return null;
    }

    private Document readDocument(IFile file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new File(file.getRawLocationURI()));
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void saveDocument(Document document) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(new File(file.getRawLocationURI()));
            Source input = new DOMSource(document);
            transformer.transform(input, output);
        } catch (TransformerException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

}
