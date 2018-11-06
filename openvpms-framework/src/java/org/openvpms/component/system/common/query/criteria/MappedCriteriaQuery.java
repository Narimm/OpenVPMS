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

package org.openvpms.component.system.common.query.criteria;


import javax.persistence.TupleElement;
import javax.persistence.criteria.CriteriaQuery;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Manages a JPA {@code javax.persistence.criteria.CriteriaQuery} and meta-data generated from mapping a
 * {@link org.openvpms.component.query.criteria.CriteriaQuery}.
 *
 * @author Tim Anderson
 */
public class MappedCriteriaQuery<T> {

    /**
     * The JPA query.
     */
    private final javax.persistence.criteria.CriteriaQuery<T> query;

    /**
     * The original TupleElements, and their JPA equivalents.
     */
    private final Map<TupleElement<?>, TupleElement<?>> elements;

    /**
     * The JPA tuple elements to their {@link TupleElement} equivalents.
     */
    private Map<TupleElement<?>, TupleElement<?>> jpaElements;

    /**
     * Constructs a {@link MappedCriteriaQuery}.
     *
     * @param query    the JPA query
     * @param elements the original tuple elements, and their JPA equivalents.
     */
    public MappedCriteriaQuery(javax.persistence.criteria.CriteriaQuery<T> query,
                               Map<TupleElement<?>, TupleElement<?>> elements) {
        this.query = query;
        this.elements = elements;
    }

    /**
     * Returns the JPA query.
     *
     * @return the JPA query
     */
    public CriteriaQuery<T> getQuery() {
        return query;
    }

    /**
     * Returns a {@link TupleElement}, given its JPA equivalent.
     *
     * @param element the JPA tuple element
     * @return the corresponding tuple element, or {@code null} if none is found
     */
    public TupleElement<?> getElement(TupleElement<?> element) {
        if (jpaElements == null) {
            jpaElements = new IdentityHashMap<>();
            for (Map.Entry<TupleElement<?>, TupleElement<?>> entry : elements.entrySet()) {
                jpaElements.put(entry.getValue(), entry.getKey());
            }
        }
        return jpaElements.get(element);
    }
}
