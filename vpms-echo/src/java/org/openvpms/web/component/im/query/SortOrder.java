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


/**
 * Enter description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SortOrder {

    /**
     * The sort node.
     */
    private final String _node;

    /**
     * if <code>true</code> sort the node in ascending order; otherwise sort it
     * in <code>descebding</code> order
     */
    private final boolean _ascending;

    /**
     * Construct a new <code>SortOrder</code>.
     *
     * @param node      the node to sort on
     * @param ascending if <code>true</code> sort the node in ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    public SortOrder(String node, boolean ascending) {
        _node = node;
        _ascending = ascending;
    }

    /**
     * Returns the sort node.
     *
     * @return the sort node
     */
    public String getNode() {
        return _node;
    }

    /**
     * Determines if the node is sorted in ascending order.
     *
     * @return <code>true</code> if the node should be sorted in ascending
     *         order; <code>false</code> if it should be sorted in
     *         <code>descebding</code> order
     */
    public boolean isAscending() {
        return _ascending;
    }
}
