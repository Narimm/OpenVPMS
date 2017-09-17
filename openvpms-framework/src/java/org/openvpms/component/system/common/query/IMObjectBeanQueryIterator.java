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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over the results of an {@link IMObject} query, returning them wrapped in an {@link IMObjectBean}.
 *
 * @author Tim Anderson
 */
public class IMObjectBeanQueryIterator implements Iterator<IMObjectBean> {

    /**
     * The underlying iterator.
     */
    private final IMObjectQueryIterator<IMObject> iterator;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link IMObjectQueryIterator}.
     *
     * @param service the archetype service\
     * @param query   the query
     */
    public IMObjectBeanQueryIterator(IArchetypeService service, ArchetypeQuery query) {
        iterator = new IMObjectQueryIterator<>(service, query);
        this.service = service;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public IMObjectBean next() {
        return new IMObjectBean(iterator.next(), service);
    }
}
