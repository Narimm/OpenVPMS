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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.user.User;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;
import org.openvpms.hl7.util.HL7Archetypes;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PharmacyOrderServiceImpl} class.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderServiceImplTestCase extends AbstractServiceTest {

    /**
     * The order service.
     */
    private PharmacyOrderService orderService;

    /**
     * The product to order.
     */
    private Product product;

    /**
     * The pharmacy.
     */
    private Entity pharmacy;

    /**
     * The user.
     */
    private User user;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        pharmacy = (Entity) create(HL7Archetypes.PHARMACY);
        IMObjectBean bean = getBean(pharmacy);
        bean.setTarget("sender", getSender().getReference());
        bean.setTarget("location", getContext().getLocation());

        Pharmacies pharmacies = new PharmaciesImpl(getArchetypeService(), getConnectors(), getEventServices()) {

            @Override
            public Entity getService(Entity group, Reference location) {
                return pharmacy;
            }
        };
        orderService = new PharmacyOrderServiceImpl(getArchetypeService(), getLookupService(), pharmacies,
                                                    getDispatcher());

        user = TestHelper.createUser();

        product = TestHelper.createProduct();
        product.setName("Valium 2mg");
        IMObjectBean productBean = getBean(product);
        productBean.setValue("dispensingUnits", TestHelper.getLookup("lookup.uom", "TAB", "Tablets", true).getCode());
        productBean.setValue("sellingUnits", TestHelper.getLookup("lookup.uom", "BOX", "Box", true).getCode());
        productBean.setValue("dispInstructions", "Give 1 tablet once daily");
        product.setId(4001);

        PatientContext context = getContext();
        Mockito.when(context.getPatientId()).thenReturn(1001L);
        Mockito.when(context.getClinicianId()).thenReturn(2001L);
        Mockito.when(context.getCustomerId()).thenReturn(3001L);
    }

    /**
     * Tests the {@link PharmacyOrderService#createOrder}
     * method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreateOrder() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900||RDE^O11^RDE_O11|1200022|P|2.5||||||UTF-8\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|NW|10231^VPMS|||||||20140825090200|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        assertTrue(orderService.createOrder(getContext(), product, BigDecimal.valueOf(2), 10231, date, pharmacy, user));
        assertTrue(getDispatcher().waitForMessage());
        checkMessage(expected);
    }


    /**
     * Tests the {@link PharmacyOrderService#updateOrder}
     * method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testUpdateOrder() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900||RDE^O11^RDE_O11|1200022|P|2.5||||||UTF-8\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|RP|10231^VPMS|||||||20140825090200|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        orderService.updateOrder(getContext(), product, BigDecimal.valueOf(2), 10231, date, pharmacy, user);
        assertTrue(getDispatcher().waitForMessage());
        checkMessage(expected);
    }

    /**
     * Tests the {@link PharmacyOrderService#updateOrder}
     * method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCancelOrder() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900||RDE^O11^RDE_O11|1200022|P|2.5||||||UTF-8\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|CA|10231^VPMS|||||||20140825090200|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        orderService.cancelOrder(getContext(), product, BigDecimal.valueOf(2), 10231, date, pharmacy, user);
        assertTrue(getDispatcher().waitForMessage());
        checkMessage(expected);
    }
}
