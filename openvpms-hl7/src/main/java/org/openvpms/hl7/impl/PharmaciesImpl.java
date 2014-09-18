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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.List;

/**
 * Caches <em>party.organisationPharmacy</em> objects.
 *
 * @author Tim Anderson
 */
public class PharmaciesImpl extends MonitoringIMObjectCache<Entity> implements Pharmacies {

    /**
     * Listeners to notify when a pharmacy changes.
     */
    private final List<Listener> listeners = new ArrayList<Listener>();

    /**
     * Constructs a {@link PharmaciesImpl}.
     *
     * @param service the archetype service
     */
    public PharmaciesImpl(IArchetypeService service) {
        super(service, "party.organisationPharmacy", Entity.class, false);
        load();
    }

    /**
     * Returns the active pharmacies.
     *
     * @return the pharmacies
     */
    @Override
    public List<Entity> getPharmacies() {
        return getObjects();
    }

    /**
     * Adds a listener to be notified of pharmacy updates.
     *
     * @param listener the listener to add
     */
    @Override
    public void addListener(Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeListener(Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Adds an object to the cache, if it active, and newer than the existing instance, if any.
     *
     * @param object the object to add
     */
    @Override
    protected void add(Entity object) {
        super.add(object);
        for (Listener listener : getListeners()) {
            listener.added(object);
        }
    }

    /**
     * Removes an object.
     *
     * @param object the object to remove
     */
    @Override
    protected void remove(Entity object) {
        super.remove(object);
        for (Listener listener : getListeners()) {
            listener.removed(object);
        }
    }

    /**
     * Returns the listeners.
     *
     * @return the listeners
     */
    protected Listener[] getListeners() {
        Listener[] result;
        synchronized (listeners) {
            result = listeners.toArray(new Listener[listeners.size()]);
        }
        return result;
    }
}
