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

package org.openvpms.web.component.im.tree;

import echopointng.tree.DefaultMutableTreeNode;
import echopointng.tree.MutableTreeNode;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.Set;


/**
 * Tree builder for acts. Acts are added to the root node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActTreeBuilder implements TreeBuilder<Act> {

    /**
     * The root node.
     */
    private IMObjectTreeNode<Act> _root;

    /**
     * Node sort criteria. May be <code>null</code>.
     */
    private SortConstraint[] _sort;


    /**
     * Start a new tree.
     *
     * @param sort node sort criteria. May be <code>null</code>
     */
    public void create(SortConstraint[] sort) {
        _root = new IMObjectTreeNode<Act>(null, sort);
        _sort = sort;
    }

    /**
     * Adds a node into the tree.
     *
     * @param object the object to add
     */
    public void add(Act object) {
        addTopDown(object, _root);
    }

    /**
     * Returns the created tree.
     *
     * @return the created tree
     */
    public MutableTreeNode getTree() {
        MutableTreeNode result = _root;
        _root = null;
        return result;
    }

    /**
     * Adds an act to the root, creating nodes for children.
     *
     * @param act  the act
     * @param root the root node
     */
    protected void addTopDown(Act act, DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new IMObjectTreeNode<Act>(act, _sort);
        root.setAllowsChildren(true);
        root.add(node);
        Set<ActRelationship> acts = act.getSourceActRelationships();
        if (!acts.isEmpty()) {
            for (ActRelationship relationship : acts) {
                Act child = (Act) IMObjectHelper.getObject(
                        relationship.getTarget());
                if (child != null) {
                    addTopDown(child, node);
                }
            }
        }
    }

}
