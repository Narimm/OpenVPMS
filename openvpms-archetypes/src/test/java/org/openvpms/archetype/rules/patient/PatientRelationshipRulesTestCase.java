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

package org.openvpms.archetype.rules.patient;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link PatientRelationshipRules} class.
 *
 * @author Tim Anderson
 */
public class PatientRelationshipRulesTestCase extends ArchetypeServiceTest {

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules rules;

    /**
     * Tests the {@link PatientRelationshipRules#checkRelationships(Party)}
     * method. Requires that the <em>archetypeService.save.party.patientpet.before</em> rule is
     * configured.
     */
    @Test
    public void testCheckRelationships() {
        Party patient = TestHelper.createPatient();
        Party owner1 = TestHelper.createCustomer();
        Party owner2 = TestHelper.createCustomer();
        Party owner3 = TestHelper.createCustomer();

        EntityRelationship r1 = rules.addPatientOwnerRelationship(owner1,
                                                                  patient);
        EntityRelationship r2 = rules.addPatientOwnerRelationship(owner2,
                                                                  patient);
        r1.setActiveEndTime(new Date(r2.getActiveStartTime().getTime() - 1000));
        save(patient);

        assertNotNull(r1.getActiveEndTime());
        assertNull(r2.getActiveEndTime());

        EntityRelationship r3 = rules.addPatientOwnerRelationship(owner3,
                                                                  patient);
        save(patient);
        assertNotNull(r2.getActiveEndTime());
        assertNull(r3.getActiveEndTime());
    }

    /**
     * Tests queries returned by {@link PatientRelationshipRules#createPatientRelationshipQuery(Party, String[])}
     * method.
     */
    @Test
    public void testCreatePatientRelationshipQuery() {
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

        IArchetypeQuery query = PatientRelationshipRules.createPatientRelationshipQuery(
                customer, new String[]{PatientArchetypes.PATIENT_OWNER});
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(query);
        assertTrue(iterator.hasNext());
        checkObjectSet(iterator.next(), rel1.getId(), patient1.getId(), patient1.getName(), patient1.getDescription(),
                       true, false);
        checkObjectSet(iterator.next(), rel2.getId(), patient2.getId(), patient2.getName(), patient2.getDescription(),
                       true, true);
        checkObjectSet(iterator.next(), rel3.getId(), patient3.getId(), patient3.getName(), patient3.getDescription(),
                       false, false);
        assertFalse(iterator.hasNext());
    }

    /**
     * Checks an patient relationship object set.
     *
     * @param set            the set to check
     * @param relationshipId the expected relationship id
     * @param patientId      the expected patient id
     * @param name           the expected patient name
     * @param description    the expected patient description
     * @param active         the expected patient active flag
     * @param deceased       the expected patient deceased flag
     */
    private void checkObjectSet(ObjectSet set, long relationshipId, long patientId, String name, String description,
                                boolean active, boolean deceased) {
        assertEquals(relationshipId, set.getLong("relationship.id"));
        assertEquals(patientId, set.getLong("patient.id"));
        assertEquals(name, set.getString("patient.name"));
        assertEquals(description, set.getString("patient.description"));
        assertEquals(active, set.getBoolean("patient.active"));
        assertEquals(deceased, set.getBoolean("patient.deceased"));
    }
}
