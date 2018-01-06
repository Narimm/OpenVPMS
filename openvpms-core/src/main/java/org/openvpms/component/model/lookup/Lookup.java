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

package org.openvpms.component.model.lookup;

import org.openvpms.component.model.object.IMObject;

import java.util.Set;

/**
 * A lookup represents a piece of static data such as a Species, Breed, Country, or PostCode etc.
 * <p>
 * A lookup has a <em>code</em>, <em>name</em> and <em>description</em>.
 * The <em>code</em> is mandatory, used to uniquely identify the lookup within
 * its domain. The other attributes are optional.
 * <br/>
 * The convention for alphabetic codes are that they appear all in uppercase,
 * with words separated by an underscore.
 * E.g, CANINE, COMPLETED, IN_PROGRESS.
 * <br/>
 * The <em>name</em> is used for display purposes. If not specified, it is derived from <em>code</em>.
 * <br/>
 * The <em>description</em> is used for display purposes, and defaults to {@code null}.
 * <p>
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface Lookup extends IMObject {

    /**
     * Returns the lookup code.
     *
     * @return the code
     */
    String getCode();

    /**
     * Sets the lookup code.
     *
     * @param code the code to set
     */
    void setCode(String code);

    /**
     * Returns the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Determines if this is the default lookup.
     *
     * @return {@code true} if this is the default lookup, otherwise {@code false}
     */
    boolean isDefaultLookup();

    /**
     * Determines if this is the default lookup.
     *
     * @param defaultLookup if {@code true} this is the default lookup
     */
    void setDefaultLookup(boolean defaultLookup);

    /**
     * Returns the relationships where this lookup is the source.
     *
     * @return the source lookup relationships
     */
    Set<LookupRelationship> getSourceLookupRelationships();

    /**
     * Add a relationship where this lookup is the source.
     *
     * @param relationship the relationship to add
     */
    void addSourceLookupRelationship(LookupRelationship relationship);

    /**
     * Removes a relationship where this lookup is the source.
     *
     * @param relationship the relationship to remove
     */
    void removeSourceLookupRelationship(LookupRelationship relationship);

    /**
     * Returns the relationships where this lookup is the target.
     *
     * @return the target lookup relationships
     */
    Set<LookupRelationship> getTargetLookupRelationships();

    /**
     * Add a relationship where this lookup is the target.
     *
     * @param relationship the relationship to add
     */
    void addTargetLookupRelationship(LookupRelationship relationship);

    /**
     * Removes a relationship where this lookup is the target.
     *
     * @param relationship the relationship to remove
     */
    void removeTargetLookupRelationship(LookupRelationship relationship);

    /**
     * Return all the relationships that the lookup has.
     * <p>
     * NOTE: the returned set cannot be used to add or remove relationships.
     *
     * @return the relationships
     */
    Set<LookupRelationship> getLookupRelationships();

    /**
     * Adds a relationship between this lookup and another.
     * <p>
     * It will determine if this is a source or target of the relationship and invoke
     * {@link #addSourceLookupRelationship} or {@link #addTargetLookupRelationship} accordingly.
     *
     * @param relationship the entity relationship to add
     */
    void addLookupRelationship(LookupRelationship relationship);

    /**
     * Remove a relationship between this lookup and another.
     * <p>
     * It will determine if this is a source or target of the relationship and invoke
     * {@link #removeSourceLookupRelationship} or {@link #removeTargetLookupRelationship} accordingly.
     *
     * @param relationship the entity relationship to remove
     */
    void removeLookupRelationship(LookupRelationship relationship);

}
