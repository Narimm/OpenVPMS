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

package org.openvpms.hl7.service;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.hl7.io.Connector;

import java.util.List;

/**
 * HL7 Service configurations.
 *
 * @author Tim Anderson
 */
public interface Services {

    /**
     * Listener for configuration events.
     */
    interface Listener {

        /**
         * Invoked when a service is added or updated.
         *
         * @param service the service configuration
         */
        void added(Entity service);

        /**
         * Invoked when a service is removed.
         *
         * @param service the service
         */
        void removed(Entity service);
    }

    /**
     * Returns the active services.
     *
     * @return the service configurations
     */
    List<Entity> getServices();

    /**
     * Returns a service given its reference.
     *
     * @param reference the service reference
     * @return the service configuration, or {@code null} if none is found
     */
    Entity getService(IMObjectReference reference);

    /**
     * Returns the service for a practice location, given the service group.
     *
     * @param group    the service group
     * @param location the practice location
     * @return the service configuration, or {@code null} if none is found
     */
    Entity getService(Entity group, IMObjectReference location);

    /**
     * Returns the connector to send messages to the service.
     *
     * @param service the service
     * @return the corresponding sender, or {@code null} if none is found
     */
    Connector getSender(Entity service);

    /**
     * Adds a listener to be notified of service updates.
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
