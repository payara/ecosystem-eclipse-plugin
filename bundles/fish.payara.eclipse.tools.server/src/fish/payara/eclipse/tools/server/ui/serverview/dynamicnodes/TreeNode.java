/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2022 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.ui.serverview.dynamicnodes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

public class TreeNode implements IPropertySource2 {

    private TreeNode parent;
    private String name;

    protected String type;
    protected List<TreeNode> childModules = new ArrayList<>();

    public TreeNode(String name, String type) {
        this(name, type, null);
    }

    /*
     *
     * type is ear, war, ejb etc
     *
     */
    public TreeNode(String name, String type, TreeNode parent) {
        this.parent = parent;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void addChild(TreeNode childModule) {
        childModules.add(childModule);
    }

    public Object[] getChildren() {
        return childModules.toArray();
    }

    public TreeNode getParent() {
        return parent;
    }

    @Override
    public Object getEditableValue() {
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        ArrayList<IPropertyDescriptor> properties = new ArrayList<>();
        return properties.toArray(new IPropertyDescriptor[0]);
    }

    @Override
    public Object getPropertyValue(Object id) {
        return null;
    }

    @Override
    public void resetPropertyValue(Object arg0) {

    }

    @Override
    public void setPropertyValue(Object arg0, Object arg1) {
    }

    @Override
    public boolean isPropertyResettable(Object arg0) {
        return false;
    }

    @Override
    public boolean isPropertySet(Object arg0) {
        return false;
    }

}
