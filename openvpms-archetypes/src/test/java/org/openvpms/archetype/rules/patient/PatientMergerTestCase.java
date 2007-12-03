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

import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.party.AbstractPartyMergerTest;
import org.openvpms.archetype.rules.party.MergeException;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

import java.util.Date;


/**
 * Tests the {@link PatientMerger} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientMergerTestCase extends AbstractPartyMergerTest {

    /**
     * Patient rules.
     */
    private PatientRules rules;


    /**
     * Tests that the most recent owner relationship is the active one after
     * the merge.
     */
    public void testMergeOwnerRelationships() {
        Party owner1 = TestHelper.createCustomer();
        Party owner2 = TestHelper.createCustomer();
        Party from = TestHelper.createPatient();
        Party to = TestHelper.createPatient();

        EntityRelationship owner1Rel = rules.addPatientOwnerRelationship(
                owner1, from);
        EntityRelationship owner2Rel = rules.addPatientOwnerRelationship(
                owner2, to);

        // ensure owner1 relationship older than owner2
        owner1Rel.setActiveStartTime(
                new Date(System.currentTimeMillis() - 1000 * 60));
        assertTrue(owner1Rel.getActiveStartTime().before(
                owner2Rel.getActiveStartTime()));

        Party merged = checkMerge(from, to);

        owner1 = get(owner1);
        owner2 = get(owner2);

        // owner2 should now be the owner
        assertTrue(rules.isOwner(owner2, merged));
        assertFalse(rules.isOwner(owner1, merged));
    }

    /**
     * Tests that entity identities are copied.
     */
    public void testMergeEntityIdentities() {
        Party from = TestHelper.createPatient();
        Party to = TestHelper.createPatient();

        EntityIdentity id1 = createIdentity("ABC1234");
        from.addIdentity(id1);

        EntityIdentity id2 = createIdentity("XYZ1234");
        to.addIdentity(id2);

        Party merged = checkMerge(from, to);

        EntityIdentity[] identities
                = merged.getIdentities().toArray(new EntityIdentity[0]);
        String idA = identities[0].getIdentity();
        String idB = identities[1].getIdentity();
        assertTrue(id1.getIdentity().equals(idA)
                || id1.getIdentity().equals(idB));
        assertTrue(id2.getIdentity().equals(idA)
                || id2.getIdentity().equals(idB));
    }

    /**
     * Verifies that participations are moved to the merged patient.
     */
    public void testMergeParticipations() {
        Party customer = TestHelper.createCustomer();
        Party from = TestHelper.createPatient();
        Party to = TestHelper.createPatient();
        Product product = TestHelper.createProduct();

        assertEquals(0, countParticipations(from));
        assertEquals(0, countParticipations(to));

        for (int i = 0; i < 10; ++i) {
            FinancialAct invoice = FinancialTestHelper.createChargesInvoice(
                    new Money(100), customer, from, product);
            save(invoice);
        }
        int fromRefs = countParticipations(from);
        assertTrue(fromRefs >= 10);

        checkMerge(from, to);

        // verify the articipations no longer reference the 'from' patient
        assertEquals(0, countParticipations(from));

        // verify all participations moved to the 'to' patient
        int toRefs = countParticipations(to);
        assertEquals(toRefs, fromRefs);  //
    }

    /**
     * Verifies that only <em>party.patientpet</em> instances can be merged.
     */
    public void testMergeInvalidParty() {
        Party from = TestHelper.createPatient();
        Party to = TestHelper.createCustomer();
        try {
            checkMerge(from, to);
            fail("Expected merge to invalid party to fail");
        } catch (MergeException expected) {
            assertEquals(MergeException.ErrorCode.InvalidType,
                         expected.getErrorCode());
        }
    }

    /**
     * Verifies that a patient cannot be merged with itself.
     */
    public void testMergeToSamePatient() {
        Party from = TestHelper.createPatient();
        try {
            checkMerge(from, from);
            fail("Expected merge to same patient to fail");
        } catch (MergeException expected) {
            assertEquals(MergeException.ErrorCode.CannotMergeToSameObject,
                         expected.getErrorCode());
        }
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        rules = new PatientRules();
    }

    /**
     * Merges two patients, and verifies the 'from' patient has been
     * deleted.
     *
     * @param from the patient to merge from
     * @param to   the patient to merge to
     * @return the merged patient
     */
    private Party checkMerge(Party from, Party to) {
        rules.mergePatients(from, to);

        // verify the from patient has been deleted
        assertNull(get(from));

        Party merged = get(to);
        assertNotNull(merged);
        return merged;
    }

    /**
     * Helper to create a new entity identity.
     *
     * @param identity the identity
     * @return a new entity identity
     * @throws ArchetypeServiceException for any error
     */
    private EntityIdentity createIdentity(String identity) {
        EntityIdentity id = (EntityIdentity) create("entityIdentity.petTag");
        id.setIdentity(identity);
        return id;
    }
}
