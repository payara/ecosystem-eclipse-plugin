/******************************************************************************
 * Copyright (c) 2018-2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.properties;

import static java.lang.Math.max;
import static org.eclipse.jface.dialogs.IDialogConstants.BUTTON_WIDTH;
import static org.eclipse.payara.tools.ui.properties.ClasspathContainerPage.newComposite;
import static org.eclipse.payara.tools.utils.PayaraLocationUtils.ALL_LIBRARIES;
import static org.eclipse.payara.tools.utils.PayaraLocationUtils.DEFAULT_LIBRARIES;
import static org.eclipse.swt.SWT.DEFAULT;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.RADIO;
import static org.eclipse.swt.layout.GridData.BEGINNING;
import static org.eclipse.swt.layout.GridData.FILL;
import static org.eclipse.swt.layout.GridData.FILL_HORIZONTAL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * This class implements the visual radio buttons on the properties page for {@link ClasspathContainerPage}.
 * 
 * @author Arjan Tijms
 */
public class SystemLibrariesVariantBlock {

    private Composite control;
    private final ListenerList<IPropertyChangeListener> changeListeners = new ListenerList<>();

    private IClasspathEntry selection;
    private String title;
    
    private Button defaultButton;
    private Button allButton;

    private IStatus status = OK_STATUS;
    private static IStatus OK_STATUS = new Status(IStatus.OK, "SystemLibrariesVariantBlock", 0, "", null);

    public SystemLibrariesVariantBlock(IClasspathEntry selection) {
        this.selection = selection;
    }
    
    /**
     * Creates this block's control in the given control.
     *
     * @param anscestor containing control
     */
    public void createControl(Composite ancestor) {
        control = newComposite(ancestor);
        
        Composite composite = newComposite(newGroup(control, title), 3);
        createDefaultButton(composite, 3);
        createAllButton(composite, 3);
        
        IPath containerPath = selection.getPath();
        String libraryGroup = DEFAULT_LIBRARIES; 
        if (containerPath.segmentCount() > 1) {
            libraryGroup = containerPath.segment(1);
        }
        
        if (DEFAULT_LIBRARIES.equals(libraryGroup)) {
            defaultButton.setSelection(true);
        } else {
            allButton.setSelection(true);
        }
    }

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        changeListeners.remove(listener);
    }

    public Control getControl() {
        return control;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String selection() {
        if (defaultButton.getSelection()) {
            return DEFAULT_LIBRARIES;
        }

        return ALL_LIBRARIES;
    }

    public IStatus getStatus() {
        return status;
    }

    private void setStatus(IStatus status) {
        this.status = status;
    }
    
    private void createDefaultButton(Composite composite, int horizontalSpan) {
        defaultButton = createRadioButton(composite, "Libraries for current version", horizontalSpan);
        defaultButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (defaultButton.getSelection()) {
                    setStatus(OK_STATUS);
                    firePropertyChange();
                }
            }
        });
    }

    private void createAllButton(Composite composite, int horizontalSpan) {
        allButton = createRadioButton(composite, "All libraries in entire server", horizontalSpan);
        allButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (allButton.getSelection()) {
                    setStatus(OK_STATUS);
                    firePropertyChange();
                }
            }
        });
    }

    private void firePropertyChange() {
        PropertyChangeEvent event = new PropertyChangeEvent(this, "Payara library selection", null, selection());
        for (IPropertyChangeListener listener : changeListeners) {
            listener.propertyChange(event);
        }
    }
    
    private static Group newGroup(Composite parent, String text) {
        Group group = new Group(parent, NONE);
        group.setLayout(new GridLayout(1, false));
        group.setText(text);
        group.setFont(parent.getFont());
        
        GridData gridData = new GridData(FILL_HORIZONTAL);
        gridData.horizontalSpan = 1;
        group.setLayoutData(gridData);
        
        return group;
    }
    
    private static Button createRadioButton(Composite parent, String label, int horizontalSpan) {
        Button button = new Button(parent, RADIO);
        button.setFont(parent.getFont());
        button.setText(label);
        
        GridData gridData = new GridData(BEGINNING);
        gridData.horizontalSpan = 3;
        gridData.horizontalAlignment = FILL;
        gridData.widthHint= computeWidth(button);
        button.setLayoutData(gridData);
        
        return button;
    }
    
    private static int computeWidth(Button button) {
        return max(
            new PixelConverter(button).convertHorizontalDLUsToPixels(BUTTON_WIDTH), 
            button.computeSize(DEFAULT, DEFAULT, true).x);
    }
}
