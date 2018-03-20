/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    
	static {
        initializeMessages(
           "org.eclipse.payara.tools.Messages", 
           Messages.class);
    }
    
    public static String serverHome;
    public static String runtimeName;
    public static String emptyRuntimeName;
    public static String duplicateRuntimeName;
    public static String runtimeIdentified;
    public static String unsupportedVersion;
    public static String runtimeNotValid;
    public static String versionsNotMatching;
    
    // server status
    public static String invalidCredentials;
    public static String serverNotMatchingLocal;
    public static String serverNotMatchingRemote;
    public static String connectionError;
    
    public static String facetNotSupported;
    
    public static String AdminName;
    public static String AdminPassword;
    public static String ServerPortNumber;
    public static String AdminServerPortNumber;
    public static String wizardSectionTitle;
    public static String wizardSectionDescription;
    public static String DomainName;
    public static String DomainDirectory;
    public static String UseAnonymousConnection;
    public static String keepSessions;
    public static String jarDeploy;
    public static String targetTooltip;
     
    // additional strings to workaround for issue 222688
    public static String canInstallPath;
    public static String possibleInstallExists;
    public static String downloadingServer;
    public static String notValidGlassfishInstall;

    public static String register;
    public static String updateCenter;
 
    public static String pathDoesNotExist;
    public static String pathNotDirectory;
    public static String pathNotWritable;
    public static String pathNotValidDomain;
    public static String missingDomainLocation;
    public static String invalidPortNumbers;
    public static String TitleWrongDomainLocation;
    public static String serverWithSameDomainPathExisting;
    public static String OKButton;

    public static String startupWarning;
    public static String noProfilersConfigured;
	public static String profilingUnsupportedInVersion;
	
	public static String serverDirectoryGone;
	public static String emptyTargetMsg;
	public static String target;
	
	public static String uniqueNameNotFound;
	
	public static String errorAppWebContentRootMapping;
	
	public static String canntCommunicate;
	public static String abortLaunchMsg;
	public static String checkVpnOrProxy;
	public static String wrongUsernamePassword;
	public static String badGateway;
	public static String domainNotMatch;
}
