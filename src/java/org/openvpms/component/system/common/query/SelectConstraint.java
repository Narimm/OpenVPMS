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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.query;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * The base class for all select constraints. Select constraints determine
 * what is returned by a query.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public abstract class SelectConstraint implements IConstraint {

    /**
     * The type alias.
     */
    private final String alias;

    /**
     * The node name.
     */
    private final String nodeName;


    /**
     * Constructs a new <code>SelectConstraint</code>.
     *
     * @param alias    the type alias. May be <code>null</code>
     * @param nodeName the node name. May be <code>null</code>
     */
    protected SelectConstraint(String alias, String nodeName) {
        this.alias = alias;
        this.nodeName = nodeName;
    }

    /**
     * Returns the type alias.
     *
     * @return the type alias. May be <code>null</code>
     */
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

    /**
     * Returns the qualified name.
     *
     * @return the qualified name
     */
    public abstract String getName();

    /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof SelectConstraint)) {
            return false;
        }

        SelectConstraint rhs = (SelectConstraint) obj;
        return new EqualsBuilder()
                .append(alias, rhs.alias)
                .append(nodeName, rhs.nodeName)
                .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("alias", alias)
                .append("nodeName", nodeName)
                .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
