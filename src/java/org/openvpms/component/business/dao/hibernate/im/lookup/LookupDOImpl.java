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
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.HashSet;
import java.util.Set;


/**
 * Implementation of the {@link LookupDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-05-02 14:28:50 +1000 (Wed, 02 May 2007) $
 */
public class LookupDOImpl extends IMObjectDOImpl implements LookupDO {

    /**
     * The lookup code.
     */
    private String code;

    /**
     * Determines if the lookup is the default amongst lookups with the
     * same archetype identifier.
     */
    private boolean defaultLookup;

    /**
     * The relationships where the lookup is the source.
     */
    private Set<LookupRelationshipDO> sourceLookupRelationshipDOs =
            new HashSet<LookupRelationshipDO>();

    /**
     * The relationships where the lookup is the target.
     */
    private Set<LookupRelationshipDO> targetLookupRelationshipDOs =
            new HashSet<LookupRelationshipDO>();


    /**
     * Default constructor.
     */
    public LookupDOImpl() {
    }

    /**
     * Creates a new lookup.
     *
     * @param archetypeId the archetype id
     */
    public LookupDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Creates a new lookup.
     *
     * @param archetypeId the archetype id
     * @param code        the lookup code
     */
    public LookupDOImpl(ArchetypeId archetypeId, String code) {
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
     * @return <tt>true</tt> if this is the default lookup, otherwise
     *         <tt>false</tt>
     */
    public boolean isDefaultLookup() {
        return defaultLookup;
    }

    /**
     * Determines if this is the default lookup.
     *
     * @param defaultLookup if <tt>true</tt> this is the default lookup
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
     * Adds a relationship where this is the source.
     *
     * @param source the relationship to add
     */
    public void addSourceLookupRelationship(LookupRelationshipDO source) {
        sourceLookupRelationshipDOs.add(source);
        source.setSource(this);
    }

    /**
     * Removes a source relationship.
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
     * Adds a relationship where this is the target.
     *
     * @param target the relationship to add
     */
    public void addTargetLookupRelationship(LookupRelationshipDO target) {
        targetLookupRelationshipDOs.add(target);
    }

    /**
     * Removes a target relationship.
     *
     * @param target the relationship to remove
     */
    public void removeTargetLookupRelationship(LookupRelationshipDO target) {
        targetLookupRelationshipDOs.remove(target);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <tt>true</tt> if this object is the same as the obj
     *         argument; <tt>false</tt> otherwise
     */
    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj == this) {
            equal = true;
        } else if (obj instanceof LookupDO) {
            LookupDO rhs = (LookupDO) obj;
            equal = ObjectUtils.equals(code, rhs.getCode())
                    && ObjectUtils.equals(getArchetypeId(),
                                          rhs.getArchetypeId());
        }
        return equal;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getArchetypeId())
                .append(getCode())
                .toHashCode();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
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
