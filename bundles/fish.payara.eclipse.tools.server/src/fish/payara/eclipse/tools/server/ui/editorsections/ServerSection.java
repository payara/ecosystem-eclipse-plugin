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

package fish.payara.eclipse.tools.server.ui.editorsections;

import static fish.payara.eclipse.tools.server.Messages.wizardSectionTitle;
import static org.eclipse.swt.SWT.DEFAULT;
import static org.eclipse.swt.SWT.FILL;
import static org.eclipse.swt.SWT.UNDERLINE_LINK;
import static org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED;
import static org.eclipse.ui.forms.widgets.ExpandableComposite.FOCUS_TITLE;
import static org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR;
import static org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE;
import static org.eclipse.ui.forms.widgets.Section.DESCRIPTION;
import static org.eclipse.ui.internal.dialogs.PropertyDialog.createDialogOn;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import fish.payara.eclipse.tools.server.Messages;
import fish.payara.eclipse.tools.server.ui.properties.ServerPropertyPage;

/**
 * This class contributes content to the "Server Editor", which is the editor that opens when you
 * double click the Payara / GlassFish server in the Servers views.
 *
 * <p>
 * This is the editor that shows the "Runtime Environment", "Open Launch Configuration" links and
 * has the "Publishing" and "Timeouts' preferences. This class adds a link to the properties page
 * (see {@link ServerPropertyPage}) that that editor.
 * </p>
 *
 * @author ludo
 */
@SuppressWarnings("restriction")
public class ServerSection extends ServerEditorSection implements PropertyChangeListener {

    @Override
    public void dispose() {
        server.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void createSection(Composite parent) {
        super.createSection(parent);

        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        Section section = toolkit.createSection(parent, TITLE_BAR | DESCRIPTION | TWISTIE | EXPANDED | FOCUS_TITLE);
        section.setText(wizardSectionTitle);
        section.setDescription(Messages.wizardSectionDescription);
        section.setLayoutData(new GridData(FILL, FILL, false, false));

        Composite composite = toolkit.createComposite(section);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.verticalSpacing = 5;
        gridLayout.marginWidth = 10;
        gridLayout.marginHeight = 5;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(FILL, FILL, false, false));

        section.setClient(composite);

        Hyperlink link = toolkit.createHyperlink(composite, "Open server properties page...", UNDERLINE_LINK);
        link.addHyperlinkListener(new IHyperlinkListener() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                createDialogOn(
                        Display.getDefault().getActiveShell(),
                        "org.eclipse.wst.server.ui.properties", //$NON-NLS-1$
                        server)
                                .open();
            }

            @Override
            public void linkExited(HyperlinkEvent e) {
            }

            @Override
            public void linkEntered(HyperlinkEvent e) {
            }
        });

        GridDataFactory.fillDefaults()
                .grab(true, false)
                .span(3, 1)
                .hint(50, DEFAULT)
                .applyTo(link);

    }

    // note that this is currently not working due to issue 140
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

}
