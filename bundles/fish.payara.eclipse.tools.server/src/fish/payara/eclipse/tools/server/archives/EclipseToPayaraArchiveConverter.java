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

package fish.payara.eclipse.tools.server.archives;

import static fish.payara.eclipse.tools.server.PayaraServerPlugin.logError;
import static fish.payara.eclipse.tools.server.utils.Utils.hasProjectFacet;
import static fish.payara.eclipse.tools.server.utils.WtpUtil.load;
import static org.eclipse.wst.common.componentcore.internal.util.IModuleConstants.JST_WEB_MODULE;
import static org.eclipse.wst.common.project.facet.core.ProjectFacetsManager.getProjectFacet;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.server.core.Servlet;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.eclipse.wst.server.core.util.HttpLaunchable;
import org.eclipse.wst.server.core.util.WebResource;

import fish.payara.eclipse.tools.server.PayaraServer;

/**
 * This class converts / adapts an Eclipse generic {@link IModuleArtifact} into an
 * {@link HttpLaunchable}, which represents an archive that can be deployed to Payara / GlassFish
 *
 * <p>
 * This class is registered in <code>plug-in.xml</code> in the
 * <code>org.eclipse.wst.server.core.launchableAdapters</code> extension point.
 * </p>
 *
 */
@SuppressWarnings("restriction")
public class EclipseToPayaraArchiveConverter extends LaunchableAdapterDelegate {

    @Override
    public Object getLaunchable(IServer server, IModuleArtifact moduleArtifact) throws CoreException {
        if (server == null) {
            return null;
        }

        String serverTypeId = server.getServerType().getId();

        if (!serverTypeId.equals("payara.server")) {
            return null;
        }

        PayaraServer glassfish = load(server, PayaraServer.class);

        

        // Implementation borrowed from org.eclipse.jst.server.tomcat.core.
        // TomcatServerLaunchableAdapterDelegate.java
        if ((glassfish == null) || (!(moduleArtifact instanceof Servlet) && !(moduleArtifact instanceof WebResource)) || !hasProjectFacet(moduleArtifact.getModule(), getProjectFacet(JST_WEB_MODULE))) {
            return null;
        }

        try {
            IURLProvider delegate = load(server, IURLProvider.class);
            if (delegate == null) {
                return null;
            }

            URL url = delegate.getModuleRootURL(moduleArtifact.getModule());
            if (url == null) {
                return null;
            }

            if (moduleArtifact instanceof Servlet) {
                Servlet servlet = (Servlet) moduleArtifact;

                String base, path;
                if (servlet.getAlias() != null) {
                    base = normalize(url.getFile());
                    path = normalize(servlet.getAlias());
                    url = new URL(url, base + path);
                } else {
                    base = "/servlet";
                    path = servlet.getServletClassName();
                }

                url = new URL(url.getProtocol(), url.getHost(), url.getPort(), base + path);
            } else if (moduleArtifact instanceof WebResource) {
                WebResource resource = (WebResource) moduleArtifact;

                String path = normalize(resource.getPath().toPortableString());
                if (path.length() != 0) {
                    String base = normalize(url.getFile());
                    url = new URL(url.getProtocol(), url.getHost(), url.getPort(), base + path);
                }
            }

            return new HttpLaunchable((new UrlPathEncoder(url)).asURL());
        } catch (Exception e) {
            logError("Error getting URL for " + moduleArtifact, e);
            return null;
        }
    }

    private String normalize(String path) {
        if ((path == null) || path.equals("/")) {
            return "";
        }

        if (path.length() > 0 && path.endsWith("/")) {
            return path.substring(0, path.length() - 2);
        }

        if (path.length() > 0 && !path.startsWith("/")) {
            return "/" + path;
        }

        return path;
    }

    static class UrlPathEncoder {

        private URL _url = null;

        public UrlPathEncoder(URL theUrl) throws MalformedURLException {
            this(theUrl.toString());
        }

        public UrlPathEncoder(String urlString) throws MalformedURLException {
            try {

                // Need to decode the URL to make sure we don't re-encode
                // any already encoded characters
                String urlStringDecoded = URLDecoder.decode(urlString, "UTF-8"); //$NON-NLS-1$

                // Now split the URL into path and query (they get encoded differently)
                URL tmpUrl = new URL(urlStringDecoded);
                String query = tmpUrl.getQuery();
                StringBuilder path = new StringBuilder();

                // Encode the path and put the '/'s back
                path.append(URLEncoder.encode(tmpUrl.getPath(), "UTF-8") //$NON-NLS-1$
                        .replaceAll("%2[fF]", "/")); //$NON-NLS-1$ //$NON-NLS-2$

                if (query != null) {
                    path.append("?").append(URLEncoder.encode(query, "UTF-8")); //$NON-NLS-1$//$NON-NLS-2$
                }

                // Rebuild using the encoded path and query with the original scheme
                _url = new URL(tmpUrl.getProtocol(), tmpUrl.getHost(), tmpUrl.getPort(), path.toString());
            } catch (UnsupportedEncodingException uee) {
                logError("Fatal: Unsupported encoding", uee);
            }
        }

        public String asString() {
            return _url.toString();
        }

        public URL asURL() {
            return _url;
        }
    }

}
