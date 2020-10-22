/**
 * Copyright (c) 2020 Payara Foundation
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.payara.tools.ui.wizards;

public class Messages extends org.eclipse.osgi.util.NLS {

    static {
        initializeMessages(
                Messages.class.getName(),
                Messages.class
        );
    }

    public static String microProjectTitle;

    public static String microProjectSettingsPageTitle;
    public static String microProjectSettingsPageDescription;
    public static String groupIdComponentLabel;
    public static String artifactIdComponentLabel;
    public static String versionComponentLabel;
    public static String packageComponentLabel;
    public static String versionValidationMessage;
    public static String packageValidationMessage;

    public static String microProjectLocationPageTitle;
    public static String microProjectLocationPageDescription;

    public static String projectArchetypeJobCreating;
    public static String projectArchetypeJobFailed;
    public static String projectPomAlreadyExists;

    public static String microSettingsPageTitle;
    public static String microSettingsPageDescription;
    public static String contextPathComponentLabel;
    public static String microVersionComponentLabel;
    public static String autobindComponentLabel;
    public static String contextPathValidationMessage;
    public static String microVersionValidationMessage;

}
