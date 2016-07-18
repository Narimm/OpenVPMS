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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache that monitors updates to objects from the {@link IArchetypeService}.
 *
 * @author Tim Anderson
 */
public class MonitoringIMObjectCache<T extends IMObject> extends AbstractMonitoringIMObjectCache<T> {

    /**
     * The cached objects, keyed on reference.
     */
    private final Map<IMObjectReference, T> objects;

    /**
     * Constructs a {@link MonitoringIMObjectCache}.
     *
     * @param service   the archetype service
     * @param shortName the short name to cache
     * @param type      the object types
     * @param prefetch  if {@code true}, pre-load objects from the archetype service
     */
    public MonitoringIMObjectCache(IArchetypeService service, String shortName, Class<T> type, boolean prefetch) {
        this(service, shortName, type, new HashMap<IMObjectReference, T>(), prefetch);
    }


    /**
     * Constructs a {@link MonitoringIMObjectCache}.
     *
     * @param service   the archetype service
     * @param shortName the short name to cache
     * @param type      the object types
     * @param cache     the cache to use
     * @param prefetch  if {@code true}, pre-load objects from the archetype service
     */
    public MonitoringIMObjectCache(IArchetypeService service, String shortName, Class<T> type,
                                   Map<IMObjectReference, T> cache, boolean prefetch) {
        super(service, shortName, type);
        this.objects = cache;
        if (prefetch) {
            load();
        }
    }

    /**
     * Returns the cached objects.
     *
     * @return the cached objects
     */
    public List<T> getObjects() {
        synchronized (objects) {
            return new ArrayList<>(objects.values());
        }
    }

    /**
     * Returns an object given its reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or {@code null} if none is found
     */
    public T getObject(IMObjectReference reference) {
        return get(reference);
    }

    /**
     * Adds an object to the cache, if it active, and newer than the existing instance, if any.
     *
     * @param object the object to add
     */
    @Override
    protected final void addObject(T object) {
        cache(object);
    }

    /**
     * Removes an object.
     *
     * @param object the object to remove
     */
    @Override
    protected final void removeObject(T object) {
        T removed;
        synchronized (objects) {
            removed = objects.remove(object.getObjectReference());
        }
        if (removed != null) {
            removed(removed);
        }
    }

    /**
     * Invoked when an object is added to the cache.
     * <p/>
     * This implementation is a no-op
     *
     * @param object the added object
     */
    protected void added(T object) {

    }

    /**
     * Invoked when an object is removed from the cache.
     * <p/>
     * This implementation is a no-op
     *
     * @param object the removed object
     */
    protected void removed(T object) {

    }

    /**
     * Returns the object corresponding to the reference.
     *
     * @param reference the reference
     * @return the corresponding object or {@code null} if none is found or the object is inactive
     */
    @Override
    protected T get(IMObjectReference reference) {
        T result;
        synchronized (objects) {
            result = objects.get(reference);
        }
        if (result == null) {
            result = super.get(reference);
            if (result != null) {
                result = cache(result);
            }
        }
        return result;
    }

    /**
     * Adds an object to the cache, if it active, and newer than the existing instance, if any.
     * If the object is inactive, any existing instance will be removed using {@link #removeObject(IMObject)}.
     *
     * @param object the object to add
     * @return the cached object, or null if the object is inactive
     */
    private T cache(T object) {
        T result;
        if (!object.isActive()) {
            removeObject(object);
            result = null;
        } else {
            T added = null;
            synchronized (objects) {
                IMObjectReference ref = object.getObjectReference();
                T current = objects.get(ref);
                if (current == null || current.getVersion() < object.getVersion()) {
                    objects.put(ref, object);
                    added(object);
                    added = object;
                    result = object;
                } else {
                    result = current;
                }
            }
            if (added != null) {
                added(object);
            }
        }
        return result;
    }

}
