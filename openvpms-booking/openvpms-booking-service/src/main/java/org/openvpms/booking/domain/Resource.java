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
 * Booking resource.
 *
 * @author Tim Anderson
 */
class Resource {

    /**
     * The identifier.
     */
    private final long id;

    /**
     * The name.
     */
    private final String name;

    /**
     * Constructs a {@link Resource}.
     *
     * @param id   the identifier
     * @param name the name
     */
    public Resource(long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the entity identifier.
     *
     * @return the identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the entity name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
