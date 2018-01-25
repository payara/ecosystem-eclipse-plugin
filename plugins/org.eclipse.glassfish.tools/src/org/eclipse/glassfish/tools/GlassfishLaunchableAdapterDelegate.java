/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.glassfish.tools.utils.Utils;
import org.eclipse.jst.server.core.Servlet;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.eclipse.wst.server.core.util.HttpLaunchable;
import org.eclipse.wst.server.core.util.WebResource;
	
public class GlassfishLaunchableAdapterDelegate extends
		LaunchableAdapterDelegate {

	@Override
	public Object getLaunchable(IServer server, IModuleArtifact moduleArtifact)
			throws CoreException {
		if (server==null)
            return null;
        String serverTypeId = server.getServerType().getId();
            
        if( !serverTypeId.equals( "glassfish.server" ))
            return null;
        
        GlassFishServer glassfish = (GlassFishServer)server.loadAdapter(GlassFishServer.class, new NullProgressMonitor());
        if (glassfish == null)
            return null;

        // implementation borrowed from org.eclipse.jst.server.tomcat.core.
        //      TomcatServerLaunchableAdapterDelegate.java
        if (!(moduleArtifact instanceof Servlet) &&
            !(moduleArtifact instanceof WebResource))
            return null;
        
        if (!Utils.hasProjectFacet(moduleArtifact.getModule(), 
				ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_WEB_MODULE)))
				return null;       

        try {
            IURLProvider delegate = (IURLProvider)server.loadAdapter(IURLProvider.class, null);
            if (delegate==null)
                return null;

            URL url = delegate.getModuleRootURL(moduleArtifact.getModule());
            if( url == null )
            	return null;
            
            if (moduleArtifact instanceof Servlet) {
                Servlet servlet = (Servlet)moduleArtifact;
                String base, path;
                if (servlet.getAlias()!=null) {
                    base = normalize(url.getFile());
                    path = normalize(servlet.getAlias());
                    url = new URL(url, base+path);
                } else {
                    base = "/servlet";
                    path = servlet.getServletClassName();
                }
                url = new URL(url.getProtocol(), url.getHost(), url.getPort(), base + path);
            } else if (moduleArtifact instanceof WebResource) {
                WebResource resource = (WebResource)moduleArtifact;
                String path = normalize(resource.getPath().toPortableString());
                if (path.length()!=0) {
                    String base = normalize(url.getFile());
                    url = new URL(url.getProtocol(), url.getHost(), url.getPort(), base+path);
                }
            } 
            return new HttpLaunchable((new UrlPathEncoder(url)).asURL());
        } catch (Exception e) {
            GlassfishToolsPlugin.logError("Error getting URL for " + moduleArtifact, e);
            return null;
        }
    }
    
    private String normalize(String path) {
        if (path==null)
            return "";
        if (path.equals("/"))
            return "";
        if (path.length()>0 && path.endsWith("/"))
            return path.substring(0, path.length()-2);
        if (path.length()>0 && !path.startsWith("/"))
            return "/"+path;
        return path;
    }
    
    static class UrlPathEncoder {

        private URL _url = null;

        public UrlPathEncoder(URL theUrl) throws MalformedURLException {
            this(theUrl.toString());
        }
        
        public UrlPathEncoder(String urlString) throws MalformedURLException {
            try {
                // need to decode the url to make sure we don't re-encode 
                //            any already encoded characters
                String urlStringDecoded = URLDecoder.decode(urlString,"UTF-8"); //$NON-NLS-1$
                
                // now split the url into path and query (they get encoded differently)
                URL tmpUrl = new URL(urlStringDecoded);
                String query = tmpUrl.getQuery();
                StringBuilder path = new StringBuilder();
                
                // encode the path and put the '/'s back
                path.append(URLEncoder.encode(tmpUrl.getPath(), "UTF-8") //$NON-NLS-1$
                            .replaceAll("%2[fF]", "/")); //$NON-NLS-1$ //$NON-NLS-2$
                if (query != null) {
                    path.append("?").append(URLEncoder.encode(query, "UTF-8"));  //$NON-NLS-1$//$NON-NLS-2$
                }
                
                // rebuild using the encoded path and query with the original scheme
                _url = new URL(tmpUrl.getProtocol(), tmpUrl.getHost(),
                              tmpUrl.getPort(), path.toString());
            } catch(UnsupportedEncodingException uee) {
                GlassfishToolsPlugin.logError("Fatal: Unsupported encoding", uee);
            }
        }
        
        public String asString() { return _url.toString(); }
        public URL asURL() { return _url; }
    }

}
