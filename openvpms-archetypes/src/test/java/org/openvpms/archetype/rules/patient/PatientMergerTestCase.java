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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.discount.DiscountTestHelper;
import org.openvpms.archetype.rules.party.AbstractPartyMergerTest;
import org.openvpms.archetype.rules.party.MergeException;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests the {@link PatientMerger} class.
 *
 * @author Tim Anderson
 */
public class PatientMergerTestCase extends AbstractPartyMergerTest {

    /**
     * Patient rules.
     */
    @Autowired
    private PatientRules rules;

    /**
     * The transaction template.
     */
    private TransactionTemplate template;


    /**
     * Tests that the most recent owner relationship is the active one after
     * the merge.
     */
    @Test
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
    @Test
    public void testMergeEntityIdentities() {
        Party from = TestHelper.createPatient();
        Party to = TestHelper.createPatient();

        EntityIdentity id1 = createIdentity("ABC1234");
        from.addIdentity(id1);

        EntityIdentity id2 = createIdentity("XYZ1234");
        to.addIdentity(id2);

        Party merged = checkMerge(from, to);

        EntityIdentity[] identities
                = merged.getIdentities().toArray(new EntityIdentity[merged.getIdentities().size()]);
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
    @Test
    public void testMergeParticipations() {
        Party customer = TestHelper.createCustomer();
        Party from = TestHelper.createPatient();
        Party to = TestHelper.createPatient();
        Product product = TestHelper.createProduct();

        assertEquals(0, countParticipations(from));
        assertEquals(0, countParticipations(to));

        for (int i = 0; i < 10; ++i) {
            List<FinancialAct> invoice
                    = FinancialTestHelper.createChargesInvoice(
                    new Money(100), customer, from, product, ActStatus.POSTED);
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
     * Verifies that discounts are moved to the merged patient.
     */
    @Test
    public void testMergeDiscounts() {
        Party from = TestHelper.createPatient();
        Party to = TestHelper.createPatient();
        Entity discount = createDiscount();

        IMObjectBean bean = new IMObjectBean(from);
        bean.addNodeTarget("discounts", discount);
        bean.save();

        Party merged = checkMerge(from, to);
        bean = new IMObjectBean(merged);
        List<IMObject> discounts = bean.getNodeTargetObjects("discounts");
        assertEquals(1, discounts.size());
        assertEquals(discount, discounts.get(0));
    }

    /**
     * Verifies that if either patient is desxed, the merged patient is desexed.
     */
    @Test
    public void testMergeDesexed() {
        Party from1 = TestHelper.createPatient();
        Party to1 = TestHelper.createPatient();

        Party merged1 = checkMerge(from1, to1);
        assertFalse(rules.isDesexed(merged1));

        Party from2 = TestHelper.createPatient();
        Party to2 = TestHelper.createPatient();
        rules.setDesexed(from2);
        Party merged2 = checkMerge(from2, to2);
        assertTrue(rules.isDesexed(merged2));

        Party from3 = TestHelper.createPatient();
        Party to3 = TestHelper.createPatient();
        rules.setDesexed(to3);

        Party merged3 = checkMerge(from3, to3);
        assertTrue(rules.isDesexed(merged3));
    }

    /**
     * Verifies that if either patient is deceased, the merged patient is deceased.
     */
    @Test
    public void testMergeDeceased() {
        Party from1 = TestHelper.createPatient();
        Party to1 = TestHelper.createPatient();

        rules.setDeceased(from1);
        Party merged1 = checkMerge(from1, to1);
        assertTrue(rules.isDeceased(merged1));

        Party from2 = TestHelper.createPatient();
        Party to2 = TestHelper.createPatient();
        rules.setDeceased(to2);

        Party merged2 = checkMerge(from2, to2);
        assertTrue(rules.isDeceased(merged2));
    }

    /**
     * Verifies that only <em>party.patientpet</em> instances can be merged.
     */
    @Test
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
    @Test
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
     */
    @Before
    public void setUp() {
        PlatformTransactionManager mgr = (PlatformTransactionManager) applicationContext.getBean("txnManager");
        template = new TransactionTemplate(mgr);
    }

    /**
     * Merges two patients in a transaction, and verifies the 'from' patient
     * has been deleted.
     *
     * @param from the patient to merge from
     * @param to   the patient to merge to
     * @return the merged patient
     */
    private Party checkMerge(final Party from, final Party to) {
        template.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus transactionStatus) {
                rules.mergePatients(from, to);
                return null;
            }
        });

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

    /**
     * Helper to create and save a new discount type entity.
     *
     * @return a new discount
     */
    private Entity createDiscount() {
        return DiscountTestHelper.createDiscount(BigDecimal.TEN, true, DiscountRules.PERCENTAGE);
    }
}
