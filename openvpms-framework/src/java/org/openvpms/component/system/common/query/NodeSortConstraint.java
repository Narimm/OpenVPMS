/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.system.common.query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * This is a sort constraint placed on a node.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class NodeSortConstraint extends SortConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The type alias. May be {@code null}.
     */
    private final String alias;

    /**
     * The name of the node.
     */
    private final String nodeName;


    /**
     * Constructs a {@link NodeSortConstraint} specifying a node name sorted in ascending order.
     *
     * @param nodeName the name of the node to sort on, optionally prefixed by the type alias
     */
    public NodeSortConstraint(String nodeName) {
        this(nodeName, true);
    }

    /**
     * Constructs a {@link NodeSortConstraint} specifying a node name and whether it is ascending or not.
     *
     * @param nodeName the name of the node to sort on, optionally prefixed by the type alias
     * @param ascending whether to sort in ascending or descending order
     */
    public NodeSortConstraint(String nodeName, boolean ascending) {
        this(null, nodeName, ascending);
    }

    /**
     * Constructs a {@link NodeSortConstraint} constraint specifying a node name sorted in ascending order.
     *
     * @param alias    the type alias
     * @param nodeName the name of the node to sort on
     */
    public NodeSortConstraint(String alias, String nodeName) {
        this(alias, nodeName, true);
    }

    /**
     * Constructs a {@link NodeSortConstraint} constraint specifying a node name and whether it is ascending or not.
     *
     * @param alias     the type alias
     * @param nodeName  the name of the node to sort on
     * @param ascending whether to sort in ascending or descending order
     */
    public NodeSortConstraint(String alias, String nodeName, boolean ascending) {
        super(ascending);
        if (StringUtils.isEmpty(nodeName)) {
            throw new ArchetypeQueryException(ArchetypeQueryException.ErrorCode.MustSpecifyNodeName);
        }
        if (alias == null) {
            int index = nodeName.indexOf(".");
            if (index != -1) {
                this.alias = nodeName.substring(0, index);
                this.nodeName = nodeName.substring(index + 1);
            } else {
                this.alias = null;
                this.nodeName = nodeName;
            }
        } else {
            this.alias = alias;
            this.nodeName = nodeName;
        }
    }

    /**
     * Returns the type alias.
     *
     * @return the type alias. May be {@code null}
     */
    @Override
    public String getAlias() {
        return alias;
    }

    /**
     * Returns the node name.
     *
     * @return the node name
     */
    public String getNodeName() {
        return nodeName;
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("nodeName", nodeName)
                .toString();
    }
}
