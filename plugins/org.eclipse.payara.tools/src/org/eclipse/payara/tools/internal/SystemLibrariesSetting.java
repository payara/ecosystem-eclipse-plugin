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

package org.eclipse.payara.tools.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * This class represents the settings that users manually did to the system
 * library container added by this plug-in.
 *
 * <p>
 * This is done for instance to attach sources, which in fact is the only user
 * applied setting we actually make use of.
 *
 */
public class SystemLibrariesSetting {

    private static final String SYSTEM_LIBRARIES_TAG = "system-libraries"; //$NON-NLS-1$
    private static final String LIBRARY_TAG = "library"; //$NON-NLS-1$
    private static final String PATH_TAG = "path"; //$NON-NLS-1$
    private static final String SOURCE_TAG = "source"; //$NON-NLS-1$
    private static final String JAVADOC_TAG = "javadoc"; //$NON-NLS-1$
    private static final String NEW_LINE = "\n"; //$NON-NLS-1$
    private static final String SETTING_XML = "/.settings/org.eclipse.payara.tools.syslib.xml"; //$NON-NLS-1$

    private ArrayList<Library> libraries = new ArrayList<>();

    public void setLibraryList(ArrayList<Library> libraries) {
        this.libraries = libraries;
    }

    public ArrayList<Library> getLibraryList() {
        return libraries;
    }

    public static SystemLibrariesSetting load(IProject project) {
        try {
            IFile settingsXmlFile = project.getFile(SETTING_XML);

            if (!settingsXmlFile.exists()) {
                return null;
            }

            SystemLibrariesSetting settings = null;
            try (InputStream stream = settingsXmlFile.getContents()) {
                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                SAXParser saxParser = saxParserFactory.newSAXParser();
                SysLibHandler handler = new SysLibHandler();
                saxParser.parse(stream, handler);
                settings = handler.getSystemLibrariesSetting();//(SystemLibrariesSetting) JAXBContext.newInstance(SystemLibrariesSetting.class).createUnmarshaller().unmarshal(stream);
            }

            if (settings.getLibraryList() == null) {
                settings.setLibraryList(new ArrayList<>());
            }

            return settings;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void save(IProject project, SystemLibrariesSetting settings) {
        try {
            IFile settingsXmlFile = project.getFile(SETTING_XML);
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(
                    new FileOutputStream(settingsXmlFile.getLocation().toFile()), StandardCharsets.UTF_8.name());
            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            writer.writeCharacters(NEW_LINE);
            writer.writeStartElement(SYSTEM_LIBRARIES_TAG);
            writer.writeCharacters(NEW_LINE);
            for (Library library : settings.getLibraryList()) {
                writer.writeStartElement(LIBRARY_TAG);
                if (library.getPath() != null) {
                    writer.writeAttribute(PATH_TAG, library.getPath());
                }
                if (library.getSource() != null) {
                    writer.writeAttribute(SOURCE_TAG, library.getSource());
                }
                if (library.getJavadoc() != null) {
                    writer.writeAttribute(JAVADOC_TAG, library.getJavadoc());
                }
                writer.writeEndElement();
                writer.writeCharacters(NEW_LINE);
            }
            writer.writeEndElement();
            writer.writeCharacters(NEW_LINE);
            writer.writeEndDocument();

            writer.flush();
            writer.close();
            settingsXmlFile.refreshLocal(0, new NullProgressMonitor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getSourcePath(File jar) {
        for (Library library : libraries) {
            if (jar.equals(new File(library.getPath()))) {
                File librarySource = new File(library.getSource());
                if (librarySource.exists()) {
                    return librarySource;
                }

                // Workspace location
                return ResourcesPlugin.getWorkspace()
                        .getRoot()
                        .getFile(new Path(library.getSource()))
                        .getLocation()
                        .toFile();
            }
        }

        return null;
    }

    static class SysLibHandler extends DefaultHandler {

        private ArrayList<Library> libs = new ArrayList<>();

        @Override
        public void startElement(String uri, String localName, String tag, Attributes attributes) throws SAXException {
            if (tag.equals(LIBRARY_TAG)) {
                Library library = new Library();
                for (int i = 0; i < attributes.getLength(); i++) {
                    switch (attributes.getQName(i)) {
                        case PATH_TAG:
                            library.setPath(attributes.getValue(i));
                            break;
                        case SOURCE_TAG:
                            library.setSource(attributes.getValue(i));
                            break;
                        case JAVADOC_TAG:
                            library.setJavadoc(attributes.getValue(i));
                            break;
                        default:
                            break;
                    }
                }
                libs.add(library);
            }
        }

        public SystemLibrariesSetting getSystemLibrariesSetting() {
            SystemLibrariesSetting systemLibrariesSetting = new SystemLibrariesSetting();
            systemLibrariesSetting.setLibraryList(libs);
            return systemLibrariesSetting;
        }

    }

}
