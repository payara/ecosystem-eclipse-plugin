/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.wizards;

import static org.eclipse.payara.tools.sapphire.IGlassfishRuntimeModel.PROP_NAME;
import static org.eclipse.payara.tools.ui.wizards.GlassfishWizardResources.wzdRuntimeDescription;
import static org.eclipse.payara.tools.utils.NamingUtils.createUniqueRuntimeName;
import static org.eclipse.wst.server.core.TaskModel.TASK_RUNTIME;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.payara.tools.exceptions.UniqueNameNotFound;
import org.eclipse.payara.tools.sapphire.IGlassfishRuntimeModel;
import org.eclipse.payara.tools.server.GlassFishRuntime;
import org.eclipse.sapphire.Element;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;

/**
 * This wizard fragment plugs-in the wizard flow when
 * <code>Servers -> New Server -> Payara -> Payara</code> is selected and subsequently the
 * <code>next</code> button is pressed when no runtime exists yet, or the <code>add</code> button
 * next to <code>Server runtime environment</code> is pressed.
 *
 * <p>
 * This fragment essentially causes the screen with <code>Name</code>, <code>Payara location</code>,
 * <code>Java Location</code> etc to be rendered, although a lot of the actual work is delegated by
 * the {@link BaseWizardFragment} to Sapphire. The UI layout for this wizard fragment is specified
 * in the file <code>GlassfishUI.sdef</code> in the "glassfish.runtime" section.
 *
 */
@SuppressWarnings("restriction")
public class NewPayaraRuntimeWizardFragment extends BaseWizardFragment {

    @Override
    protected String getTitle() {
        return ((IRuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME))
                .getRuntimeType()
                .getName();
    }

    @Override
    protected String getDescription() {
        return wzdRuntimeDescription;
    }

    /**
     * The section in <code>GlassfishUI.sdef</code> that contains the UI layout for this wizard
     * fragment.
     */
    @Override
    protected String getUserInterfaceDef() {
        return "glassfish.runtime";
    }

    @Override
    protected Element getModel() {
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
        final GlassFishRuntime runtimeDelegate = (GlassFishRuntime) runtime.loadAdapter(GlassFishRuntime.class, null);
        IGlassfishRuntimeModel model = runtimeDelegate.getModel();

        return model;
    }

    @Override
    public void setTaskModel(final TaskModel taskModel) {
        super.setTaskModel(taskModel);

        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
        if (runtime.getOriginal() == null) {
            try {
                runtime.setName(createUniqueRuntimeName(runtime.getRuntimeType().getName()));
            } catch (UniqueNameNotFound e) {
                // Set the type name and let the user handle validation error
                runtime.setName(runtime.getRuntimeType().getName());
            }
        }
    }

    @Override
    public void performFinish(IProgressMonitor monitor) throws CoreException {
        super.performFinish(monitor);

        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
        runtime.save(true, monitor);
        ((RuntimeWorkingCopy) runtime).dispose();
    }

    @Override
    public void performCancel(final IProgressMonitor monitor) throws CoreException {
        super.performCancel(monitor);

        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
        ((RuntimeWorkingCopy) runtime).dispose();
    }

    @Override
    protected String getInitialFocus() {
        return PROP_NAME.name();
    }

}
