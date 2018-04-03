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
import java.io.IOException;
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

@XmlRootElement(name = "system-libraries")
public class SystemLibrariesSetting {

    static final String SETTING_XML = "/.settings/org.eclipse.payara.tools.syslib.xml"; //$NON-NLS-1$
    private ArrayList<Library> libraryList = new ArrayList<>();

    @XmlElement(name = "library")
    public void setLibraryList(ArrayList<Library> libList) {
        this.libraryList = libList;
    }

    public ArrayList<Library> getLibraryList() {
        return libraryList;
    }

    public static SystemLibrariesSetting load(IProject proj) {
        try {
            IFile file = proj.getFile(SETTING_XML);
            JAXBContext context = JAXBContext.newInstance(SystemLibrariesSetting.class);
            SystemLibrariesSetting settings = null;
            if (!file.exists()) {
                return null;
            } else {
                final InputStream stream = file.getContents();

                try {
                    settings = (SystemLibrariesSetting) context.createUnmarshaller().unmarshal(stream);
                } finally {
                    try {
                        stream.close();
                    } catch (final IOException e) {
                    }
                }

                ArrayList<Library> libsList = settings.getLibraryList();
                if (libsList == null) {
                    libsList = new ArrayList<>();
                    settings.setLibraryList(libsList);
                }
            }

            return settings;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void save(IProject proj, SystemLibrariesSetting settings) {
        try {
            IFile file = proj.getFile(SETTING_XML);
            JAXBContext context = JAXBContext.newInstance(SystemLibrariesSetting.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Write to System.out
            m.marshal(settings, System.out);

            // Write to File
            m.marshal(settings, file.getLocation().toFile());

            file.refreshLocal(0, new NullProgressMonitor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getSourcePath(File jar) {
        for (Library lib : libraryList) {
            if (jar.equals(new File(lib.getPath()))) {
                File f = new File(lib.getSource());
                if (f.exists()) {
                    return f;
                }
                // Workspace location
                IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(lib.getSource()));
                return file.getLocation().toFile();
            }
        }
        return null;
    }

}