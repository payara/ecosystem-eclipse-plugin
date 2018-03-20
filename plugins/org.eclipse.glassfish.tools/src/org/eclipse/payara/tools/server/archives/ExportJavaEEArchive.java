/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.server.archives;

import static org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties.ARCHIVE_DESTINATION;
import static org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties.EXPORT_SOURCE_FILES;
import static org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties.OVERWRITE_EXISTING;
import static org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties.PROJECT_NAME;
import static org.eclipse.payara.tools.server.archives.AssembleModules.isModuleType;
import static org.eclipse.wst.common.frameworks.datamodel.DataModelFactory.createDataModel;

import java.io.File;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jst.j2ee.application.internal.operations.EARComponentExportDataModelProvider;
import org.eclipse.jst.j2ee.internal.ejb.project.operations.EJBComponentExportDataModelProvider;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebComponentExportDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;

@SuppressWarnings("restriction")
public class ExportJavaEEArchive {

	public static File export(final IModule module, final IProgressMonitor monitor) throws ExecutionException {

		final File root = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

		IProject p = module.getProject();
		File archiveName = null;
		IDataModel dataModel = null;

		if (isModuleType(module, "jst.web")) {
			dataModel = createDataModel(new WebComponentExportDataModelProvider());
			archiveName = new File(root, p.getName() + ".war");
		} else if (isModuleType(module, "jst.ear")) {
			dataModel = createDataModel(new EARComponentExportDataModelProvider());
			archiveName = new File(root, p.getName() + ".ear");
		} else { // default
			dataModel = createDataModel(new EJBComponentExportDataModelProvider());
			archiveName = new File(root, p.getName() + ".jar");

		}

		dataModel.setProperty(PROJECT_NAME, p.getName());
		dataModel.setProperty(EXPORT_SOURCE_FILES, false);
		dataModel.setProperty(OVERWRITE_EXISTING, true);
		dataModel.setProperty(ARCHIVE_DESTINATION, archiveName.getAbsolutePath());

		dataModel.getDefaultOperation().execute(monitor, null);
		
		return archiveName;

	}

}
