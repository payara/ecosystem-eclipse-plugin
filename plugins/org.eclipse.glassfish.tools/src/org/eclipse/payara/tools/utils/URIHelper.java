/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.utils;

import static java.io.File.separator;
import static org.eclipse.payara.tools.utils.Utils.getHttpListenerProtocol;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.payara.tools.server.GlassFishServer;

public class URIHelper {

	public static final void showURI(String uriParam) throws URISyntaxException, IOException {
		Desktop.getDesktop().browse(new URI(uriParam));
	}

	public static final void showURI(URI uri) throws IOException {
		Desktop.getDesktop().browse(uri);
	}

	public static final URI getServerAdminURI(GlassFishServer server) throws URISyntaxException {
		return new URI(getHttpListenerProtocol(server.getHost(), server.getAdminPort()), null, server.getHost(),
				server.getAdminPort(), null, null, null);
	}

	public static final URI getServerHomeURI(GlassFishServer server) throws URISyntaxException {
		return new File(server.getServerHome()).toURI();
	}

	public static final URI getDomainHomeURI(GlassFishServer server) throws URISyntaxException {
		return new File(server.getDomainsFolder() + separator + server.getDomainName()).toURI();
	}
}
