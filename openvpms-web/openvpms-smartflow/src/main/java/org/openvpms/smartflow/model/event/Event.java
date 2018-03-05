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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.openvpms.smartflow.client.EventDeserializer;

/**
 * Smart Flow Sheet event.
 *
 * @author Tim Anderson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = EventDeserializer.class)
public abstract class Event<T> {

    /**
     * The clinic API key of the clinic that the event is for.
     */
    private String clinicApiKey;

    /**
     * The type of the event.
     */
    private String eventType;

    /**
     * The event data.
     */
    private T object;

    /**
     * Returns the clinic API key.
     *
     * @return the clinic API key
     */
    public String getClinicApiKey() {
        return clinicApiKey;
    }

    /**
     * Sets the clinic API key.
     *
     * @param clinicApiKey the clinic API key
     */
    public void setClinicApiKey(String clinicApiKey) {
        this.clinicApiKey = clinicApiKey;
    }

    /**
     * Returns the event type.
     *
     * @return the event type
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the event type.
     *
     * @param eventType the event type
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the event data.
     *
     * @return the event data
     */
    public T getObject() {
        return object;
    }

    /**
     * Sets the event data.
     *
     * @param object the event data
     */
    public void setObject(T object) {
        this.object = object;
    }
}
