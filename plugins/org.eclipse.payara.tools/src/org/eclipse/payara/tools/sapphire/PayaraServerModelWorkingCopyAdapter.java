/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sapphire;

import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_ADMIN_NAME;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_ADMIN_PASSWORD;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_ADMIN_PORT;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_ATTACH_DEBUGGER_EARLY;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_DEBUG_PORT;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_DOMAIN_PATH;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_HOST_NAME;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_NAME;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_PRESERVE_SESSIONS;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_RESTART_PATTERN;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_SERVER_PORT;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_USE_ANONYMOUS_CONNECTIONS;
import static org.eclipse.payara.tools.sapphire.IPayaraServerModel.PROP_USE_JAR_DEPLOYMENT;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_ADMIN;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_ADMINPASS;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_ADMINPORT;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_DEBUG_PORT;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_DOMAINPATH;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_JARDEPLOY;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_KEEPSESSIONS;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_SERVERPORT;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_USEANONYMOUSCONNECTIONS;

import java.beans.PropertyChangeListener;

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyBinding;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.ValuePropertyBinding;
import org.eclipse.wst.server.core.IServerWorkingCopy;

/**
 * This class provides an adapter for the generated implementation of {@link IPayaraRuntimeModel},
 * which maps the getters/setters to the attributes of an {@link IServerWorkingCopy}.
 * 
 * <p>
 * Usage example:
 * 
 * <pre>
 * <code>
 * IPayaraServerModel serverModel = 
 *     IPayaraServerModel.TYPE.instantiate(
 *         new PayaraServerModelWorkingCopyAdapter(serverWorkingCopy));
 * </code>
 * </pre>
 *
 */
public class PayaraServerModelWorkingCopyAdapter extends Resource {

    /**
     * The working copy used as backing storage for an IPayaraServerModel
     * implementation.
     */
    private final IServerWorkingCopy workingCopy;

    public PayaraServerModelWorkingCopyAdapter(IServerWorkingCopy workingCopy) {
        super(null);
        this.workingCopy = workingCopy;
    }

    @Override
    protected PropertyBinding createBinding(Property property) {
        PropertyDef propertyDef = property.definition();

        if (propertyDef == PROP_NAME) {
            return new ValuePropertyBinding() {
                @Override
                public String read() {
                    return workingCopy.getName();
                }

                @Override
                public void write(String value) {
                    workingCopy.setName(value);
                }
            };
        }

        if (propertyDef == PROP_HOST_NAME) {
            return new ValuePropertyBinding() {
                private PropertyChangeListener listener;

                @Override
                public void init(Property property) {
                    super.init(property);

                    listener = e -> {
                        if ("hostname".equals(e.getPropertyName())) {
                            property().refresh();
                        }
                    };

                    workingCopy.addPropertyChangeListener(this.listener);
                }

                @Override
                public String read() {
                    return workingCopy.getHost();
                }

                @Override
                public void write(String value) {
                    workingCopy.setHost(value);
                }

                @Override
                public void dispose() {
                    super.dispose();

                    workingCopy.removePropertyChangeListener(listener);
                    listener = null;
                }
            };
        }

        if (propertyDef == PROP_ADMIN_NAME) {
            return new AttributeValueBinding(workingCopy, ATTR_ADMIN);
        }

        if (propertyDef == PROP_ADMIN_PASSWORD) {
            return new AttributeValueBinding(workingCopy, ATTR_ADMINPASS);
        }

        if (propertyDef == PROP_ADMIN_PORT) {
            return new AttributeValueBinding(workingCopy, ATTR_ADMINPORT);
        }

        if (propertyDef == PROP_DEBUG_PORT) {
            return new AttributeValueBinding(workingCopy, ATTR_DEBUG_PORT);
        }

        if (propertyDef == PROP_SERVER_PORT) {
            return new AttributeValueBinding(workingCopy, ATTR_SERVERPORT);
        }

        if (propertyDef == PROP_DOMAIN_PATH) {
            return new AttributeValueBinding(workingCopy, ATTR_DOMAINPATH);
        }

        if (propertyDef == PROP_PRESERVE_SESSIONS) {
            return new AttributeValueBinding(workingCopy, ATTR_KEEPSESSIONS);
        }

        if (propertyDef == PROP_USE_ANONYMOUS_CONNECTIONS) {
            return new AttributeValueBinding(workingCopy, ATTR_USEANONYMOUSCONNECTIONS);
        }

        if (propertyDef == PROP_USE_JAR_DEPLOYMENT) {
            return new AttributeValueBinding(workingCopy, ATTR_JARDEPLOY);
        }

        if (propertyDef == PROP_RESTART_PATTERN) {
            return new AttributeValueBinding(workingCopy, PROP_RESTART_PATTERN.name());
        }
        
        if (propertyDef == PROP_ATTACH_DEBUGGER_EARLY) {
            return new AttributeValueBinding(workingCopy, PROP_ATTACH_DEBUGGER_EARLY.name());
        }

        throw new IllegalStateException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A adapt(Class<A> adapterType) {
        if (adapterType == IServerWorkingCopy.class) {
            return (A) workingCopy;
        }

        return super.adapt(adapterType);
    }
}

/**
 * Value binding that binds the read and write operations for a property
 * to a working copy's get- and set attributes.
 * 
 */
class AttributeValueBinding extends ValuePropertyBinding {
    private final IServerWorkingCopy workingCopy;
    private final String attribute;

    public AttributeValueBinding(IServerWorkingCopy workingCopy, String attribute) {
        this.workingCopy = workingCopy;
        this.attribute = attribute;
    }

    @Override
    public String read() {
        return workingCopy.getAttribute(attribute, (String) null);
    }

    @Override
    public void write(String value) {
        this.workingCopy.setAttribute(attribute, value);
    }
}
