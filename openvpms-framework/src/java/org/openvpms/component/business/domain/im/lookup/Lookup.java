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


package org.openvpms.component.business.domain.im.lookup;

// java lang
import java.util.HashSet;
import java.util.Set;

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

// openvpms-common
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * A lookup represents a piece of static data that is used by 
 * OpenVPMS. It can be used to represent a Species, a Breed, a Country,
 * a PostCode etc.
 * <p> 
 * A lookup can have additional information stored in the details 
 * attribute 
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Lookup extends IMObject {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The value of the lookup. This is mandatory.
     */
    private String value;
    
    /**
     * The code associsted with the lookup. This is an optional.
     */
    private String code;
    
    /**
     * Is this the default lookup for a particular domain
     */
    private boolean defaultLookup;
    
    /**
     * Holds all the {@link LookupRelationship}s that this lookup is a source off.
     */
    private Set<LookupRelationship> sourceLookupRelationships =
        new HashSet<LookupRelationship>();
    
    /**
     * Holds all the {@link LookupRelationship}s that this lookup is a target off.
     */
    private Set<LookupRelationship> targetLookupRelationships =
        new HashSet<LookupRelationship>();

    /**
     * Details holds dynamic attributes for a lookup
     */
    private DynamicAttributeMap details = new DynamicAttributeMap();
    
    
    /**
     * Default constructor
     */
    public Lookup() {
    }

    /**
     * Construct a lookup
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param value
     *            the value associated with the lookup
     * @param code 
     *            an optional code 
     */
    public Lookup(ArchetypeId archetypeId, String value, 
        String code) {
        super(archetypeId);
        
        this.value = value;
        this.code = code;
    }

    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return Returns the defaultLookup.
     */
    public boolean isDefaultLookup() {
        return defaultLookup;
    }

    /**
     * @param defaultLookup The defaultLookup to set.
     */
    public void setDefaultLookup(boolean defaultLookup) {
        this.defaultLookup = defaultLookup;
    }
    /**
     * @return Returns the sourceLookupRelationships.
     */
    public Set<LookupRelationship> getSourceLookupRelationships() {
        return sourceLookupRelationships;
    }

    /**
     * @param sourceLookupRelationships The sourceLookupRelationships to set.
     */
    public void setSourceLookupRelationships(
            Set<LookupRelationship> sourceLookupRelationships) {
        this.sourceLookupRelationships = sourceLookupRelationships;
    }

    /**
     * Add a source {@link LookupRelationship}.
     * 
     * @param source 
     */
    public void addSourceLookupRelationship(LookupRelationship source) {
        this.sourceLookupRelationships.add(source);
    }

    /**
     * Remove a source {@link LookupRelationship}.
     * 
     * @param source
     */
    public void removeSourceLookupRelationship(LookupRelationship source) {
        this.sourceLookupRelationships.remove(source);
    }

    /**
     * @return Returns the targetLookupRelationships.
     */
    public Set<LookupRelationship> getTargetLookupRelationships() {
        return targetLookupRelationships;
    }

    /**
     * Set this lookup to be a targt of an {@link LookupRelationship}.
     * 
     * @param targetLookupRelationships The targetLookupRelationships to set.
     */
    public void setTargetLookupRelationships(
            Set<LookupRelationship> targetLookupRelationships) {
        this.targetLookupRelationships = targetLookupRelationships;
    }
    
    /**
     * Add a target {@link LookupRelationship}.
     * 
     * @param target 
     *            add a new target.
     */
    public void addTargetLookupRelationship(LookupRelationship target) {
        this.targetLookupRelationships.add(target);
    }

    /**
     * Remove a target {@link LookupRelationship}.
     * 
     * @param target
     */
    public void removeTargetLookupRelationship(LookupRelationship target) {
        this.targetLookupRelationships.remove(target);
    }

    /**
     * Add a relationship to this lookup. It will determine whether it is a 
     * source or target relationship before adding it.
     * 
     * @param rel
     *            the lookup relationship to add
     * @throws LookupRelationshipException
     *            if this relationship cannot be added to this lookup            
     */
    public void addLookupRelationship(LookupRelationship rel) {
        if ((rel.getSource().getLinkId().equals(this.getLinkId())) &&
            (rel.getSource().getArchetypeId().equals(this.getArchetypeId()))){
            addSourceLookupRelationship(rel);
        } else if ((rel.getTarget().getLinkId().equals(this.getLinkId())) &&
            (rel.getTarget().getArchetypeId().equals(this.getArchetypeId()))){
            addTargetLookupRelationship(rel);
        } else {
            throw new LookupRelationshipException(
                    LookupRelationshipException.ErrorCode.FailedToAddLookRelationship,
                    new Object[] { rel.getSource(), rel.getTarget()});
        }
    }

    /**
     * Remove a relationship to this lookup. It will determine whether it is a 
     * source or target relationship before removing it.
     * 
     * @param rel
     *            the lookup relationship to remove
     * @throws LookupRelationshipException
     *            if this relationship cannot be removed from this lookup            
     */
    public void removeLookupRelationship(LookupRelationship rel) {
        if ((rel.getSource().getLinkId().equals(this.getLinkId())) &&
            (rel.getSource().getArchetypeId().equals(this.getArchetypeId()))){
            removeSourceLookupRelationship(rel);
        } else if ((rel.getTarget().getLinkId().equals(this.getLinkId())) &&
            (rel.getTarget().getArchetypeId().equals(this.getArchetypeId()))){
            removeTargetLookupRelationship(rel);
        } else {
            throw new LookupRelationshipException(
                    LookupRelationshipException.ErrorCode.FailedToRemoveLookRelationship,
                    new Object[] { rel.getSource(), rel.getTarget()});
        }
    }
    
    /**
     * Return all the lookup relationships. Do not use the returned set to 
     * add and remove lookup relationships. Instead use {@link #addLookupRelationship(LookupRelationship)}
     * and {@link #removeLookupRelationship(LookupRelationship)} repsectively.
     * 
     * @return Set<LookupRelationship>
     */
    public Set<LookupRelationship> getLookupRelationships() {
        Set<LookupRelationship> relationships = 
            new HashSet<LookupRelationship>(sourceLookupRelationships);
        relationships.addAll(targetLookupRelationships);
        
        return relationships;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Lookup)) {
            return false;
        }
        Lookup rhs = (Lookup) obj;
        return new EqualsBuilder()
            .append(getArchetypeId(), rhs.getArchetypeId())
            .append(value, rhs.value)
            .append(code, rhs.code)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(getArchetypeId())
        .append(code)
        .append(value)
        .toHashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("linkId", getLinkId())
            .append("uid", getUid())
            .append("value", value)
            .append("code", code)
            .append("default", defaultLookup)
            .toString();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Lookup copy = (Lookup)super.clone();
        copy.code = this.code;
        copy.details = (DynamicAttributeMap)(this.details == null ?
                null : this.details.clone());
        copy.value = this.value;
        copy.defaultLookup = this.defaultLookup;
        
        return copy;
    }
}
