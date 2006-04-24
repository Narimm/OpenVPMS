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

// commons-lang
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The use can specify one or more archetype short names as 
 * the constraint. The short names can be complete short names or
 * short names with wildcard caharacters
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeShortNameConstraint extends BaseArchetypeConstraint {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L; 

    /**
     * The list of short names
     */
    private String[] shortNames;
    
    /**
     * Create an instance of this constraint with the specified short name
     * 
     * @param shortName
     *            the short name
     * @param primaryOnly 
     *            only deal with primary archetypes
     * @param activeOnly
     *            only deal with active entities                        
     */
    public ArchetypeShortNameConstraint(String shortName, boolean primaryOnly,
            boolean activeOnly) {
        super(primaryOnly, activeOnly);
        
        if (StringUtils.isEmpty(shortName)) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.NoShortNameSpecified);
        }
        this.shortNames = new String[] {shortName};
    }
    
    /**
     * Create an instance of this class with the specified archetype
     * short names
     * 
     * @param shortNames
     *            an array of archetype short names
     * @param primaryOnly 
     *            only deal with primary archetypes            
     * @param activeOnly
     *            only deal with active entities                        
     */
    public ArchetypeShortNameConstraint(String[] shortNames, boolean primaryOnly,
            boolean activeOnly) {
        super(primaryOnly, activeOnly);
        
        if ((shortNames == null) ||
            (shortNames.length == 0)) {
            throw new ArchetypeQueryException(
                    ArchetypeQueryException.ErrorCode.MustSpecifyAtLeastOneShortName);
        }
        this.shortNames = shortNames;
    }

    /**
     * @return Returns the shortNames.
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * @param shortNames The shortNames to set.
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

        if (!(obj instanceof ArchetypeShortNameConstraint)) {
            return false;
        }
        
        ArchetypeShortNameConstraint rhs = (ArchetypeShortNameConstraint) obj;
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
        ArchetypeShortNameConstraint copy = (ArchetypeShortNameConstraint)super.clone();
        copy.shortNames = new String[this.shortNames.length];
        System.arraycopy(this.shortNames, 0, copy.shortNames, 0, this.shortNames.length);
        
        return copy;
    }
}
