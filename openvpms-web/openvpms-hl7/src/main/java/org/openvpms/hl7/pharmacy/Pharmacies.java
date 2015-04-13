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

package org.openvpms.hl7.pharmacy;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.hl7.io.Connector;

import java.util.List;

/**
 * HL7 Pharmacies.
 *
 * @author Tim Anderson
 */
public interface Pharmacies {

    /**
     * Listener for pharmacy events.
     */
    interface Listener {

        /**
         * Invoked when a pharmacy is added or updated.
         *
         * @param pharmacy the pharmacy
         */
        void added(Entity pharmacy);

        /**
         * Invoked when a pharmacy is removed.
         *
         * @param pharmacy the pharmacy
         */
        void removed(Entity pharmacy);
    }

    /**
     * Returns the active pharmacies.
     *
     * @return the pharmacies
     */
    List<Entity> getPharmacies();

    /**
     * Returns a pharmacy given its reference.
     *
     * @param reference the pharmacy reference
     * @return the pharmacy, or {@code null} if none is found
     */
    Entity getPharmacy(IMObjectReference reference);

    /**
     * Returns the pharmacy for a practice location, given the pharmacy group.
     *
     * @param group    the pharmacy group
     * @param location the practice location
     * @return the pharmacy, or {@code null} if none is found
     */
    Entity getPharmacy(Entity group, IMObjectReference location);

    /**
     * Returns the connection to send orders to.
     *
     * @param pharmacy the pharmacy
     * @return the corresponding sender, or {@code null} if none is found
     */
    Connector getOrderConnection(Entity pharmacy);

    /**
     * Adds a listener to be notified of pharmacy updates.
     *
     * @param listener the listener to add
     */
    void addListener(Listener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    void removeListener(Listener listener);


}
