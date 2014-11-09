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

package org.openvpms.hl7.patient;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.hl7.io.Connector;

import java.util.Collection;

/**
 * Manages connections for applications that receive patient events via the {@link PatientInformationService}.
 *
 * @author Tim Anderson
 */
public interface PatientEventServices {

    /**
     * Registers a service to be notified of patient events.
     *
     * @param service the service
     */
    void add(Entity service);

    /**
     * Removes a service.
     *
     * @param service the service to remove
     */
    void remove(Entity service);

    /**
     * Returns the connections to services for a given practice location.
     *
     * @param location the practice location
     * @return the connections
     */
    Collection<Connector> getConnections(Party location);

    /**
     * Returns the connections to services for a given practice location.
     *
     * @param location the practice location reference
     * @return the connections
     */
    Collection<Connector> getConnections(IMObjectReference location);

}