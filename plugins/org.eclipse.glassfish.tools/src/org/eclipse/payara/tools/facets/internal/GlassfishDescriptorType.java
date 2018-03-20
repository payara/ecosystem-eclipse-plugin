/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.facets.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.payara.tools.facets.models.IGlassfishDeploymentDescriptorModel;
import org.eclipse.payara.tools.facets.models.IGlassfishEjbDescriptorModel;
import org.eclipse.sapphire.ElementType;

enum GlassfishDescriptorType {
    GF_WEB, GF_EJB;

    private static final Map<ElementType, GlassfishDescriptorType> ROOT_MODEL_ELEMENT_TYPE_TO_DESCRIPTOR_TYPE = new HashMap<>();

    static {
        ROOT_MODEL_ELEMENT_TYPE_TO_DESCRIPTOR_TYPE.put(
                IGlassfishDeploymentDescriptorModel.TYPE,
                GlassfishDescriptorType.GF_WEB);
        ROOT_MODEL_ELEMENT_TYPE_TO_DESCRIPTOR_TYPE.put(
                IGlassfishEjbDescriptorModel.TYPE,
                GlassfishDescriptorType.GF_EJB);
    }

    public static GlassfishDescriptorType getDescriptorType(
            ElementType modelType) {
        return ROOT_MODEL_ELEMENT_TYPE_TO_DESCRIPTOR_TYPE.get(modelType);
    }

    private static final GlassfishRootElementInfo WEB_ROOT_INFO_V31 = new GlassfishRootElementInfo(
            "-//GlassFish.org//DTD GlassFish Application Server 3.1 Servlet 3.0//EN",
            "http://glassfish.org/dtds/glassfish-web-app_3_0-1.dtd",
            "glassfish-web-app");

    private static final GlassfishRootElementInfo EJB_ROOT_INFO_V31 = new GlassfishRootElementInfo(
            "-//GlassFish.org//DTD GlassFish Application Server 3.1 EJB 3.1//EN",
            "http://glassfish.org/dtds/glassfish-ejb-jar_3_1-1.dtd",
            "glassfish-ejb-jar");

    public static GlassfishRootElementInfo getGlassfishRootElementInfo(
            GlassfishDescriptorType type) {
        switch (type) {
        case GF_WEB:
            return WEB_ROOT_INFO_V31;
        case GF_EJB:
            return EJB_ROOT_INFO_V31;
        }
        return null;
    }
}

class GlassfishRootElementInfo {
    private final String publicId;
    private final String systemId;
    private final String rootElementName;

    GlassfishRootElementInfo(final String namespace,
            final String schemaLocation, final String rootElementName) {
        this.publicId = namespace;
        this.systemId = schemaLocation;
        this.rootElementName = rootElementName;
    }

    public String getPublicId() {
        return this.publicId;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public String getRootElementName() {
        return this.rootElementName;
    }

}
