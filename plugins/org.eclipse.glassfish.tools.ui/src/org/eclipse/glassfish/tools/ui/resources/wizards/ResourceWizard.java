/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.resources.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.glassfish.tools.server.GlassFishRuntime;
import org.eclipse.glassfish.tools.utils.ResourceUtils;

/**
 * This is a superclass for wizards to create resources
 */

@SuppressWarnings("restriction")
public abstract class ResourceWizard extends Wizard implements INewWizard {

	protected IStructuredSelection selection;
	protected String dirName;
	protected IFolder folder;
	
	/**
	 * Constructor 
	 */
	public ResourceWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	protected void checkDir(IProject selectedProject) throws CoreException {
		dirName = ResourceUtils.getResourceLocation(selectedProject);
		if(dirName == null) {
			IStatus status = new Status(IStatus.ERROR, getClass().getName(), IStatus.OK, 
					NLS.bind(Messages.errorFolderNull, dirName), null);
			throw new CoreException(status);
		}
		IContainer containerResource = selectedProject;
		folder = containerResource.getFolder(new Path(dirName));
		if (!folder.exists()) {
			IStatus status = new Status(IStatus.ERROR, getClass().getName(), IStatus.OK, 
					NLS.bind(Messages.errorFolderMissing, dirName), null);
			throw new CoreException(status);
		}
	}

	
	protected static String replaceOrRemove(String originalLine, String pattern, String value) {
		String containsPattern = ".*" + pattern + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
		if ((originalLine != null) && Pattern.matches(containsPattern, originalLine)) {
			return (((value == null) || (value.length() == 0)) ? null : 
				originalLine.replaceAll(pattern, value));
		}
		return originalLine;
	}

	protected IContainer getContainerResource() {
		if (selection != null && selection.isEmpty() == false) {
			IStructuredSelection ssel =  selection;
			if (ssel.size() > 1)
				return null;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer containerResource;
				if (obj instanceof IContainer)
					containerResource = (IContainer) obj;
				else
					containerResource = ((IResource) obj).getParent();
				
				return ((containerResource != null) ? containerResource.getProject() : null);
			}
		}
		return null;
	}

	protected List<IProject> getGlassFishAndSailfinProjects() {
		IProject[] allProjects = ProjectUtilities.getAllProjects();
		List<IProject> returnProjects = new ArrayList<IProject>();

		for (IProject project2 : allProjects) {
			try {
				if (FacetedProjectFramework.hasProjectFacet(project2, "sun.facet")) { //$NON-NLS-1$
					returnProjects.add(project2);
				} else {
					IRuntime runtime = J2EEProjectUtilities.getServerRuntime(project2);
					if (runtime != null) {
						String runtimeId = runtime.getRuntimeType().getId();
						
						if( runtimeId.equals( GlassFishRuntime.TYPE_ID ) )
						{
						    returnProjects.add(project2);
						}
                    }
				}
			} catch (CoreException e) {
				// just skip from list
			}
		}
		return returnProjects;
	}


	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
