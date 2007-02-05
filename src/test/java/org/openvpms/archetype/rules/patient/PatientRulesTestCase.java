/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;


/**
 * Tests the {@link PatientRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private PatientRules rules;


    /**
     * Tests the {@link PatientRules#getOwner} method.
     */
    public void testGetOwner() {
        Party patient1 = TestHelper.createPatient();
        assertNull(rules.getOwner(patient1));

        Party customer = TestHelper.createCustomer();
        Party patient2 = TestHelper.createPatient();
        rules.addPatientOwnerRelationship(customer, patient2);
        assertEquals(customer, rules.getOwner(patient2));


        deactivateOwnerRelationship(patient2);
        assertNull(rules.getOwner(patient2));
    }

    /**
     * Tests the {@link PatientRules#isOwner} method.
     */
    public void testIsOwner() {
        Party patient1 = TestHelper.createPatient();
        Party customer = TestHelper.createCustomer();
        Party patient2 = TestHelper.createPatient();
        rules.addPatientOwnerRelationship(customer, patient2);

        assertFalse(rules.isOwner(customer, patient1));
        assertTrue(rules.isOwner(customer, patient2));

        deactivateOwnerRelationship(patient2);
        assertFalse(rules.isOwner(customer, patient2));
    }

    /**
     * Tests the {@link PatientRules#getReferralVet} method.
     */
    public void testGetReferralVet() {
        Party patient = TestHelper.createPatient(false);
        Party vet = TestHelper.createSupplierVet();
        EntityBean bean = new EntityBean(patient);
        EntityRelationship referral
                = bean.addRelationship("entityRelationship.referredFrom", vet);
        PatientRules rules = new PatientRules();

        // verify the vet is returned for a time > the default start time
        Party vet2 = rules.getReferralVet(patient, new Date());
        assertEquals(vet, vet2);

        // verify no vet returned if the relationship has no start time
        referral.setActiveStartTime(null);
        assertNull(rules.getReferralVet(patient, new Date()));

        // now set the start and end time and verify that there is no referrer
        // for a later time (use time addition due to system clock granularity)
        Date start = new Date();
        Date end = new Date(start.getTime() + 1);
        Date later = new Date(end.getTime() + 1);
        referral.setActiveStartTime(start);
        referral.setActiveEndTime(end);
        assertNull(rules.getReferralVet(patient, later));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        rules = new PatientRules(getArchetypeService());
    }

    /**
     * Marks the patient-owner relationship inactive.
     *
     * @param patient the patient
     */
    private void deactivateOwnerRelationship(Party patient) {
        for (EntityRelationship relationship
                : patient.getEntityRelationships()) {
            if (TypeHelper.isA(relationship,
                               "entityRelationship.patientOwner")) {
                relationship.setActive(false);
            }
        }
    }

}
