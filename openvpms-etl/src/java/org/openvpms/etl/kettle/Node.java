/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.kettle;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Node {

    private final String archetype;
    private final String name;
    private final int index;
    private Node parent;
    private Node child;

    public Node(String archetype, String name, int index) {
        this.archetype = archetype;
        this.name = name;
        this.index = index;
    }

    public String getArchetype() {
        return archetype;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public String getParentPath() {
        String result = getParentKey();
        if (parent != null) {
            result = parent.getKey() + result;
        }
        return result;
    }

    public String getParentKey() {
        return "<" + archetype + ">";
    }

    public String getPath() {
        String result = getKey();
        if (parent != null) {
            result = parent.getPath() + result;
        }
        return result;
    }

    private String getKey() {
        String result = getParentKey();
        if (index != -1) {
            result = result + name + "[" + index + "]";
        }
        return result;
    }

    public String getObjectPath() {
        return getParentPath();
    }

    public String getObjectKey() {
        String result = getKey();
        if (child != null) {
            result += child.getObjectKey();
        }
        return result;
    }

    public Node getChild() {
        return child;
    }

    public void setChild(Node child) {
        this.child = child;
        child.setParent(this);
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getLeaf() {
        if (child != null) {
            return child.getLeaf();
        }
        return this;
    }

}
