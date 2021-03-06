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

package org.openvpms.insurance.internal.policy;

import org.junit.Test;
import org.openvpms.archetype.rules.insurance.InsuranceTestHelper;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.domain.customer.Customer;
import org.openvpms.domain.patient.Patient;
import org.openvpms.insurance.policy.Policy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openvpms.archetype.test.TestHelper.createActIdentity;
import static org.openvpms.archetype.test.TestHelper.createEmailContact;
import static org.openvpms.archetype.test.TestHelper.createLocationContact;
import static org.openvpms.archetype.test.TestHelper.createPhoneContact;

/**
 * Tests the {@link PolicyImpl} class.
 *
 * @author Tim Anderson
 */
public class PolicyImplTestCase extends ArchetypeServiceTest {

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules patientRules;

    /**
     * Tests the {@link PolicyImpl} methods.
     */
    @Test
    public void testPolicy() {
        Contact address = createLocationContact("12 Broadwater Avenue", "CAPE_WOOLAMAI", "VIC", "3925");
        Contact home = createPhoneContact("03", "9123456", false, false, ContactArchetypes.HOME_PURPOSE);
        Contact work = createPhoneContact("03", "9123456", false, false, ContactArchetypes.WORK_PURPOSE);
        Contact mobile = createPhoneContact(null, "04987654321", true);
        Contact email = createEmailContact("foo@test.com");
        Party customer = TestHelper.createCustomer("MS", "J", "Bloggs", address, home, work, mobile, email);

        Date dateOfBirth = DateRules.getDate(DateRules.getToday(), -1, DateUnits.YEARS);
        Party patient = PatientTestHelper.createPatient("Fido", "CANINE", "PUG", "MALE", dateOfBirth, "123454321",
                                                        "BLACK", customer);
        Party insurer = (Party) InsuranceTestHelper.createInsurer(TestHelper.randomName("ZInsurer-"));
        Act act = (Act) InsuranceTestHelper.createPolicy(customer, patient, insurer,
                                                         createActIdentity("actIdentity.insurancePolicy", "123456"));

        PartyRules partyRules = new PartyRules(getArchetypeService(), getLookupService());
        Policy policy = new PolicyImpl(act, (IArchetypeRuleService) getArchetypeService(), partyRules, patientRules);
        assertEquals("123456", policy.getPolicyNumber());
        assertEquals(act.getActivityEndTime(), DateRules.toDate(policy.getExpiryDate()));

        Customer policyHolder = policy.getPolicyHolder();
        assertNotNull(policyHolder);
        assertEquals("Ms J Bloggs", policyHolder.getFullName());
        assertEquals(address, policyHolder.getAddress());
        assertEquals(work, policyHolder.getWorkPhone());
        assertEquals(home, policyHolder.getHomePhone());
        assertEquals(mobile, policyHolder.getMobilePhone());
        assertEquals(email, policyHolder.getEmail());

        Patient animal = policy.getAnimal();
        assertNotNull(animal);
        assertEquals(patient.getId(), animal.getId());
        assertEquals("Fido", animal.getName());
        assertEquals(dateOfBirth, DateRules.toDate(animal.getDateOfBirth()));
        assertEquals("Canine", animal.getSpeciesName());
        assertEquals("Pug", animal.getBreedName());
        assertEquals(Patient.Sex.MALE, animal.getSex());
        assertEquals("123454321", animal.getMicrochip().getIdentity());
        assertEquals("Black", animal.getColourName());

        assertEquals(insurer, policy.getInsurer());
    }
}
