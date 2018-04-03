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
import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.payara.tools.sdk.server.parser.ResourcesReader.ResourceType;
import org.eclipse.payara.tools.ui.resources.MailInfo;
import org.eclipse.payara.tools.utils.ResourceUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 *
 */

public class MailResourceWizardPage extends WizardPage {
    private Text jndiText;
    private Text hostText;
    private Text userText;
    private Text fromText;

    private IProject selectedProject;
    private List<IProject> candidateProjects;
    private MailInfo mailInfo;

    private Combo projectNameCombo;

    private List<String> resources = new ArrayList<>();
    private String defaultJndiName = "mail/mymailSession"; //$NON-NLS-1$

    /**
     * Constructor for MailResourceWizardPage.
     *
     * @param selection
     */
    public MailResourceWizardPage(IProject project, List<IProject> projects) {
        super("wizardPage"); //$NON-NLS-1$
        setTitle(Messages.mailWizardTitle);
        setDescription(Messages.mailWizardDescription);
        selectedProject = project;
        candidateProjects = projects;
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    @Override
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
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
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
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        label = new Label(container, SWT.NULL);
        label.setText(Messages.MailHost);

        hostText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridDataFactory.defaultsFor(hostText).span(2, 1).applyTo(hostText);
        hostText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        label = new Label(container, SWT.NULL);
        label.setText(Messages.MailUser);

        userText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridDataFactory.defaultsFor(userText).span(2, 1).applyTo(userText);
        userText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        label = new Label(container, SWT.NULL);
        label.setText(Messages.MailFrom);

        fromText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridDataFactory.defaultsFor(fromText).span(2, 1).applyTo(fromText);
        fromText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        initialize();
        dialogChanged();
        setControl(container);
    }

    private void initialize() {
        resources = ResourceUtils.getResources(selectedProject, ResourceType.JAVA_MAIL);
        if (resources.contains(defaultJndiName)) {
            String jndiName = ResourceUtils.getUniqueResourceName(defaultJndiName, resources);
            jndiText.setText(jndiName);
        }
        populateCombos();
        dialogChanged();
    }

    public String getJNDIName() {
        return jndiText.getText();
    }

    public String getMailHost() {
        return hostText.getText();
    }

    public String getMailUser() {
        return userText.getText();
    }

    public String getMailFrom() {
        return fromText.getText();
    }

    public IProject getSelectedProject() {
        return selectedProject;
    }

    private void dialogChanged() {
        setPageComplete(false);
        boolean hasProject = (projectNameCombo.getSelectionIndex() != -1);
        if (!hasProject) {
            setErrorMessage(Messages.errorProjectMissing);
            return;
        }
        String jndiName = getJNDIName();
        if ((jndiName == null) || (jndiName.length() == 0)) {
            setErrorMessage(Messages.errorJndiNameMissing);
            return;
        } else {
            if (ResourceUtils.isDuplicate(jndiName, resources)) {
                setErrorMessage(NLS.bind(Messages.errorDuplicateName, jndiName));
                return;
            }
        }
        String mailHost = getMailHost();
        if ((mailHost == null) || (mailHost.length() == 0)) {
            setErrorMessage(Messages.errorMailHostNameMissing);
            return;
        }
        String mailUser = getMailUser();
        if ((mailUser == null) || (mailUser.length() == 0)) {
            setErrorMessage(Messages.errorMailUserNameMissing);
            return;
        }
        String mailFrom = getMailFrom();
        if ((mailFrom == null) || (mailFrom.length() == 0)) {
            setErrorMessage(Messages.errorMailReturnAddrMissing);
            return;
        }
        setPageComplete(true);
        setErrorMessage(null);
    }

    private void populateCombos() {
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

    public MailInfo getMailInfo() {
        mailInfo = new MailInfo();
        mailInfo.setJndiName(getJNDIName());
        mailInfo.setMailFrom(getMailFrom());
        mailInfo.setMailHost(getMailHost());
        mailInfo.setMailUser(getMailUser());
        return mailInfo;
    }

}
