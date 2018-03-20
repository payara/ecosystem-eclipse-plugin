/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.sapphire;

import org.eclipse.glassfish.tools.internal.CreateGlassfishDomainOpMethods;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ExecutableElement;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;

public interface ICreateGlassfishDomainOp extends ExecutableElement {
	
	ElementType TYPE = new ElementType(ICreateGlassfishDomainOp.class);
	
	@Label(standard = "&Name")
	@Service(impl = DomainNameValidationService.class)
	@Required
	ValueProperty PROP_NAME = new ValueProperty(TYPE, "Name");
	Value<String> getName();

	void setName(String name);

	@Label(standard = "Java location used to run GlassFish asadmin")
	ValueProperty PROP_JAVA_LOCATION = new ValueProperty(TYPE, "JavaLocation");

	Value<Path> getJavaLocation();
	void setJavaLocation(String location);

	
	@Type(base = Path.class)
	@AbsolutePath
	@MustExist
	@ValidFileSystemResourceType(FileSystemResourceType.FOLDER)
	@Required
	ValueProperty PROP_LOCATION = new ValueProperty(TYPE, "Location");

	Value<Path> getLocation();
	void setLocation(String location);
	void setLocation(Path location);

	
	@Label(standard = "&Domain directory")
	@Type(base = Path.class)
	@AbsolutePath
	@MustExist
	@DefaultValue(text = "${Location}/domains")
	@ValidFileSystemResourceType(FileSystemResourceType.FOLDER)
	@Required
	ValueProperty PROP_DOMAIN_DIR = new ValueProperty(TYPE, "DomainDir");

	Value<Path> getDomainDir();
	void setDomainDir(String location);
	void setDomainDir(Path location);

	
	@Label(standard = "&Portbase")
	@Type(base = Integer.class)
	@Documentation(content = "Determines the number with which port assignments"
			+ "should start. A domain uses a certain number of ports "
			+ "that are statically assigned. The portbase value determines "
			+ "where the assignment should start. The values for" + "the ports are calculated as follows:\n"
			+ "Administration port: portbase + 48;\n" + "HTTP listener port: portbase + 80;\n"
			+ "HTTPS listener port: portbase + 81;\n" + "JMS port: portbase + 76;\n"
			+ "IIOP listener port: portbase + 37;\n" + "Secure IIOP listener port: portbase + 38;\n"
			+ "Secure IIOP with mutual authentication port: portbase + 39;\n" + "JMX port: portbase + 86;\n"
			+ "JPDA debugger port: portbase + 9;\n"
			+ "Felix shell service port for OSGi module  management: portbase + 66")
	@Required
	@NumericRange(min = "1025", max = "65535")
	@DefaultValue(text = "8000")
	ValueProperty PROP_PORT_BASE = new ValueProperty(TYPE, "PortBase");

	Value<Integer> getPortBase();
	void setPortBase(String val);
	void setPortBase(int val);

	
	// *** Method: execute ***
	@DelegateImplementation(CreateGlassfishDomainOpMethods.class)
	Status execute(ProgressMonitor monitor);

}
