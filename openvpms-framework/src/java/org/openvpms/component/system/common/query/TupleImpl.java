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

package org.openvpms.component.system.common.query;


import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import java.util.List;

/**
 * Default implementation of {@link Tuple}.
 *
 * @author Tim Anderson
 */
public class TupleImpl implements Tuple {

    /**
     * The elements.
     */
    private final List<TupleElement<?>> elements;

    /**
     * The values.
     */
    private final Object[] values;

    /**
     * Constructs a {@link TupleImpl}.
     *
     * @param elements the elements
     * @param values   the element values
     */
    public TupleImpl(List<TupleElement<?>> elements, Object[] values) {
        this.elements = elements;
        this.values = values;
    }

    /**
     * Returns the value of the specified tuple element.
     *
     * @param element the tuple element
     * @return the value of tuple element
     * @throws IllegalArgumentException if tuple element does not correspond to an element in the query result tuple
     */
    @Override
    public <X> X get(TupleElement<X> element) {
        int index = elements.indexOf(element);
        if (index == -1) {
            throw new IllegalArgumentException("Argument 'element' does not correspond to an element in the tuple");
        }
        return (X) values[index];
    }

    /**
     * Returns the value of the tuple element with the specified alias.
     *
     * @param alias the tuple element alias
     * @param type  the type of the tuple element
     * @return the value of the tuple element
     * @throws IllegalArgumentException if alias does not correspond to an element in the query result tuple or
     *                                  element cannot be assigned to the specified type
     */
    @Override
    public <X> X get(String alias, Class<X> type) {
        return type.cast(get(alias));
    }

    /**
     * Returns the value of the tuple element to which the specified alias has been assigned.
     *
     * @param alias the tuple element alias
     * @return the value of the tuple element
     * @throws IllegalArgumentException if {@code alias} does not correspond to an element in the query result tuple
     */
    @Override
    public Object get(String alias) {
        int index = 0;
        for (TupleElement element : elements) {
            if (alias.equals(element.getAlias())) {
                return values[index];
            }
        }
        throw new IllegalArgumentException("Argument 'alias' does not correspond to an element in the tuple: " + alias);
    }

    /**
     * Returns the value of the element at the specified position in the result tuple.<br/>
     * The first position is 0.
     *
     * @param i    the position in the result tuple
     * @param type the type of the tuple element
     * @return the value of the tuple element
     * @throws IllegalArgumentException if i exceeds the length of result tuple or the value cannot be assigned to the
     *                                  specified type
     */
    @Override
    public <X> X get(int i, Class<X> type) {
        return type.cast(get(i));
    }

    /**
     * Returns the value of the element at the specified position in the result tuple.<br/>
     *
     * @param i the position in the result tuple
     * @return the value of the tuple element
     * @throws IllegalArgumentException if i exceeds the length of the result tuple
     */
    @Override
    public Object get(int i) {
        if (i > values.length) {
            throw new IllegalArgumentException("Argument 'i' exceeds the length of the result tuple");
        }
        return values[i];
    }

    /**
     * Return the values of the result tuple elements as an array.
     *
     * @return the tuple element values
     */
    @Override
    public Object[] toArray() {
        return values;
    }

    /**
     * Return the tuple elements.
     *
     * @return tuple elements
     */
    @Override
    public List<TupleElement<?>> getElements() {
        return elements;
    }
}
