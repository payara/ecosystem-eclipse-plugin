/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.ui.resources.wizards;



public class Messages extends org.eclipse.osgi.util.NLS {
    static {
        org.eclipse.osgi.util.NLS.initializeMessages(
                "org.eclipse.glassfish.tools.ui.resources.wizards.Messages", Messages.class);
    }
    
    public static String Connection;
	public static String Create;
	public static String JNDIName;
	public static String ProjectName;
    public static String wizardTitle;
    public static String wizardDescription;
    public static String ErrorTitle;
    public static String errorUnknown;
    public static String errorFileExists;
    public static String errorFolderMissing;
    public static String errorProjectMissing;
    public static String errorConnectionMissing;
    public static String errorConnectionInvalid;
    
    //JavaMail Wizard
    public static String mailWizardTitle;
    public static String mailWizardDescription;
	public static String MailHost;
	public static String MailUser;
	public static String MailFrom;
	public static String errorMailHostNameMissing;
	public static String errorMailUserNameMissing;
	public static String errorMailReturnAddrMissing;
	
	//JMS Wizard
	public static String jmsWizardTitle;
	public static String jmsWizardDescription;
	public static String lblChooseType;
	public static String lblAdminObject;
	public static String lblConnector;
	public static String lblQueue;
	public static String lblTopic;
	public static String lblQueueConnectionFactory;
	public static String lblTopicConnectionFactory;
	public static String lblConnectionFactory;
	
	//Common
	public static String errorJndiNameMissing;
	public static String errorResourceTypeMissing;
	public static String errorFolderNull;
	public static String errorDuplicateName;
}
