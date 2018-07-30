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

import org.openvpms.archetype.rules.workflow.CalendarService;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.product.Product;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Service ratio service.
 *
 * @author Tim Anderson
 */
public class ServiceRatioService {

    /**
     * The calendar service.
     */
    private final CalendarService calendarService;

    /**
     * The product price rules.
     */
    private final ProductPriceRules rules;

    /**
     * Constructs a {@link ServiceRatioService}.
     *
     * @param calendarService the calendar service
     * @param rules           the product price rules
     */
    public ServiceRatioService(CalendarService calendarService, ProductPriceRules rules) {
        this.calendarService = calendarService;
        this.rules = rules;
    }

    /**
     * Returns the service ratio for a product at the specified location and date/time.
     *
     * @param product  the product
     * @param location the practice location
     * @param date     the date/time
     * @return the service ratio, or {@code null} if no service ratio applies
     */
    public BigDecimal getServiceRatio(Product product, Party location, Date date) {
        BigDecimal result = null;
        ServiceRatio ratio = rules.getServiceRatio((org.openvpms.component.business.domain.im.product.Product) product,
                                                   (org.openvpms.component.business.domain.im.party.Party) location);
        if (ratio != null) {
            Reference calendar = ratio.getCalendar();
            if (calendar != null) {
                Times times = new Times(date, date);
                Times event = calendarService.getOverlappingEvent(times, calendar);
                if (event != null) {
                    result = ratio.getRatio();
                }
            } else {
                result = ratio.getRatio();
            }
        }
        return result;
    }
}
