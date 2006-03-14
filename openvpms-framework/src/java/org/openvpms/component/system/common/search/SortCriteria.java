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
     * An enumerator for the sort direction
     */
    public enum SortDirection {
        Ascending,
        Descending
    }
    
    /**
     * The node that the sort will execute on
     */
    private String sortNode;
    
    /**
     * The direction of the sort
     */
    private SortDirection sortDirection;

    /**
     * Construct an instance of this constuctor specifying a node and a sort
     * direction
     * 
     * @param node
     *            the node to sort on
     * @param direction
     *            the direction of the sort
     */
    public SortCriteria(String node, SortDirection direction) {
        this.sortNode = node;
        this.sortDirection = direction;
    }

    /**
     * @return Returns the sortDirection.
     */
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    /**
     * @return Returns the sortNode.
     */
    public String getSortNode() {
        return sortNode;
    }
}
