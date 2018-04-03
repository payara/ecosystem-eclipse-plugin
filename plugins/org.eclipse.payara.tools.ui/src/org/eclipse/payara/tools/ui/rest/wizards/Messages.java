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

package org.eclipse.payara.tools.ui.rest.wizards;

public class Messages extends org.eclipse.osgi.util.NLS {
    static {
        org.eclipse.osgi.util.NLS.initializeMessages(
                "org.eclipse.payara.tools.ui.rest.wizards.Messages", Messages.class);
    }

    public static String ProjectName;
    public static String sessionWizardTitle;
    public static String errorBusinessInterfaceMissing;

    public static String timerWizardTitle;
    public static String timerWizardDescription;
    public static String timerScheduleLabel;
    public static String timerScheduleDefault;
    public static String errorTimerScheduleMissing;

    public static String genericResourceWizardTitle;
    public static String genericResourceWizardDescription;
    public static String patternTypeLabel;
    public static String patternTypeSimpleValue;
    public static String patternTypeContainerValue;
    public static String patternTypeClientContainerValue;
    public static String mimeTypeLabel;
    public static String errorMimeTypeMissing;
    public static String representationClassLabel;
    public static String representationClassDialogTitle;
    public static String representationClassDialogLabel;
    public static String errorRepresentationClassMissing;
    public static String errorRepresentationClassInvalid;

    public static String containerRepresentationClassLabel;
    public static String containerRepresentationClassDialogTitle;
    public static String containerRepresentationClassDialogLabel;
    public static String errorContainerRepresentationClassMissing;
    public static String errorContainerRepresentationClassInvalid;
    public static String pathLabel;
    public static String errorPathMissing;
    public static String errorPathInvalid;
    public static String containerPathLabel;
    public static String errorContainerPathMissing;
}
