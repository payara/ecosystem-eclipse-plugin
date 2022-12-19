/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.ui.rest.wizards;

import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.JAVA_PACKAGE;
import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.PROJECT;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jst.j2ee.internal.plugin.J2EEEditorUtility;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.servlet.ui.internal.plugin.ServletUIPlugin;
import org.eclipse.jst.servlet.ui.internal.wizard.NewWebArtifactWizard;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;

@SuppressWarnings("restriction")
public class AddGenericResourceWizard extends NewWebArtifactWizard {

    public AddGenericResourceWizard() {
        this(null);
    }

    public AddGenericResourceWizard(IDataModel model) {
        super(model);
        setWindowTitle(Messages.genericResourceWizardTitle);
    }

    @Override
    protected void doAddPages() {
        AddGenericResourceWizardPage page1 = new AddGenericResourceWizardPage(getDataModel(),
                "page1", Messages.genericResourceWizardDescription, //$NON-NLS-1$
                Messages.genericResourceWizardTitle, IJ2EEFacetConstants.DYNAMIC_WEB);
        addPage(page1);
    }

    @Override
    protected IDataModelProvider getDefaultProvider() {
        return new AddGenericResourceDataModelProvider();
    }

    @Override
    protected ImageDescriptor getImage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getTitle() {
        return Messages.genericResourceWizardTitle;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizard#postPerformFinish()
     */
    @Override
    protected void postPerformFinish() throws InvocationTargetException {
        openJavaClass();
        super.postPerformFinish();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jst.servlet.ui.internal.wizard.NewWebArtifactWizard#openJavaClass()
     */
    @Override
    protected void openJavaClass() {
        IDataModel model = getDataModel();
        if (model.getBooleanProperty(AddGenericResourceDataModelProvider.IN_CONTAINER_CLASS)) {
            try {
                String className = model.getStringProperty(AddGenericResourceDataModelProvider.ORIGINAL_CLASS_NAME);
                String packageName = model.getStringProperty(JAVA_PACKAGE);

                if (packageName != null && packageName.trim().length() > 0) {
                    className = packageName + "." + className; //$NON-NLS-1$
                }

                IProject p = (IProject) model.getProperty(PROJECT);
                IJavaProject javaProject = J2EEEditorUtility.getJavaProject(p);
                IFile file = (IFile) javaProject.findType(className).getResource();
                openEditor(file);
            } catch (Exception cantOpen) {
                ServletUIPlugin.log(cantOpen);
            }
        }
        super.openJavaClass();
    }
}
