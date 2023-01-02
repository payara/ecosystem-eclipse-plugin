package fish.payara.eclipse.tools.server.ui.wizards;

import static fish.payara.eclipse.tools.server.PayaraServerPlugin.SYMBOLIC_NAME;
import static org.eclipse.core.runtime.IStatus.INFO;
import static org.eclipse.debug.core.ILaunchManager.RUN_MODE;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import fish.payara.eclipse.tools.server.PayaraRuntime;
import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.PayaraServerPlugin;

/**
 * Invoked when the user clicks on the "new domain" icon next to the "Domain Path"
 * input field in the "new server" wizard.
 *
 */

public class CreatePayaraDomain extends MessageDialog {

	private Text domainName;
	private Text domainDir;
	private Spinner portBase;
	private Label message;
	private ProgressBar pb;
	private PayaraServer payaraServer;
	private PayaraRuntime runtime;
	private String path;
	public static int MAXIMUM_PORT = 999999;
	public static int DEFAULT_PORT = 8000;
	public static String DEFAULT_DOMAIN = "domain1";

	public CreatePayaraDomain(Shell parentShell, PayaraServer payaraServer, PayaraRuntime runtime) {
		super(parentShell, GlassfishWizardResources.newDomainTitle,
				PayaraServerPlugin.getImage(PayaraServerPlugin.GF_SERVER_IMG),
				GlassfishWizardResources.newDomainDescription, CONFIRM,
				new String[] { GlassfishWizardResources.newDomainCreateButton, IDialogConstants.CANCEL_LABEL }, 0);
		this.payaraServer = payaraServer;
		this.runtime = runtime;
	}

	public String getPath() {
		return path;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		setReturnCode(buttonId);
		if (buttonId != 0 || execute()) {
			close();
		}
	}
	
	protected boolean domainNameValidation() {
		String name = domainName.getText();
		if (name != null && name.trim().length() > 0) {

			if (name.indexOf(' ') > 0) {
				setMessage("Invalid value for domain name."); //$NON-NLS-1$
				return false;
			}
			File domainsDir = new File(domainDir.getText(), name);
			if (domainsDir.exists()) {
				setMessage("A domain already exists at the specified location."); //$NON-NLS-1$
				return false;
			}
			return true;
		}
		return false;

	}

//    @Override
//    protected Object run(Presentation context) {
//        IRuntime runtime = load(context.part().getModelElement().adapt(IServerWorkingCopy.class), PayaraServer.class)
//                .getServer()
//                .getRuntime();
//
//        ICreatePayaraDomainOp createDomainOperation = ICreatePayaraDomainOp.TYPE.instantiate();
//
//        // Set existing domain location
//        createDomainOperation.setLocation(fromPortableString(runtime.getLocation().toPortableString()));
//
//        // Set existing JDK location
//        createDomainOperation.setJavaLocation(
//                load(runtime, PayaraRuntime.class).getVMInstall().getInstallLocation().getAbsolutePath());
//
//        // Explicitly open Sapphire dialog that asks the user to fill out fields for new domain
//        WizardDialog dlg = new WizardDialog(
//                Display.getDefault().getActiveShell(),
//                new SapphireWizard<>(
//                        createDomainOperation,
//                        context(BaseWizardFragment.class)
//                                .sdef("fish.payara.eclipse.tools.server.ui.PayaraUI")
//                                .wizard("new-domain-wizard")));
//
//        // If user okay'ed dialog, copy the provided values to our model
//        if (dlg.open() == OK) {
//            IPayaraServerModel model = (IPayaraServerModel) context.part().getModelElement();
//
//            model.setDomainPath(createDomainOperation.getDomainDir().content().append(createDomainOperation.getName().content()));
//            model.setDebugPort(createDomainOperation.getPortBase().content() + 9);
//        }
//
//        createDomainOperation.dispose();
//
//        return null;
//    }
//            @Override

	public boolean execute() {
		if(!domainNameValidation()) {
			return false;
		}
		File asadmin = new File(new File(payaraServer.getServerHome(), "bin"),
				Platform.getOS().equals(Platform.OS_WIN32) ? "asadmin.bat" : "asadmin");
		if (asadmin.exists()) {
			String javaExecutablePath = asadmin.getAbsolutePath();
			String[] cmdLine = new String[] { javaExecutablePath, "create-domain", "--nopassword=true", "--portbase",
					String.valueOf(portBase.getSelection()), "--domaindir", domainDir.getText(), domainName.getText() };

			Process p = null;

			try {
				final StringBuilder output = new StringBuilder();
				final StringBuilder errOutput = new StringBuilder();
				output.append(Arrays.toString(cmdLine) + "\n");

				// Set AS_JAVA location which will be used to run asadmin
				String envp[] = new String[1];
				envp[0] = "AS_JAVA=" + runtime.getVMInstall().getInstallLocation().getPath();

				p = DebugPlugin.exec(cmdLine, null, envp);
				IProcess process = DebugPlugin.newProcess(new Launch(null, RUN_MODE, null), p, "GlassFish asadmin"); //$NON-NLS-1$

				// Log output
				process.getStreamsProxy().getOutputStreamMonitor().addListener((text, monitor) -> output.append(text));

				process.getStreamsProxy().getErrorStreamMonitor()
						.addListener((text, monitor) -> errOutput.append(text));
				setMessage("");
				for (int i = 0; i < 600; i++) {
					// Wait no more than 30 seconds (600 * 50 milliseconds)
					if (process.isTerminated()) {
						PayaraServerPlugin.getInstance().getLog().log(new org.eclipse.core.runtime.Status(INFO,
								SYMBOLIC_NAME, 1, output.toString() + "\n" + errOutput.toString(), null));
						break;
					}
					try {
						Thread.sleep(50);
						pb.setSelection(i);
					} catch (InterruptedException e) {
					}
				}

				File f = new File(domainDir.getText(), domainName.getText());
				if (!f.exists()) {
					setMessage("Error in creating the Payara Server domain");
					return false;
				}
			} catch (CoreException ioe) {
				LaunchingPlugin.log(ioe);
				setMessage(ioe.getMessage());
				return false;
			} finally {
				if (p != null) {
					p.destroy();
				}
			}
		}
		path = domainDir.getText() + File.separator + domainName.getText();
		return true;
	}

	private void setMessage(String text) {
		message.setText(text);
		message.setVisible(!text.isEmpty());
		pb.setVisible(text.isEmpty());
	}

	@Override
	protected Control createCustomArea(Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout(2, false);
		grid.marginWidth = 0;
		container.setLayout(grid);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout(1, true);
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite group = new Composite(container, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.domainName);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		domainName = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		domainName.setLayoutData(data);
		domainName.setText(DEFAULT_DOMAIN);

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.domainDir);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		domainDir = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		domainDir.setLayoutData(data);
		domainDir.setText(payaraServer.getDomainsFolder());

		Button browse = new Button(group, SWT.PUSH);
		browse.setText(GlassfishWizardResources.browse);
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setMessage(GlassfishWizardResources.selectInstallDir);
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null && !selectedDirectory.isEmpty())
					domainDir.setText(selectedDirectory);
			}
		});

		label = new Label(group, SWT.NONE);
		label.setText(GlassfishWizardResources.portBase);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		portBase = new Spinner(group, SWT.BORDER);
		portBase.setMinimum(0);
		portBase.setMaximum(MAXIMUM_PORT);
		portBase.setTextLimit((Integer.toString(MAXIMUM_PORT)).length());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		portBase.setLayoutData(data);
		portBase.setSelection(DEFAULT_PORT);

		message = new Label(group, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		data.horizontalSpan = 2;
		message.setLayoutData(data);
		message.setForeground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		pb = new ProgressBar(group, SWT.HORIZONTAL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		pb.setLayoutData(data);
		pb.setMinimum(0);
		pb.setMaximum(100);
		pb.setVisible(false);
		return container;
	}

}
