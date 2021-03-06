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

package org.openvpms.component.business.service.archetype.helper;

import org.junit.Test;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.model.archetype.ArchetypeDescriptor;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link TypeHelper} class.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class TypeHelperTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the {@link TypeHelper#isA(IMObject, String)} method.
     */
    @Test
    public void testIsASingle() {
        IMObject object = create("party.animalpet");
        assertTrue(TypeHelper.isA(object, "party.animalpet"));
        assertFalse(TypeHelper.isA(object, "party.horsepet"));

        // test wildcards
        assertTrue(TypeHelper.isA(object, "party.animal*"));
        assertTrue(TypeHelper.isA(object, "party.animalp*"));
        assertFalse(TypeHelper.isA(object, "party.horsep*"));
    }

    /**
     * Tests the {@link TypeHelper#isA(IMObject, String[])} method.
     */
    @Test
    public void testIsAMultiple() {
        IMObject object = create("act.customerAccountPayment");
        assertNotNull(object);
        String[] matches = {"act.customerAccountPaymentCash", "act.customerAccountPayment"};
        assertTrue(TypeHelper.isA(object, matches));

        String[] nomatches = {"act.customerAccountPaymentCash", "act.customerAccountPaymentCredit",
                              "act.customerAccountPaymentEFT"};
        assertFalse(TypeHelper.isA(object, nomatches));

        // test wildcards
        String[] wildmatch = {"act.customerEstimation*", "act.customerAccount*"};
        assertTrue(TypeHelper.isA(object, wildmatch));

        String[] wildnomatch = {"act.customerEstimation*", "act.customerInvoice*"};
        assertFalse(TypeHelper.isA(object, wildnomatch));
    }

    /**
     * Tests the {@link TypeHelper#isA(Reference, String)} method.
     */
    @Test
    public void testIsARef() {
        IMObject object = create("party.customerperson");
        assertNotNull(object);
        Reference ref = object.getObjectReference();

        assertTrue(TypeHelper.isA(ref, "party.customerperson"));
        assertFalse(TypeHelper.isA(ref, "party.patientpet"));

        // test wildcards
        assertTrue(TypeHelper.isA(ref, "party.*"));
        assertTrue(TypeHelper.isA(ref, "party.customer*"));
        assertFalse(TypeHelper.isA(ref, "party.patient*"));
    }

    /**
     * Tests the {@link TypeHelper#isA(Reference, String[])} method.
     */
    @Test
    public void testIsARefMultiple() {
        IMObject object = create("act.customerAccountPayment");
        Reference ref = object.getObjectReference();
        assertNotNull(ref);
        String[] matches = {"act.customerAccountPaymentCash", "act.customerAccountPayment"};
        assertTrue(TypeHelper.isA(ref, matches));

        String[] nomatches = {"act.customerAccountPaymentCash", "act.customerAccountPaymentCredit",
                              "act.customerAccountPaymentEFT"};
        assertFalse(TypeHelper.isA(ref, nomatches));

        // test wildcards
        String[] wildmatch = {"act.customerEstimation*", "act.customerAccount*"};
        assertTrue(TypeHelper.isA(ref, wildmatch));

        String[] wildnomatch = {"act.customerEstimation*", "act.customerInvoice*"};
        assertFalse(TypeHelper.isA(ref, wildnomatch));
    }

    /**
     * Tests the {@link TypeHelper#isA(ArchetypeId, String[])} method.
     */
    @Test
    public void testIsAIdMultiple() {
        ArchetypeId id = new ArchetypeId("act.customerAccountPayment.1.0");
        assertNotNull(id);
        String[] matches = {"act.customerAccountPaymentCash", "act.customerAccountPayment"};
        assertTrue(TypeHelper.isA(id, matches));

        String[] nomatches = {"act.customerAccountPaymentCash", "act.customerAccountPaymentCredit",
                              "act.customerAccountPaymentEFT"};
        assertFalse(TypeHelper.isA(id, nomatches));

        // test wildcards
        String[] wildmatch = {"act.customerEstimation*", "act.customerAccount*"};
        assertTrue(TypeHelper.isA(id, wildmatch));

        String[] wildnomatch = {"act.customerEstimation*", "act.customerInvoice*"};
        assertFalse(TypeHelper.isA(id, wildnomatch));
    }

    /**
     * Tests the {@link TypeHelper#isA(ArchetypeDescriptor, String)} method.
     */
    @Test
    public void testIsADescriptor() {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor("party.customerperson");
        assertNotNull(descriptor);

        assertTrue(TypeHelper.isA(descriptor, "party.customerperson"));
        assertFalse(TypeHelper.isA(descriptor, "party.patientpet"));

        // test wildcards
        assertTrue(TypeHelper.isA(descriptor, "party.*"));
        assertTrue(TypeHelper.isA(descriptor, "party.customer*"));
        assertFalse(TypeHelper.isA(descriptor, "party.patient*"));
    }

    /**
     * Tests the {@link TypeHelper#isA(ArchetypeDescriptor, String[])} method.
     */
    @Test
    public void testIsADescriptorMultiple() {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor("act.customerAccountPayment");
        assertNotNull(descriptor);
        String[] matches = {"act.customerAccountPaymentCash", "act.customerAccountPayment"};
        assertTrue(TypeHelper.isA(descriptor, matches));

        String[] nomatches = {"act.customerAccountPaymentCash", "act.customerAccountPaymentCredit",
                              "act.customerAccountPaymentEFT"};
        assertFalse(TypeHelper.isA(descriptor, nomatches));

        // test wildcards
        String[] wildmatch = {"act.customerEstimation*", "act.customerAccount*"};
        assertTrue(TypeHelper.isA(descriptor, wildmatch));

        String[] wildnomatch = {"act.customerEstimation*", "act.customerInvoice*"};
        assertFalse(TypeHelper.isA(descriptor, wildnomatch));
    }

    /**
     * Tests the {@link TypeHelper#matches(ArchetypeId, String)} method.
     */
    @Test
    public void testMatchesId() {
        ArchetypeId id = new ArchetypeId("party.customerperson.1.0");

        assertTrue(TypeHelper.matches(id, "party.customerperson"));
        assertFalse(TypeHelper.matches(id, "party.patientpet"));

        // test wildcards
        assertTrue(TypeHelper.matches(id, "party.*"));
        assertTrue(TypeHelper.matches(id, "party.customer*"));
        assertFalse(TypeHelper.matches(id, "party.patient*"));
    }

    /**
     * Tests the {@link TypeHelper#matches(String, String)} method.
     */
    @Test
    public void testMatches() {
        assertTrue(TypeHelper.matches("party.customerperson", "party.customerperson"));
        assertFalse(TypeHelper.matches("party.customerperson", "party.patientpet"));

        // test wildcards
        assertTrue(TypeHelper.matches("party.customerperson", "party.*"));
        assertTrue(TypeHelper.matches("party.customerperson", "party.customer*"));
        assertFalse(TypeHelper.matches("party.customerperson", "party.patient*"));
    }

    /**
     * Tests the {@link TypeHelper#matches(String[], String)} method.
     */
    @Test
    public void testShortNamesMatches() {
        String[] parties = {"party.customerperson", "party.patientpet"};
        assertTrue(TypeHelper.matches(parties, "party.*"));
        assertTrue(TypeHelper.matches(parties, "party*"));
        assertFalse(TypeHelper.matches(parties, "*.customerperson"));
    }

}
