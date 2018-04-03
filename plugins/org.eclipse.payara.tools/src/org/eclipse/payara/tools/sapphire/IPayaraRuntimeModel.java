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

package org.eclipse.payara.tools.sapphire;

import static org.eclipse.sapphire.modeling.annotations.FileSystemResourceType.FOLDER;
import static org.eclipse.payara.tools.sapphire.PayaraServerConfigServices.UniqueRuntimeNameValidationService;
import static org.eclipse.payara.tools.sapphire.PayaraServerConfigServices.ServerLocationValidationService;
import static org.eclipse.payara.tools.sapphire.PayaraServerConfigServices.ServerLocationListener;

import static org.eclipse.payara.tools.sapphire.PayaraServerConfigServices.JdkValidationService;
import static org.eclipse.payara.tools.sapphire.PayaraServerConfigServices.JdkDefaultValueService;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Listeners;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

public interface IPayaraRuntimeModel extends Element {

    ElementType TYPE = new ElementType(IPayaraRuntimeModel.class);

    // *** Name ***

    @XmlBinding(path = "name")
    @Label(standard = "na&me")
    @Required
    @Service(impl = UniqueRuntimeNameValidationService.class)
    ValueProperty PROP_NAME = new ValueProperty(TYPE, "Name");
    Value<String> getName();
    void setName(String value);

    
    // *** ServerRoot ***

    @Type(base = Path.class)
    @MustExist
    @AbsolutePath
    @ValidFileSystemResourceType(FOLDER)
    @XmlBinding(path = "server-root")
    @Label(standard = "&Payara location")
    @Required
    @Service(impl = ServerLocationValidationService.class)
    @Listeners(ServerLocationListener.class)
    ValueProperty PROP_SERVER_ROOT = new ValueProperty(TYPE, "ServerRoot");
    Value<Path> getServerRoot();
    void setServerRoot(Path value);
    void setServerRoot(String value);

    
    // *** JavaRuntimeEnvironment ***

    @Type(base = Path.class)
    @MustExist
    @AbsolutePath
    @ValidFileSystemResourceType(FOLDER)
    @Label(standard = "&Java location")
    @Required
    @Service(impl = JdkValidationService.class)
    @Service(impl = JdkDefaultValueService.class)
    ValueProperty PROP_JAVA_RUNTIME_ENVIRONMENT = new ValueProperty(TYPE, "JavaRuntimeEnvironment");
    Value<Path> getJavaRuntimeEnvironment();
    void setJavaRuntimeEnvironment(Path value);
    void setJavaRuntimeEnvironment(String value);
}
