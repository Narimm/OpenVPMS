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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.util;

import org.apache.commons.collections.Transformer;
import org.openvpms.component.system.common.query.NodeSortConstraint;


/**
 * A sort constraint used to indicate that query results should be sorted in memory instead of via the archetype
 * service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see IMObjectSorter
 */
public class VirtualNodeSortConstraint extends NodeSortConstraint {

    /**
     * The transformer to get the node value. May be <tt>null</tt>
     */
    private Transformer transformer;

    /**
     * Constructs a <tt>VirtualNodeSortConstraint</tt>.
     *
     * @param nodeName  the name of the node to sort on
     * @param ascending determines whether to sort in ascending or descending order
     */
    public VirtualNodeSortConstraint(String nodeName, boolean ascending) {
        this(nodeName, ascending, null);
    }

    /**
     * Constructs a <tt>VirtualNodeSortConstraint</tt>.
     *
     * @param nodeName    the name of the node to sort on
     * @param ascending   determines whether to sort in ascending or descending order
     * @param transformer a transformer to get the  node value, or <tt>null</tt> if no transformation is required
     */
    public VirtualNodeSortConstraint(String nodeName, boolean ascending, Transformer transformer) {
        super(nodeName, ascending);
        this.transformer = transformer;
    }

    /**
     * Returns the transformer to get the node value.
     *
     * @return the transformer, or <tt>null</tt> if no transformer is required
     */
    public Transformer getTransformer() {
        return transformer;
    }

}
