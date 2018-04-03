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

package org.eclipse.payara.tools.ui.rest.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties;
import org.eclipse.jst.j2ee.internal.dialogs.TypeSearchEngine;
import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
import org.eclipse.jst.j2ee.internal.web.operations.INewWebClassDataModelProperties;
import org.eclipse.jst.jee.ui.internal.navigator.web.WebAppProvider;
import org.eclipse.jst.servlet.ui.internal.wizard.NewWebClassWizardPage;
import org.eclipse.payara.tools.utils.WizardUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

@SuppressWarnings("restriction")
public class AddGenericResourceWizardPage extends NewWebClassWizardPage {

    private Combo patternTypeCombo;
    private Text pathText;
    private Combo mimeTypeCombo;
    private Text repText;
    private Text containerRepText;
    private Button containerRepButton;
    private Text containerPathText;

    private static final Map<String, String> patternStringToObject;

    static {
        patternStringToObject = new HashMap<>();
        patternStringToObject.put(Messages.patternTypeSimpleValue, AddGenericResourceTemplateModel.SIMPLE_PATTERN);
        patternStringToObject.put(Messages.patternTypeContainerValue, AddGenericResourceTemplateModel.CONTAINER_PATTERN);
        patternStringToObject.put(Messages.patternTypeClientContainerValue, AddGenericResourceTemplateModel.CLIENT_CONTAINER_PATTERN);
    }

    public AddGenericResourceWizardPage(IDataModel model, String pageName,
            String pageDesc, String pageTitle, String moduleType) {
        super(model, pageName, pageDesc, pageTitle, moduleType);
    }

    @Override
    protected Composite createTopLevelComposite(Composite parent) {
        Composite composite = super.createTopLevelComposite(parent);
        Label typeLabel = new Label(composite, SWT.NONE);
        GridData data = new GridData();

        typeLabel.setText(Messages.patternTypeLabel);
        typeLabel.setLayoutData(data);
        patternTypeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        patternTypeCombo.setLayoutData(data);
        patternTypeCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo combo = (Combo) e.getSource();
                String patternName = combo.getItem(combo.getSelectionIndex());
                String patternObject = patternStringToObject.get(patternName);

                model.setProperty(AddGenericResourceDataModelProvider.PATTERN,
                        patternObject);
                model.setProperty(AddGenericResourceDataModelProvider.PATH,
                        model.getDefaultProperty(AddGenericResourceDataModelProvider.PATH));
                model.notifyPropertyChange(AddGenericResourceDataModelProvider.PATTERN, DataModelEvent.ENABLE_CHG);
                updateEnablementFromPattern();
                validatePage();
            }

        });
        new Label(composite, SWT.NONE); // placeholder so layout is correct

        typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText(Messages.pathLabel);
        data = new GridData();
        typeLabel.setLayoutData(data);
        pathText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        pathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        synchHelper.synchText(pathText, AddGenericResourceDataModelProvider.PATH, null);
        new Label(composite, SWT.NONE); // placeholder so layout is correct

        typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText(Messages.mimeTypeLabel);
        data = new GridData();
        typeLabel.setLayoutData(data);
        mimeTypeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 300;
        data.horizontalSpan = 1;
        mimeTypeCombo.setLayoutData(data);
        synchHelper.synchCombo(mimeTypeCombo,
                AddGenericResourceDataModelProvider.MIME_TYPE, null);
        populateCombos();
        new Label(composite, SWT.NONE); // placeholder so layout is correct

        addRepresentationClassGroup(composite);
        addContainerRepresentationClassGroup(composite);

        typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText(Messages.containerPathLabel);
        data = new GridData();
        typeLabel.setLayoutData(data);
        containerPathText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        containerPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        synchHelper.synchText(containerPathText, AddGenericResourceDataModelProvider.CONTAINER_PATH, null);
        classText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                model.setProperty(AddGenericResourceDataModelProvider.CONTAINER_PATH,
                        model.getDefaultProperty(AddGenericResourceDataModelProvider.CONTAINER_PATH));
            }
        });

        // remove entire existing class section
        hideControl(existingClassButton);
        hideControl(existingClassLabel);
        hideControl(existingClassText);
        hideControl(existingButton);

        updateEnablementFromPattern();

        return composite;
    }

    /**
     * Add representation class group to the composite
     */
    private void addRepresentationClassGroup(Composite composite) {
        repText = addRepresentationClassLabelAndTextGroup(composite, Messages.representationClassLabel,
                AddGenericResourceDataModelProvider.REPRESENTATION_CLASS);
        addRepresentationClassButton(composite, Messages.representationClassDialogTitle,
                Messages.representationClassDialogLabel, repText);
    }

    /**
     * Add container representation class group to the composite
     */
    private void addContainerRepresentationClassGroup(Composite composite) {
        containerRepText = addRepresentationClassLabelAndTextGroup(composite, Messages.containerRepresentationClassLabel,
                AddGenericResourceDataModelProvider.CONTAINER_REPRESENTATION_CLASS);
        containerRepButton = addRepresentationClassButton(composite, Messages.containerRepresentationClassDialogTitle,
                Messages.containerRepresentationClassDialogLabel, containerRepText);
    }

    /**
     * Utility method for adding representation class groups
     */
    private Text addRepresentationClassLabelAndTextGroup(Composite composite, String repLabelString, String propertyName) {
        Label repLabel = new Label(composite, SWT.LEFT);
        repLabel.setText(repLabelString);
        repLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        Text repTextField = new Text(composite, SWT.SINGLE | SWT.BORDER);
        repTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        synchHelper.synchText(repTextField, propertyName, null);

        return repTextField;
    }

    /**
     * Utility method for adding representation class groups
     */
    private Button addRepresentationClassButton(Composite composite, final String dialogTitle, final String dialogLabel,
            final Text repTextField) {
        Button repButton = new Button(composite, SWT.PUSH);
        repButton.setText(J2EEUIMessages.BROWSE_BUTTON_LABEL);
        repButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        repButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleRepButtonPressed(dialogTitle, dialogLabel, repTextField);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }
        });
        return repButton;
    }

    protected void handleRepButtonPressed(String dialogTitle, String dialogLabel, Text repTextField) {
        getControl().setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
        IPackageFragmentRoot packRoot = (IPackageFragmentRoot) model
                .getProperty(INewJavaClassDataModelProperties.JAVA_PACKAGE_FRAGMENT_ROOT);
        if (packRoot == null) {
            return;
        }

        // this eliminates the non-exported classpath entries
        final IJavaSearchScope scope = TypeSearchEngine.createJavaSearchScopeForAProject(packRoot.getJavaProject(), true, true);

        FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(getShell(), false, getWizard().getContainer(), scope,
                IJavaSearchConstants.CLASS);
        dialog.setTitle(dialogTitle);
        dialog.setMessage(dialogLabel);

        if (dialog.open() == Window.OK) {
            IType type = (IType) dialog.getFirstResult();
            String repClassFullPath = J2EEUIMessages.EMPTY_STRING;
            if (type != null) {
                repClassFullPath = type.getFullyQualifiedName();
            }
            repTextField.setText(repClassFullPath);
            getControl().setCursor(null);
            return;
        }
        getControl().setCursor(null);
    }

    private void updateEnablementFromPattern() {
        boolean enableExtras = !model.getProperty(AddGenericResourceDataModelProvider.PATTERN).equals(
                AddGenericResourceTemplateModel.SIMPLE_PATTERN);

        containerPathText.setEnabled(enableExtras);
        containerRepText.setEnabled(enableExtras);
        containerRepButton.setEnabled(enableExtras);
    }

    private void populateCombos() {
        mimeTypeCombo.add(AddGenericResourceTemplateModel.TYPE_APP_XML);
        mimeTypeCombo.add(AddGenericResourceTemplateModel.TYPE_APP_JSON);
        mimeTypeCombo.add(AddGenericResourceTemplateModel.TYPE_TEXT_PLAIN);
        mimeTypeCombo.add(AddGenericResourceTemplateModel.TYPE_TEXT_HTML);
        mimeTypeCombo.select(0);

        patternTypeCombo.add(Messages.patternTypeSimpleValue);
        patternTypeCombo.add(Messages.patternTypeContainerValue);
        patternTypeCombo.add(Messages.patternTypeClientContainerValue);
        patternTypeCombo.select(0);
    }

    @Override
    protected String[] getValidationPropertyNames() {
        String[] base = super.getValidationPropertyNames();
        String[] result = new String[base.length + 5];
        System.arraycopy(base, 0, result, 0, base.length);
        result[base.length] = AddGenericResourceDataModelProvider.PATH;
        result[base.length + 1] = AddGenericResourceDataModelProvider.MIME_TYPE;
        result[base.length + 2] = AddGenericResourceDataModelProvider.REPRESENTATION_CLASS;
        result[base.length + 3] = AddGenericResourceDataModelProvider.CONTAINER_REPRESENTATION_CLASS;
        result[base.length + 4] = AddGenericResourceDataModelProvider.CONTAINER_PATH;

        return result;
    }

    @Override
    protected boolean isProjectValid(IProject project) {
        if (super.isProjectValid(project)) {
            return WizardUtil.hasGF3Runtime(project);
        }
        return false;
    }

    @Override
    protected IProject getExtendedSelectedProject(Object selection) {
        if (selection instanceof WebAppProvider) {
            return ((WebAppProvider) selection).getProject();
        }

        return super.getExtendedSelectedProject(selection);
    }

    @Override
    protected String getUseExistingCheckboxText() {
        // this is for the existing class browse button, which we have hidden
        return "Unused"; //$NON-NLS-1$
    }

    @Override
    protected String getUseExistingProperty() {
        // this is for the existing class browse button, which we have hidden
        return INewWebClassDataModelProperties.USE_EXISTING_CLASS;
    }

    @Override
    protected void handleClassButtonSelected() {
        // this is for the existing class browse button, which we have hidden
    }

    private void hideControl(Control control) {
        if (control != null) {
            control.setVisible(false);
            GridData data = new GridData();
            data.exclude = true;
            data.horizontalSpan = 2;
            data.horizontalAlignment = SWT.FILL;
            control.setLayoutData(data);
        }
    }
}
