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

package org.openvpms.insurance.service;

import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.party.Party;

import java.util.List;

/**
 * Service for querying and creating insurers.
 *
 * @author Tim Anderson
 */
public interface Insurers {

    /**
     * Returns an insurer.
     *
     * @param archetype the insurer identity archetype
     * @param insurerId the insurer identifier
     * @return the insurer, or {@code null} if none is found. The returned insurer may be inactive
     */
    Party getInsurer(String archetype, String insurerId);

    /**
     * Returns all insurers with the specified insurer identity archetype.
     *
     * @param archetype  the insurer identity archetype
     * @param activeOnly if {@code true}, only return active insurers
     * @return the insurers
     */
    List<Party> getInsurers(String archetype, boolean activeOnly);

    /**
     * Creates and saves an insurer.
     *
     * @param archetype        the insurer identity archetype
     * @param insurerId        the insurer identifier. This must be unique
     * @param name             the insurer name
     * @param description      the insurer description. May be {@code null}
     * @param insuranceService the service that manages claims for this insurer
     * @return a new insurer
     */
    Party createInsurer(String archetype, String insurerId, String name, String description, Entity insuranceService);

    /**
     * Returns the insurer identifier.
     *
     * @param insurer the insurer
     * @return the insurer identifier, or {@code null} if the insurer doesn't have one
     */
    String getInsurerId(Party insurer);

}
