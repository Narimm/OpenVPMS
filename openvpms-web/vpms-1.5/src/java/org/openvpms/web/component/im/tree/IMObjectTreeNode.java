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
import org.apache.commons.collections.Transformer;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.IMObjectSorter;


/**
 * Tree node associated with an {@link org.openvpms.component.business.domain.im.common.IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectTreeNode<T extends IMObject>
        extends DefaultMutableTreeNode {

    /**
     * Transformer, used to order nodes.
     */
    private static final Transformer _transformer = new TreeNodeTransfomer();

    /**
     * The object associated with the node.
     */
    private final T _object;

    /**
     * Node sort criteria.
     */
    private final SortConstraint[] _sort;

    /**
     * Construct a new <code>IMObjectTreeNode</code>.
     */
    public IMObjectTreeNode() {
        this(null);
    }

    /**
     * Construct a new <code>IMObjectTreeNode</code>.
     *
     * @param object the object to associate with the node.
     *               May be <code>null</code>
     */
    public IMObjectTreeNode(T object) {
        this(object, null);
    }

    /**
     * Construct a new <code>IMObjectTreeNode</code>.
     *
     * @param object the object to associate with the node. May be
     *               <code>null</code>
     * @param sort   node sort criteria. May be <code>null</code>
     */
    public IMObjectTreeNode(T object, SortConstraint[] sort) {
        _object = object;
        _sort = sort;
        if (_object != null) {
            setUserObject(object.getDescription());
        }
    }

    /**
     * Returns the associated object.
     *
     * @return the object. May be <code>null</code>
     */
    public T getObject() {
        return _object;
    }

    /**
     * Adds a new child node, ordering the nodes if required.
     *
     * @throws IllegalArgumentException if <code>newChild</code>
     *                                  is null
     * @throws IllegalStateException    if this node does not allow
     *                                  children
     */
    @Override
    public void add(MutableTreeNode newChild) {
        super.add(newChild);
        if (_sort != null) {
            IMObjectSorter.sort(children, _sort, _transformer);
        }
    }

    private static class TreeNodeTransfomer implements Transformer {

        /**
         * Transforms the input object (leaving it unchanged) into some output
         * object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         */
        public Object transform(Object input) {
            if (input instanceof IMObjectTreeNode) {
                return ((IMObjectTreeNode) input).getObject();
            }
            return null;
        }
    }

}
