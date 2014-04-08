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

import java.util.Collection;

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
     * If {@code true}, use prices with no pricing group.
     */
    private final boolean useFallback;

    /**
     * Default constructor.
     */
    private PricingGroup() {
        this(null, false, true);
    }

    /**
     * Constructs a {@link PricingGroup}.
     *
     * @param group the pricing group lookup. If {@code null} indicates no pricing group
     */
    public PricingGroup(Lookup group) {
        this(group, true);
    }

    /**
     * Constructs a {@link PricingGroup}.
     *
     * @param group       the pricing group lookup. If {@code null} indicates no pricing group
     * @param useFallback if {@code true}, use prices that have no pricing group
     */
    public PricingGroup(Lookup group, boolean useFallback) {
        this(group, useFallback, false);
    }

    /**
     * Constructs an {@link PricingGroup}.
     *
     * @param group       the group lookup. If {@code null} indicates no group
     * @param useFallback if {@code true}, use prices that have no pricing group
     * @param all         if {@code true}, matches all pricing groups
     */
    private PricingGroup(Lookup group, boolean useFallback, boolean all) {
        this.group = group;
        this.useFallback = useFallback;
        this.all = all;
    }

    /**
     * Returns the pricing group to query.
     * <p/>
     * If there is no group and {@link #isAll()} is {@code false}, this matches prices that have no pricing group.
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
     * Determines if prices that don't have any pricing group should be used if there is no exact match.
     *
     * @return {@code true} if prices with no pricing group should be returned
     */
    public boolean useFallback() {
        return useFallback;
    }

    /**
     * Determines if this group matches the specified groups.
     *
     * @param groups the groups
     * @return {@code true} if the groups match
     */
    public boolean matches(Collection<Lookup> groups) {
        if (all) {
            return true;
        } else if (group == null) {
            return groups.isEmpty();
        } else if ((groups.isEmpty() && useFallback) || groups.contains(group)) {
            return true;
        }
        return false;
    }
}
