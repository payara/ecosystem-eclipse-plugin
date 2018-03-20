/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.ui.editorsections;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.wst.server.ui.ServerLaunchConfigurationTab;

/**
 * The launch configuration that's shown when the "Open Launch Configuration" is clicked on e.g. the
 * "Server Editor". See {@link ServerSection}
 *
 * <p>
 * This is referred in <code>plug-in.xml</code> as extension
 * <code>org.eclipse.debug.ui.launchConfigurationTabGroups</code>
 *
 */
public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[7];
        tabs[0] = new ComboServerLaunchConfigurationTab(new String[] { "payara.server" });
        tabs[0].setLaunchConfigurationDialog(dialog);

        tabs[1] = new JavaArgumentsTab();
        tabs[1].setLaunchConfigurationDialog(dialog);

        tabs[2] = new JavaClasspathTab();
        tabs[2].setLaunchConfigurationDialog(dialog);

        tabs[3] = new SourceLookupTab();
        tabs[3].setLaunchConfigurationDialog(dialog);

        tabs[4] = new EnvironmentTab();
        tabs[4].setLaunchConfigurationDialog(dialog);

        tabs[5] = new JavaJRETab();
        tabs[5].setLaunchConfigurationDialog(dialog);

        tabs[6] = new CommonTab();
        tabs[6].setLaunchConfigurationDialog(dialog);
        setTabs(tabs);
    }

    private static class ComboServerLaunchConfigurationTab extends ServerLaunchConfigurationTab {
        public ComboServerLaunchConfigurationTab(String[] ids) {
            super(ids);
        }

        @Override
        public void initializeFrom(ILaunchConfiguration configuration) {
            super.initializeFrom(configuration);
            handleServerSelection();
        }
    }
}
