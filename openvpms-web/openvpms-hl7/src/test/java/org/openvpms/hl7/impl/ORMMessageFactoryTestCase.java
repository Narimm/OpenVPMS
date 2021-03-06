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
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.hl7.patient.PatientContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.openvpms.hl7.impl.PopulateHelper.populateDTM;

/**
 * Tests the {@link ORMMessageFactory} class.
 *
 * @author Tim Anderson
 */
public class ORMMessageFactoryTestCase extends AbstractMessageTest {

    /**
     * The message factory.
     */
    private ORMMessageFactory messageFactory;


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

        messageFactory = new ORMMessageFactory(hapiContext, getArchetypeService(), getLookupService());

        PatientContext context = getContext();
        Mockito.when(context.getPatientId()).thenReturn(1001L);
        Mockito.when(context.getClinicianId()).thenReturn(2001L);
        Mockito.when(context.getCustomerId()).thenReturn(3001L);
    }

    /**
     * Tests the {@link RDEMessageFactory#createOrder(PatientContext, Product, BigDecimal, long, String, Date,
     * HL7Mapping)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreateOrder() throws Exception {
        String expected = "MSH|^~\\&|||||20140825090000.105+1000||ORM^O01^ORM_O01|1200022|P|2.5\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|NW|123456|||||||20140825090200.11+1000|||2001^Blogs^Joe\r" +
                          "OBR|1|123456||SOME_SERVICE||20140825090200.11+1000\r";

        HL7Mapping config = new HL7Mapping();
        Date date = getDatetime("2014-08-25 09:02:00.110").getTime();
        Message order = messageFactory.createOrder(getContext(), 123456, "SOME_SERVICE", date, config);
        MSH msh = (MSH) order.get("MSH");
        populateDTM(msh.getDateTimeOfMessage().getTime(), getDatetime("2014-08-25 09:00:00.105"), config);
        String encode = order.encode();
        assertEquals(expected, encode);
    }

}
