/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.resources.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.datatools.connectivity.ICategory;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.IProfileListener;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.internal.ConnectionProfileManager;
import org.eclipse.datatools.connectivity.internal.ui.wizards.CPWizardNode;
import org.eclipse.datatools.connectivity.internal.ui.wizards.NewCPWizard;
import org.eclipse.datatools.connectivity.internal.ui.wizards.ProfileWizardProvider;
import org.eclipse.datatools.connectivity.ui.wizards.IWizardCategoryProvider;
import org.eclipse.payara.tools.sdk.server.parser.ResourcesReader.ResourceType;
import org.eclipse.payara.tools.ui.resources.JDBCInfo;
import org.eclipse.payara.tools.utils.ResourceUtils;
import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */

public class JDBCResourceWizardPage extends WizardPage {
	private static final String DATABASE_CATEGORY_ID = "org.eclipse.datatools.connectivity.db.category"; //$NON-NLS-1$

	private Text jndiText;
	private IConnectionProfile connectionProfile;
	private IProject selectedProject;
	private List<IProject> candidateProjects;

	private Combo combo;
	private Combo projectNameCombo;

	private NewCPWizard wizard;

	private WizardDialog wizardDialog;

	private List<String> resources = new ArrayList<String>();
	private String defaultJndiName = "jdbc/myDatasource"; //$NON-NLS-1$
	
	/**
	 * Constructor for JDBCResourceWizardPage.
	 * 
	 * @param selection
	 */
	public JDBCResourceWizardPage(IProject project, List<IProject> projects) {
		super("wizardPage"); //$NON-NLS-1$
		setTitle(Messages.wizardTitle);
		setDescription(Messages.wizardDescription);
		selectedProject = project;
		candidateProjects = projects;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(Messages.ProjectName);

		projectNameCombo = new Combo(container, SWT.READ_ONLY | SWT.SINGLE);
		GridDataFactory.defaultsFor(projectNameCombo).span(2, 1).applyTo(projectNameCombo);
		projectNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected( SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected( SelectionEvent e) {
				String newSelection = projectNameCombo.getText();
				if (newSelection != null) {
					selectedProject = ProjectUtilities.getProject(newSelection);
					resources = ResourceUtils.getResources(selectedProject, ResourceType.JDBC_RESOURCE);
					dialogChanged();
				}
			}
		});
		
		label = new Label(container, SWT.NULL);
		label.setText(Messages.JNDIName);

		jndiText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridDataFactory.defaultsFor(jndiText).span(2, 1).applyTo(jndiText);
		jndiText.setText(defaultJndiName); 
		jndiText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });
		
		label = new Label(container, SWT.NULL);
		label.setText(Messages.Connection);

		combo = new Combo(container, SWT.READ_ONLY | SWT.SINGLE);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected( SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected( SelectionEvent e) {
				String newSelection = combo.getText();
				if (newSelection != null) {
					connectionProfile = ProfileManager.getInstance().
						getProfileByName(newSelection);
				}
			}
		});
		GridDataFactory.defaultsFor(combo).align(SWT.FILL, SWT.CENTER).applyTo(combo);

		Button button = new Button(container, SWT.PUSH);
		button.setText(Messages.Create);
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		
			public void widgetSelected(SelectionEvent e) {
				IConnectionProfile newProfile = showCPWizard();
				if (newProfile != null) {
					connectionProfile = newProfile;
					String newName = connectionProfile.getName();
					
					combo.add(newName);
					combo.select(combo.indexOf(newName));
					combo.pack();
				}
				dialogChanged();
			}
		});
		GridDataFactory.defaultsFor(button).align(SWT.END, SWT.CENTER).applyTo(button);

		initialize();
		setControl(container);
	}

	private void initialize() {
		populateCombos();
		dialogChanged();
        resources = ResourceUtils.getResources(selectedProject, ResourceType.JDBC_RESOURCE);
		if(resources.contains(defaultJndiName)){
			String jndiName = ResourceUtils.getUniqueResourceName(defaultJndiName, resources);
			jndiText.setText(jndiName);
		}
	}

	private void dialogChanged() {
		setPageComplete(false);
		boolean hasProject = (projectNameCombo.getSelectionIndex() != -1);
		boolean hasConnection = (combo.getSelectionIndex() != -1);
		
		if (!hasProject) {
			setErrorMessage(Messages.errorProjectMissing);
			return;
		}
		String jndiName = getJNDIName();
		if ((jndiName == null) || (jndiName.length() == 0 )) {
			setErrorMessage(Messages.errorJndiNameMissing);
			return;
		}else {
			if(ResourceUtils.isDuplicate(jndiName, resources)) {
				setErrorMessage(NLS.bind(Messages.errorDuplicateName, jndiName));
				return;
			}	
		}
		if (!hasConnection) {
			setErrorMessage(Messages.errorConnectionMissing);
			return;
		}
		if(connectionProfile == null){
			setErrorMessage(Messages.errorConnectionInvalid);
			return;
		}
		
		setErrorMessage(null);
		setPageComplete(true);
	}
	
	public String getJNDIName() {
		return jndiText.getText();
	}

	public IProject getSelectedProject() {
		return selectedProject;
	}

	private IConnectionProfile showCPWizard () {
		// Filter datasource category
	  	ViewerFilter viewerFilter = new ViewerFilter() {
	
			@Override
			public boolean select( Viewer viewer, Object parentElement, Object element) {
				
				CPWizardNode wizardNode = ( CPWizardNode) element;
				if( !( wizardNode.getProvider() instanceof IWizardCategoryProvider)) {
					ICategory cat = ConnectionProfileManager.getInstance().getProvider(
									(( ProfileWizardProvider) wizardNode.getProvider()).getProfile()).getCategory();
					
					// Only display wizards belong to database category
					while( cat != null) {
						if( cat.getId().equals(DATABASE_CATEGORY_ID))
							return true;
						cat = cat.getParent();
					}
				}
				return false;
			}
		};
		wizard = new NewCPWizard( viewerFilter, null);
		Shell currentShell = Display.getCurrent().getActiveShell();
		wizardDialog = new WizardDialog( currentShell, wizard);
		wizardDialog.setBlockOnOpen( true);
		
		ProfileManager profileManager = ProfileManager.getInstance();
		ProfileListener listener = new ProfileListener();
		profileManager.addProfileListener(listener);
		int result = wizardDialog.open();
		profileManager.removeProfileListener(listener);
		
		return ((result != Window.CANCEL) ? listener.newProfile : null);
	}

	private IConnectionProfile[] getConnectionProfiles () {
		return ProfileManager.getInstance().getProfilesByCategory(DATABASE_CATEGORY_ID);
	}

	private void populateCombos() {
		combo.removeAll();
		for (IConnectionProfile profile : getConnectionProfiles()) {
			if (connectionProfile == null) {
				connectionProfile = profile;
			}
			combo.add(profile.getName());
		}

		if (combo.getItemCount() > 0) {
			combo.select(0);
		}

		projectNameCombo.removeAll();
		String selectProjectName = ((selectedProject != null) ? selectedProject.getName() : null);
		int selectionIndex = -1;
		for (int i = 0; i < candidateProjects.size(); i++) {
			IProject nextProject = candidateProjects.get(i);
			String projectName = nextProject.getName();
			projectNameCombo.add(projectName);
			if (projectName.equals(selectProjectName)) {
				selectionIndex = i;
			}
		}
		if ((selectionIndex != -1) && (projectNameCombo.getItemCount() > 0)) {
			projectNameCombo.select(selectionIndex);
		} else { // selectedProject is not a valid candidate project
			selectedProject = null;
		}
	}

	JDBCInfo getJDBCInfo() {
		return new JDBCInfo(connectionProfile);
	}

	static class ProfileListener implements IProfileListener {
		IConnectionProfile newProfile;
		
		public void profileAdded( IConnectionProfile profile) {
			newProfile = profile;
		}
	
		public void profileChanged( IConnectionProfile profile) {
			// do nothing
		}
	
		public void profileDeleted( IConnectionProfile profile) {
			// do nothing
		}
	}
}
