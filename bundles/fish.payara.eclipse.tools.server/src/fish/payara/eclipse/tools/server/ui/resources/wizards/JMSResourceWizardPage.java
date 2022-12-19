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

package fish.payara.eclipse.tools.server.ui.resources.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import fish.payara.eclipse.tools.server.sdk.server.parser.ResourcesReader.ResourceType;
import fish.payara.eclipse.tools.server.ui.resources.JMSInfo;
import fish.payara.eclipse.tools.server.utils.ResourceUtils;

/**
 * @author Nitya Doraisamy
 */

public class JMSResourceWizardPage extends WizardPage {

    private Text jndiText;
    private Button queueRButton;
    private Button topicRButton;
    private Button queueConnectionRButton;
    private Button topicConnectionRButton;
    private Button connectionRButton;

    private IProject selectedProject;
    private List<IProject> candidateProjects;
    private JMSInfo jmsInfo;

    private Combo projectNameCombo;

    private List<String> resources = new ArrayList<>();
    private String defaultJndiName = "jms/myQueue"; //$NON-NLS-1$

    /**
     * Constructor for JMSResourceWizardPage.
     *
     * @param selection
     */
    public JMSResourceWizardPage(IProject project, List<IProject> projects) {
        super("wizardPage"); //$NON-NLS-1$
        setTitle(Messages.jmsWizardTitle);
        setDescription(Messages.jmsWizardDescription);
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

        // Dummy Label for spacing
        label = new Label(container, SWT.NULL);
        GridDataFactory.defaultsFor(label).span(3, 1).applyTo(label);

        label = new Label(container, SWT.NULL);
        label.setText(Messages.lblChooseType);
        GridDataFactory.defaultsFor(label).span(3, 1).applyTo(label);

        // Dummy Label for spacing
        label = new Label(container, SWT.NULL);
        GridDataFactory.defaultsFor(label).span(3, 1).applyTo(label);

        label = new Label(container, SWT.NULL);
        label.setText(Messages.lblAdminObject);
        GridDataFactory.defaultsFor(label).span(3, 1).applyTo(label);

        GridData gridData = new GridData();
        gridData.horizontalIndent = 40;
        gridData.horizontalSpan = 3;
        gridData.horizontalAlignment = GridData.FILL;

        queueRButton = new Button(container, SWT.RADIO);
        queueRButton.setText(Messages.lblQueue);
        queueRButton.setLayoutData(gridData);
        queueRButton.setSelection(true);

        topicRButton = new Button(container, SWT.RADIO);
        topicRButton.setText(Messages.lblTopic);
        topicRButton.setLayoutData(gridData);

        // Dummy Label for spacing
        label = new Label(container, SWT.NULL);
        GridDataFactory.defaultsFor(label).span(3, 1).applyTo(label);

        label = new Label(container, SWT.NULL);
        label.setText(Messages.lblConnector);
        GridDataFactory.defaultsFor(label).span(3, 1).applyTo(label);

        queueConnectionRButton = new Button(container, SWT.RADIO);
        queueConnectionRButton.setText(Messages.lblQueueConnectionFactory);
        queueConnectionRButton.setLayoutData(gridData);

        topicConnectionRButton = new Button(container, SWT.RADIO);
        topicConnectionRButton.setText(Messages.lblTopicConnectionFactory);
        topicConnectionRButton.setLayoutData(gridData);

        connectionRButton = new Button(container, SWT.RADIO);
        connectionRButton.setText(Messages.lblConnectionFactory);
        connectionRButton.setLayoutData(gridData);

        initialize();
        dialogChanged();
        setControl(container);
    }

    private void initialize() {
        resources = ResourceUtils.getResources(selectedProject, ResourceType.CONNECTOR_RESOURCE, ResourceType.ADMIN_OBJECT_RESOURCE);
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

    public String getResourceType() {
        String resourceType = null;
        if (queueRButton.getSelection()) {
            resourceType = JMSInfo.QUEUE;
        } else if (topicRButton.getSelection()) {
            resourceType = JMSInfo.TOPIC;
        } else if (queueConnectionRButton.getSelection()) {
            resourceType = JMSInfo.QUEUE_CONNECTION;
        } else if (topicConnectionRButton.getSelection()) {
            resourceType = JMSInfo.TOPIC_CONNECTION;
        } else if (connectionRButton.getSelection()) {
            resourceType = JMSInfo.CONNECTION_FACTORY;
        }
        return resourceType;
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
        if (jndiName.length() == 0) {
            setErrorMessage(Messages.errorJndiNameMissing);
            return;
        } else {
            if (ResourceUtils.isDuplicate(jndiName, resources)) {
                setErrorMessage(NLS.bind(Messages.errorDuplicateName, jndiName));
                return;
            }
        }

        if ((getResourceType() == null) || (getResourceType().length() == 0)) {
            setErrorMessage(Messages.errorResourceTypeMissing);
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

    public JMSInfo getJMSInfo() {
        jmsInfo = new JMSInfo();
        jmsInfo.setJndiName(getJNDIName());
        jmsInfo.setResourceType(getResourceType());
        return jmsInfo;
    }

}
