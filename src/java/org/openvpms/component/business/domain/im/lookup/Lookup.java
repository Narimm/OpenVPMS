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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import static org.openvpms.component.business.domain.im.lookup.LookupRelationshipException.ErrorCode.FailedToAddLookRelationship;
import static org.openvpms.component.business.domain.im.lookup.LookupRelationshipException.ErrorCode.FailedToRemoveLookRelationship;

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
 * @version $LastChangedDate$
 */
public class Lookup extends IMObject {

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
    private Set<LookupRelationship> sourceLookupRelationships =
            new HashSet<LookupRelationship>();

    /**
     * The {@link LookupRelationship}s that this lookup is a target of.
     */
    private Set<LookupRelationship> targetLookupRelationships =
            new HashSet<LookupRelationship>();


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
    public Set<LookupRelationship> getSourceLookupRelationships() {
        return sourceLookupRelationships;
    }

    /**
     * Sets the source lookup relationships.
     *
     * @param relationships the relationships to set
     */
    public void setSourceLookupRelationships(
            Set<LookupRelationship> relationships) {
        sourceLookupRelationships = relationships;
    }

    /**
     * Add a source {@link LookupRelationship}.
     *
     * @param source the relationship to add
     */
    public void addSourceLookupRelationship(LookupRelationship source) {
        sourceLookupRelationships.add(source);
    }

    /**
     * Remove a source {@link LookupRelationship}.
     *
     * @param source the relationship to remove
     */
    public void removeSourceLookupRelationship(LookupRelationship source) {
        sourceLookupRelationships.remove(source);
    }

    /**
     * Returns the target lookup relationships.
     *
     * @return the target lookup relationships
     */
    public Set<LookupRelationship> getTargetLookupRelationships() {
        return targetLookupRelationships;
    }

    /**
     * Sets the target lookup relationships
     *
     * @param relationships the relationships to set
     */
    public void setTargetLookupRelationships(
            Set<LookupRelationship> relationships) {
        targetLookupRelationships = relationships;
    }

    /**
     * Adds a target {@link LookupRelationship}.
     *
     * @param target the relationship to add
     */
    public void addTargetLookupRelationship(LookupRelationship target) {
        targetLookupRelationships.add(target);
    }

    /**
     * Removes a target {@link LookupRelationship}.
     *
     * @param target the relationship to remove
     */
    public void removeTargetLookupRelationship(LookupRelationship target) {
        targetLookupRelationships.remove(target);
    }

    /**
     * Add a relationship to this lookup. It will determine whether it is a
     * source or target relationship before adding it.
     *
     * @param rel the relationship to add
     * @throws LookupRelationshipException if the relationship cannot be added
     *                                     to this lookup
     */
    public void addLookupRelationship(LookupRelationship rel) {
        if ((rel.getSource().getLinkId().equals(this.getLinkId())) &&
            (rel.getSource().getArchetypeId().equals(
                    this.getArchetypeId()))) {
            addSourceLookupRelationship(rel);
        } else if ((rel.getTarget().getLinkId().equals(this.getLinkId())) &&
                   (rel.getTarget().getArchetypeId().equals(
                           this.getArchetypeId()))) {
            addTargetLookupRelationship(rel);
        } else {
            throw new LookupRelationshipException(
                    FailedToAddLookRelationship,
                    new Object[]{rel.getSource(), rel.getTarget()});
        }
    }

    /**
     * Remove a relationship from this lookup. It will determine whether it is a
     * source or target relationship before removing it.
     *
     * @param rel the lookup relationship to remove
     * @throws LookupRelationshipException if the relationship cannot be removed
     *                                     from this lookup
     */
    public void removeLookupRelationship(LookupRelationship rel) {
        if ((rel.getSource().getLinkId().equals(this.getLinkId())) &&
            (rel.getSource().getArchetypeId().equals(
                    this.getArchetypeId()))) {
            removeSourceLookupRelationship(rel);
        } else if ((rel.getTarget().getLinkId().equals(this.getLinkId())) &&
                   (rel.getTarget().getArchetypeId().equals(
                           this.getArchetypeId()))) {
            removeTargetLookupRelationship(rel);
        } else {
            throw new LookupRelationshipException(
                    FailedToRemoveLookRelationship,
                    new Object[]{rel.getSource(), rel.getTarget()});
        }
    }

    /**
     * Returns all the lookup relationships. Do not use the returned set to
     * add and remove lookup relationships.
     * Instead use {@link #addLookupRelationship(LookupRelationship)}
     * and {@link #removeLookupRelationship(LookupRelationship)} repsectively.
     *
     * @return the set of all lookup relationships
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
