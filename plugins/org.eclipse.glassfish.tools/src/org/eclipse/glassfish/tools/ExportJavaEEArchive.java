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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jst.j2ee.application.internal.operations.EARComponentExportDataModelProvider;
import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties;
import org.eclipse.jst.j2ee.internal.ejb.project.operations.EJBComponentExportDataModelProvider;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebComponentExportDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;



@SuppressWarnings("restriction")
public class ExportJavaEEArchive {

	
	public static File export(final IModule module, final IProgressMonitor monitor) throws ExecutionException {

		final File root = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

		IProject p = module.getProject();
		File archiveName = null;
		IDataModel dataModel = null;

		if (AssembleModules.isModuleType(module, "jst.web")) {
			dataModel = DataModelFactory
					.createDataModel(new WebComponentExportDataModelProvider());
			archiveName = new File(root, p.getName() + ".war");

		} else if (AssembleModules.isModuleType(module, "jst.ear")) {
			dataModel = DataModelFactory
					.createDataModel(new EARComponentExportDataModelProvider());
			archiveName = new File(root, p.getName() + ".ear");
		} else {// default
			 dataModel = DataModelFactory.createDataModel(new
					 EJBComponentExportDataModelProvider());
			archiveName = new File(root, p.getName() + ".jar");

		}		
		


			dataModel.setProperty(IJ2EEComponentExportDataModelProperties.PROJECT_NAME, p.getName());
			dataModel.setProperty(IJ2EEComponentExportDataModelProperties.EXPORT_SOURCE_FILES, false);
			dataModel.setProperty(IJ2EEComponentExportDataModelProperties.OVERWRITE_EXISTING, true);
			dataModel.setProperty(IJ2EEComponentExportDataModelProperties.ARCHIVE_DESTINATION, archiveName.getAbsolutePath());

			dataModel.getDefaultOperation().execute(monitor, null);
			return archiveName;
		
	}
		
	
	
}
