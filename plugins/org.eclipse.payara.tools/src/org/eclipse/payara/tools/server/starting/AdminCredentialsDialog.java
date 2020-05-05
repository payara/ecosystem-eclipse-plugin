/******************************************************************************
 * Copyright (c) 2020 Payara Foundation All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.server.starting;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;

public class AdminCredentialsDialog extends TitleAreaDialog {

    private Text adminNameText;
    private Text passwordText;

    private static final String SAVE_LABEL = "Save";

    private IServerWorkingCopy serverWorkingCopy;


    private AdminCredentialsDialog(IServerWorkingCopy serverWorkingCopy, Shell parentShell) {
        super(parentShell);
        this.serverWorkingCopy = serverWorkingCopy;
    }


    public static void open(IServer server) {
        Display display = PlatformUI.getWorkbench().getDisplay();
        display.asyncExec(() -> {
            IServerWorkingCopy serverWorkingCopy;
            if (server instanceof IServerWorkingCopy) {
                serverWorkingCopy = (IServerWorkingCopy) server;
            } else {
                serverWorkingCopy = server.createWorkingCopy();
            }
            AdminCredentialsDialog dialog = new AdminCredentialsDialog(serverWorkingCopy, display.getActiveShell());
            dialog.create();
            dialog.open();
        });
    }


    @Override
    public void create() {
        super.create();
        super.getShell().setText("Payara Administrator Credentials");
        setTitle("Wrong user name or password");
        setMessage("Authorization failed while checking " + serverWorkingCopy.getName()
            + " status. Please provide valid administrator credentials.", IMessageProvider.WARNING);
    }


    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, SAVE_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }


    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        createAdminNameComponent(container);
        createPasswordComponent(container);

        return area;
    }


    private void createAdminNameComponent(Composite container) {
        Label adminNameLabel = new Label(container, SWT.NONE);
        adminNameLabel.setText("Username");

        GridData grid = new GridData();
        grid.grabExcessHorizontalSpace = true;
        grid.horizontalAlignment = GridData.FILL;

        adminNameText = new Text(container, SWT.BORDER);
        adminNameText.setText(serverWorkingCopy.getAttribute(PayaraServer.ATTR_ADMIN, "admin"));
        adminNameText.setLayoutData(grid);
    }


    private void createPasswordComponent(Composite container) {
        Label passwordLabel = new Label(container, SWT.NONE);
        passwordLabel.setText("Password");

        GridData grid = new GridData();
        grid.grabExcessHorizontalSpace = true;
        grid.horizontalAlignment = GridData.FILL;
        passwordText = new Text(container, SWT.PASSWORD | SWT.BORDER);
        passwordText.setText(serverWorkingCopy.getAttribute(PayaraServer.ATTR_ADMINPASS, ""));
        passwordText.setLayoutData(grid);
    }


    @Override
    protected void okPressed() {
        try {
            serverWorkingCopy.setAttribute(PayaraServer.ATTR_ADMIN, adminNameText.getText());
            serverWorkingCopy.setAttribute(PayaraServer.ATTR_ADMINPASS, passwordText.getText());
            serverWorkingCopy.save(true, null);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        super.okPressed();
    }

}
