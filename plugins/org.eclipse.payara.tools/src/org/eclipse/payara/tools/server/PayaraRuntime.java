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

package org.eclipse.payara.tools.server;

import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.osgi.util.NLS.bind;
import static org.eclipse.payara.tools.Messages.notValidGlassfishInstall;
import static org.eclipse.payara.tools.Messages.pathDoesNotExist;
import static org.eclipse.payara.tools.Messages.runtimeNotValid;
import static org.eclipse.payara.tools.Messages.unsupportedVersion;
import static org.eclipse.payara.tools.PayaraToolsPlugin.SYMBOLIC_NAME;
import static org.eclipse.payara.tools.sapphire.IPayaraRuntimeModel.PROP_JAVA_RUNTIME_ENVIRONMENT;
import static org.eclipse.payara.tools.sapphire.IPayaraRuntimeModel.PROP_NAME;
import static org.eclipse.payara.tools.sapphire.IPayaraRuntimeModel.PROP_SERVER_ROOT;
import static org.eclipse.payara.tools.utils.JdtUtil.findOrCreateJvm;
import static org.eclipse.payara.tools.utils.PayaraLocationUtils.find;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jst.server.core.IJavaRuntime;
import org.eclipse.payara.tools.sapphire.IPayaraRuntimeModel;
import org.eclipse.payara.tools.utils.PayaraLocationUtils;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyBinding;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.ValuePropertyBinding;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.platform.StatusBridge;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.model.RuntimeDelegate;

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

    private IPayaraRuntimeModel model;

    // #### RuntimeDelegate overridden methods

    @Override
    public IStatus validate() {
        IStatus status = super.validate();

        if (status.isOK()) {
            status = StatusBridge.create(getModel().validation());
        }

        return status;
    }

    @Override
    public void dispose() {
        super.dispose();

        synchronized (this) {
            if (model != null) {
                model.dispose();
                model = null;
            }
        }
    }

    
    // #### IJavaRuntime implementation methods

    @Override
    public IVMInstall getVMInstall() {
        return findOrCreateJvm(getModel().getJavaRuntimeEnvironment().text());
    }

    @Override
    public boolean isUsingDefaultJRE() {
        return false;
    }
    
    
    // #### Static methods

    public static String createDefaultRuntimeName(Version version) {
        String baseName = "Payara Server"; // TODO: - detect GF

        if (version != null) {
            if (version.matches("[5-6)")) {
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
        
        if (!version.matches("[3.1-6)")) {
            return new Status(ERROR, SYMBOLIC_NAME, unsupportedVersion);
        }
        
        return OK_STATUS;
    }

    public VersionConstraint getJavaVersionConstraint() {
        Version version = getVersion();

        if (version != null) {
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

    public synchronized IPayaraRuntimeModel getModel() {
        if (model == null) {
            model = IPayaraRuntimeModel.TYPE.instantiate(new ConfigResource(getRuntime()));
            model.initialize();
        }

        return model;
    }

    public IStatus validateServerLocation() {
        IPath location = getRuntime().getLocation();

        // This is maybe a redundant check to the GUI annotation but
        // needed in case where a GUI is not involved (although we don't know
        // yet what case that would be)
        
        if (location == null || !location.toFile().exists()) {
            return new Status(ERROR, SYMBOLIC_NAME, bind(pathDoesNotExist, "Specified path"));
        }

        if (find(location.toFile()) == null) {
            return new Status(ERROR, SYMBOLIC_NAME, notValidGlassfishInstall);
        }

        return OK_STATUS;
    }

    private static final class ConfigResource extends Resource {
        private final IRuntime runtime;

        public ConfigResource(final IRuntime runtime) {
            super(null);

            if (runtime == null) {
                throw new IllegalArgumentException();
            }

            this.runtime = runtime;
        }

        @Override
        protected PropertyBinding createBinding(Property property) {
            final PropertyDef propertyDef = property.definition();

            if (propertyDef == PROP_NAME) {
                return new ValuePropertyBinding() {
                    @Override
                    public String read() {
                        return ConfigResource.this.runtime.getName();
                    }

                    @Override
                    public void write(final String value) {
                        if (ConfigResource.this.runtime instanceof RuntimeWorkingCopy) {
                            RuntimeWorkingCopy workingCopy = (RuntimeWorkingCopy) ConfigResource.this.runtime;
                            workingCopy.setName(value);
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    }
                };
            }
            
            if (propertyDef == PROP_JAVA_RUNTIME_ENVIRONMENT) {
                return new AttributeValueBinding(this.runtime, ATTR_SERVER_JDK);
            }
            
            if (propertyDef == PROP_SERVER_ROOT) {
                return new ValuePropertyBinding() {
                    @Override

                    public String read() {
                        final IPath path = ConfigResource.this.runtime.getLocation();
                        return path == null ? null : path.toOSString();
                    }

                    @Override

                    public void write(final String value) {
                        if (ConfigResource.this.runtime instanceof RuntimeWorkingCopy) {
                            RuntimeWorkingCopy workingCopy = (RuntimeWorkingCopy) ConfigResource.this.runtime;
                            IPath path = value == null ? Path.EMPTY : new Path(value);
                            workingCopy.setLocation(path);
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    }
                };
            }

            throw new IllegalStateException();
        }

        @Override
        public <A> A adapt(final Class<A> adapterType) {
            if (adapterType == IRuntime.class) {
                return adapterType.cast(this.runtime);
            }

            return super.adapt(adapterType);
        }
    }

    private static final class AttributeValueBinding extends ValuePropertyBinding {
        private final IRuntime runtime;
        private final String attribute;

        public AttributeValueBinding(final IRuntime runtime, final String attribute) {
            this.runtime = runtime;
            this.attribute = attribute;
        }

        @Override
        public String read() {
            return ((org.eclipse.wst.server.core.internal.Runtime) this.runtime).getAttribute(this.attribute,
                    (String) null);
        }

        @Override
        public void write(final String value) {
            if (this.runtime instanceof RuntimeWorkingCopy) {
                RuntimeWorkingCopy workingCopy = (RuntimeWorkingCopy) this.runtime;
                workingCopy.setAttribute(this.attribute, value);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

}
