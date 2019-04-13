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

import static java.lang.Boolean.TRUE;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * This class represents the settings that users manually did to the system library container added
 * by this plug-in.
 * 
 * <p>
 * This is done for instance to attach sources, which in fact is the only user applied setting
 * we actually make use of.
 *
 */
@XmlRootElement(name = "system-libraries")
public class SystemLibrariesSetting {

    static final String SETTING_XML = "/.settings/org.eclipse.payara.tools.syslib.xml"; //$NON-NLS-1$
    private ArrayList<Library> libraries = new ArrayList<>();

    @XmlElement(name = "library")
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
                settings = (SystemLibrariesSetting) JAXBContext.newInstance(SystemLibrariesSetting.class).createUnmarshaller().unmarshal(stream);
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
            Marshaller settingsMarshaller = JAXBContext.newInstance(SystemLibrariesSetting.class).createMarshaller();
            settingsMarshaller.setProperty(JAXB_FORMATTED_OUTPUT, TRUE);

            // Write to System.out
            settingsMarshaller.marshal(settings, System.out);

            // Write to File
            settingsMarshaller.marshal(settings, settingsXmlFile.getLocation().toFile());

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
                return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(library.getSource()))
                                      .getLocation()
                                      .toFile();
            }
        }
        
        return null;
    }

}