/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.facets.models;

import org.eclipse.glassfish.tools.facets.internal.GlassfishEJBDescriptorRootController;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlRootBinding;

@CustomXmlRootBinding(GlassfishEJBDescriptorRootController.class)
public interface IGlassfishEjbDescriptorModel extends Element {

	ElementType TYPE = new ElementType( IGlassfishEjbDescriptorModel.class );
	
//	@Type( base = ElementType.class )
//	@Label( standard = "enterprise beans definitions" )
//	@XmlListBinding( mappings = { @XmlListBinding.Mapping( element = "ejb", type = Element.class ) } )
//
//	ListProperty PROP_ENTERPRISE_BEANS = new ListProperty( TYPE, "EnterpriseBeans" ); //$NON-NLS-1$;
//    
//    ElementList<Element> getEnterpriseBeans();
	
}
