/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.internal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "library")
public class Library {

  private String path;
  private String javadoc;
  private String source;

  @XmlAttribute( name = "path")
  public String getPath() {
    return path;
  }

  public void setPath(String name) {
    this.path = name;
  }

  @XmlAttribute( name = "javadoc")
  public String getJavadoc() {
    return javadoc;
  }

  public void setJavadoc(String doc) {
    this.javadoc = doc;
  }

  @XmlAttribute( name = "source")
  public String getSource() {
    return source;
  }

  public void setSource(String src) {
    this.source = src;
  }

} 