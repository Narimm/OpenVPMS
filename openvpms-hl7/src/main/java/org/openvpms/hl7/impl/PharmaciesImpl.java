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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.patient.PatientEventServices;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.util.HL7Archetypes;

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
     * The services that receive patient events.
     */
    private final PatientEventServices services;

    /**
     * The connectors.
     */
    private final Connectors connectors;

    /**
     * Constructs a {@link PharmaciesImpl}.
     *
     * @param service    the archetype service
     * @param connectors the connectors
     * @param services   the services that receive patient events
     */
    public PharmaciesImpl(IArchetypeService service, Connectors connectors, PatientEventServices services) {
        super(service, HL7Archetypes.PHARMACY, Entity.class, false);
        this.connectors = connectors;
        this.services = services;
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
     * Returns a pharmacy given its reference.
     *
     * @param reference the pharmacy reference
     * @return the pharmacy, or {@code null} if none is found
     */
    @Override
    public Entity getPharmacy(IMObjectReference reference) {
        return getObject(reference);
    }

    /**
     * Returns the pharmacy for a practice location, given the pharmacy group.
     *
     * @param group    the pharmacy group
     * @param location the practice location
     * @return the pharmacy, or {@code null} if none is found
     */
    @Override
    public Entity getPharmacy(Entity group, IMObjectReference location) {
        EntityBean bean = new EntityBean(group, getService());
        for (IMObjectReference ref : bean.getNodeTargetEntityRefs("pharmacies")) {
            Entity pharmacy = getPharmacy(ref);
            if (pharmacy != null && hasLocation(pharmacy, location)) {
                return pharmacy;
            }
        }
        return null;
    }

    /**
     * Returns the connection to send orders to.
     *
     * @param pharmacy the pharmacy
     * @return the corresponding sender, or {@code null} if none is found
     */
    @Override
    public Connector getOrderConnection(Entity pharmacy) {
        EntityBean bean = new EntityBean(pharmacy, getService());
        IMObjectReference ref = bean.getNodeTargetObjectRef("sender");
        return (ref != null) ? connectors.getConnector(ref) : null;
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
    protected void addObject(Entity object) {
        super.addObject(object);
        services.add(object);  // register the pharmacy to receive patient information
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
    protected void removeObject(Entity object) {
        super.removeObject(object);
        services.remove(object);  // de-register the pharmacy so it no longer receives patient events
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

    /**
     * Determines if a pharmacy is used to order for a particular practice location.
     *
     * @param pharmacy the pharmacy
     * @param location the location
     * @return {@code true} if the pharmacy is used to order for the location
     */
    private boolean hasLocation(Entity pharmacy, IMObjectReference location) {
        EntityBean bean = new EntityBean(pharmacy, getService());
        return ObjectUtils.equals(location, bean.getNodeTargetObjectRef("location"));
    }
}
