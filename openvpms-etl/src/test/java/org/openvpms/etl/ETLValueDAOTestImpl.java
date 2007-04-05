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

import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;


/**
 * Mock implementation of {@link ETLValueDAO}, for testing purposes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLValueDAOTestImpl implements ETLValueDAO {

    /**
     * The values.
     */
    private List<ETLValue> values = new ArrayList<ETLValue>();

    /**
     * Save a value.
     *
     * @param value the value to save
     */
    public void save(ETLValue value) {
        save(Arrays.asList(value));
    }

    /**
     * Save a collection of values.
     *
     * @param values the values to save
     */
    public void save(Iterable<ETLValue> values) {
        for (ETLValue value : values) {
            this.values.add(value);
        }
        Collections.sort(this.values, new Comparator<ETLValue>() {
            public int compare(ETLValue o1, ETLValue o2) {
                return o1.getObjectId().compareTo(o2.getObjectId());
            }
        });
    }

    /**
     * Returns a collection of values, ordered on
     * {@link ETLValue#getObjectId()}.
     *
     * @param firstResult the index of the first result
     * @param maxResults  the maximum no. of results, or <tt>-1</tt> for all
     *                    results
     * @return a collection of values
     */
    public List<ETLValue> get(int firstResult, int maxResults) {
        List<ETLValue> result = Collections.emptyList();
        if (firstResult < values.size()) {
            int to = (maxResults != -1) ? firstResult + maxResults : -1;
            if (to == -1 || to > values.size()) {
                to = values.size();
            }
            result = values.subList(firstResult, to);
        }
        return result;
    }

    /**
     * Returns an {@link ETLValue} given its value identifier.
     *
     * @param valueId the value identifier
     * @return the corresponding value, or <tt>null</tt> if none is found
     */
    public ETLValue get(long valueId) {
        for (ETLValue value : values) {
            if (value.getValueId() == valueId) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns all {@link ETLValue}s associated with an object identifier.
     *
     * @param objectId the object identifier
     * @return all values with matching <tt>objectId</tt>
     */
    public List<ETLValue> get(String objectId) {
        List<ETLValue> result = new ArrayList<ETLValue>();
        for (ETLValue value : values) {
            if (value.getObjectId().equals(objectId)) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Returns all {@link ETLValue}s associated with a legacy identifier and
     * archetype short name.
     *
     * @param legacyId  the legacy identifier
     * @param archetype the archetype short name
     * @return all values with matching <tt>legacyId</tt> and <tt>archetype</tt>
     */
    public List<ETLValue> get(String legacyId, String archetype) {
        List<ETLValue> result = new ArrayList<ETLValue>();
        for (ETLValue value : values) {
            if (value.getLegacyId().equals(legacyId)
                    && TypeHelper.matches(value.getArchetype(), archetype)) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Returns all distinct archetypes and their corresponding nodes referred
     * to {@link ETLValue} instances.
     *
     * @return the archetypes and their corresponding nodes
     */
    public Collection<ETLArchetype> getArchetypes() {
        Map<String, ETLArchetype> result = new HashMap<String, ETLArchetype>();
        for (ETLValue value : values) {
            ETLArchetype archetype = result.get(value.getArchetype());
            if (archetype == null) {
                archetype = new ETLArchetype(value.getArchetype(),
                                             new ArrayList<String>());
                result.put(value.getArchetype(), archetype);
            }
            List<String> names = archetype.getNames();
            if (!names.contains(value.getName())) {
                names.add(value.getName());
            }
        }
        return result.values();
    }

    /**
     * Returns all distinct values for an archetype and node name referred
     * to by {@link ETLValue} instances. This excludes reference values.
     *
     * @param archetype the archetype short name
     * @param name      the node name
     * @return distinct values for the archetype and node name
     */
    public Collection<String> getDistinctValues(String archetype, String name) {
        Set<String> result = new HashSet<String>();
        for (ETLValue value : values) {
            if (!value.isReference() && value.getArchetype().equals(
                    archetype) && value.getName().equals(name)) {
                result.add(value.getValue());
            }
        }
        return result;
    }

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
    public Collection<ETLPair> getDistinctValuePairs(String archetype,
                                                     String name1,
                                                     String name2) {
        Map<String, ETLPair> byObjectId = new LinkedHashMap<String, ETLPair>();
        for (ETLValue value : values) {
            if (!value.isReference()
                    && value.getArchetype().equals(archetype)) {
                String name = value.getName();
                if (name.equals(name1) || name.equals(name2)) {
                    ETLPair pair = byObjectId.get(value.getObjectId());
                    if (pair == null) {
                        pair = new ETLPair();
                        byObjectId.put(value.getObjectId(), pair);
                    }
                    if (name.equals(name1)) {
                        pair.setValue1(value.getValue());
                    } else {
                        pair.setValue2(value.getValue());
                    }
                }
            }
        }
        return new LinkedHashSet<ETLPair>(byObjectId.values());
    }
}

