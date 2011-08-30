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

import echopointng.tree.MutableTreeNode;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Tree builder.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface TreeBuilder<T extends IMObject> {

    /**
     * Start a new tree.
     *
     * @param sort node sort criteria. May be <code>null</code>
     */
    void create(SortConstraint[] sort);

    /**
     * Adds a node into the tree.
     *
     * @param object the object to add
     */
    void add(T object);

    /**
     * Returns the created tree.
     *
     * @return the created tree
     */
    MutableTreeNode getTree();

}
