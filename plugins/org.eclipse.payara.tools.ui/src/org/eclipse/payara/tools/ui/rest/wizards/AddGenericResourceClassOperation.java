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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.codegen.jet.JETException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.j2ee.internal.web.operations.NewWebClassOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.internal.enablement.nonui.WFTWrappedException;

@SuppressWarnings("restriction")
public class AddGenericResourceClassOperation extends NewWebClassOperation {

    /**
     * folder location of the Generic Resource creation templates directory
     */
    protected static final String TEMPLATE_FILE = "/templates/genericresource.javajet"; //$NON-NLS-1$

    public AddGenericResourceClassOperation(IDataModel dataModel) {
        super(dataModel);
    }

    @Override
    protected void generateUsingTemplates(IProgressMonitor monitor,
            IPackageFragment fragment) throws WFTWrappedException,
            CoreException {
        // Create the template model
        AddGenericResourceTemplateModel tempModel = new AddGenericResourceTemplateModel(model);
        // Using the WTPJetEmitter, generate the java source
        // template model
        try {
            if (fragment != null) {
                // Create the Generic Resource java file
                doGeneration(monitor, fragment, GenericResourceTemplate.create(null), tempModel);
                // also generate the second class if necessary
                if (!tempModel.isSimplePattern()) {
                    tempModel.setIsContainerClass();
                    doGeneration(monitor, fragment, ContainerResourceTemplate.create(null), tempModel);
                }
            }
        } catch (Exception e) {
            throw new WFTWrappedException(e);
        }
    }

    private void doGeneration(IProgressMonitor monitor, IPackageFragment fragment,
            Object tempImpl, AddGenericResourceTemplateModel tempModel) throws JavaModelException, JETException {
        try {
            Method method = tempImpl.getClass().getMethod("generate", //$NON-NLS-1$
                    new Class[] { Object.class });
            String source = (String) method.invoke(tempImpl, tempModel);
            String javaFileName = tempModel.getClassName() + ".java"; //$NON-NLS-1$
            createJavaFile(monitor, fragment, source, javaFileName);
        } catch (SecurityException e) {
            throw new JETException(e);
        } catch (NoSuchMethodException e) {
            throw new JETException(e);
        } catch (IllegalArgumentException e) {
            throw new JETException(e);
        } catch (IllegalAccessException e) {
            throw new JETException(e);
        } catch (InvocationTargetException e) {
            throw new JETException(e);
        }
    }

    protected IFile createJavaFile(IProgressMonitor monitor, IPackageFragment fragment, String source, String className)
            throws JavaModelException {
        if (fragment != null) {
            ICompilationUnit cu = fragment.getCompilationUnit(className);
            // Add the compilation unit to the java file
            if (cu == null || !cu.exists()) {
                cu = fragment.createCompilationUnit(className, source,
                        true, monitor);
            }
            return (IFile) cu.getResource();
        }
        return null;
    }

    @Override
    protected AddGenericResourceTemplateModel createTemplateModel() {
        return new AddGenericResourceTemplateModel(model);
    }

    @Override
    protected String getTemplateFile() {
        return TEMPLATE_FILE;
    }

    @Override
    protected Object getTemplateImplementation() {
        return GenericResourceTemplate.create(null);
    }
}
