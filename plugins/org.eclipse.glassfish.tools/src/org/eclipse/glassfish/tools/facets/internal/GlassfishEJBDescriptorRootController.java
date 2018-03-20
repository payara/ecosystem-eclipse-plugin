/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.facets.internal;

import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.w3c.dom.Document;

public class GlassfishEJBDescriptorRootController extends GlassfishDescriptorRootController {

	@Override
	protected void createRootElement(Document document) {
		super.createRootElement(document);
		XmlElement root = new XmlElement(resource().adapt(RootXmlResource.class).store(), document.getDocumentElement());
		root.addChildElement("enterprise-beans");
	}

}
