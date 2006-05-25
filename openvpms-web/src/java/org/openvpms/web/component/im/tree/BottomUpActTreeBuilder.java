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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.tree;

import echopointng.tree.DefaultMutableTreeNode;
import echopointng.tree.MutableTreeNode;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Tree builder for acts. The supplied acts are treated as leaf nodes,
 * with the tree being built bottom up.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-19 07:20:38Z $
 */
public class BottomUpActTreeBuilder implements TreeBuilder<Act> {

    /**
     * The root node.
     */
    private DefaultMutableTreeNode _root;

    /**
     * The set of known acts, and their corresponding nodes.
     */
    private Map<IMObjectReference, IMObjectTreeNode> _acts;

    /**
     * Start a new tree.
     */
    public void create() {
        _root = new DefaultMutableTreeNode();
        _acts = new HashMap<IMObjectReference, IMObjectTreeNode>();
    }

    /**
     * Adds a node into the tree.
     *
     * @param object the object to add
     */
    public void add(Act object) {
        addBottomUp(object, _root);
    }

    /**
     * Returns the created tree.
     *
     * @return the created tree
     */
    public MutableTreeNode getTree() {
        MutableTreeNode result = _root;
        _root = null;
        _acts.clear();
        return result;
    }

    /**
     * Adds a leaf node, building up the parents if they don't exist.
     *
     * @param act  the act
     * @param root the root node
     * @return the new node
     */
    protected DefaultMutableTreeNode addBottomUp(Act act,
                                                 DefaultMutableTreeNode root) {
        DefaultMutableTreeNode parentNode = null;
        Set<ActRelationship> relationships = act.getTargetActRelationships();
        if (!relationships.isEmpty()) {
            ActRelationship[] acts = relationships.toArray(
                    new ActRelationship[0]);
            for (ActRelationship relationship : acts) {
                IMObjectReference source = relationship.getSource();
                parentNode = _acts.get(source);
                if (parentNode != null) {
                    break;
                }
            }
            if (parentNode == null) {
                ActRelationship relationship = acts[0];
                Act parent = (Act) IMObjectHelper.getObject(
                        relationship.getSource());
                if (parent == null) {
                    parentNode = root;
                } else {
                    parentNode = addBottomUp(parent, root);
                }
            }
        }
        if (parentNode == null) {
            parentNode = root;
        }
        IMObjectTreeNode<Act> child = new IMObjectTreeNode<Act>(act);
        parentNode.setAllowsChildren(true);
        parentNode.add(child);
        _acts.put(act.getObjectReference(), child);
        return child;
    }

}
