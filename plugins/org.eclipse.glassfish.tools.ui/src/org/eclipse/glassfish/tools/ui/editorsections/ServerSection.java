/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.editorsections;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import org.eclipse.glassfish.tools.Messages;

/**
 * 
 * @author ludo
 */
@SuppressWarnings("restriction")
public class ServerSection extends ServerEditorSection implements
		PropertyChangeListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.server.ui.editor.ServerEditorSection#dispose()
	 */
	@Override
	public void dispose() {
		server.removePropertyChangeListener(this);
		super.dispose();
	}

	public void createSection(Composite parent) {
		super.createSection(parent);

		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent,
				ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
						| ExpandableComposite.TWISTIE
						| ExpandableComposite.EXPANDED
						| ExpandableComposite.FOCUS_TITLE);

		section.setText(Messages.wizardSectionTitle);

		section.setDescription(Messages.wizardSectionDescription);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		Composite comp = toolkit.createComposite(section);
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		gl.verticalSpacing = 5;
		gl.marginWidth = 10;
		gl.marginHeight = 5;
		comp.setLayout(gl);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		section.setClient(comp);
		
		Hyperlink link = toolkit.createHyperlink(comp, "Open server properties page...", SWT.UNDERLINE_LINK);
		link.addHyperlinkListener(new IHyperlinkListener() {
			
			@Override
			public void linkExited(HyperlinkEvent e) {
			}
			
			@Override
			public void linkEntered(HyperlinkEvent e) {
			}
			
			@Override
			public void linkActivated(HyperlinkEvent e) {
				String id = "org.eclipse.wst.server.ui.properties";//$NON-NLS-1$
				PropertyDialog dialog = PropertyDialog.createDialogOn(Display.getDefault().getActiveShell(), id, server);
				dialog.open();
			}
		});
		
		GridDataFactory txtGDF = GridDataFactory.fillDefaults()
				.grab(true, false).span(3, 1).hint(50, SWT.DEFAULT);

		txtGDF.applyTo(link);

	}

	// note that this is currently not working due to issue 140
	public void propertyChange(PropertyChangeEvent evt) {
	}

}
