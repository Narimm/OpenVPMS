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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.hl7.laboratory.LaboratoryOrderService;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientEventServices;
import org.openvpms.hl7.util.HL7Archetypes;

import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link LaboratoryOrderServiceImpl} class.
 *
 * @author Tim Anderson
 */
public class LaboratoryOrderServiceImplTestCase extends AbstractServiceTest {

    /**
     * The order service.
     */
    private LaboratoryOrderService orderService;

    /**
     * The laboratory.
     */
    private Entity lab;

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
        lab = (Entity) create(HL7Archetypes.LABORATORY);
        EntityBean bean = new EntityBean(lab);
        bean.addNodeTarget("sender", getSender().getReference());
        bean.addNodeTarget("location", getContext().getLocation());

        PatientEventServices eventServices = Mockito.mock(PatientEventServices.class);
        Laboratories labs = new LaboratoriesImpl(getArchetypeService(), getConnectors(), eventServices) {

            @Override
            public Entity getService(Entity group, IMObjectReference location) {
                return lab;
            }
        };
        orderService = new LaboratoryOrderServiceImpl(getArchetypeService(), getLookupService(), labs,
                                                      getDispatcher());

        user = TestHelper.createUser();

        Lookup canine = TestHelper.getLookup(PatientArchetypes.SPECIES, "CANINE");
        HL7TestHelper.removeRelationships(canine);
        Lookup idexxCanine = TestHelper.getLookup("lookup.speciesIDEXX", "CANINE", "Canine", true);
        HL7TestHelper.addMapping(canine, idexxCanine);

        PatientContext context = getContext();
        Mockito.when(context.getPatientId()).thenReturn(1001L);
        Mockito.when(context.getClinicianId()).thenReturn(2001L);
        Mockito.when(context.getCustomerId()).thenReturn(3001L);
    }

    /**
     * Tests the {@link LaboratoryOrderService#createOrder(PatientContext, long, String, Date, Entity, User)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreateOrder() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|IDEXX|IDEXX|20140825085900||ORM^O01^ORM_O01|1200022|P|2.5||||||UTF-8\r" +
                          "PID|1||1001||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|NW|10231|||||||20140825090200|||2001^Blogs^Joe\r" +
                          "OBR|1|10231||SERVICE_ID||20140825090200\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        assertTrue(orderService.createOrder(getContext(), 10231, "SERVICE_ID", date, lab, user));
        assertTrue(getDispatcher().waitForMessage());
        checkMessage(expected);
    }

    /**
     * Tests the {@link LaboratoryOrderService#createOrder(PatientContext, long, String, Date, Entity, User)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCancelOrder() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|IDEXX|IDEXX|20140825085900||ORM^O01^ORM_O01|1200022|P|2.5||||||UTF-8\r" +
                          "PID|1||1001||Bar^Fido||20140701000000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Canine\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500\r" +
                          "AL1|1|MA|^Penicillin|U|Respiratory distress\r" +
                          "AL1|2|MA|^Pollen|U|Produces hives\r" +
                          "ORC|CA|10231|||||||20140825090200|||2001^Blogs^Joe\r" +
                          "OBR|1|10231||SERVICE_ID||20140825090200\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        orderService.cancelOrder(getContext(), 10231, "SERVICE_ID", date, lab, user);
        assertTrue(getDispatcher().waitForMessage());
        checkMessage(expected);
    }

    /**
     * Creates an MLLP sender.
     *
     * @return a new sender
     */
    @Override
    protected MLLPSender createSender() {
        return HL7TestHelper.createSender(-1, HL7TestHelper.createIDEXXMapping(), "IDEXX", "IDEXX");
    }
}
