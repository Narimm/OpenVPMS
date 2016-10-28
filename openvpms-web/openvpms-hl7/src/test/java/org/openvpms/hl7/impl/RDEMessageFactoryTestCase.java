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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.util.idgenerator.IDGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.hl7.patient.PatientContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.openvpms.hl7.impl.PopulateHelper.populateDTM;

/**
 * Tests the {@link RDEMessageFactory} class.
 *
 * @author Tim Anderson
 */
public class RDEMessageFactoryTestCase extends AbstractMessageTest {

    /**
     * The message factory.
     */
    private RDEMessageFactory messageFactory;

    /**
     * The product.
     */
    private Product product;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();

        HapiContext hapiContext = HapiContextFactory.create(new IDGenerator() {
            @Override
            public String getID() throws IOException {
                return "1200022";
            }
        });

        messageFactory = new RDEMessageFactory(hapiContext, getArchetypeService(), getLookupService());

        product = TestHelper.createProduct();
        product.setName("Valium 2mg");
        IMObjectBean productBean = new IMObjectBean(product);
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
     * Tests the {@link RDEMessageFactory#createOrder(PatientContext, Product, BigDecimal, long, String, Date, HL7Mapping)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreateOrder() throws Exception {
        String expected = "MSH|^~\\&|||||20140825090000.105||RDE^O11^RDE_O11|1200022|P|2.5\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|NW|10231|||||||20140825090200.11|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        HL7Mapping config = new HL7Mapping();
        config.setPopulatePID2(true);
        config.setPopulatePID3(false);
        config.setIncludeTimeZone(false);
        Date date = getDatetime("2014-08-25 09:02:00.110").getTime();
        Message order = messageFactory.createOrder(getContext(), product, BigDecimal.valueOf(2), 10231, "", date, config);
        MSH msh = (MSH) order.get("MSH");
        populateDTM(msh.getDateTimeOfMessage().getTime(), getDatetime("2014-08-25 09:00:00.105"), config);
        String encode = order.encode();
        assertEquals(expected, encode);
    }

    /**
     * Tests the {@link RDEMessageFactory#updateOrder(PatientContext, Product, BigDecimal, long, String, Date, HL7Mapping)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testUpdateOrder() throws Exception {
        String expected = "MSH|^~\\&|||||20140825090000.105||RDE^O11^RDE_O11|1200022|P|2.5\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|RP|10231|||||||20140825090200|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        HL7Mapping config = new HL7Mapping();
        config.setPopulatePID2(true);
        config.setPopulatePID3(false);
        config.setIncludeTimeZone(false);
        Message order = messageFactory.updateOrder(getContext(), product, BigDecimal.valueOf(2), 10231, "", date, config);
        MSH msh = (MSH) order.get("MSH");
        populateDTM(msh.getDateTimeOfMessage().getTime(), getDatetime("2014-08-25 09:00:00.105"), config);
        String encode = order.encode();
        assertEquals(expected, encode);
    }

    /**
     * Tests the {@link RDEMessageFactory#cancelOrder(PatientContext, Product, BigDecimal, long, String, HL7Mapping, Date)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCancelOrder() throws Exception {
        String expected = "MSH|^~\\&|||||20140825090000||RDE^O11^RDE_O11|1200022|P|2.5\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|CA|10231|||||||20140825090200|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        HL7Mapping config = new HL7Mapping();
        config.setPopulatePID2(true);
        config.setPopulatePID3(false);
        config.setIncludeMillis(false);
        config.setIncludeTimeZone(false);
        Message order = messageFactory.cancelOrder(getContext(), product, BigDecimal.valueOf(2), 10231, "", config, date);
        MSH msh = (MSH) order.get("MSH");
        populateDTM(msh.getDateTimeOfMessage().getTime(), getDatetime("2014-08-25 09:00:00.105"), config);
        String encode = order.encode();
        assertEquals(expected, encode);
    }

    /**
     * Tests the {@link RDEMessageFactory#discontinueOrder(PatientContext, Product, BigDecimal, long, String, HL7Mapping, Date)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDiscontinueOrder() throws Exception {
        String expected = "MSH|^~\\&|||||20140825090000||RDE^O11^RDE_O11|1200022|P|2.5\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|DC|10231|||||||20140825090200|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        HL7Mapping config = new HL7Mapping();
        config.setPopulatePID2(true);
        config.setPopulatePID3(false);
        config.setIncludeMillis(false);
        config.setIncludeTimeZone(false);
        Message order = messageFactory.discontinueOrder(getContext(), product, BigDecimal.valueOf(2), 10231, "", config,
                                                        date);
        MSH msh = (MSH) order.get("MSH");
        populateDTM(msh.getDateTimeOfMessage().getTime(), getDatetime("2014-08-25 09:00:00.105"), config);
        String encode = order.encode();
        assertEquals(expected, encode);
    }

    /**
     * Verifies that merchandise products can be ordered.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreateOrderForMerchandise() throws Exception {
        String expected = "MSH|^~\\&|||||20140825090000.105||RDE^O11^RDE_O11|1200022|P|2.5\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|NW|10231|||||||20140825090200.11|2001^Blogs^Joe\r" +
                          "RXO|4001^Azostix^OpenVPMS||||||||||2|BOX^Box^OpenVPMS\r";

        Product product = TestHelper.createProduct(ProductArchetypes.MERCHANDISE, null);
        product.setName("Azostix");
        IMObjectBean productBean = new IMObjectBean(product);
        productBean.setValue("sellingUnits", TestHelper.getLookup("lookup.uom", "BOX", "Box", true).getCode());
        product.setId(4001);

        HL7Mapping config = new HL7Mapping();
        config.setPopulatePID2(true);
        config.setPopulatePID3(false);
        config.setIncludeTimeZone(false);
        Date date = getDatetime("2014-08-25 09:02:00.110").getTime();
        Message order = messageFactory.createOrder(getContext(), product, BigDecimal.valueOf(2), 10231, "", date,
                                                   config);
        MSH msh = (MSH) order.get("MSH");
        populateDTM(msh.getDateTimeOfMessage().getTime(), getDatetime("2014-08-25 09:00:00.105"), config);
        String encode = order.encode();
        assertEquals(expected, encode);
    }

}
