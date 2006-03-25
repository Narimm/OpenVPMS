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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.system.common.search;

import java.io.Serializable;

/**
 * This is used to specify a sort criteria for a query. The sort criteria
 * is composed of the node descriptor name of the attribute that the sort
 * will execute against and a sort direction ascending or descending
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class SortCriteria implements Serializable {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The node that the sort will execute on
     */
    private String sortNode;
    
    /**
     * Whether the sort direction is ascending
     */
    private boolean ascending;

    /**
     * Construct an instance of this constuctor specifying a node and a sort
     * direction
     * 
     * @param node
     *            the node to sort on
     * @param ascending
     *            indicates that the sort or is ascending
     */
    public SortCriteria(String node, boolean ascending) {
        this.sortNode = node;
        this.ascending = ascending;
    }

    /**
     * @return Returns the ascending.
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * @return Returns the sortNode.
     */
    public String getSortNode() {
        return sortNode;
    }
}
