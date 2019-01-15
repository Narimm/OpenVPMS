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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.service.lookup;

import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.IMObject;

import java.util.Collection;

/**
 * Service for accessing {@link Lookup}s.
 *
 * @author Tim Anderson
 */
public interface LookupService {

    /**
     * Returns the active lookup with the specified lookup archetype and code.
     *
     * @param archetype the lookup archetype. May contain wildcards
     * @param code      the lookup code
     * @return the corresponding lookup or {@code null} if none is found
     */
    Lookup getLookup(String archetype, String code);

    /**
     * Returns the lookup with the specified lookup archetype and code.
     *
     * @param archetype  the lookup archetype. May contain wildcards
     * @param code       the lookup code
     * @param activeOnly if {@code true}, the lookup must be active, otherwise it may be active/inactive
     * @return the corresponding lookup or {@code null} if none is found
     */
    Lookup getLookup(String archetype, String code, boolean activeOnly);

    /**
     * Returns all active lookups with the specified lookup archetype.
     *
     * @param archetype the lookup archetype. May contain wildcards
     * @return a collection of lookups with the specified archetype
     */
    Collection<Lookup> getLookups(String archetype);

    /**
     * Returns the default lookup for the specified lookup archetype.
     *
     * @param archetype the lookup archetype. May contain wildcards
     * @return the default lookup, or {@code null} if none is found
     */
    Lookup getDefaultLookup(String archetype);

    /**
     * Returns the lookups that are the source of any lookup relationship where supplied lookup is the target.
     *
     * @param lookup the target lookup
     * @return a collection of source lookups
     */
    Collection<Lookup> getSourceLookups(Lookup lookup);

    /**
     * Returns the lookups that are the source of specific lookup relationships where the supplied lookup is the target.
     *
     * @param lookup    the target lookup
     * @param archetype the relationship archetype. May contain wildcards
     * @return a collection of source lookups
     */
    Collection<Lookup> getSourceLookups(Lookup lookup, String archetype);

    /**
     * Returns the lookups that are the target of any lookup relationship where the supplied lookup is the source.
     *
     * @param lookup the source lookup
     * @return a collection of target lookups
     */
    Collection<Lookup> getTargetLookups(Lookup lookup);

    /**
     * Returns the lookups that are the target of specific lookup relationships where the supplied lookup is the source.
     *
     * @param lookup    the source lookup
     * @param archetype the relationship archetype. May contain wildcards
     * @return a collection of target lookups
     */
    Collection<Lookup> getTargetLookups(Lookup lookup, String archetype);

    /**
     * Returns a list of lookups for an archetype's node.
     *
     * @param archetype the archetype
     * @param node      the node name
     * @return a list of lookups
     */
    Collection<Lookup> getLookups(String archetype, String node);

    /**
     * Return a list of lookups for a given object and node value.
     * <p/>
     * Inactive lookups will be excluded, unless they are explicitly referred to.<br/>
     * This will limit lookups returned if the node refers to the source or target of a lookup relationship.
     *
     * @param object the object
     * @param node   the node name
     * @return a list of lookups
     */
    Collection<Lookup> getLookups(IMObject object, String node);

    /**
     * Returns a lookup based on the value of a node. The lookup may be inactive.
     *
     * @param object the object
     * @param node   the node name
     * @return the lookup, or {@code null} if none is found.
     */
    Lookup getLookup(IMObject object, String node);

    /**
     * Returns a lookup's name based on the value of a node. The lookup may be inactive.
     *
     * @param object the object
     * @param node   the node name
     * @return the lookup's name, or {@code null} if none is found
     */
    String getName(IMObject object, String node);

}
