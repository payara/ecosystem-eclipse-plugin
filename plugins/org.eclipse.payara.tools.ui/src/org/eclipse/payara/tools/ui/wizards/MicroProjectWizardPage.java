/**
 * Copyright (c) 2020 Payara Foundation
 * Copyright (c) 2008-2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.payara.tools.ui.wizards;

import java.util.regex.Pattern;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.wizards.AbstractMavenWizardPage;
import org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardArchetypeParametersPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MicroProjectWizardPage extends AbstractMavenWizardPage {

    private Combo groupIdCombo;
    
    private Combo artifactIdCombo;

    private Combo versionCombo;

    private Combo packageCombo;
    
    private boolean packageCustomized;

    private static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT"; //$NON-NLS-1$

    public MicroProjectWizardPage(ProjectImportConfiguration projectImportConfiguration) {
        super(MicroProjectWizardPage.class.getSimpleName(), projectImportConfiguration);
        setTitle(Messages.microProjectSettingsPageTitle);
        setDescription(Messages.microProjectSettingsPageDescription);
        setPageComplete(false);
    }

    public String getGroupId() {
        return this.groupIdCombo.getText();
    }

    public String getArtifactId() {
        return this.artifactIdCombo.getText();
    }

    public String getVersion() {
        return this.versionCombo.getText();
    }
    
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout(3, false));
        createUI(composite);
        validate();
        createAdvancedSettings(composite, new GridData(SWT.FILL, SWT.TOP, false, false, 3, 1));
        resolverConfigurationComponent.setModifyListener(e -> validate());
        setControl(composite);
    }

    private void createUI(Composite parent) {
        Label groupIdlabel = new Label(parent, SWT.NONE);
        groupIdlabel.setText(Messages.groupIdComponentLabel);

        groupIdCombo = new Combo(parent, SWT.BORDER);
        groupIdCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        addFieldWithHistory("groupId", groupIdCombo); //$NON-NLS-1$
        groupIdCombo.setData("name", "groupId"); //$NON-NLS-1$ //$NON-NLS-2$
        groupIdCombo.addModifyListener(e -> {
            updateJavaPackage();
            validate();
        });

        Label artifactIdLabel = new Label(parent, SWT.NONE);
        artifactIdLabel.setText(Messages.artifactIdComponentLabel);

        artifactIdCombo = new Combo(parent, SWT.BORDER);
        artifactIdCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        addFieldWithHistory("artifactId", artifactIdCombo); //$NON-NLS-1$
        artifactIdCombo.setData("name", "artifactId"); //$NON-NLS-1$ //$NON-NLS-2$
        artifactIdCombo.addModifyListener(e -> {
            updateJavaPackage();
            validate();
        });

        Label versionLabel = new Label(parent, SWT.NONE);
        versionLabel.setText(Messages.versionComponentLabel);

        versionCombo = new Combo(parent, SWT.BORDER);
        GridData gd_versionCombo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        gd_versionCombo.widthHint = 150;
        versionCombo.setLayoutData(gd_versionCombo);
        versionCombo.setText(DEFAULT_VERSION);
        addFieldWithHistory("version", versionCombo); //$NON-NLS-1$
        versionCombo.addModifyListener(e -> validate());

        Label packageLabel = new Label(parent, SWT.NONE);
        packageLabel.setText(Messages.packageComponentLabel);

        packageCombo = new Combo(parent, SWT.BORDER);
        packageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        packageCombo.setData("name", "package"); //$NON-NLS-1$ //$NON-NLS-2$
        addFieldWithHistory("package", packageCombo); //$NON-NLS-1$
        packageCombo.addModifyListener(e -> {
            if (!packageCustomized && !packageCombo.getText().equals(getDefaultJavaPackage())) {
                packageCustomized = true;
            }
            validate();
        });
        
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            if (groupIdCombo.getText().length() == 0 && groupIdCombo.getItemCount() > 0) {
                groupIdCombo.setText(groupIdCombo.getItem(0));
                packageCombo.setText(getDefaultJavaPackage());
                packageCustomized = false;
            }
            validate();
        }
    }

    void validate() {
        if (isVisible()) {
            String error = validateInput();
            setErrorMessage(error);
            setPageComplete(error == null);
        }
    }

    private boolean isVisible() {
        return getControl() != null && getControl().isVisible();
    }

    private String validateInput() {
        String error = validateGroupIdInput(groupIdCombo.getText().trim());
        if (error != null) {
            return error;
        }

        error = validateArtifactIdInput(artifactIdCombo.getText().trim());
        if (error != null) {
            return error;
        }

        String versionValue = versionCombo.getText().trim();
        if (versionValue.length() == 0) {
            return Messages.versionValidationMessage;
        }

        String packageName = packageCombo.getText();
        if (packageName.trim().length() != 0) {
            if (!Pattern.matches("[A-Za-z_$][A-Za-z_$\\d]*(?:\\.[A-Za-z_$][A-Za-z_$\\d]*)*", packageName)) { //$NON-NLS-1$
                return Messages.packageValidationMessage;
            }
        }

        return null;
    }

    /**
     * Updates the package name if the related fields changed.
     */
    protected void updateJavaPackage() {
        if (packageCustomized) {
            return;
        }

        String defaultPackageName = getDefaultJavaPackage();
        packageCombo.setText(defaultPackageName);
    }

    /**
     * Returns the default package name.
     */
    protected String getDefaultJavaPackage() {
        return MavenProjectWizardArchetypeParametersPage.getDefaultJavaPackage(
                groupIdCombo.getText().trim(),
                artifactIdCombo.getText().trim()
        );
    }

    /**
     * Returns the package name.
     */
    public String getJavaPackage() {
        if (packageCombo.getText().length() > 0) {
            return packageCombo.getText();
        }
        return getDefaultJavaPackage();
    }

}
