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

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.util.idgenerator.IDGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.hl7.PatientContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.openvpms.hl7.impl.PopulateHelper.populateDTM;

/**
 * Tests the {@link ADTMessageFactory} class.
 *
 * @author Tim Anderson
 */
public class ADTMessageFactoryTestCase extends AbstractMessageTest {

    /**
     * The message factory.
     */
    private ADTMessageFactory messageFactory;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();

        HapiContext hapiContext = new DefaultHapiContext();
        hapiContext.getParserConfiguration().setIdGenerator(new IDGenerator() {
            @Override
            public String getID() throws IOException {
                return "1200022";
            }
        });

        messageFactory = new ADTMessageFactory(hapiContext, getArchetypeService(),
                                               LookupServiceHelper.getLookupService());

        PatientContext context = getContext();
        Mockito.when(context.getPatientId()).thenReturn(1001L);
        Mockito.when(context.getClinicianId()).thenReturn(2001L);
    }

    /**
     * Tests the {@link ADTMessageFactory#createAdmit(PatientContext, MessageConfig)} method.
     *
     * @throws HL7Exception for any HL7 error
     */
    @Test
    public void testCreateAdmit() throws HL7Exception {
        String expected = "MSH|^~\\&|||||20140825090000.105+1000||ADT^A01^ADT_A01|1200022|P|2.5\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r";
        MessageConfig config = new MessageConfig();
        Message admit = messageFactory.createAdmit(getContext(), config);
        MSH msh = (MSH) admit.get("MSH");
        msh.getDateTimeOfMessage().getTime().setValue(getDatetime("2014-08-25 09:00:00.105"));
        String encode = admit.encode();
        assertEquals(expected, encode);
    }

    /**
     * Tests the {@link ADTMessageFactory#createCancelAdmit(PatientContext, MessageConfig)} method.
     *
     * @throws HL7Exception for any HL7 error
     */
    @Test
    public void testCreateCancelAdmit() throws HL7Exception {
        String expected = "MSH|^~\\&|||||20140825090000+1000||ADT^A11^ADT_A09|1200022|P|2.5\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r";
        MessageConfig config = new MessageConfig();
        config.setIncludeMillis(false);
        Message admit = messageFactory.createCancelAdmit(getContext(), config);
        MSH msh = (MSH) admit.get("MSH");
        populateDTM(msh.getDateTimeOfMessage().getTime(), getDatetime("2014-08-25 09:00:00.105"), config);
        String encode = admit.encode();
        assertEquals(expected, encode);
    }

    /**
     * Tests the {@link ADTMessageFactory#createDischarge(PatientContext, MessageConfig)} method.
     *
     * @throws HL7Exception for any HL7 error
     */
    @Test
    public void testCreateDischarge() throws HL7Exception {
        String expected = "MSH|^~\\&|||||20140825090000||ADT^A03^ADT_A03|1200022|P|2.5\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500|20140825100500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700\r";
        MessageConfig config = new MessageConfig();
        config.setIncludeMillis(false);
        config.setIncludeTimeZone(false);
        PatientContext context = getContext();
        Act visit = context.getVisit();
        visit.setActivityEndTime(getDatetime("2014-08-25 10:05:00").getTime());
        Message admit = messageFactory.createDischarge(context, config);
        MSH msh = (MSH) admit.get("MSH");
        populateDTM(msh.getDateTimeOfMessage().getTime(), getDatetime("2014-08-25 09:00:00.105"), config);
        String encode = admit.encode();
        assertEquals(expected, encode);
    }

    /**
     * Tests the {@link ADTMessageFactory#createUpdate(PatientContext, MessageConfig)}.
     *
     * @throws HL7Exception for any HL7 error
     */
    @Test
    public void testCreateUpdate() throws HL7Exception {
        String expected = "MSH|^~\\&|||||20140825090000+1000||ADT^A08^ADT_A01|1200022|P|2.5\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r";

        MessageConfig config = new MessageConfig();
        Message admit = messageFactory.createUpdate(getContext(), config);
        MSH msh = (MSH) admit.get("MSH");
        msh.getDateTimeOfMessage().getTime().setValue(getDatetime("2014-08-25 09:00:00"));
        String encode = admit.encode();
        assertEquals(expected, encode);
    }

}
