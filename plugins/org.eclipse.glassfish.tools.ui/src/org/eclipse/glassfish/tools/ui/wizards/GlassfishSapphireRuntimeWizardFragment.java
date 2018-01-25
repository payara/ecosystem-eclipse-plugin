/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.sapphire.Element;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;

import org.eclipse.glassfish.tools.GlassFishRuntime;
import org.eclipse.glassfish.tools.IGlassfishRuntimeModel;
import org.eclipse.glassfish.tools.RuntimeConfigurator;
import org.eclipse.glassfish.tools.exceptions.UniqueNameNotFound;

public class GlassfishSapphireRuntimeWizardFragment extends GlassfishSapphireWizardFragment {

	//private IRuntimeWorkingCopy runtime;

	@Override
	protected String getTitle() {
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject( TaskModel.TASK_RUNTIME );
		return runtime.getRuntimeType().getName();
	}

	@Override
	protected String getDescription() {
		return GlassfishWizardResources.wzdRuntimeDescription;
	}

	@Override
	protected Element getModel() {
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject( TaskModel.TASK_RUNTIME );
		final GlassFishRuntime runtimeDelegate = (GlassFishRuntime) runtime.loadAdapter(GlassFishRuntime.class, null);
		IGlassfishRuntimeModel model = runtimeDelegate.getModel();
		//getTaskModel().putObject(TaskModel.TASK_RUNTIME, runtime);
        return model;
	}
	
	@Override
	public void enter() {
		super.enter();
	}

	@Override
    public void setTaskModel( final TaskModel taskModel )
    {
        super.setTaskModel( taskModel );
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject( TaskModel.TASK_RUNTIME );
        if (runtime.getOriginal() == null) {
			try {
				runtime.setName(RuntimeConfigurator.createUniqueRuntimeName(runtime.getRuntimeType().getName()));
			} catch (UniqueNameNotFound e) {
				// set the type name and let the user handle validation error
				runtime.setName(runtime.getRuntimeType().getName());
			}
		}
    }
	
	@Override
	protected String getUserInterfaceDef() {
		return "glassfish.runtime";
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException
	{
		super.performFinish(monitor);
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject( TaskModel.TASK_RUNTIME );
		runtime.save(true, monitor);
		( (RuntimeWorkingCopy) runtime ).dispose();
	}
	
	@Override
    public void performCancel( final IProgressMonitor monitor ) throws CoreException
    {
        super.performCancel( monitor );
        IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject( TaskModel.TASK_RUNTIME );
        ( (RuntimeWorkingCopy) runtime ).dispose();
    }

    @Override
	protected String getInitialFocus() {
		return IGlassfishRuntimeModel.PROP_NAME.name();
	}
	
	

}
