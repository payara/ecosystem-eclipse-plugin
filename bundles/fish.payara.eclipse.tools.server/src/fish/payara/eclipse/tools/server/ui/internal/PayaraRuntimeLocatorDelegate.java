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

package fish.payara.eclipse.tools.server.ui.internal;

import fish.payara.eclipse.tools.server.PayaraRuntime;
import fish.payara.eclipse.tools.server.PayaraServerPlugin;
import fish.payara.eclipse.tools.server.utils.PayaraLocationUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ResourceManager;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.model.RuntimeLocatorDelegate;

@SuppressWarnings("restriction")
public final class PayaraRuntimeLocatorDelegate extends RuntimeLocatorDelegate {

    private static final IRuntimeType RUNTIME_TYPE = ServerCore.findRuntimeType("payara.runtime");

    @Override
    public void searchForRuntimes(IPath path, IRuntimeSearchListener listener, IProgressMonitor monitor) {
        search(path.toFile(), listener, monitor);
    }

    private void search(File file, IRuntimeSearchListener listener, IProgressMonitor monitor) {
        if (monitor.isCanceled() || !file.isDirectory() || file.isHidden()) {
            return;
        }

        try {
            IRuntime runtime = create(file);
            if (runtime != null) {
                IRuntimeWorkingCopy wc = runtime.createWorkingCopy();
                listener.runtimeFound(wc);
                return;
            }
        } catch (CoreException e) {
            PayaraServerPlugin.log(e);
            return;
        }

        File[] children = file.listFiles();

        if (children != null) {
            for (File child : children) {
                search(child, listener, monitor);
            }
        }
    }

    private static IRuntime create(File gfhome) throws CoreException {
        PayaraLocationUtils install = PayaraLocationUtils.find(gfhome);
        if ((install == null) || (findRuntime(gfhome) != null)) {
            return null;
        }

        String name = PayaraRuntime.createDefaultRuntimeName(install.version());

        final IRuntimeWorkingCopy created = RUNTIME_TYPE.createRuntime(name, null);
        created.setLocation(new Path(gfhome.getAbsolutePath()));
        created.setName(name);

        final RuntimeWorkingCopy rwc = (RuntimeWorkingCopy) created;

        final Map<String, String> props = new HashMap<>();
        props.put("payara.rootdirectory", rwc.getLocation().toPortableString());
        rwc.setAttribute("generic_server_instance_properties", props);

        final PayaraRuntime gf = (PayaraRuntime) rwc.loadAdapter(PayaraRuntime.class, null);
        
//        final IPayaraRuntimeModel gfmodel = gf.getModel();
//        final Value<org.eclipse.sapphire.modeling.Path> javaRuntimeEnvironmentProperty = gfmodel
//                .getJavaRuntimeEnvironment();

//        if (javaRuntimeEnvironmentProperty.content() == null) {
//            final Display display = Display.getDefault();
//
//            display.syncExec(new Runnable() {
//                @Override
//                public void run() {
//                    new SapphireDialog(display.getActiveShell(), gfmodel,
//                            DefinitionLoader.sdef(PayaraRuntimeLocatorDelegate.class).dialog()).open();
//                }
//            });
//
//            if (javaRuntimeEnvironmentProperty.content() == null) {
//                rwc.dispose();
//                return null;
//            } else {
//                // Force JVM definition to be created
//
//                gf.getVMInstall();
//
//                // Clear the explicit JVM location as the DefaultValueService will now pick it
//                // up
//
//                javaRuntimeEnvironmentProperty.clear();
//            }
//        }

        final IStatus validationResult = created.validate(null);

        if (validationResult.getSeverity() != IStatus.ERROR) {
            created.save(true, null);
            return created.getOriginal();
        }

        return null;
    }

    private static IRuntime findRuntime(File location) {
        for (IRuntime runtime : ResourceManager.getInstance().getRuntimes()) {
            if (RUNTIME_TYPE == runtime.getRuntimeType() && location.equals(runtime.getLocation().toFile())) {
                return runtime;
            }
        }

        return null;
    }

}
