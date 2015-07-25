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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.patient.PatientEventServices;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.util.HL7Archetypes;

/**
 * Caches <em>entity.HL7ServicePharmacy</em> objects.
 *
 * @author Tim Anderson
 */
public class PharmaciesImpl extends ServicesImpl implements Pharmacies {

    /**
     * The services that receive patient events.
     */
    private final PatientEventServices services;


    /**
     * Constructs a {@link PharmaciesImpl}.
     *
     * @param service    the archetype service
     * @param connectors the connectors
     * @param services   the services that receive patient events
     */
    public PharmaciesImpl(IArchetypeService service, Connectors connectors, PatientEventServices services) {
        super(service, HL7Archetypes.PHARMACY, Entity.class, false, connectors);
        this.services = services;
        load();
    }

    /**
     * Adds an object to the cache, if it active, and newer than the existing instance, if any.
     *
     * @param object the object to add
     */
    @Override
    protected void addObject(Entity object) {
        services.add(object);  // register the pharmacy to receive patient information
        super.addObject(object);
    }

    /**
     * Removes an object.
     *
     * @param object the object to remove
     */
    @Override
    protected void removeObject(Entity object) {
        services.remove(object);  // de-register the service so it no longer receives patient events
        super.removeObject(object);
    }
}
