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

package org.openvpms.web.workspace.customer;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link PatientRelationshipStateQuery} class.
 *
 * @author Tim Anderson
 */
public class PatientRelationshipStateQueryTestCase extends AbstractAppTest {

    /**
     * Tests querying a customer's patient-owner relationships.
     */
    @Test
    public void testQueryForCustomer() {
        Party customer = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient(customer);
        Party patient2 = TestHelper.createPatient(customer);
        Party patient3 = TestHelper.createPatient(customer);

        IMObjectBean patientBean = new IMObjectBean(patient2);
        patientBean.setValue("deceased", true);
        patientBean.save();

        patient3.setActive(false);
        save(patient3);

        EntityBean bean = new EntityBean(customer);
        EntityRelationship rel1 = bean.getRelationship(patient1);
        EntityRelationship rel2 = bean.getRelationship(patient2);
        EntityRelationship rel3 = bean.getRelationship(patient3);

        List<IMObject> relationships = new ArrayList<IMObject>(bean.getRelationships(PatientArchetypes.PATIENT_OWNER));
        Map<IMObjectRelationship, RelationshipState> results = getOwnerRelationships(customer, relationships);
        assertEquals(3, results.size());
        checkResults(results, rel1, customer, patient1, false);
        checkResults(results, rel2, customer, patient2, true);
        checkResults(results, rel3, customer, patient3, false);
    }

    /**
     * Tests querying a patient's owner relationships.
     */
    @Test
    public void testQueryForPatient() {
        PatientRules rules = ServiceHelper.getBean(PatientRules.class);
        Party customer1 = TestHelper.createCustomer();
        Party customer2 = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();

        EntityRelationship rel1 = rules.addPatientOwnerRelationship(customer1, patient);
        save(patient, customer1);

        EntityRelationship rel2 = rules.addPatientOwnerRelationship(customer2, patient);
        save(patient, customer2);

        IMObjectBean bean = new IMObjectBean(patient);
        List<IMObject> relationships = bean.getValues("customers", IMObject.class);
        Map<IMObjectRelationship, RelationshipState> results = getOwnerRelationships(patient, relationships);
        assertEquals(2, results.size());
        checkResults(results, rel1, customer1, patient, false);
        checkResults(results, rel2, customer2, patient, false);

        bean.setValue("deceased", true);
        bean.save();
        results = getOwnerRelationships(patient, relationships);
        checkResults(results, rel1, customer1, patient, true);
        checkResults(results, rel2, customer2, patient, true);
    }

    /**
     * Returns the owner relationships for an object as {@link RelationshipState} instances.
     *
     * @param parent        the parent object
     * @param relationships the relationships
     * @return the relationships
     */
    private Map<IMObjectRelationship, RelationshipState> getOwnerRelationships(Party parent,
                                                                               List<IMObject> relationships) {
        PatientRelationshipStateQuery query = new PatientRelationshipStateQuery(
                parent, relationships, new String[]{PatientArchetypes.PATIENT_OWNER});
        return query.query();
    }

    /**
     * Verifies that the results match those expected.
     *
     * @param results      the results
     * @param relationship the relationship to check
     * @param customer     the expected customer
     * @param patient      the expected patient
     * @param deceased     if {@code true} expect the patient to be deceased
     */
    private void checkResults(Map<IMObjectRelationship, RelationshipState> results, EntityRelationship relationship,
                              Party customer, Party patient, boolean deceased) {
        boolean active = !deceased && patient.isActive() && relationship.isActive();
        RelationshipState state = results.get(relationship);
        assertNotNull(relationship);
        assertEquals(customer.getId(), state.getSourceId());
        assertEquals(customer.getObjectReference(), state.getSource());
        assertEquals(customer.getName(), state.getSourceName());
        assertEquals(customer.getDescription(), state.getSourceDescription());
        assertEquals(patient.getId(), state.getTargetId());
        assertEquals(patient.getObjectReference(), state.getTarget());
        assertEquals(patient.getName(), state.getTargetName());
        assertEquals(patient.getDescription(), state.getTargetDescription());
        assertEquals(relationship, state.getRelationship());
        assertEquals(active, state.isActive());
        assertEquals(deceased, ((PatientRelationshipState) state).isDeceased());
    }

}
