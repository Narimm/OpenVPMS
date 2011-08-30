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

import org.openvpms.web.resource.util.Messages;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.SortConstraint;

import echopointng.tree.MutableTreeNode;


/**
 * Abstract implmentation of the {@link TreeBuilder} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractTreeBuilder<T extends IMObject>
        implements TreeBuilder<T> {

    /**
     * The root node.
     */
    private IMObjectTreeNode<T> _root;

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
        _root = new IMObjectTreeNode<T>(null, sort);
        _sort = sort;
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
     * Creates a new node.
     *
     * @param object the node object. May be <code>null</code>
     * @param leaf   if <code>true</code> the node is a leaf node
     * @return a new node
     */
    protected IMObjectTreeNode<T> create(T object, boolean leaf) {
        IMObjectTreeNode<T> node = new IMObjectTreeNode<T>(object, _sort);
        if (leaf) {
            String type = DescriptorHelper.getDisplayName(object);
            String description = object.getDescription();
            String userObject
                    = Messages.get("tree.imobject.leaf", type, description);
            node.setUserObject(userObject);
        }
        return node;
    }

    /**
     * Returns the root node.
     *
     * @return the root node
     */
    protected IMObjectTreeNode<T> getRoot() {
        return _root;
    }

}


