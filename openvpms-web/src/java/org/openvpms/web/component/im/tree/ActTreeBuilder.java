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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.Set;


/**
 * Tree builder for acts. Acts are added to the root node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActTreeBuilder extends AbstractTreeBuilder<Act> {


    /**
     * Adds a node into the tree.
     *
     * @param object the object to add
     */
    public void add(Act object) {
        addTopDown(object, getRoot());
    }

    /**
     * Adds an act to the root, creating nodes for children.
     *
     * @param act  the act
     * @param root the root node
     */
    protected void addTopDown(Act act, DefaultMutableTreeNode root) {
        root.setAllowsChildren(true);
        Set<ActRelationship> acts = act.getSourceActRelationships();
        boolean leaf = acts.isEmpty();
        IMObjectTreeNode node = create(act, leaf);
        root.add(node);
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
