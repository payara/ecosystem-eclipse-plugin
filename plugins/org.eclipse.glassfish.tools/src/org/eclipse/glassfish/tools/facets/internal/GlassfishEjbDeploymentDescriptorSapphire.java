/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.facets.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.glassfish.tools.facets.IGlassfishEjbDeploymentDescriptor;
import org.eclipse.glassfish.tools.facets.models.IGlassfishEjbDescriptorModel;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;

class GlassfishEjbDeploymentDescriptorSapphire extends
		AbstractGlassfishDeploymentDescriptor implements IGlassfishEjbDeploymentDescriptor {
	
	private IFile file;
	
	private IGlassfishEjbDescriptorModel model;
	
	GlassfishEjbDeploymentDescriptorSapphire(IFile file) throws ResourceStoreException {
		this.file = file;
		this.model = IGlassfishEjbDescriptorModel.TYPE.instantiate(new RootXmlResource(new XmlResourceStore(new WorkspaceFileResourceStore( file ))));
	}

	@Override
	protected void prepareDescriptor() {
		
	}

	@Override
	protected boolean isPossibleToCreate() {
		// check for existence of older sun descriptor
		IPath sunDescriptor = file.getLocation().removeLastSegments(1).append(IGlassfishEjbDeploymentDescriptor.SUN_EJB_DEPLOYMENT_DESCRIPTOR_NAME);
		if (sunDescriptor.toFile().exists())
			return false;
		return true;
	}

	@Override
	protected void save() {
		try {
			model.resource().save();
		} catch (ResourceStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
