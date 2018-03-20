/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.resources.wizards;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.payara.tools.ui.resources.MailInfo;
import org.eclipse.payara.tools.utils.ResourceUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * This is a wizard that creates a new JavaMail Session resource.
 */

public class MailWizard extends ResourceWizard {
	private MailResourceWizardPage page;
	
	/**
	 * Constructor for MailWizard.
	 */
	public MailWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	@Override
	public void addPages() {
		IContainer containerResource = getContainerResource();
		IProject selectedProject = ((containerResource != null) ? containerResource.getProject() : null);
		page = new MailResourceWizardPage(selectedProject, getGlassFishAndSailfinProjects());
		addPage(page);
	}
	
	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final String jndiName = page.getJNDIName();
		final MailInfo mailInfo = page.getMailInfo();
		final IProject selectedProject = page.getSelectedProject();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(jndiName, mailInfo, selectedProject, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			String message = realException.getMessage();
			if (message == null) {
				message = Messages.errorUnknown;
			}
			MessageDialog.openError(getShell(), Messages.ErrorTitle, message);
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file and open the editor on the newly created file.  If the 
	 * file already exists, show an error
	 */

	private void doFinish(String jndiName, MailInfo mailInfo, IProject selectedProject, IProgressMonitor monitor) throws CoreException {
		checkDir(selectedProject);
		
		monitor.beginTask("Creating " + ResourceUtils.RESOURCE_FILE_NAME, 2); 

		final IFile file = folder.getFile(new Path(ResourceUtils.RESOURCE_FILE_NAME));
		
		try {
			String fragment = createFragment(jndiName, mailInfo);
			InputStream stream = ResourceUtils.appendResource(file, fragment);
			if (!folder.exists()) {
				folder.create(true, true, monitor);
			}
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
            public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * Initialize the file contents to contents of the given resource.
	 */
	public static String createFragment(String jndiName, MailInfo mailInfo)
		throws CoreException {

		/* We want to be truly OS-agnostic */
		final String newline = System.getProperty("line.separator"); //$NON-NLS-1$

		String line;
		StringBuilder sb = new StringBuilder();
		final String mailHost = mailInfo.getMailHost();
		final String mailUser = mailInfo.getMailUser();
		final String mailFrom = mailInfo.getMailFrom();
		
		boolean matchStart = false;
		boolean matchEnd = false;
		
		try {
			InputStream input = MailInfo.class.getResourceAsStream(ResourceUtils.RESOURCE_FILE_TEMPLATE);
			BufferedReader reader = new BufferedReader( new InputStreamReader( input, StandardCharsets.UTF_8 ) );
			try {
				while ((line = reader.readLine()) != null) {
					if( line.indexOf("<mail-resource") != -1) { //$NON-NLS-1$
						matchStart = true;
					}
					if ( (matchStart) && (! matchEnd) ) {
						line = line.replaceAll("\\$\\{jndiName\\}", jndiName); //$NON-NLS-1$
						line = line.replaceAll("\\$\\{mailHost\\}", mailHost); //$NON-NLS-1$
						line = line.replaceAll("\\$\\{mailUser\\}", mailUser); //$NON-NLS-1$
						line = line.replaceAll("\\$\\{mailFrom\\}", mailFrom); //$NON-NLS-1$

						if (line != null) {
							sb.append(line);
							sb.append(newline);
						}
						if(line.indexOf("</mail-resource>") != -1) { //$NON-NLS-1$
							matchEnd = true;
						}
					}
				}
				
			} finally {
				reader.close();
			}
		} catch (IOException ioe) {
			IStatus status = new Status(IStatus.ERROR, "MailWizard", IStatus.OK, //$NON-NLS-1$
					ioe.getLocalizedMessage(), null);
			throw new CoreException(status);
		}

		return sb.toString();

	}

}
