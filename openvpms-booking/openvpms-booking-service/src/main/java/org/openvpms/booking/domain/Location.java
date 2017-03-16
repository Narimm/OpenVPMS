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

package org.openvpms.booking.domain;

/**
 * Practice location.
 *
 * @author Tim Anderson
 */
public class Location extends Resource {


    /**
     * The practice location time zone.
     */
    private final String timeZone;

    /**
     * Constructs a {@link Location}.
     *
     * @param id   the practice location identifier
     * @param name the practice location name
     */
    public Location(long id, String name, String timeZone) {
        super(id, name);
        this.timeZone = timeZone;
    }

    /**
     * Returns the practice location time zone.
     *
     * @return the time zone
     */
    public String getTimeZone() {
        return timeZone;
    }

}
