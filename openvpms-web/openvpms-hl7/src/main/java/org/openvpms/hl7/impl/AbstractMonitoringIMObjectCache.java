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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.beans.factory.DisposableBean;

/**
 * Cache that monitors updates to objects from the {@link IArchetypeService}.
 *
 * @author Tim Anderson
 */
abstract class AbstractMonitoringIMObjectCache<T extends IMObject> implements DisposableBean {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The listener for archetype service events.
     */
    private final IArchetypeServiceListener listener;

    /**
     * The short name of objects to cache.
     */
    private final String shortName;

    /**
     * The type of objects to cache.
     */
    private final Class<T> type;


    public AbstractMonitoringIMObjectCache(IArchetypeService service, String shortName, final Class<T> type) {
        this.service = service;
        this.shortName = shortName;
        this.type = type;

        listener = new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                AbstractMonitoringIMObjectCache.this.add(type.cast(object));
            }

            @Override
            public void removed(IMObject object) {
                AbstractMonitoringIMObjectCache.this.remove(type.cast(object));
            }
        };
        service.addListener(shortName, listener);
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     */
    @Override
    public void destroy() {
        service.removeListener(shortName, listener);
    }

    /**
     * Loads objects from the archetype service.
     */
    protected void load() {
        ArchetypeQuery query = new ArchetypeQuery(shortName, true);
        IMObjectQueryIterator<T> iter = new IMObjectQueryIterator<T>(service, query);
        while (iter.hasNext()) {
            add(iter.next());
        }
    }

    /**
     * Adds an object to the cache.
     * <p/>
     * Implementations may ignore the object if it is older than any cached instance, or is inactive
     *
     * @param object the object to add
     */
    protected abstract void add(T object);

    /**
     * Removes an object.
     *
     * @param object the object to remove
     */
    protected abstract void remove(T object);

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the object corresponding to the reference.
     *
     * @param reference the reference
     * @return the corresponding object or {@code null} if none is found
     */
    protected T get(IMObjectReference reference) {
        return type.cast(service.get(reference));
    }
}
