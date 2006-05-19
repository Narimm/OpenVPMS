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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.Set;


/**
 * Enter description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActTreeNodeFactory implements IMObjectTreeNodeFactory {

    /**
     * Creates a new tree node for an object.
     *
     * @param object the object
     * @return a new tree node representing <code>object</code>
     */
    public MutableTreeNode create(IMObject object) {
        DefaultMutableTreeNode node = new IMObjectTreeNode(object);
        if (object instanceof Act) {
            Act act = (Act) object;
            Set<ActRelationship> acts = act.getSourceActRelationships();
            if (!acts.isEmpty()) {
                node.setAllowsChildren(true);
                for (ActRelationship relationship : acts) {
                    IMObject child = IMObjectHelper.getObject(
                            relationship.getTarget());
                    if (child != null) {
                        node.add(create(child));
                    }
                }
            }
        }

        return node;
    }
}
