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

package org.eclipse.payara.tools.facets.models;

import org.eclipse.payara.tools.facets.internal.GlassfishDescriptorRootController;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlRootBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

@CustomXmlRootBinding(GlassfishDescriptorRootController.class)

public interface IGlassfishDeploymentDescriptorModel extends Element {

    ElementType TYPE = new ElementType(IGlassfishDeploymentDescriptorModel.class);

    @Label(standard = "context root")
    @XmlBinding(path = "context-root")

    ValueProperty PROP_CONTEXT_ROOT = new ValueProperty(TYPE, "ContextRoot"); //$NON-NLS-1$ ;

    Value<String> getContextRoot();

    void setContextRoot(String contextRoot);

    @Label(standard = "jsp config")
    @XmlBinding(path = "jsp-config")
    ValueProperty PROP_JSP_CONFIG = new ValueProperty(TYPE, "JspConfig"); //$NON-NLS-1$ ;

    Value<String> getJspConfig();

    void setJspConfig(String jspConfig);
}
