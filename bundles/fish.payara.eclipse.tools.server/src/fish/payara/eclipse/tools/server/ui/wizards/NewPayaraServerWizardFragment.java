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

package fish.payara.eclipse.tools.server.ui.wizards;

import static fish.payara.eclipse.tools.server.PayaraServer.getDefaultDomainDir;
import static fish.payara.eclipse.tools.server.PayaraServerPlugin.log;
import static fish.payara.eclipse.tools.server.utils.NamingUtils.createUniqueRuntimeName;
import static org.eclipse.wst.server.core.TaskModel.TASK_RUNTIME;
import static org.eclipse.wst.server.core.TaskModel.TASK_SERVER;
import static org.eclipse.wst.server.core.internal.Server.AUTO_PUBLISH_RESOURCE;
import static org.eclipse.wst.server.core.internal.Server.PROP_AUTO_PUBLISH_SETTING;
import static org.eclipse.wst.server.core.internal.Server.PROP_AUTO_PUBLISH_TIME;

//import org.eclipse.core.internal.content.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

import fish.payara.eclipse.tools.server.PayaraRuntime;
import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.PayaraServerPlugin;
import fish.payara.eclipse.tools.server.exceptions.UniqueNameNotFound;
import fish.payara.eclipse.tools.server.utils.WtpUtil;
import org.eclipse.wst.server.core.internal.Runtime;

/**
 * This wizard fragment plugs-in the wizard flow when
 * <code>Servers -> New Server -> Payara -> Payara</code> is selected and
 * subsequently the <code>next</code> button is pressed.
 *
 * <p>
 * This fragment essentially causes the screen with <code>Name</code>,
 * <code>Host name</code>, <code>Domain path</code> etc to be rendered.
 *
 */
@SuppressWarnings("restriction")
public class NewPayaraServerWizardFragment extends WizardFragment {

	protected Text serverName;

	protected Text serverHost;

	protected Text domainLocation;

	protected Text adminName;

	protected Text adminPassword;

	protected Text restartPattern;

	protected Spinner debugPort;

	protected Button keepSessions;

	protected Button jarDeploy;

	protected Button hotDeploy;

	protected Button attachDebuggerEarly;

	public static int MAXIMUM_PORT = 999999;

	public static final String DEFAULT_DOMAINS_DIR = "domains";

	public static final String DEFAULT_DOMAIN_NAME = "domain1";

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.wst.server.ui.wizard.WizardFragment#hasComposite()
	 */
	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout(2, false);
		grid.marginWidth = 0;
		container.setLayout(grid);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		handle.setImageDescriptor(PayaraServerPlugin.getImageDescriptor(PayaraServerPlugin.GF_WIZARD));
		handle.setTitle(getTitle());
		handle.setDescription(getDescription());

		PayaraServer payaraServer = getServer().getAdapter(PayaraServer.class);
		payaraServer.setDomainPath(getDefaultDomainDir(getServer().getRuntime().getLocation()).toOSString());

		createContent(container, handle);

		try {
			getServer().setAttribute(PROP_AUTO_PUBLISH_SETTING, AUTO_PUBLISH_RESOURCE);
			getServer().setAttribute(PROP_AUTO_PUBLISH_TIME, 1);
		} catch (Exception e) {
			log(e);
		}

		return container;
	}

	public void createContent(Composite parent, IWizardHandle handle) {
		PayaraServer payaraServer = getServer().getAdapter(PayaraServer.class);

		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite group = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.serverName);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		serverName = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		serverName.setLayoutData(data);
		serverName.setText(getServer().getName());
		serverName.addModifyListener(e -> {
			getServer().setName(serverName.getText());
			validate(handle);
		});
		updateServerName();

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.serverHost);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		serverHost = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		serverHost.setLayoutData(data);
		serverHost.setText(getServer().getHost());
		serverHost.addModifyListener(e -> {
			getServer().setHost(serverHost.getText());
			validate(handle);
		});

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.domainPath);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		domainLocation = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		domainLocation.setLayoutData(data);
		domainLocation.setText(payaraServer.getDomainPath());
		domainLocation.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				payaraServer.setDomainPath(domainLocation.getText());
				updateServerName();
				validate(handle);
			}
		});

		Button browse = new Button(group, SWT.PUSH);
		browse.setText(GlassfishWizardResources.browse);
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setMessage(GlassfishWizardResources.selectInstallDir);
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null && !selectedDirectory.isEmpty())
					domainLocation.setText(selectedDirectory);
				updateServerName();
				validate(handle);
			}
		});

		Button createDomain = new Button(group, SWT.PUSH);
		createDomain.setText(GlassfishWizardResources.newDomainCreateButton);
		createDomain.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				PayaraRuntime runtime = (PayaraRuntime) getServerRuntime().loadAdapter(PayaraRuntime.class, null);
				CreatePayaraDomain domain = new CreatePayaraDomain(parent.getShell(), payaraServer, runtime);
				domain.open();
				String selectedDirectory = domain.getPath();
				if (selectedDirectory != null && !selectedDirectory.isEmpty()) {
					domainLocation.setText(selectedDirectory);
					validate(handle);
				}
			}
		});

		label = new Label(group, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		data.verticalIndent = 10;
		label.setLayoutData(data);

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.adminName);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		adminName = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		adminName.setLayoutData(data);
		adminName.setText(payaraServer.getAdminUser());
		adminName.addModifyListener(e -> {
			payaraServer.setAdminUser(adminName.getText());
			validate(handle);
		});

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.adminPassword);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		adminPassword = new Text(group, SWT.BORDER | SWT.PASSWORD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		adminPassword.setLayoutData(data);
		adminPassword.setText(payaraServer.getAdminPassword());
		adminPassword.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
//				IServerWorkingCopy copy = getServer();
//				PayaraServer ps = getServer().getAdapter(PayaraServer.class);
//				IRuntimeWorkingCopy c2 = getServerRuntime();
				payaraServer.setAdminPassword(adminPassword.getText());
			}
		});

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.debugPort);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		debugPort = new Spinner(group, SWT.BORDER);
		debugPort.setMinimum(0);
		debugPort.setMaximum(MAXIMUM_PORT);
		debugPort.setTextLimit((Integer.toString(MAXIMUM_PORT)).length());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		debugPort.setLayoutData(data);
		debugPort.setSelection(payaraServer.getDebugPort());
		debugPort.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				payaraServer.setDebugPort(debugPort.getSelection());
			}
		});

		label = new Label(group, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		data.verticalIndent = 10;
		label.setLayoutData(data);

		keepSessions = new Button(group, SWT.CHECK);
		keepSessions.setText(GlassfishWizardResources.keepSessions);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		keepSessions.setLayoutData(data);
		keepSessions.setSelection(payaraServer.getKeepSessions());
		keepSessions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				payaraServer.setKeepSessions(keepSessions.getSelection());
			}
		});

		jarDeploy = new Button(group, SWT.CHECK);
		jarDeploy.setText(GlassfishWizardResources.jarDeploy);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		jarDeploy.setLayoutData(data);
		jarDeploy.setSelection(payaraServer.getJarDeploy());
		jarDeploy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				payaraServer.setJarDeploy(jarDeploy.getSelection());
			}
		});

		label = new Label(group, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		data.verticalIndent = 10;
		label.setLayoutData(data);

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.restartPattern);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		restartPattern = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		restartPattern.setLayoutData(data);
		restartPattern.setText(payaraServer.getRestartPattern());
		restartPattern.addModifyListener(e -> payaraServer.setRestartPattern(restartPattern.getText()));

		hotDeploy = new Button(group, SWT.CHECK);
		hotDeploy.setText(GlassfishWizardResources.enableHotDeploy);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		hotDeploy.setLayoutData(data);
		hotDeploy.setSelection(payaraServer.getHotDeploy());
		hotDeploy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				payaraServer.setHotDeploy(hotDeploy.getSelection());
			}
		});

		attachDebuggerEarly = new Button(group, SWT.CHECK);
		attachDebuggerEarly.setText(GlassfishWizardResources.attachDebugEarly);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		attachDebuggerEarly.setLayoutData(data);
		attachDebuggerEarly.setSelection(payaraServer.getAttachDebuggerEarly());
		attachDebuggerEarly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				payaraServer.setAttachDebuggerEarly(attachDebuggerEarly.getSelection());
			}
		});
	}

	protected boolean showPreferencePage(Composite parent) {
		String id = "org.eclipse.jdt.debug.ui.preferences.VMPreferencePage";

		// should be using the following API, but it only allows a single preference
		// page instance.
		// see bug 168211 for details
		// PreferenceDialog dialog =
		// PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id },
		// null);
		// return (dialog.open() == Window.OK);

		PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();
		IPreferenceNode node = manager.find("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage").findSubNode(id);
		PreferenceManager manager2 = new PreferenceManager();
		manager2.addToRoot(node);
		PreferenceDialog dialog = new PreferenceDialog(parent.getShell(), manager2);
		dialog.create();
		return (dialog.open() == Window.OK);
	}

	@SuppressWarnings("unused")
	private String getServerName() {
		if (getServer() != null && getServer().getRuntime() != null)
			return getServer().getRuntime().getRuntimeType().getName();
		return null;
	}

	private IServerWorkingCopy getServer() {
		return (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
	}

	private IRuntime getServerRuntime() {
		return (IRuntime) getTaskModel().getObject(TASK_RUNTIME);
	}

	private void updateServerName() {
		PayaraServer payaraServer = getPayaraServer();
		String name = getServer().getRuntime().getName() + " [";

		if (payaraServer.isRemote()) {
			name = name + payaraServer.getHost();
		} else {
			name = name + payaraServer.getDomainName();
		}

		name = name + "]";

		getServer().setName(WtpUtil.findUniqueServerName(name));
		serverName.setText(getServer().getName());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.wst.server.ui.wizard.WizardFragment#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return validate(null);
	}

	@SuppressWarnings("unused")
	private PayaraServer getPayaraServer() {
		PayaraServer payaraServer = getServer().getAdapter(PayaraServer.class);
		if (payaraServer == null)
			payaraServer = (PayaraServer) getServer().loadAdapter(PayaraServer.class, null);
		return payaraServer;
	}

	protected String getTitle() {
		return getServer().getServerType().getName();
	}

	protected String getDescription() {
		return GlassfishWizardResources.wzdServerDescription;
	}

	@Override
	public void setTaskModel(TaskModel taskModel) {
		super.setTaskModel(taskModel);
		if (getTaskModel().getObject(TASK_RUNTIME) instanceof RuntimeWorkingCopy) {
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
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		super.performFinish(monitor);

		if (getTaskModel().getObject(TASK_RUNTIME) instanceof RuntimeWorkingCopy) {
			RuntimeWorkingCopy runtime = (RuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
			runtime.save(true, monitor);
			runtime.dispose();
		}
	}

	@Override
	public void performCancel(final IProgressMonitor monitor) throws CoreException {
		super.performCancel(monitor);
		if (getTaskModel().getObject(TASK_RUNTIME) instanceof RuntimeWorkingCopy) {
			RuntimeWorkingCopy runtime = (RuntimeWorkingCopy) getTaskModel().getObject(TASK_RUNTIME);
			runtime.dispose();
		}
	}

	protected boolean validate(IWizardHandle wizard) {
		boolean valid = true;
		PayaraServer payaraServer = getServer().getAdapter(PayaraServer.class);
		if (wizard != null) {
			wizard.setMessage(null, IMessageProvider.NONE);
		}
		if (payaraServer.getName() == null || payaraServer.getName().isBlank()) {
			if (wizard != null) {
				wizard.setMessage("Server name is not valid", IMessageProvider.ERROR);
			}
		}
		if (payaraServer.getHost() == null || payaraServer.getHost().isBlank()) {
			if (wizard != null) {
				wizard.setMessage("Server Host is not valid", IMessageProvider.ERROR);
			}
		}
		if (payaraServer.getAdminUser() == null || payaraServer.getAdminUser().isBlank()) {
			if (wizard != null) {
				wizard.setMessage("Admin user name is not valid", IMessageProvider.ERROR);
			}
		}
		IStatus status = payaraServer.validate();
		if (status.getSeverity() > 0) {
			valid = false;
			if (wizard != null) {
				wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
			}
		}
		if (wizard != null) {
			wizard.update();
		}
		return valid;
	}

}
