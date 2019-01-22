/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2019 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.sapphire;

import static org.eclipse.sapphire.modeling.annotations.FileSystemResourceType.FOLDER;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Listeners;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.SensitiveData;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * This interface represents the user properties for a Payara server.
 * 
 * <p>
 * Layout of this model to create a UI is done in 
 * <code>org.eclipse.payara.tools.ui/src/org/eclipse/payara/tools/ui/PayaraUI.sdef</code>
 *
 * <p>
 * This class has direct references to XML elements in that file via
 * the XmlBinding annotations.
 * 
 * <p>
 * Data storage of this model is in the Server's general attributes, which it is
 * coupled to via the {@link PayaraServerModelWorkingCopyAdapter} class.
 */
public interface IPayaraServerModel extends Element {

    ElementType TYPE = new ElementType(IPayaraServerModel.class);

    // *** Name ***

    @Label(standard = "na&me")
    @Required
    @Service(impl = PayaraServerConfigServices.UniqueServerNameValidationService.class)
    ValueProperty PROP_NAME = new ValueProperty(TYPE, "Name");
    Value<String> getName();
    void setName(String value);

    
    // *** HostName ***

    @Label(standard = "&host name")
    @Required
    ValueProperty PROP_HOST_NAME = new ValueProperty(TYPE, "HostName");
    Value<String> getHostName();
    void setHostName(String value);

    
    // *** Remote ***

    @Type(base = Boolean.class)
    @Derived(text = "${ HostName != 'localhost' }")
    ValueProperty PROP_REMOTE = new ValueProperty(TYPE, "Remote");
    Value<Boolean> getRemote();

    
    // *** Domain path ***

    @XmlBinding(path = "domain-path")
    @Label(standard = "domain &path")
    @Required
    @Type(base = Path.class)
    @MustExist
    @AbsolutePath
    @ValidFileSystemResourceType(FOLDER)
    @Service(impl = PayaraServerConfigServices.DomainLocationValidationService.class)
    @Listeners(PayaraServerConfigServices.DomainLocationListener.class)
    @Enablement(expr = "${ ! Remote }")
    ValueProperty PROP_DOMAIN_PATH = new ValueProperty(TYPE, "DomainPath");
    Value<Path> getDomainPath();
    void setDomainPath(Path value);

    
    // *** Admin ***

    @XmlBinding(path = "admin-name")
    @Label(standard = "admin nam&e")
    @Required
    @DefaultValue(text = "admin")
    ValueProperty PROP_ADMIN_NAME = new ValueProperty(TYPE, "AdminName");
    Value<String> getAdminName();
    void setAdminName(String value);

    
    // *** Admin pass ***

    @XmlBinding(path = "admin-password")
    @Label(standard = "admin pass&word")
    @SensitiveData
    ValueProperty PROP_ADMIN_PASSWORD = new ValueProperty(TYPE, "AdminPassword");
    Value<String> getAdminPassword();
    void setAdminPassword(String value);

    
    // *** Admin port ***

    @Type(base = Integer.class)
    @XmlBinding(path = "admin-port")
    @Label(standard = "admin p&ort")
    @Required
    @NumericRange(min = "1025", max = "65535")
    @Enablement(expr = "${ Remote }")
    ValueProperty PROP_ADMIN_PORT = new ValueProperty(TYPE, "AdminPort");
    Value<Integer> getAdminPort();
    void setAdminPort(Integer value);

    
    // *** Debug port ***

    @Type(base = Integer.class)
    @XmlBinding(path = "debug-port")
    @Label(standard = "deb&ug port")
    @NumericRange(min = "1034", max = "65535")
    @DefaultValue(text = "8009")
    @Documentation(content = 
        "For local server you can specify the port on which the server will be debugged. If empty, the current value" + 
        " from domain.xml will be used.\n For remote servers you may want to specify the debug port to be able to debug your" + 
        " applications running on the server.")
    ValueProperty PROP_DEBUG_PORT = new ValueProperty(TYPE, "DebugPort");
    Value<Integer> getDebugPort();
    void setDebugPort(Integer value);

    
    // *** Server port ***

    @Type(base = Integer.class)
    @XmlBinding(path = "server-port")
    @Label(standard = "server port")
    @Enablement(expr = "false")
    ValueProperty PROP_SERVER_PORT = new ValueProperty(TYPE, "ServerPort");
    Value<Integer> getServerPort();
    void setServerPort(Integer value);

    
    // *** Preserve sessions ***

    @Type(base = Boolean.class)
    @XmlBinding(path = "preserve-sessions")
    @DefaultValue(text = "false")
    @Label(standard = "preser&ve sessions across redeployment")
    ValueProperty PROP_PRESERVE_SESSIONS = new ValueProperty(TYPE, "PreserveSessions");
    Value<Boolean> getPreserveSessions();
    void setPreserveSessions(Boolean value);

    
    // *** Use anonymous connections for admin commands ***

    @Type(base = Boolean.class)
    @XmlBinding(path = "use-anonymous-connections")
    @DefaultValue(text = "true")
    @Label(standard = "use anon&ymous connections for admin commands")
    ValueProperty PROP_USE_ANONYMOUS_CONNECTIONS = new ValueProperty(TYPE, "UseAnonymousConnections");
    Value<Boolean> getUseAnonymousConnections();
    void setUseAnonymousConnections(Boolean value);

    
    // *** Use JAR archives for deployment ***

    @Type(base = Boolean.class)
    @XmlBinding(path = "use-jar-deployment")
    @DefaultValue(text = "false")
    @Label(standard = "use &JAR archives for deployment")
    @Enablement(expr = "${ ! Remote }")
    ValueProperty PROP_USE_JAR_DEPLOYMENT = new ValueProperty(TYPE, "UseJarDeployment");
    Value<Boolean> getUseJarDeployment();
    void setUseJarDeployment(Boolean value);
    
    String PROP_RESTART_PATTERN_DEFAULT = "//.jar$";
    @DefaultValue(text = PROP_RESTART_PATTERN_DEFAULT)
    ValueProperty PROP_RESTART_PATTERN = new ValueProperty(TYPE, "RestartPattern");
    Value<String> getRestartPattern();
    void setRestartPattern(String value);
    
    // <property-editor>AttachDebuggerEarly</property-editor>
    
    
    boolean PROP_ATTACH_DEBUGGER_DEFAULT = false;
    @Type(base = Boolean.class)
    @DefaultValue(text = PROP_ATTACH_DEBUGGER_DEFAULT + "")
    ValueProperty PROP_ATTACH_DEBUGGER_EARLY = new ValueProperty(TYPE, "AttachDebuggerEarly");
    Value<Boolean> getAttachDebuggerEarly();
    void setAttachDebuggerEarly(Boolean value);
}
