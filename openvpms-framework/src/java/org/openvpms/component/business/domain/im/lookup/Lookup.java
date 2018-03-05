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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.domain.im.lookup;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.HashSet;
import java.util.Set;

import static org.openvpms.component.business.domain.im.lookup.LookupRelationshipException.ErrorCode.FailedToAddLookRelationship;
import static org.openvpms.component.business.domain.im.lookup.LookupRelationshipException.ErrorCode.FailedToRemoveLookRelationship;


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
 * @author Jim Alateras
 */
public class Lookup extends IMObject implements org.openvpms.component.model.lookup.Lookup {

    /**
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The lookup code.
     */
    private String code;

    /**
     * Is this the default lookup for a particular domain.
     */
    private boolean defaultLookup;

    /**
     * The {@link LookupRelationship}s that this lookup is a source of.
     */
    private Set<org.openvpms.component.model.lookup.LookupRelationship> sourceLookupRelationships = new HashSet<>();

    /**
     * The {@link LookupRelationship}s that this lookup is a target of.
     */
    private Set<org.openvpms.component.model.lookup.LookupRelationship> targetLookupRelationships = new HashSet<>();


    /**
     * Default constructor.
     */
    public Lookup() {
    }

    /**
     * Constructs a new <code>Lookup</code>.
     *
     * @param archetypeId the archetype id constraining this object
     * @param code        the lookup code
     */
    public Lookup(ArchetypeId archetypeId, String code) {
        this(archetypeId, code, null);
    }

    /**
     * Constructs a new <code>Lookup</code>.
     *
     * @param archetypeId the archetype id constraining this object
     * @param code        the lookup code
     * @param name        the lookup name
     */
    public Lookup(ArchetypeId archetypeId, String code, String name) {
        super(archetypeId, name, null);
        this.code = code;
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
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        String name = super.getName();
        if (name == null && code != null) {
            name = code.replace('_', ' ');
            name = WordUtils.capitalizeFully(name);
        }
        return name;
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
    public Set<org.openvpms.component.model.lookup.LookupRelationship> getSourceLookupRelationships() {
        return sourceLookupRelationships;
    }

    /**
     * Sets the source lookup relationships.
     *
     * @param relationships the relationships to set
     */
    public void setSourceLookupRelationships(
            Set<org.openvpms.component.model.lookup.LookupRelationship> relationships) {
        sourceLookupRelationships = relationships;
    }

    /**
     * Add a source {@link LookupRelationship}.
     *
     * @param relationship the relationship to add
     */
    public void addSourceLookupRelationship(org.openvpms.component.model.lookup.LookupRelationship relationship) {
        sourceLookupRelationships.add(relationship);
    }

    /**
     * Remove a source {@link LookupRelationship}.
     *
     * @param relationship the relationship to remove
     */
    public void removeSourceLookupRelationship(org.openvpms.component.model.lookup.LookupRelationship relationship) {
        sourceLookupRelationships.remove(relationship);
    }

    /**
     * Returns the target lookup relationships.
     *
     * @return the target lookup relationships
     */
    public Set<org.openvpms.component.model.lookup.LookupRelationship> getTargetLookupRelationships() {
        return targetLookupRelationships;
    }

    /**
     * Sets the target lookup relationships
     *
     * @param relationships the relationships to set
     */
    public void setTargetLookupRelationships(
            Set<org.openvpms.component.model.lookup.LookupRelationship> relationships) {
        targetLookupRelationships = relationships;
    }

    /**
     * Adds a target {@link LookupRelationship}.
     *
     * @param relationship the relationship to add
     */
    public void addTargetLookupRelationship(org.openvpms.component.model.lookup.LookupRelationship relationship) {
        targetLookupRelationships.add(relationship);
    }

    /**
     * Removes a target {@link LookupRelationship}.
     *
     * @param relationship the relationship to remove
     */
    public void removeTargetLookupRelationship(org.openvpms.component.model.lookup.LookupRelationship relationship) {
        targetLookupRelationships.remove(relationship);
    }

    /**
     * Add a relationship to this lookup. It will determine whether it is a
     * source or target relationship before adding it.
     *
     * @param relationship the relationship to add
     * @throws LookupRelationshipException if the relationship cannot be added
     *                                     to this lookup
     */
    public void addLookupRelationship(org.openvpms.component.model.lookup.LookupRelationship relationship) {
        if ((relationship.getSource().getLinkId().equals(this.getLinkId())) &&
            (relationship.getSource().getArchetype().equals(this.getArchetype()))) {
            addSourceLookupRelationship(relationship);
        } else if ((relationship.getTarget().getLinkId().equals(this.getLinkId())) &&
                   (relationship.getTarget().getArchetype().equals(this.getArchetype()))) {
            addTargetLookupRelationship(relationship);
        } else {
            throw new LookupRelationshipException(
                    FailedToAddLookRelationship,
                    new Object[]{relationship.getSource(), relationship.getTarget()});
        }
    }

    /**
     * Remove a relationship from this lookup. It will determine whether it is a
     * source or target relationship before removing it.
     *
     * @param relationship the lookup relationship to remove
     * @throws LookupRelationshipException if the relationship cannot be removed
     *                                     from this lookup
     */
    public void removeLookupRelationship(org.openvpms.component.model.lookup.LookupRelationship relationship) {
        if ((relationship.getSource().getLinkId().equals(this.getLinkId())) &&
            (relationship.getSource().getArchetype().equals(this.getArchetype()))) {
            removeSourceLookupRelationship(relationship);
        } else if ((relationship.getTarget().getLinkId().equals(this.getLinkId())) &&
                   (relationship.getTarget().getArchetype().equals(this.getArchetype()))) {
            removeTargetLookupRelationship(relationship);
        } else {
            throw new LookupRelationshipException(
                    FailedToRemoveLookRelationship,
                    new Object[]{relationship.getSource(), relationship.getTarget()});
        }
    }

    /**
     * Returns all the lookup relationships.
     * @return the set of all lookup relationships
     */
    public Set<org.openvpms.component.model.lookup.LookupRelationship> getLookupRelationships() {
        Set<org.openvpms.component.model.lookup.LookupRelationship> relationships
                = new HashSet<>(sourceLookupRelationships);
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
        return ObjectUtils.equals(code, rhs.code) && ObjectUtils.equals(getArchetypeId(), rhs.getArchetypeId());
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getArchetypeId())
                .append(code)
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
                .append("code", getCode())
                .append("default", isDefaultLookup())
                .toString();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
