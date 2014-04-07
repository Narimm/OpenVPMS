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

package org.openvpms.archetype.rules.product;

import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.List;

/**
 * Pricing group.
 *
 * @author Tim Anderson
 */
public class PricingGroup {

    /**
     * Indicates all pricing groups.
     */
    public static final PricingGroup ALL = new PricingGroup();

    /**
     * Determines if all groups are being queried.
     */
    private final boolean all;

    /**
     * The pricing group to query. May be {@code null}
     */
    private final Lookup group;

    /**
     * Default constructor.
     */
    private PricingGroup() {
        group = null;
        all = true;
    }

    /**
     * Constructs a {@link PricingGroup}.
     *
     * @param group the group lookup. If {@code null} indicates no group
     */
    public PricingGroup(Lookup group) {
        this.group = group;
        all = false;
    }

    /**
     * Returns the price group to query.
     *
     * @return the price group. May be {@code null}
     */
    public Lookup getGroup() {
        return group;
    }

    /**
     * Determines if all pricing groups are being queried.
     *
     * @return {@code true} if all pricing groups are being queried
     */
    public boolean isAll() {
        return all;
    }

    /**
     * Determines if this group matches the specified groups.
     *
     * @param groups the groups
     * @return {@code true} if the groups match
     */
    public boolean matches(List<Lookup> groups) {
        if (all) {
            return true;
        } else if (group == null) {
            return groups.isEmpty();
        } else if (groups.isEmpty() || groups.contains(group)) {
            return true;
        }
        return false;
    }
}
