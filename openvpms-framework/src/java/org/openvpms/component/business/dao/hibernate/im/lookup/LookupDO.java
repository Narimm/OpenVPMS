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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.HashSet;
import java.util.Set;


/**
 * A lookup represents a piece of static data that is used by
 * OpenVPMS. It can be used to represent a Species, a Breed, a Country,
 * a PostCode etc.
 * <p/>
 * A lookup has a <em>code</em>, <em>name</em> and <em>description</em>.
 * The <em>code</em> is mandatory, used to uniquely identify the lookup within
 * its domain. The other attributes are optional.
 * The convention for alphabetic codes are that they appear all in uppercase,
 * with words separated by an underscore.
 * E.g, CANINE, COMPLETED, IN_PROGRESS.
 * The <em>name</em> is used for display purposes. If not specified, it
 * is derived from <em>code</code>.
 * The <em>description</em> is used for display purposes, and defaults to
 * <code>null</code>.
 * <p/>
 * A lookup can have additional information stored in the details
 * attribute.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-05-02 14:28:50 +1000 (Wed, 02 May 2007) $
 */
public class LookupDO extends IMObjectDO {

    /**
     * The lookup code.
     */
    private String code;

    /**
     * Is this the default lookup for a particular domain.
     */
    private boolean defaultLookup;

    /**
     * The {@link LookupRelationshipDO}s that this lookup is a source of.
     */
    private Set<LookupRelationshipDO> sourceLookupRelationshipDOs =
            new HashSet<LookupRelationshipDO>();

    /**
     * The {@link LookupRelationshipDO}s that this lookup is a target of.
     */
    private Set<LookupRelationshipDO> targetLookupRelationshipDOs =
            new HashSet<LookupRelationshipDO>();


    /**
     * Default constructor.
     */
    public LookupDO() {
    }

    /**
     * Creates a new lookup.
     *
     * @param archetypeId the archetype id
     */
    public LookupDO(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Creates a new lookup.
     *
     * @param archetypeId the archetype id
     * @param code        the lookup code
     */
    public LookupDO(ArchetypeId archetypeId, String code) {
        super(archetypeId);
        setCode(code);
    }

    /**
     * Returns the lookup code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the lookup code.
     *
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Determines if this is the default lookup.
     *
     * @return <code>true</code> if this is the default lookup, otherwise
     *         <code>false</code>
     */
    public boolean isDefaultLookup() {
        return defaultLookup;
    }

    /**
     * Determines if this is the default lookup.
     *
     * @param defaultLookup if <code>true</code> this is the default lookup
     */
    public void setDefaultLookup(boolean defaultLookup) {
        this.defaultLookup = defaultLookup;
    }

    /**
     * Returns the the source lookup relationships.
     *
     * @return the source lookup relationships
     */
    public Set<LookupRelationshipDO> getSourceLookupRelationships() {
        return sourceLookupRelationshipDOs;
    }

    /**
     * Add a source {@link LookupRelationshipDO}.
     *
     * @param source the relationship to add
     */
    public void addSourceLookupRelationship(LookupRelationshipDO source) {
        sourceLookupRelationshipDOs.add(source);
        source.setSource(this);
    }

    /**
     * Remove a source {@link LookupRelationshipDO}.
     *
     * @param source the relationship to remove
     */
    public void removeSourceLookupRelationship(LookupRelationshipDO source) {
        sourceLookupRelationshipDOs.remove(source);
    }

    /**
     * Returns the target lookup relationships.
     *
     * @return the target lookup relationships
     */
    public Set<LookupRelationshipDO> getTargetLookupRelationships() {
        return targetLookupRelationshipDOs;
    }

    /**
     * Adds a target {@link LookupRelationshipDO}.
     *
     * @param target the relationship to add
     */
    public void addTargetLookupRelationship(LookupRelationshipDO target) {
        targetLookupRelationshipDOs.add(target);
    }

    /**
     * Removes a target {@link LookupRelationshipDO}.
     *
     * @param target the relationship to remove
     */
    public void removeTargetLookupRelationship(LookupRelationshipDO target) {
        targetLookupRelationshipDOs.remove(target);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj == this) {
            equal = true;
        } else if (obj instanceof LookupDO) {
            LookupDO rhs = (LookupDO) obj;
            equal = ObjectUtils.equals(code, rhs.code)
                    && ObjectUtils.equals(getArchetypeId(),
                                          rhs.getArchetypeId());
        }
        return equal;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getArchetypeId())
                .append(getCode())
                .toHashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("uid", getId())
                .append("code", getCode())
                .append("default", isDefaultLookup())
                .toString();
    }

    /**
     * Sets the source lookup relationships.
     *
     * @param relationships the relationships to set
     */
    protected void setSourceLookupRelationships(
            Set<LookupRelationshipDO> relationships) {
        sourceLookupRelationshipDOs = relationships;
    }

    /**
     * Sets the target lookup relationships
     *
     * @param relationships the relationships to set
     */
    protected void setTargetLookupRelationships(
            Set<LookupRelationshipDO> relationships) {
        targetLookupRelationshipDOs = relationships;
    }

}
