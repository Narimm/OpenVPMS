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


package org.openvpms.component.system.common.query;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This is a sort constraint placed on a node
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class NodeSortConstraint extends SortConstraint {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The name of the node
     */
    private String nodeName;
    
    /**
     * Construct an instance of this sort constraint specifying a node name 
     * and whether it is ascending or not.
     * 
     * @param nodeName
     *            the name of the node to sort on
     * @param ascending
     *            whether to sort in ascending order   
     */
    public NodeSortConstraint(String nodeName, boolean ascending) {
        super(ascending);
        this.nodeName = nodeName;
    }

    /**
     * @return Returns the nodeName.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @param nodeName The nodeName to set.
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.SortConstraint#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        NodeSortConstraint copy = (NodeSortConstraint)super.clone();
        copy.nodeName = this.nodeName;
        
        return copy;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.SortConstraint#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof NodeSortConstraint)) {
            return false;
        }
        
        NodeSortConstraint rhs = (NodeSortConstraint) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(obj))
            .append(nodeName, rhs.nodeName)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.query.SortConstraint#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("nodeName", nodeName)
            .toString();
    }
}
