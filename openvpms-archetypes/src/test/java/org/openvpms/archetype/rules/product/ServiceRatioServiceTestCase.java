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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.CalendarService;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertNull;

/**
 * Tests the {@link ServiceRatioService}.
 *
 * @author Tim Anderson
 */
public class ServiceRatioServiceTestCase extends ArchetypeServiceTest {

    /**
     * The calendar service.
     */
    private CalendarService calendarService;

    /**
     * Verifies that a service ratio only applies at all times when no calendar is attached.
     */
    @Test
    public void testServiceRatioWithoutCalendar() {
        ProductPriceRules rules = new ProductPriceRules(getArchetypeService());
        ServiceRatioService service = new ServiceRatioService(calendarService, rules);
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();

        // set up a service ratio at location1 for the product type.
        Entity productType = ProductTestHelper.createProductType();
        Product product = ProductTestHelper.createMedication(productType);
        ProductTestHelper.addServiceRatio(location1, productType, BigDecimal.TEN);

        // ratio applies at all times at location1, as no calendar attached
        Date now = new Date();
        Date lastYear = DateRules.getDate(now, -1, DateUnits.YEARS);
        Date nextYear = DateRules.getDate(now, 1, DateUnits.YEARS);
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location1, now));
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location1, lastYear));
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location1, nextYear));

        // no ratio for location2
        assertNull(service.getServiceRatio(product, location2, now));
        assertNull(service.getServiceRatio(product, location2, lastYear));
        assertNull(service.getServiceRatio(product, location2, nextYear));
    }

    /**
     * Verifies that a service ratio only applies where events are present, for service ratios with a calendar.
     */
    @Test
    public void testServiceRatioWithCalendar() {
        ProductPriceRules rules = new ProductPriceRules(getArchetypeService());
        ServiceRatioService service = new ServiceRatioService(calendarService, rules);
        Entity calendar = ProductTestHelper.createServiceRatioCalendar();
        Party location = TestHelper.createLocation();
        Entity productType = ProductTestHelper.createProductType();
        Product product = ProductTestHelper.createMedication(productType);
        ProductTestHelper.addServiceRatio(location, productType, BigDecimal.TEN, calendar);

        BigDecimal ratio1 = service.getServiceRatio(product, location, new Date());
        assertNull(ratio1);

        Date start1 = TestHelper.getDatetime("2018-07-29 22:00:00");
        Date end1 = TestHelper.getDatetime("2018-07-30 07:00:00");
        Date start2 = TestHelper.getDatetime("2018-07-30 22:00:00");
        Date end2 = TestHelper.getDatetime("2018-07-31 07:00:00");
        Date start3 = TestHelper.getDatetime("2018-07-31 07:00:00");
        Date end3 = TestHelper.getDatetime("2018-07-31 12:00:00");
        ProductTestHelper.addServiceRatioEvent(calendar, start1, end1);
        ProductTestHelper.addServiceRatioEvent(calendar, start2, end2);
        ProductTestHelper.addServiceRatioEvent(calendar, start3, end3); // starts right after end2

        Date beforeStart1 = DateRules.getDate(start1, -1, DateUnits.MINUTES);
        Date afterStart1 = DateRules.getDate(start1, 1, DateUnits.MINUTES);
        assertNull(service.getServiceRatio(product, location, beforeStart1));
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location, start1));
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location, afterStart1));

        Date beforeEnd1 = DateRules.getDate(end1, -1, DateUnits.MINUTES);
        Date afterEnd1 = DateRules.getDate(end1, 1, DateUnits.MINUTES);
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location, beforeEnd1));
        assertNull(service.getServiceRatio(product, location, end1));
        assertNull(service.getServiceRatio(product, location, afterEnd1));

        Date beforeStart2 = DateRules.getDate(start2, -1, DateUnits.MINUTES);
        Date afterStart2 = DateRules.getDate(start2, 1, DateUnits.MINUTES);
        assertNull(service.getServiceRatio(product, location, beforeStart2));
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location, start2));
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location, afterStart2));

        Date beforeEnd2 = DateRules.getDate(end2, -1, DateUnits.MINUTES);
        Date afterEnd2 = DateRules.getDate(end2, 1, DateUnits.MINUTES);
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location, beforeEnd2));
        checkEquals(BigDecimal.TEN, (service.getServiceRatio(product, location, end2)));   // runs into start3 event
        checkEquals(BigDecimal.TEN, service.getServiceRatio(product, location, afterEnd2));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        calendarService = new CalendarService(getArchetypeService(), ScheduleTestHelper.createCache(10));
    }

    /**
     * Cleans up after the test.
     *
     * @throws Exception for any error
     */
    @After
    public void tearDown() throws Exception {
        calendarService.destroy();
    }

}
