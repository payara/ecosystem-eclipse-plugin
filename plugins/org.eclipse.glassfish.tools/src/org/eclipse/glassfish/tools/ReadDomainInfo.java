/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Ludovic Champenois
 * @author vince kraemer
 */
public class ReadDomainInfo {

    String adminPort = "1"; //$NON-NLS-1$
    String serverPort = "2"; //$NON-NLS-1$
    private String domainScriptFilePath;

    public ReadDomainInfo(String domainDir, String domainName) {
        domainScriptFilePath = domainDir + File.separator + domainName + "/config/domain.xml"; //$NON-NLS-1$ //$NON-NLS-2$

        // Load domain.xml
        Document domainScriptDocument = loadDomainScriptFile();
        if (domainScriptDocument == null) {
            return;
        }

        // Find the "http-listener" element
//        NodeList httplistenerNodeList = domainScriptDocument.getElementsByTagName("http-listener"); //$NON-NLS-1$
//        if (httplistenerNodeList == null || httplistenerNodeList.getLength() == 0) {
//            System.err.println("DomainInfo: cannot find 'http-listener' section in domain config file " + domainScriptFilePath); //$NON-NLS-1$
//            return ;
//        }
//        for (int i=0;i<httplistenerNodeList.getLength();i++){
//            Element n = (Element) httplistenerNodeList.item(i);
//            String p =n.getAttribute("id"); //$NON-NLS-1$
//            if ("http-listener-1".equals(p)){ //$NON-NLS-1$
//                serverPort = p;
//            }
//            if ("admin-listener".equals(p)){ //$NON-NLS-1$
//                adminPort = p;
//            }
//        }
    }

    public String getAdminPort() {
        return adminPort;
    }

    public String getServerPort() {
        return serverPort;
    }

// creates Document instance from domain.xml
    private Document loadDomainScriptFile() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            dBuilder.setEntityResolver(new EntityResolver() {

                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    StringReader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$ 
                    InputSource source = new InputSource(reader);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            });

            return dBuilder.parse(new File(domainScriptFilePath));

        } catch (Exception e) {
            System.err.println("DomainInfo: unable to parse domain config file " + domainScriptFilePath); //$NON-NLS-1$
            return null;
        }
    }

// saves Document to domain.xml
    private boolean saveDomainScriptFile(Document domainScriptDocument) {
        boolean result = false;

        Writer domainScriptFileWriter = null;

        //try {

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
            DocumentType dt = domainScriptDocument.getDoctype();
            if (null != dt) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, dt.getPublicId());
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); //$NON-NLS-1$

            DOMSource domSource = new DOMSource(domainScriptDocument);
            domainScriptFileWriter = new OutputStreamWriter( new FileOutputStream( this.domainScriptFilePath ), StandardCharsets.UTF_8 );
            StreamResult streamResult = new StreamResult(domainScriptFileWriter);

            transformer.transform(domSource, streamResult);
            result = true;
        } catch (IOException ioex) {
            System.err.println("DomainInfo: cannot create output stream for domain config file " + domainScriptFilePath); //$NON-NLS-1$
            result = false;
        } catch (Exception e) {
            System.err.println("DomainInfo: Unable to save domain config file " + domainScriptFilePath); //$NON-NLS-1$
            result = false;
        } finally {
            try {
                if (domainScriptFileWriter != null) {
                    domainScriptFileWriter.close();
                }
            } catch (IOException ioex2) {
                System.err.println("DomainInfo: cannot close output stream for " + domainScriptFilePath); //$NON-NLS-1$
            }
        }

        return result;
    }

    public static boolean isUnix() {
        return File.separatorChar == '/';
    }

    /**
     * Perform server instrumentation for profiling
     * @param domainDoc Document object representing domain.xml
     * @param nativeLibraryPath Native Library Path
     * @param jvmOptions Values for jvm-options to enable profiling
     * @return returns true if server is ready for profiling
     */
    public boolean addProfilerElements(String nativeLibraryPath, String[] jvmOptions) {
        //    String domainPath = getDomainLocation();
        Document domainDoc = loadDomainScriptFile();

        // Remove any previously defined 'profiler' element(s)
        removeProfiler(domainDoc);

        // If no 'profiler' element needs to be defined, the existing one is simply removed (by the code above)
        // (This won't happen for NetBeans Profiler, but is a valid scenario)
        // Otherwise new 'profiler' element is inserted according to provided parameters
        if (nativeLibraryPath != null || jvmOptions != null) {

            // Create "profiler" element
            Element profilerElement = domainDoc.createElement("profiler"); //$NON-NLS-1$
            profilerElement.setAttribute("enabled", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            profilerElement.setAttribute("name", "TPTP"); //$NON-NLS-1$ //$NON-NLS-2$
            if (nativeLibraryPath != null) {
                profilerElement.setAttribute("native-library-path", nativeLibraryPath); //$NON-NLS-1$
            }

            // Create "jvm-options" element
            if (jvmOptions != null) {
                for (int i = 0; i < jvmOptions.length; i++) {
                    Element jvmOptionsElement = domainDoc.createElement("jvm-options"); //$NON-NLS-1$
                    Text tt = domainDoc.createTextNode(formatJvmOption(jvmOptions[i]));
                    jvmOptionsElement.appendChild(tt);
                    profilerElement.appendChild(jvmOptionsElement);
                }
            }

            // Find the "java-config" element
            NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config"); //$NON-NLS-1$
            if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
                System.err.println("DomainInfo: cannot find 'java-config' section in domain config file " + domainScriptFilePath); //$NON-NLS-1$
                return false;
            }

            // Insert the "profiler" element as a first child of "java-config" element
            Node javaConfigNode = javaConfigNodeList.item(0);
            if (javaConfigNode.getFirstChild() != null) {
                javaConfigNode.insertBefore(profilerElement, javaConfigNode.getFirstChild());
            } else {
                javaConfigNode.appendChild(profilerElement);
            }

        }
        // Save domain.xml
        return saveDomainScriptFile(domainDoc);
    }

    /**
     * Remove server instrumentation to disable profiling
     * @param domainDoc Document object representing domain.xml
     * @return true if profiling support has been removed
     */
    public boolean removeProfilerElements() {
        Document domainDoc = loadDomainScriptFile();
        boolean eleRemoved = removeProfiler(domainDoc);
        if (eleRemoved) {
            // Save domain.xml
            return saveDomainScriptFile(domainDoc);
        } else {
            //no need to save.
            return true;
        }
    }

    private boolean removeProfiler(Document domainDoc) {
    	if (domainDoc != null) {
	        // Remove any previously defined 'profiler' element(s)
	        NodeList profilerElementNodeList = domainDoc.getElementsByTagName("profiler"); //$NON-NLS-1$
	        if (profilerElementNodeList != null && profilerElementNodeList.getLength() > 0) {
	            Vector<Node> nodes = new Vector<Node>(); //temp storage for the nodes to delete
	            //we only want to delete the NBPROFILERNAME nodes.
	            // otherwise, see bug # 77026
	            for (int i = 0; i < profilerElementNodeList.getLength(); i++) {
	                Node n = profilerElementNodeList.item(i);
	                Node a = n.getAttributes().getNamedItem("name"); //$NON-NLS-1$
	                if ((a != null) && (a.getNodeValue().equals("TPTP"))) { //$NON-NLS-1$
	                    nodes.add(n);
	                }
	            }
	            for (int i = 0; i < nodes.size(); i++) {
	                Node nd = nodes.get(i);
	                nd.getParentNode().removeChild(nd);
	            }
	            return true;
	        }
    	}

        return false;
    }

    // Converts -agentpath:"C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\"",5140
    // to -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140 (AS 8.1 and AS 8.2)
    // or to  "-agentpath:C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\",5140" (GlassFish or AS 9.0)
    private String formatJvmOption(String jvmOption) {
        // only jvmOption containing \" needs to be formatted
        if (jvmOption.indexOf("\"") != -1) { //$NON-NLS-1$ 
            // special handling for -agentpath
            if (jvmOption.indexOf("\\\"") != -1 && jvmOption.indexOf("-agentpath") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
                // Modification for AS 8.1, 8.2, initial modification for AS 9.0, GlassFish
                // Converts -agentpath:"C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\"",5140
                // to -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140
                String modifiedOption = jvmOption.replaceAll("\\\\\"", "#"); // replace every \" by # //$NON-NLS-1$ //$NON-NLS-2$
                modifiedOption = modifiedOption.replaceAll("\\\"", ""); // delete all " //$NON-NLS-1$ //$NON-NLS-2$
                modifiedOption = modifiedOption.replaceAll("#", "\""); // replace every # by " //$NON-NLS-1$ //$NON-NLS-2$

                // Modification for AS 9.0, GlassFish should be done only if native launcher isn't used,
                // otherwise will cause server startup failure. It seems that currently native launcher is used
                // for starting the servers from the IDE.
                //String osType=System.getProperty("os.name"); //$NON-NLS-1$
                //if ((osType.startsWith("Mac OS"))||isGlassfishV1OrV2){//no native for mac of glassfish //$NON-NLS-1$
                // Modification for AS 9.0, GlassFish
                // Converts -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140
                // "-agentpath:C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\",5140"
                modifiedOption = "\"" + modifiedOption.replaceAll("\\\"", "\\\\\"") + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                //}

                // return correctly formatted jvmOption
                return modifiedOption;
            } else {
                return jvmOption.replace('"', ' ');
            }
        }
        // return original jvmOption
        return jvmOption;
    }
}
