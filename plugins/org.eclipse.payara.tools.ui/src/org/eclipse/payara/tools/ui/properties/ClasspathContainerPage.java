/******************************************************************************
 * Copyright (c) 2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.properties;

import static org.eclipse.swt.layout.GridData.FILL_BOTH;
import static org.eclipse.swt.layout.GridData.FILL_HORIZONTAL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class implements the properties that are shown when Payara is set as a target runtime for a project
 * and the corresponding library container is right clicked and then <code>properties</code> are selected.
 * 
 * <p>
 * For instance in an Eclipse project that would typically be
 * 
 * <p>
 * <verbatim>
 * <code>
 * Project explorer - [project name] - Java Resources - Libraries - Payara System Libraries - (right click) - Properties
 * </code>
 * </verbatim>
 * 
 * <p>
 * The properties this dialog implements are the choice between the default libraries for a Payara version that are targeted
 * at application developers, and all the available libraries in Payara. 
 * 
 * @author Arjan Tijms
 *
 */
public class ClasspathContainerPage extends WizardPage implements IClasspathContainerPage {
    private final static String PAGE_NAME = ClasspathContainerPage.class.getName();

    private IClasspathEntry selection;

    public ClasspathContainerPage() {
        super(PAGE_NAME);
    }

    @Override
    public void createControl(Composite parent) {
        setTitle("Payara System Library");
        setDescription("Select system library variant for the project build path.");
        setMessage("Select system library variant for the project build path.");
        
        Composite composite = newComposite(parent);
        
        SystemLibrariesVariantBlock libraryChoice = newLibraryChoiceBlock(composite, "System library variant");
        
        libraryChoice.addPropertyChangeListener(event -> {
            IStatus status = libraryChoice.getStatus();
            if (status.isOK()) {
                setErrorMessage(null);
                
                IPath containerPath = 
                    new Path(
                        selection.getPath()
                                 .segments()[0])
                                 .append((String)event.getNewValue());
                
                selection = JavaCore.newContainerEntry(
                    containerPath, 
                    selection.getAccessRules(), 
                    selection.getExtraAttributes(), 
                    selection.isExported());
            } else {
                setErrorMessage(status.getMessage());
            }
        });
        
        setControl(composite);
    }
    
    SystemLibrariesVariantBlock newLibraryChoiceBlock(Composite parent, String title) {
        SystemLibrariesVariantBlock libraryChoice = new SystemLibrariesVariantBlock(selection);
        libraryChoice.setTitle(title);
        libraryChoice.createControl(parent);
        libraryChoice.getControl().setLayoutData(new GridData(FILL_HORIZONTAL));
        
        return libraryChoice;
    }
    
    public static Composite newComposite(Composite parent) {
        return newComposite(parent, 1);
    }
    
    public static Composite newComposite(Composite parent, int columns) {
        Composite composite = new Composite(parent, NONE);
        composite.setLayout(new GridLayout(columns, false));
        composite.setFont(parent.getFont());
        
        GridData gridData = new GridData(FILL_BOTH);
        gridData.horizontalSpan = 1;
        composite.setLayoutData(gridData);
        
        return composite;
    }

    @Override
    public boolean finish() {
        return true;
    }

    @Override
    public IClasspathEntry getSelection() {
        return selection;
    }

    @Override
    public void setSelection(IClasspathEntry containerEntry) {
        this.selection = containerEntry;
    }

}
