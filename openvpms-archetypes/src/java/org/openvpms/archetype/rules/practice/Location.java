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

package org.openvpms.archetype.rules.practice;

import org.openvpms.component.business.domain.im.party.Party;

/**
 * Location filter.
 *
 * @author Tim Anderson
 */
public class Location {

    /**
     * Indicates all locations.
     */
    public static final Location ALL = new Location(null, true);

    /**
     * Indicates customers with no locations.
     */
    public static final Location NONE = new Location(null);

    /**
     * The location to query. May be {@code null}
     */
    private final Party location;

    /**
     * Determines if customers in all locations are being queried.
     */
    private final boolean all;

    /**
     * Constructs a {@link Location}.
     *
     * @param location the location. If {@code null}, indicates to query customers with no location
     */
    public Location(Party location) {
        this(location, false);
    }

    /**
     * Constructs a {@link Location}.
     *
     * @param location the location. If {@code null}, and {@code all} is {@code false} indicates to query customers with
     *                 no location
     * @param all      if {@code true}, indicates to query customers in all locations
     */
    private Location(Party location, boolean all) {
        this.location = location;
        this.all = all;
    }

    /**
     * Returns the location being queried.
     *
     * @return the location. May be {@code null}
     */
    public Party getLocation() {
        return location;
    }

    /**
     * Determines if customers in all locations are being queried.
     *
     * @return {@code true} if all locations are being queried
     */
    public boolean isAll() {
        return all;
    }

    /**
     * Determines if customers with no location are being queried.
     *
     * @return {@code true} if customers with no location are being queried.
     */
    public boolean isNone() {
        return !isAll() && location == null;
    }

}
