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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl;

import java.util.Collection;
import java.util.List;


/**
 * Data access object for {@link ETLValue} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ETLValueDAO {

    /**
     * Save a value.
     *
     * @param value the value to save
     */
    void save(ETLValue value);

    /**
     * Save a collection of values
     *
     * @param values the values to save
     */
    void save(Iterable<ETLValue> values);

    /**
     * Returns a collection of values.
     *
     * @param firstResult the index of the first result
     * @param maxResults  the maximum no. of results, or <tt>-1</tt> for all
     *                    results
     * @return a collection of values
     */
    List<ETLValue> get(int firstResult, int maxResults);

    /**
     * Returns an {@link ETLValue} given its value identifier.
     *
     * @param valueId the value identifier
     * @return the corresponding value, or <tt>null</tt> if none is found
     */
    ETLValue get(long valueId);

    /**
     * Returns all {@link ETLValue}s associated with an object identifier.
     *
     * @param objectId the object identifier
     * @return all values with matching <tt>objectId</tt>
     */
    List<ETLValue> get(String objectId);

    /**
     * Returns all {@link ETLValue}s associated with a legacy identifier and
     * archetype short name.
     *
     * @param legacyId  the legacy identifier
     * @param archetype the archetype short name
     * @return all values with matching <tt>legacyId</tt> and <tt>archetype</tt>
     */
    List<ETLValue> get(String legacyId, String archetype);

    /**
     * Returns all distinct archetypes and their corresponding nodes referred
     * to by {@link ETLValue} instances.
     *
     * @return the archetypes and their corresponding nodes
     */
    Collection<ETLArchetype> getArchetypes();

    /**
     * Returns all distinct values for an archetype and node name referred
     * to by {@link ETLValue} instances. This excludes reference values.
     *
     * @param archetype the archetype short name
     * @param name      the node name
     * @return distinct values for the archetype and node name
     */
    Collection<String> getDistinctValues(String archetype, String name);

    /**
     * Returns all distinct value pairs for an archetype and node pair,
     * referred to by {@link ETLValue} instances with the same objectId.
     * This excludes reference values.
     *
     * @param archetype the archetype short name
     * @param name1     the first node name
     * @param name2     the second node name
     * @return distinct values for the archetype and node name
     */
    Collection<ETLPair> getDistinctValuePairs(String archetype, String name1,
                                              String name2);

}
