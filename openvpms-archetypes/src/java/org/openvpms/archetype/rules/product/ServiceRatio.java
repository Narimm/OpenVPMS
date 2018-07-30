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

package org.openvpms.archetype.rules.product;

import org.openvpms.component.model.object.Reference;

import java.math.BigDecimal;

/**
 * Encapsulates a service ratio that applies to prices at certain times.
 *
 * @author Tim Anderson
 */
public class ServiceRatio {

    /**
     * The service ratio.
     */
    private final BigDecimal ratio;

    /**
     * Service ratio calendar reference. May be {@code null}.
     */
    private final Reference calendar;

    /**
     * Constructs a {@link ServiceRatio}.
     *
     * @param ratio    the ratio
     * @param calendar the calendar (<em>entity.calendarServiceRatio</em>) reference, that determines when the ratio applies. If {@code null}, the ratio always applies
     */
    public ServiceRatio(BigDecimal ratio, Reference calendar) {
        this.ratio = ratio;
        this.calendar = calendar;
    }

    /**
     * Returns the service ratio.
     *
     * @return the ratio
     */
    public BigDecimal getRatio() {
        return ratio;
    }

    /**
     * Returns the reference to the service ratio calendar.
     * <p>
     * Events in this calendar determine when the ratio applies.
     *
     * @return the calendar reference or {@code null}, if the ratio applies at all times
     */
    public Reference getCalendar() {
        return calendar;
    }
}
