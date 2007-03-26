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
 * Represents an object node, linked to an optional parent and child node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see NodeParser
 */
public class Node {

    /**
     * The archetype short name.
     */
    private final String archetype;

    /**
     * The node name.
     */
    private final String name;

    /**
     * The collection index, or <tt>-1</tt> if the node isn't a collection
     */
    private final int index;

    /**
     * The parent node. May be <tt>null</tt>
     */
    private Node parent;

    /**
     * The child node. May be <tt>null</tt>
     */
    private Node child;

    /**
     * Constructs a new <tt>Node</tt>.
     *
     * @param archetype the archetype short name
     * @param name      the node name
     * @param index     the collection index, or <tt>-1</tt> if this is not a
     *                  collection node
     */
    public Node(String archetype, String name, int index) {
        this.archetype = archetype;
        this.name = name;
        this.index = index;
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name
     */
    public String getArchetype() {
        return archetype;
    }

    /**
     * Returns the node name.
     *
     * @return the node name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the collection index.
     *
     * @return the collection index or <tt>-1</tt> if this is not a collection
     *         node
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the object path.
     * This is the concatenation of the parent node path, the parent
     * collection index (if not <tt>-1</tt>) and the archetype.
     *
     * @return the object path
     */
    public String getObjectPath() {
        String result = "<" + archetype + ">";
        if (parent != null) {
            result = parent.getNodePath() + result;
            if (parent.getIndex() != -1) {
                result += "[" + parent.getIndex() + "]";
            }
        }
        return result;
    }

    /**
     * Returns the node path.
     * This is the concatention of the {@link #getObjectPath()} with
     * the node name.
     *
     * @return the node path
     */
    public String getNodePath() {
        String result = getObjectPath();
        result += name;
        return result;
    }

    /**
     * Returns the child node.
     *
     * @return the child node, or <tt>null</tt> if none is present
     */
    public Node getChild() {
        return child;
    }

    /**
     * Sets the child node.
     *
     * @param child the child node. May be <tt>null</tt>
     */
    public void setChild(Node child) {
        this.child = child;
        child.setParent(this);
    }

    /**
     * Returns the parent node.
     *
     * @return the parent node, or <tt>null</tt> if none is present
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the parent node.
     *
     * @param parent the parent node. May be <tt>null</tt>
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

}
