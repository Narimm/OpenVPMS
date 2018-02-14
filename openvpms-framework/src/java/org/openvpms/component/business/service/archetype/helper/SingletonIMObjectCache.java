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

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

/**
 * Cache that monitors updates to a single object from the {@link IArchetypeService}.
 * <p>
 * This loads the first active object for the given archetype with the lowest id and monitors it, until either
 * it is deleted or set inactive.
 * <br/>
 * The next available active object will then be loaded, if any.
 *
 * @author Tim Anderson
 */
public class SingletonIMObjectCache<T extends IMObject> extends AbstractMonitoringIMObjectCache<T> {

    /**
     * The singleton instance.
     */
    private T singleton;

    /**
     * Constructs an {@link SingletonIMObjectCache}.
     *
     * @param service   the archetype service
     * @param archetype the archetype to cache
     * @param type      the object types
     */
    public SingletonIMObjectCache(IArchetypeService service, String archetype, Class<T> type) {
        super(service, archetype, type);
        loadFirst();
    }

    /**
     * Returns the singleton object.
     *
     * @return the singleton object, or {@code null} if no instance exists
     */
    public synchronized T getObject() {
        if (singleton == null) {
            loadFirst();
        }
        return singleton;
    }

    /**
     * Adds an object to the cache.
     * <p>
     * This implementation delegates to {@link #removeObject(IMObject)} if the object is inactive, otherwise
     * updates the singleton object if:
     * <ul>
     * <li>it is {@code null}; or</li>
     * <li>has the same reference but is an older version; or</li>
     * <li>has a greater identifier than the object.</li>
     * </ul>
     * <p>
     * This last requirement is necessary, as that is the behaviour of the cache when it starts up i.e. it picks the
     * first active object with the lowest id.
     *
     * @param object the object to add
     */
    @Override
    protected synchronized void addObject(T object) {
        if (!object.isActive()) {
            removeObject(object);
        } else if (singleton == null || (singleton.equals(object) && singleton.getVersion() <= object.getVersion())
                   || singleton.getId() > object.getId()) {
            // changes to object relationships may not increment the object version
            singleton = object;
            updated(singleton);
        }
    }

    /**
     * Removes an object.
     * <p>
     * This implementation sets the singleton to {@code null} if the supplied object has the same reference,
     * otherwise the object is ignored.
     *
     * @param object the object to remove
     */
    @Override
    protected synchronized void removeObject(T object) {
        if (ObjectUtils.equals(singleton, object)) {
            singleton = null;
            updated(null);
        }
    }

    /**
     * Invoked when the singleton instance updates.
     * <p>
     * This implementation is a no-op.
     *
     * @param object the object. May be {@code null}
     */
    protected void updated(T object) {

    }

    /**
     * Loads the first active instance.
     */
    private void loadFirst() {
        ArchetypeQuery query = new ArchetypeQuery(getArchetypes(), true, true);
        query.add(Constraints.sort("id"));
        IMObjectQueryIterator<T> iter = new IMObjectQueryIterator<T>(getService(), query);
        if (iter.hasNext()) {
            addObject(iter.next());
        }
    }

}
