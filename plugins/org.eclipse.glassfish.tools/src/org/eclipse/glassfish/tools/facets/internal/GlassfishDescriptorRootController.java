/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.facets.internal;

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;
import static org.eclipse.sapphire.modeling.util.MiscUtil.normalizeToNull;

import org.eclipse.core.resources.IProject;
import org.eclipse.glassfish.tools.utils.GlassFishLocationUtils;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.StandardRootElementController;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

public class GlassfishDescriptorRootController extends StandardRootElementController {

	private GlassfishDescriptorType type;
    
    @Override
    public void init( final XmlResource resource )
    {
        super.init( resource );
        
        this.type = GlassfishDescriptorType.getDescriptorType( resource.element().type() );
    }

    @Override
    public void createRootElement()
    {
    	Document document = ( (RootXmlResource) resource().root() ).getDomDocument();
        createRootElement(document);
    }
    
    protected void createRootElement(Document document) {
    	GlassfishRootElementInfo gfRootInfo = getGlassfishRootElementInfo();
    	
        final Element root = document.createElementNS(null,
                gfRootInfo.getRootElementName());
        DocumentType doctype = null;

        if (gfRootInfo.getPublicId() != null ) {
            doctype = document.getImplementation().createDocumentType(
                    gfRootInfo.getRootElementName(), gfRootInfo.getPublicId(), gfRootInfo.getSystemId());
        } else {
            doctype = document.getImplementation().createDocumentType(
                    gfRootInfo.getRootElementName(), null, gfRootInfo.getSystemId());
        }
        if (doctype != null) {
            document.appendChild(doctype);
            document.insertBefore(root, doctype);
        }
        document.appendChild(root);
    }
    
    @Override
    public boolean checkRootElement() 
    {
        final Document document = ( (RootXmlResource) resource().root() ).getDomDocument();
        final Element root = document.getDocumentElement();
        
        GlassfishRootElementInfo gfRootInfo = getGlassfishRootElementInfo();
        
        if( equal( root.getLocalName(), gfRootInfo.getRootElementName() ) )
        {
            final DocumentType documentType = document.getDoctype();
            
            if( documentType != null &&
                gfRootInfo.getSystemId().equals( documentType.getSystemId() ) &&
                equal( gfRootInfo.getPublicId(), normalizeToNull( documentType.getPublicId() ) ) )
            {
                return true;
            }
        }
        
        return false;
    }

	private GlassfishRootElementInfo getGlassfishRootElementInfo() {
    	GlassfishRootElementInfo defaultInfo = GlassfishDescriptorType.getGlassfishRootElementInfo(type);
    	GlassFishLocationUtils gfInstall = GlassFishLocationUtils.find(resource().adapt(IProject.class));
    	if (gfInstall == null)
    		return defaultInfo;
    	Version v = gfInstall.version();
        Version gfVersion = new Version(v.toString());
        if (gfVersion == null)
        	return defaultInfo;
        
        GlassfishRootElementInfo rootInfo = GlassfishDescriptorType.getGlassfishRootElementInfo(type);
        return rootInfo != null ? rootInfo : defaultInfo;
    }

}
