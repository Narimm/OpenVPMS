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
 * .
 *
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
     * Returns the the source lookup relationships.
     *
     * @return the source lookup relationships
     */
    Set<LookupRelationship> getSourceLookupRelationships();

    /**
     * Add a source {@link LookupRelationship}.
     *
     * @param source the relationship to add
     */
    void addSourceLookupRelationship(LookupRelationship source);

    /**
     * Remove a source {@link LookupRelationship}.
     *
     * @param source the relationship to remove
     */
    void removeSourceLookupRelationship(LookupRelationship source);

    /**
     * Returns the target lookup relationships.
     *
     * @return the target lookup relationships
     */
    Set<LookupRelationship> getTargetLookupRelationships();

    /**
     * Adds a target {@link LookupRelationship}.
     *
     * @param target the relationship to add
     */
    void addTargetLookupRelationship(LookupRelationship target);

    /**
     * Removes a target {@link LookupRelationship}.
     *
     * @param target the relationship to remove
     */
    void removeTargetLookupRelationship(LookupRelationship target);

    /**
     * Add a relationship to this lookup. It will determine whether it is a
     * source or target relationship before adding it.
     *
     * @param rel the relationship to add
     */
    void addLookupRelationship(LookupRelationship rel);

    /**
     * Remove a relationship from this lookup. It will determine whether it is a
     * source or target relationship before removing it.
     *
     * @param rel the lookup relationship to remove
     */
    void removeLookupRelationship(LookupRelationship rel);

    /**
     * Returns all the lookup relationships. Do not use the returned set to
     * add and remove lookup relationships.
     * Instead use {@link #addLookupRelationship(LookupRelationship)}
     * and {@link #removeLookupRelationship(LookupRelationship)} repsectively.
     *
     * @return the set of all lookup relationships
     */
    Set<LookupRelationship> getLookupRelationships();

}
