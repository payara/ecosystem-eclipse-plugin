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

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties;
import org.eclipse.jst.j2ee.internal.common.operations.NewJavaClassDataModelProvider;
import org.eclipse.jst.j2ee.internal.common.operations.NewJavaEEArtifactClassOperation;
import org.eclipse.jst.j2ee.internal.web.operations.AddWebClassOperation;
import org.eclipse.jst.j2ee.internal.web.operations.NewWebClassDataModelProvider;
import org.eclipse.payara.tools.PayaraToolsPlugin;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;

@SuppressWarnings("restriction")
public class AddGenericResourceDataModelProvider extends
        NewWebClassDataModelProvider {

    public static final String PATTERN = "AddGenericResource.PATTERN"; //$NON-NLS-1$
    public static final String PATH = "AddGenericResource.PATH"; //$NON-NLS-1$
    public static final String MIME_TYPE = "AddGenericResource.MIME_TYPE"; //$NON-NLS-1$
    public static final String REPRESENTATION_CLASS = "AddGenericResource.REPRESENTATION_CLASS"; //$NON-NLS-1$
    public static final String CONTAINER_REPRESENTATION_CLASS = "AddGenericResource.CONTAINER_REPRESENTATION_CLASS"; //$NON-NLS-1$
    public static final String CONTAINER_PATH = "AddGenericResource.CONTAINER_PATH"; //$NON-NLS-1$
    public static final String IN_CONTAINER_CLASS = "AddGenericResource.IN_CONTAINER_CLASS"; //$NON-NLS-1$
    public static final String ORIGINAL_CLASS_NAME = "AddGenericResource.ORIGINAL_CLASS_NAME"; //$NON-NLS-1$

    @Override
    public IDataModelOperation getDefaultOperation() {
        return new AddWebClassOperation(getDataModel()) {

            @Override
            protected NewJavaEEArtifactClassOperation getNewClassOperation() {
                return new AddGenericResourceClassOperation(getDataModel());
            }

            @Override
            protected void generateMetaData(IDataModel aModel, String qualifiedClassName) {
                // for now, do nothing here - data model should be ok as is
            }
        };
    }

    /**
     * Subclasses may extend this method to add their own data model's properties as valid base
     * properties.
     *
     * @see org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider#getPropertyNames()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getPropertyNames() {
        // Add Resource specific properties defined in this data model
        Set<String> propertyNames = super.getPropertyNames();

        propertyNames.add(REPRESENTATION_CLASS);
        propertyNames.add(MIME_TYPE);
        propertyNames.add(CONTAINER_REPRESENTATION_CLASS);
        propertyNames.add(CONTAINER_PATH);
        propertyNames.add(PATH);
        propertyNames.add(PATTERN);
        propertyNames.add(IN_CONTAINER_CLASS);
        propertyNames.add(ORIGINAL_CLASS_NAME);

        return propertyNames;
    }

    /**
     * Subclasses may extend this method to provide their own default values for any of the properties
     * in the data model hierarchy. This method does not accept a null parameter. It may return null.
     *
     * @see NewJavaClassDataModelProvider#getDefaultProperty(String)
     * @see IDataModelProvider#getDefaultProperty(String)
     *
     * @param propertyName
     * @return Object default value of property
     */
    @Override
    public Object getDefaultProperty(String propertyName) {
        if (REPRESENTATION_CLASS.equals(propertyName) || CONTAINER_REPRESENTATION_CLASS.equals(propertyName)) {
            return "java.lang.String"; //$NON-NLS-1$
        }

        if (PATTERN.equals(propertyName)) {
            return AddGenericResourceTemplateModel.SIMPLE_PATTERN;
        }

        if (PATH.equals(propertyName)) {
            return (isSimplePattern() ? "generic" : (isClientControlledPattern() ? "{name}" : "{id}")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if (CONTAINER_PATH.equals(propertyName)) {
            String className = getStringProperty(INewJavaClassDataModelProperties.CLASS_NAME);
            if ((className != null) && (className.length() > 0)) {
                return "/" + className.substring(0, 1).toLowerCase() + className.substring(1) + "s"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        if (IN_CONTAINER_CLASS.equals(propertyName)) {
            return Boolean.FALSE;
        }

        // Otherwise check super for default value for property
        return super.getDefaultProperty(propertyName);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jst.j2ee.internal.web.operations.NewWebClassDataModelProvider#isPropertyEnabled(java.
     * lang.String)
     */
    @Override
    public boolean isPropertyEnabled(String propertyName) {
        if (CONTAINER_REPRESENTATION_CLASS.equals(propertyName) || CONTAINER_PATH.equals(propertyName)) {
            return !isSimplePattern();
        }
        return super.isPropertyEnabled(propertyName);
    }

    @Override
    public IStatus validate(String propertyName) {
        if (MIME_TYPE.equals(propertyName)) {
            String value = (String) getProperty(MIME_TYPE);
            if (value == null || value.trim().length() == 0) {
                return PayaraToolsPlugin.createErrorStatus(
                        Messages.errorMimeTypeMissing, null);
            }
        }
        if (REPRESENTATION_CLASS.equals(propertyName)) {
            return validateRepClass(REPRESENTATION_CLASS, Messages.errorRepresentationClassMissing,
                    Messages.errorRepresentationClassInvalid);
        }

        if (PATH.equals(propertyName)) {
            return validatePath((String) getProperty(PATH));
        }

        // these only need validation in the case of pattern != simple
        if (isPropertyEnabled(propertyName)) {
            if (CONTAINER_PATH.equals(propertyName)) {
                String value = (String) getProperty(CONTAINER_PATH);
                if (value == null || value.trim().length() == 0) {
                    return PayaraToolsPlugin.createErrorStatus(
                            Messages.errorContainerPathMissing, null);
                }
            }
            if (CONTAINER_REPRESENTATION_CLASS.equals(propertyName)) {
                return validateRepClass(CONTAINER_REPRESENTATION_CLASS, Messages.errorContainerRepresentationClassMissing,
                        Messages.errorContainerRepresentationClassInvalid);
            }
        }
        IStatus status = super.validate(propertyName);
        return status;
    }

    private boolean isSimplePattern() {
        return AddGenericResourceTemplateModel.SIMPLE_PATTERN.equals(getStringProperty(PATTERN));
    }

    private boolean isClientControlledPattern() {
        return AddGenericResourceTemplateModel.CLIENT_CONTAINER_PATTERN.equals(getStringProperty(PATTERN));
    }

    protected IStatus validateRepClass(String propertyName, String errorMessageKeyMissing, String errorMessageKeyInvalid) {
        String value = (String) getProperty(propertyName);
        if (value == null || value.trim().length() == 0) {
            return PayaraToolsPlugin.createErrorStatus(errorMessageKeyMissing, null);
        }
        // Check that unqualified class name is valid by standard java conventions
        String className = value;
        int index = value.lastIndexOf("."); //$NON-NLS-1$
        if (index != -1) {
            className = value.substring(index + 1);
        }
        IStatus javaStatus = validateJavaClassName(className);
        if (javaStatus.getSeverity() != IStatus.ERROR) {
            // If the class does not exist, throw an error
            IJavaProject javaProject = JavaCore.create(getTargetProject());
            IType type = null;
            try {
                type = javaProject.findType(value);
            } catch (Exception e) {
                // Just throw error below
            }
            if (type == null) {
                return WTPCommonPlugin.createErrorStatus(errorMessageKeyInvalid);
            }
            return WTPCommonPlugin.OK_STATUS;
        }
        return javaStatus;
    }

    private IStatus validatePath(String path) {
        if (path == null || path.trim().length() == 0) {
            return PayaraToolsPlugin.createErrorStatus(
                    Messages.errorPathMissing, null);
        }
        if (!isSimplePattern()) {
            StringTokenizer segments = new StringTokenizer(path, "/ "); //$NON-NLS-1$
            Set<String> pathParts = new HashSet<>();
            while (segments.hasMoreTokens()) {
                String segment = segments.nextToken();
                if (segment.startsWith("{")) { //$NON-NLS-1$
                    if (segment.length() > 2 && segment.endsWith("}")) { //$NON-NLS-1$
                        String pathPart = segment.substring(1, segment.length() - 1);
                        IStatus javaStatus = JavaConventions.validateIdentifier(pathPart,
                                CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
                        if (javaStatus.getSeverity() == IStatus.ERROR) {
                            String msg = javaStatus.getMessage();
                            return WTPCommonPlugin.createErrorStatus(msg);
                        } else if (javaStatus.getSeverity() == IStatus.WARNING) {
                            String msg = javaStatus.getMessage();
                            return WTPCommonPlugin.createWarningStatus(msg);
                        }
                        if (pathParts.contains(pathPart)) {
                            return WTPCommonPlugin.createErrorStatus(Messages.errorPathInvalid);
                        } else {
                            pathParts.add(pathPart);
                        }
                    } else {
                        return WTPCommonPlugin.createErrorStatus(Messages.errorPathInvalid);
                    }
                } else {
                    if (segment.contains("{") || segment.contains("}")) { //$NON-NLS-1$ //$NON-NLS-2$
                        return WTPCommonPlugin.createErrorStatus(Messages.errorPathInvalid);
                    }
                }
            }
        }
        return WTPCommonPlugin.OK_STATUS;
    }
}
