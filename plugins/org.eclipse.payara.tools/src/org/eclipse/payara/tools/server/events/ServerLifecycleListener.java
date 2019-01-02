/******************************************************************************
 * Copyright (c) 2018 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.server.events;

import static java.io.File.separator;
import static org.eclipse.payara.tools.server.PayaraServer.ATTR_DOMAINPATH;
import static org.eclipse.payara.tools.utils.IsPayaraUtil.isPayara;
import static org.eclipse.payara.tools.utils.Jobs.scheduleShortJob;
import static org.eclipse.payara.tools.utils.WtpUtil.load;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.payara.tools.server.PayaraServer;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.editor.GlobalCommandManager;


/**
 * This listener listens to servers being added, changed or removed in Eclipse.
 * 
 * <p>
 * The main (and currently only) action that's is taken after such an event is creating
 * and updating a project in the workspace that shows the content of the Payara's domain
 * folder to which this server corresponds.
 * 
 * @author Arjan Tijms
 *
 */
public class ServerLifecycleListener implements IServerLifecycleListener {

    @Override
    public void serverAdded(IServer server) {
        if (!isPayara(server)) {
            return;
        }
        
        // Copy the server name and server domain to separate attributes so we can
        // track domain changes
        resetPreviousServerNameAndDomain(server);
        
        // Create a project in the workspace that shows the Server's domain
        // directory.
        createServerProject(load(server, PayaraServer.class));
    }

    @Override
    public void serverChanged(IServer server) {
        if (!isPayara(server)) {
            return;
        }
        
        String domainPath = server.getAttribute(ATTR_DOMAINPATH, "");
        String previousDomainPath = server.getAttribute("previous-domain", "");
        
        String serverName = server.getAttribute("name", "");
        String previousServerName = server.getAttribute("previous-name", "");
        
        if (!domainPath.equals(previousDomainPath)) {
            
            // Server domain has been changed
            
            // There doesn't seem to be an option to retarget to a new location, so
            // delete the existing project and re-create. This will set both the server domain
            // as well as the server name (should it have changed too).
            
            deleteServerProject(previousServerName);
            
            // Copy the server name and server domain to separate attributes so we can
            // track domain changes
            resetPreviousServerNameAndDomain(server);
            
            // Create a project in the workspace that shows the Server's domain
            // directory.
            createServerProject(load(server, PayaraServer.class));
            
            // Updating the previous name is a change in the server storage, so need to refresh any
            // editor(s) that have this server open.
            GlobalCommandManager.getInstance().reload(server.getId());
            
            return;
        }
        
        if (!serverName.equals(previousServerName)) {
            
            // Only server name has been changed (we capture domain and the both case above).
            
            // Rename the project associated with the server to the new server's
            // name
            renameServerProject(previousServerName, serverName);
         
            // Update the previous name to the current name, so we can track the
            // next name change.
            resetPreviousServerName(server);
            
            // Updating the previous name is a change in the server storage, so need to refresh any
            // editor(s) that have this server open.
            GlobalCommandManager.getInstance().reload(server.getId());
        }
    }

    @Override
    public void serverRemoved(IServer server) {
        if (!isPayara(server)) {
            return;
        }
        
        // Since the server is removed, we can remove the corresponding project
        // as well.
        deleteServerProject(server.getAttribute("name", ""));
    }
    
    private void createServerProject(PayaraServer payaraServer) {
        
        IWorkspace workSpace = ResourcesPlugin.getWorkspace();
        IProject project = workSpace.getRoot().getProject(payaraServer.getName());

        if (project.exists()) {
            // Another project with the same name already exists.
            return;
        }

        // Schedule a job to create the new project
        scheduleShortJob("Create project for new server", 
            monitor -> {
                IProjectDescription projectDescription = workSpace.newProjectDescription(payaraServer.getName());
                monitor.worked(10);

                try {
                    projectDescription
                        .setLocation(Path.fromOSString(
                            new File(payaraServer.getDomainsFolder() + separator + payaraServer.getDomainName()).getCanonicalPath()));
                
                    project.create(projectDescription, monitor);
                    monitor.worked(25);
                    
                    project.open(null);
                    monitor.worked(50);
                
                } catch (IOException | CoreException e) {
                    e.printStackTrace();
                } 
                
            });
    }
    
    private void renameServerProject(String projectName, String newProjectName) {
        
        IWorkspace workSpace = ResourcesPlugin.getWorkspace();
        IProject project = workSpace.getRoot().getProject(projectName);
        
        if (project.exists()) {
            
            try {
                if (!project.isOpen()) {
                   project.open(null);
                }
                
                IProjectDescription projectDescription = project.getDescription();
                projectDescription.setName(newProjectName);
                project.move(projectDescription, true, null);
            
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void deleteServerProject(String projectName) {

        IWorkspace workSpace = ResourcesPlugin.getWorkspace();
        IProject project = workSpace.getRoot().getProject(projectName);

        if (!project.exists()) {
            // No need to delete that what doesn't exist
            return;
        }
        
        try {
            project.delete(false, true, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    
    private void resetPreviousServerName(IServer server) {
        try {
            IServerWorkingCopy serverWorkingCopy = server.createWorkingCopy();
            serverWorkingCopy.setAttribute("previous-name", serverWorkingCopy.getAttribute("name", ""));
            serverWorkingCopy.save(true, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    private void resetPreviousServerNameAndDomain(IServer server) {
        try {
            IServerWorkingCopy serverWorkingCopy = server.createWorkingCopy();
            serverWorkingCopy.setAttribute("previous-name", serverWorkingCopy.getAttribute("name", ""));
            serverWorkingCopy.setAttribute("previous-domain", serverWorkingCopy.getAttribute(ATTR_DOMAINPATH, ""));
            serverWorkingCopy.save(true, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

}
