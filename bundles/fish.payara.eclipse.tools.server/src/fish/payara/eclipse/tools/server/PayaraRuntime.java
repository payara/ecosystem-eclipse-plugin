/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2024 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server;

import static fish.payara.eclipse.tools.server.Messages.notValidPayaraInstall;
import static fish.payara.eclipse.tools.server.Messages.pathDoesNotExist;
import static fish.payara.eclipse.tools.server.Messages.runtimeNotValid;
import static fish.payara.eclipse.tools.server.Messages.unsupportedVersion;
import static fish.payara.eclipse.tools.server.PayaraServerPlugin.SYMBOLIC_NAME;
import static fish.payara.eclipse.tools.server.utils.PayaraLocationUtils.find;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.osgi.util.NLS.bind;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jst.server.core.IJavaRuntime;
import org.eclipse.jst.server.core.internal.IGenericRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.RuntimeDelegate;

import fish.payara.eclipse.tools.server.utils.PayaraLocationUtils;
import fish.payara.eclipse.tools.server.utils.Version;
import fish.payara.eclipse.tools.server.utils.VersionConstraint;

/**
 * This class represents the specific type of runtime associated with the server that we implement;
 * Payara / GlassFish.
 *
 * <p>
 * A few methods from RuntimeDelegate are overridden here, while a whole slew of central runtime
 * like functionality is put here as well.
 * </p>
 *
 * <p>
 * This class is registered in <code>plug-in.xml</code> in the
 * <code>org.eclipse.wst.server.core.runtimeTypes</code> extension point.
 * </p>
 *
 */
@SuppressWarnings("restriction")
public final class PayaraRuntime extends RuntimeDelegate implements IJavaRuntime {

    public static final String TYPE_ID = "payara.runtime";
    public static final IRuntimeType TYPE = ServerCore.findRuntimeType(TYPE_ID);
    public static final String ATTR_SERVER_ROOT = "server.root"; //$NON-NLS-1$
    public static final String ATTR_SERVER_JDK = "server.jdk";

    public static final boolean IS_MACOSX = Platform.OS_MACOSX.equals(Platform.getOS());
    public static final String DEFAULT_JRE_KEY = "###DefaultJREForGlassFishCode###";

    private static final VersionConstraint VERSION_CONSTRAINT_3_1 = new VersionConstraint("[1.6-1.7]");
    private static final VersionConstraint VERSION_CONSTRAINT_4 = new VersionConstraint("[1.7");
    private static final VersionConstraint VERSION_CONSTRAINT_5 = new VersionConstraint("[1.8");
    private static final VersionConstraint VERSION_CONSTRAINT_6 = new VersionConstraint("[1.8");
    private static final VersionConstraint VERSION_CONSTRAINT_7 = new VersionConstraint("[1.8");
    protected static final String PROP_VM_INSTALL_TYPE_ID = "vm-install-type-id";
    protected static final String PROP_VM_INSTALL_ID = "vm-install-id";

    // #### RuntimeDelegate overridden methods

    @Override
    public IStatus validate() {
        IStatus status = super.validate();

//        if (status.isOK()) {
//            status = StatusBridge.create(getModel().validation());
//        }

        return status;
    }

    // #### IJavaRuntime implementation methods

    @Override
//    public IVMInstall getVMInstall() {
//        return findOrCreateJvm(getModel().getJavaRuntimeEnvironment().text());
//    }

    public IVMInstall getVMInstall() {
		if (getVMInstallTypeId() == null)
			return JavaRuntime.getDefaultVMInstall();
		try {
			IVMInstallType vmInstallType = JavaRuntime.getVMInstallType(getVMInstallTypeId());
			IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();
			String id = getVMInstallId();
			for (IVMInstall vmInst : vmInstalls) {
				if (id.equals(vmInst.getId()))
					return vmInst;
			}
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

    private String getVMInstallTypeId() {
    	return this.getAttribute(PROP_VM_INSTALL_TYPE_ID, (String) null);
    }

    private String getVMInstallId() {
    	return this.getAttribute(PROP_VM_INSTALL_ID, (String) null);
    }

    /**
	 * @see IGenericRuntimeWorkingCopy#setVMInstall(IVMInstall)
	 */
	public void setVMInstall(IVMInstall vmInstall) {
		if (vmInstall == null) {
			setVMInstall(null, null);
		} else
			setVMInstall(vmInstall.getVMInstallType().getId(), vmInstall.getId());
	}

	protected void setVMInstall(String typeId, String id) {
		if (typeId == null)
			setAttribute(PROP_VM_INSTALL_TYPE_ID, (String)null);
		else
			setAttribute(PROP_VM_INSTALL_TYPE_ID, typeId);

		if (id == null)
			setAttribute(PROP_VM_INSTALL_ID, (String)null);
		else
			setAttribute(PROP_VM_INSTALL_ID, id);
	}

    @Override
    public boolean isUsingDefaultJRE() {
        return false;
    }


    // #### Static methods

    public static String createDefaultRuntimeName(Version version) {
        String baseName = "Payara Server"; // TODO: - detect GF

        if (version != null) {
            if (version.matches("[7-8)")) {
                baseName += " 7";
            } else if (version.matches("[6-7)")) {
                baseName += " 6";
            } else if (version.matches("[5-6)")) {
                baseName += " 5";
            } else if (version.matches("[4-5)")) {
                baseName += " 4";
            } else if (version.matches("[3.1-4)")) {
                baseName += " 3.1";
            }
        }

        baseName += " (" + version + ")";

        int counter = 1;

        while (true) {
            final String name = createDefaultRuntimeName(baseName, counter);

            if (ServerCore.findRuntime(name) == null) {
                return name;
            }

            counter++;
        }
    }

    private static String createDefaultRuntimeName(String baseName, int counter) {
        StringBuilder runtimeName = new StringBuilder();

        runtimeName.append(baseName);

        if (counter != 1) {
            runtimeName.append(" (")
               .append(counter)
               .append(')');
        }

        return runtimeName.toString();
    }


    // #### Other public methods

    public Version getVersion() {
        IPath location = getRuntime().getLocation();

        if (location != null) {
            PayaraLocationUtils payaraInstall = find(location.toFile());

            if (payaraInstall != null) {
                return payaraInstall.version();
            }
        }

        return null;
    }

    public IStatus validateVersion() {
        Version version = getVersion();

        if (version == null) {
            // Should not happen if called after validateServerLocation
            return new Status(ERROR, SYMBOLIC_NAME, runtimeNotValid);
        }

        if (!version.matches("[3.1-8)")) {
            return new Status(ERROR, SYMBOLIC_NAME, unsupportedVersion);
        }

        return OK_STATUS;
    }

    public VersionConstraint getJavaVersionConstraint() {
        Version version = getVersion();

        if (version != null) {
            if (version.matches("[7")) {
                return VERSION_CONSTRAINT_7;
            }

            if (version.matches("[6")) {
                return VERSION_CONSTRAINT_6;
            }

            if (version.matches("[5")) {
                return VERSION_CONSTRAINT_5;
            }

            if (version.matches("[4")) {
                return VERSION_CONSTRAINT_4;
            }

            return VERSION_CONSTRAINT_3_1;
        }

        return null;
    }

    public IStatus validateServerLocation() {
        return validateServerLocation(getRuntime().getLocation());
    }

    public IStatus validateServerLocation(IPath location) {

        // This is maybe a redundant check to the GUI annotation but
        // needed in case where a GUI is not involved (although we don't know
        // yet what case that would be)
        if (location == null || !location.toFile().exists()) {
            return new Status(ERROR, SYMBOLIC_NAME, bind(pathDoesNotExist, "Specified path"));
        }

        if (find(location.toFile()) == null) {
            return new Status(ERROR, SYMBOLIC_NAME, notValidPayaraInstall);
        }

        return OK_STATUS;
    }

}
