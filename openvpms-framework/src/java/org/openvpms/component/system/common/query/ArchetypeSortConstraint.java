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

/**
 * This class is used to specify a sort constraint on the archetype short name.
 *
 * @author Tim Anderson
 */
public class ArchetypeSortConstraint extends SortConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 3L;

    /**
     * The type alias.
     */
    private final String alias;

    /**
     * Construct an instance of this class with the specified parameters.
     *
     * @param ascending whether to sort in ascending or descending order
     */
    public ArchetypeSortConstraint(boolean ascending) {
        this(null, ascending);
    }

    /**
     * Construct an instance of this class with the specified parameters.
     *
     * @param alias     the type alias. May be {@code null}
     * @param ascending whether to sort in ascending or descending order
     */
    public ArchetypeSortConstraint(String alias, boolean ascending) {
        super(ascending);
        this.alias = alias;
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

    /* (non-Javadoc)
         * @see org.openvpms.component.system.common.query.SortConstraint#equals(java.lang.Object)
         */
    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof ArchetypeSortConstraint && super.equals(obj));
    }

}
