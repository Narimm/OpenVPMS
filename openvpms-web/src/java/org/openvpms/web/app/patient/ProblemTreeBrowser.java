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
package org.openvpms.web.app.patient;

import echopointng.tree.DefaultMutableTreeNode;
import echopointng.tree.MutableTreeNode;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractTreeBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.tree.ActTreeNodeFactory;
import org.openvpms.web.component.im.tree.IMObjectTreeNodeFactory;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProblemTreeBrowser extends AbstractTreeBrowser<Act> {

    /**
     * Tree node factory.
     */
    private static final IMObjectTreeNodeFactory FACTORY
            = new ActTreeNodeFactory();

    /**
     * Construct a new <code>ProblemTreeBrowser</code> that queries IMObjects
     * using the * specified query.
     *
     * @param query the query
     * @param sort  the sort criteria. May be <code>null</code>
     */
    public ProblemTreeBrowser(Query<Act> query, SortConstraint[] sort) {
        super(query, sort);
    }

    /**
     * Creates a tree from a result set.
     *
     * @param set the result set
     * @return the root of the tree
     */
    protected MutableTreeNode createTree(ResultSet<Act> set) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);

        while (set.hasNext()) {
            IPage<Act> page = set.next();
            for (IMObject object : page.getRows()) {
                create(root, object);
            }
        }
        return root;
    }

    /**
     * Creates a node if the object has an 'act.patientClinialProblem'
     */
    protected void create(DefaultMutableTreeNode root, IMObject object) {
        if (IMObjectHelper.isA(object, "act.patientClinicalProblem")) {
            MutableTreeNode child = FACTORY.create(object);
            root.add(child);
        } else if (object instanceof Act) {
            Act act = (Act) object;
            Set<ActRelationship> acts = act.getSourceActRelationships();
            if (!acts.isEmpty()) {
                for (ActRelationship relationship : acts) {
                    IMObject child = IMObjectHelper.getObject(
                            relationship.getTarget());
                    if (child != null) {
                        create(root, child);
                    }
                }
            }
        }
    }

}
