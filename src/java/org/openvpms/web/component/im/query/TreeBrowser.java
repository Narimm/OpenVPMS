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

package org.openvpms.web.component.im.query;

import echopointng.tree.DefaultMutableTreeNode;
import echopointng.tree.MutableTreeNode;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.tree.IMObjectTreeNodeFactory;


/**
 * Browser that displays objects in a tree.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TreeBrowser<T extends IMObject> extends AbstractTreeBrowser<T> {

    /**
     * The tree node factory.
     */
    private final IMObjectTreeNodeFactory _factory;


    /**
     * Construct a new <code>TreeBrowser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query   the query
     * @param sort    the sort criteria. May be <code>null</code>
     * @param factory factory for tree nodes
     */
    public TreeBrowser(Query<T> query, SortConstraint[] sort,
                       IMObjectTreeNodeFactory factory) {
        super(query, sort);
        _factory = factory;
    }

    /**
     * Creates a tree from a result set.
     *
     * @param set the result set
     * @return the root of the tree
     */
    protected MutableTreeNode createTree(ResultSet<T> set) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);

        while (set.hasNext()) {
            IPage<T> page = set.next();
            for (IMObject object : page.getRows()) {
                MutableTreeNode child = _factory.create(object);
                root.add(child);
            }
        }
        return root;
    }

}
