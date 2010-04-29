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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.patient;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.Date;


/**
 * Tests the {@link PatientRelationshipRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientRelationshipRulesTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link PatientRelationshipRules#checkRelationships(Party)}
     * method. Requires that the
     * <tt>archetypeService.save.party.patientpet.before</tt> rule is
     * configured.
     */
    @Test
    public void testCheckRelationships() {
        Party patient = TestHelper.createPatient();
        PatientRules rules = new PatientRules();
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
}
