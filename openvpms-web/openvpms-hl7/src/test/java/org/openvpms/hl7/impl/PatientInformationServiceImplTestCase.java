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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientInformationService;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PatientInformationServiceImpl}.
 *
 * @author Tim Anderson
 */
public class PatientInformationServiceImplTestCase extends AbstractServiceTest {

    /**
     * The admission service.
     */
    private PatientInformationService service;

    /**
     * The user.
     */
    private User user;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        user = TestHelper.createUser();
        service = new PatientInformationServiceImpl(getArchetypeService(), getLookupService(), getEventServices(),
                                                    getDispatcher());

        PatientContext context = getContext();
        Mockito.when(context.getPatientId()).thenReturn(1001L);
        Mockito.when(context.getClinicianId()).thenReturn(2001L);
    }

    /**
     * Tests the {@link PatientInformationService#admitted(PatientContext, org.openvpms.component.business.domain.im.security.User)} method.
     *
     * @throws Exception
     */
    @Test
    public void testAdmitted() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||ADT^A01^ADT_A01|1200022|P|2.5||||||UTF-8\r" +
                          "EVN|A01|20140825085900+1000|||||Main Clinic\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r";

        service.admitted(getContext(), user);
        assertTrue(getDispatcher().waitForMessage());
        checkMessage(expected);
    }

    /**
     * Tests the {@link PatientInformationService#admissionCancelled(PatientContext, org.openvpms.component.business.domain.im.security.User)} method.
     *
     * @throws Exception
     */
    @Test
    public void testAdmissionCancelled() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||ADT^A11^ADT_A09|1200022|P|2.5||||||UTF-8\r" +
                          "EVN|A11|20140825085900+1000|||||Main Clinic\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r";

        service.admissionCancelled(getContext(), user);
        assertTrue(getDispatcher().waitForMessage());
        checkMessage(expected);
    }

    /**
     * Tests the {@link PatientInformationService#discharged(PatientContext, org.openvpms.component.business.domain.im.security.User)} method.
     *
     * @throws Exception
     */
    @Test
    public void testDischarged() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||ADT^A03^ADT_A03|1200022|P|2.5||||||UTF-8\r" +
                          "EVN|A03|20140825085900+1000|||||Main Clinic\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r";

        service.discharged(getContext(), user);
        assertTrue(getDispatcher().waitForMessage());
        checkMessage(expected);
    }

    /**
     * Tests the {@link PatientInformationService#updated(PatientContext, org.openvpms.component.business.domain.im.security.User)} method.
     *
     * @throws Exception
     */
    @Test
    public void testUpdated() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||ADT^A08^ADT_A01|1200022|P|2.5||||||UTF-8\r" +
                          "EVN|A08|20140825085900+1000|||||Main Clinic\r" +
                          "PID|1|1001|||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "OBX|1|NM|3141-9^BODY WEIGHT MEASURED^LN||10|kg^kilogram||||||||20140825085700+1000\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r";

        service.updated(getContext(), user);
        assertTrue(getDispatcher().waitForMessage());
        checkMessage(expected);
    }

}
