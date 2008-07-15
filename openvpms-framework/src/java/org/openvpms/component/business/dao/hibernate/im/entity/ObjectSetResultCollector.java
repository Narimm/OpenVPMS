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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Implementation of {@link ResultCollector} that collects {@link ObjectSet}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ObjectSetResultCollector
        extends HibernateResultCollector<ObjectSet> {

    /**
     * The object names.
     */
    private String[] names;

    /**
     * Indicates which columns starts references.
     */
    private final String[] refColumnNames;

    /**
     * The results.
     */
    private final List<ObjectSet> result = new ArrayList<ObjectSet>();

    /**
     * A map of type aliases to their corresponding sbort names.
     * May be <code>null</code>
     */
    private final Map<String, Set<String>> types;


    /**
     * Constructs a new <tt>ObjectSetResultCollector</tt>.
     *
     * @param names    the object names
     * @param refNames the names of the references being selected.
     * @param types    a map of type aliases to their corresponding archetype
     *                 short names. May be <tt>null</tt>
     */
    public ObjectSetResultCollector(List<String> names,
                                    List<String> refNames,
                                    Map<String, Set<String>> types) {
        this.names = names.toArray(new String[0]);
        refColumnNames = new String[this.names.length];

        for (String refName : refNames) {
            int index = names.indexOf(refName + ".archetypeId");
            if (index == -1 || index + 2 >= refColumnNames.length) {
                throw new IllegalArgumentException(
                        "Argument 'refNames' contains an invalid reference");
            }
            refColumnNames[index] = refName + ".reference";
        }
        this.types = types;
    }

    /**
     * Collects an object.
     *
     * @param object the object to collect
     */
    public void collect(Object object) {
        ObjectSet set = createObjectSet();
        ObjectLoader loader = getLoader();
        if (object instanceof Object[]) {
            Object[] values = (Object[]) object;
            if (values.length != names.length) {
                throw new IllegalStateException("Mismatch args");
            }
            for (int i = 0; i < names.length;) {
                if (refColumnNames[i] != null) {
                    ArchetypeId archetypeId = (ArchetypeId) values[i];
                    Long id = (Long) values[i + 1];
                    if (id == null) {
                        id = -1L; // should never happen
                    }
                    String linkId = (String) values[i + 2];

                    IMObjectReference ref = new IMObjectReference(
                            archetypeId, id, linkId);
                    set.set(refColumnNames[i], ref);
                    i += 3;
                } else {
                    Object value = values[i];
                    if (value != null) {
                        loader.load(value);
                    }
                    set.set(names[i], value);
                    i++;
                }
            }
        } else if (names.length != 1) {
            throw new IllegalStateException("Mismatch args");
        } else {
            loader.load(object);
            set.set(names[0], object);
        }
        result.add(set);
    }

    /**
     * Returns the results.
     *
     * @return the results
     */
    protected List<ObjectSet> getResults() {
        return result;
    }

    /**
     * Creates a new object set.
     *
     * @return a new object set
     */
    private ObjectSet createObjectSet() {
        ObjectSet result = new ObjectSet();
        if (types != null) {
            for (Map.Entry<String, Set<String>> entry : types.entrySet()) {
                result.addType(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

}
