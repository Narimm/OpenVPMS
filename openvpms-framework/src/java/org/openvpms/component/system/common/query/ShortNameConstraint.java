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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.system.common.query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static org.openvpms.component.system.common.query.ArchetypeQueryException.ErrorCode.MustSpecifyAtLeastOneShortName;


/**
 * A constraint on one or more archetype short names. The short names can be
 * complete short names or short names with wildcard characters.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class ShortNameConstraint extends BaseArchetypeConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The list of short names.
     */
    private String[] shortNames;


    /**
     * Creates an instance of this constraint with the specified short name
     * for both primary/non-primary and active/inactive instances.
     *
     * @param shortName the short name
     */
    public ShortNameConstraint(String shortName) {
        this(null, shortName);
    }

    /**
     * Creates an instance of this constraint with the specified short name
     * for both primary/non-primary and active/inactive instances.
     *
     * @param alias     the type alias. May be {@code null}
     * @param shortName the short name
     */
    public ShortNameConstraint(String alias, String shortName) {
        this(alias, shortName, false);
    }

    /**
     * Creates an instance of the constraint with the specified short name
     * for primary and non-primary instances.
     *
     * @param shortName  the short name
     * @param activeOnly if {@code true} only deal with active entities
     */
    public ShortNameConstraint(String shortName, boolean activeOnly) {
        this(null, shortName, activeOnly);
    }

    /**
     * Creates an instance of the constraint with the specified short name
     * for primary and non-primary instances.
     *
     * @param shortName the short name
     * @param state     determines if active and/or inactive instances are returned
     */
    public ShortNameConstraint(String shortName, State state) {
        this(null, shortName, state);
    }

    /**
     * Creates an instance of the constraint with the specified short name
     * for primary and non-primary instances.
     *
     * @param alias      the type alias. May be {@code null}
     * @param shortName  the short name
     * @param activeOnly if {@code true} only deal with active entities
     */
    public ShortNameConstraint(String alias, String shortName, boolean activeOnly) {
        this(alias, shortName, false, activeOnly);
    }

    /**
     * Creates an instance of the constraint with the specified short name
     * for primary and non-primary instances.
     *
     * @param alias     the type alias. May be {@code null}
     * @param shortName the short name
     * @param state     determines if active and/or inactive instances are returned
     */
    public ShortNameConstraint(String alias, String shortName, State state) {
        this(alias, shortName, false, state);
    }

    /**
     * Create an instance of this constraint with the specified short name.
     *
     * @param shortName   the short name
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param activeOnly  if {@code true} only deal with active entities
     */
    public ShortNameConstraint(String shortName, boolean primaryOnly, boolean activeOnly) {
        this(null, shortName, primaryOnly, activeOnly);
    }

    /**
     * Create an instance of this constraint with the specified short name.
     *
     * @param shortName   the short name
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param state       determines if active and/or inactive instances are returned
     */
    public ShortNameConstraint(String shortName, boolean primaryOnly, State state) {
        this(null, shortName, primaryOnly, state);
    }

    /**
     * Create an instance of this constraint with the specified short name.
     *
     * @param alias       the type alias. May be {@code null}
     * @param shortName   the short name
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param activeOnly  if {@code true} only deal with active entities
     */
    public ShortNameConstraint(String alias, String shortName, boolean primaryOnly, boolean activeOnly) {
        super(alias, primaryOnly, activeOnly);

        if (StringUtils.isEmpty(shortName)) {
            throw new ArchetypeQueryException(ArchetypeQueryException.ErrorCode.NoShortNameSpecified);
        }
        this.shortNames = new String[]{shortName};
    }

    /**
     * Create an instance of this constraint with the specified short name.
     *
     * @param alias       the type alias. May be {@code null}
     * @param shortName   the short name
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param state       determines if active and/or inactive instances are returned
     */
    public ShortNameConstraint(String alias, String shortName, boolean primaryOnly, State state) {
        super(alias, primaryOnly, state);

        if (StringUtils.isEmpty(shortName)) {
            throw new ArchetypeQueryException(ArchetypeQueryException.ErrorCode.NoShortNameSpecified);
        }
        this.shortNames = new String[]{shortName};
    }

    /**
     * Create an instance of this class with the specified archetype short names.
     *
     * @param shortNames  an array of archetype short names
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param activeOnly  if {@code true} only deal with active entities
     */
    public ShortNameConstraint(String[] shortNames, boolean primaryOnly, boolean activeOnly) {
        this(null, shortNames, primaryOnly, activeOnly);
    }

    /**
     * Create an instance of this class with the specified archetype short names.
     *
     * @param shortNames  an array of archetype short names
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param state       determines if active and/or inactive instances are returned
     */
    public ShortNameConstraint(String[] shortNames, boolean primaryOnly, State state) {
        this(null, shortNames, primaryOnly, state);
    }

    /**
     * Creates an instance of this class with the specified archetype short names.
     *
     * @param alias       the type alias. May be {@code null}
     * @param shortNames  an array of archetype short names
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param activeOnly  if {@code true} only deal with active entities
     */
    public ShortNameConstraint(String alias, String[] shortNames, boolean primaryOnly, boolean activeOnly) {
        super(alias, primaryOnly, activeOnly);

        if (shortNames == null || shortNames.length == 0) {
            throw new ArchetypeQueryException(MustSpecifyAtLeastOneShortName);
        }
        this.shortNames = shortNames;
    }

    /**
     * Creates an instance of this class with the specified archetype short names.
     *
     * @param alias       the type alias. May be {@code null}
     * @param shortNames  an array of archetype short names
     * @param primaryOnly if {@code true} only deal with primary archetypes
     * @param state       determines if active and/or inactive instances are returned
     */
    public ShortNameConstraint(String alias, String[] shortNames, boolean primaryOnly, State state) {
        super(alias, primaryOnly, state);

        if (shortNames == null || shortNames.length == 0) {
            throw new ArchetypeQueryException(MustSpecifyAtLeastOneShortName);
        }
        this.shortNames = shortNames;
    }

    /**
     * Returns the archetype short names.
     *
     * @return the short names
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * Sets the archetype short names.
     *
     * @param shortNames the short names
     */
    public void setShortNames(String[] shortNames) {
        this.shortNames = shortNames;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ShortNameConstraint)) {
            return false;
        }

        ShortNameConstraint rhs = (ShortNameConstraint) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(rhs))
                .append(shortNames, rhs.shortNames)
                .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("shortNames", shortNames)
                .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ShortNameConstraint copy = (ShortNameConstraint) super.clone();
        copy.shortNames = new String[this.shortNames.length];
        System.arraycopy(this.shortNames, 0, copy.shortNames, 0,
                         this.shortNames.length);
        return copy;
    }
}
