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

package org.openvpms.archetype.rules.party;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountQueryFactory;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.statement.EndOfPeriodProcessor;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link CustomerMerger} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerMergerTestCase extends AbstractPartyMergerTest {

    /**
     * Customer rules.
     */
    private CustomerRules customerRules;

    /**
     * Patient rules.
     */
    private PatientRules patientRules;

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The transaction template.
     */
    private TransactionTemplate template;


    /**
     * Merges two customers and verifies the merged customer contains the
     * contacts of both.
     */
    public void testMergeContacts() {
        Party from = TestHelper.createCustomer();
        Party to = TestHelper.createCustomer();

        int fromContactsSize = from.getContacts().size();
        int toContactsSize = to.getContacts().size();

        Party merged = checkMerge(from, to);

        // verify contacts copied accross
        assertEquals(fromContactsSize + toContactsSize,
                     merged.getContacts().size());
    }

    /**
     * Tests a merge where the merge-from customer has an account type,
     * and the merge-to customer doesn't.
     */
    public void testMergeAccountType() {
        Party from = TestHelper.createCustomer();
        Party to = TestHelper.createCustomer();
        Lookup accountType = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, BigDecimal.ZERO);

        from.addClassification(accountType);

        Party merged = checkMerge(from, to);
        assertEquals(accountType, customerRules.getAccountType(merged));
    }

    /**
     * Tests a merge where both customers have different account types.
     * The merge-to customer's account type should take precedence.
     */
    public void testMergeAccountTypes() {
        Party from = TestHelper.createCustomer();
        Party to = TestHelper.createCustomer();
        Lookup accountType1 = FinancialTestHelper.createAccountType(
                30, DateUnits.DAYS, BigDecimal.ZERO);
        Lookup accountType2 = FinancialTestHelper.createAccountType(
                15, DateUnits.DAYS, BigDecimal.ZERO);

        from.addClassification(accountType1);
        to.addClassification(accountType2);

        Lookup fromAccountType = customerRules.getAccountType(from);
        Lookup toAccountType = customerRules.getAccountType(to);

        assertEquals(accountType1, fromAccountType);
        assertEquals(accountType2, toAccountType);

        Party merged = checkMerge(from, to);

        assertEquals(accountType2, customerRules.getAccountType(merged));
    }

    /**
     * Tests that entity relationships are copied.
     */
    public void testMergeEntityRelationships() {
        Party from = TestHelper.createCustomer();
        Party to = TestHelper.createCustomer();
        Party patient1 = TestHelper.createPatient(from, true);
        Party patient2 = TestHelper.createPatient(to, true);

        Party merged = checkMerge(from, to);

        patient1 = get(patient1);
        patient2 = get(patient2);

        assertTrue(patientRules.isOwner(merged, patient1));
        assertTrue(patientRules.isOwner(merged, patient2));
    }

    /**
     * Tests that entity identities are copied.
     */
    public void testMergeEntityIdentities() {
        Party from = TestHelper.createCustomer();
        Party to = TestHelper.createCustomer();

        EntityIdentity id1 = createIdentity("ABC1234");
        from.addIdentity(id1);

        EntityIdentity id2 = createIdentity("XYZ1234");
        to.addIdentity(id2);

        Party merged = checkMerge(from, to);

        EntityIdentity[] identities
                = merged.getIdentities().toArray(
                new EntityIdentity[merged.getIdentities().size()]);
        String idA = identities[0].getIdentity();
        String idB = identities[1].getIdentity();
        assertTrue(id1.getIdentity().equals(idA)
                || id1.getIdentity().equals(idB));
        assertTrue(id2.getIdentity().equals(idA)
                || id2.getIdentity().equals(idB));
    }

    /**
     * Verifies that participations are moved to the merged customer.
     */
    public void testMergeParticipations() {
        Party from = TestHelper.createCustomer();
        Party to = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        Product product = TestHelper.createProduct();

        assertEquals(0, countParticipations(from));
        assertEquals(0, countParticipations(to));

        for (int i = 0; i < 10; ++i) {
            List<FinancialAct> invoice
                    = FinancialTestHelper.createChargesInvoice(
                    new Money(100), from, patient, product, ActStatus.POSTED);
            save(invoice);
        }
        int fromRefs = countParticipations(from);
        assertTrue(fromRefs >= 10);

        checkMerge(from, to);

        // verify the participations no longer reference the from customer
        assertEquals(0, countParticipations(from));

        // verify all participations moved to the 'to' customer
        int toRefs = countParticipations(to);
        assertEquals(toRefs, fromRefs);  //
    }

    /**
     * Verifies that only <em>party.customerperson</em> instances can be merged.
     */
    public void testMergeInvalidParty() {
        Party from = TestHelper.createCustomer();
        Party to = TestHelper.createSupplier();
        try {
            checkMerge(from, to);
            fail("Expected merge to invalid party to fail");
        } catch (MergeException expected) {
            assertEquals(MergeException.ErrorCode.InvalidType,
                         expected.getErrorCode());
        }
    }

    /**
     * Verifies that a customer cannot be merged with itself.
     */
    public void testMergeToSameCustomer() {
        Party from = TestHelper.createCustomer();
        try {
            checkMerge(from, from);
            fail("Expected merge to same customer to fail");
        } catch (MergeException expected) {
            assertEquals(MergeException.ErrorCode.CannotMergeToSameObject,
                         expected.getErrorCode());
        }
    }

    /**
     * Verifies that the 'to' customer includes the 'from' customer's balance
     * after the merge, and that any opening and closing balance acts on or
     * after the first transaction of the from customer are removed.
     */
    public void testMergeAccounts() {
        final Money eighty = new Money(80);
        final Money forty = new Money(40);
        final Money fifty = new Money(50);
        final Money ninety = new Money(90);
        Party from = TestHelper.createCustomer();
        Party fromPatient = TestHelper.createPatient();
        Party to = TestHelper.createCustomer();
        Party toPatient = TestHelper.createPatient();
        Product product = TestHelper.createProduct();

        CustomerAccountRules rules = new CustomerAccountRules();

        // add some transaction history for the 'from' customer
        Date firstStartTime = getDatetime("2007-01-02 10:0:0");
        addInvoice(firstStartTime, eighty, from, fromPatient,
                   product);
        addPayment(getDatetime("2007-01-02 11:0:0"), forty, from);

        runEOP(from, getDate("2007-02-01"));

        // ... and the 'to' customer
        addInvoice(getDatetime("2007-01-01 10:0:0"), fifty, to, toPatient,
                   product);
        runEOP(to, getDate("2007-01-01"));
        runEOP(to, getDate("2007-02-01"));

        // verify balances prior to merge
        assertEquals(0, forty.compareTo(rules.getBalance(from)));
        assertEquals(0, fifty.compareTo(rules.getBalance(to)));

        to = checkMerge(from, to);

        // verify balances after merge
        assertEquals(0, BigDecimal.ZERO.compareTo(rules.getBalance(from)));
        assertEquals(0, ninety.compareTo(rules.getBalance(to)));

        // now verify that the only opening and closing balance acts for the
        // to customer are prior to the first act of the from customer
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(
                to, new String[]{CustomerAccountArchetypes.OPENING_BALANCE,
                                 CustomerAccountArchetypes.CLOSING_BALANCE});
        IMObjectQueryIterator<Act> iter = new IMObjectQueryIterator<Act>(query);
        int count = 0;
        while (iter.hasNext()) {
            Act act = iter.next();
            long startTime = act.getActivityStartTime().getTime();
            assertTrue(startTime < firstStartTime.getTime());
            ++count;
        }
        assertEquals(2, count); // expect a closing and opening balance

        // verify there are no acts associated with the removed 'from' customer
        assertEquals(0, countParticipations(from));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        PlatformTransactionManager mgr = (PlatformTransactionManager)
                applicationContext.getBean("txnManager");
        template = new TransactionTemplate(mgr);

        customerRules = new CustomerRules();
        patientRules = new PatientRules();
        practice = (Party) create("party.organisationPractice");
    }

    /**
     * Runs end of period for a customer.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     */
    private void runEOP(Party customer, Date statementDate) {
        EndOfPeriodProcessor eop = new EndOfPeriodProcessor(statementDate,
                                                            true, practice);
        eop.process(customer);
    }

    /**
     * Merges two customers in a transaction, and verifies the 'from' customer
     * has been deleted.
     *
     * @param from the customer to merge from
     * @param to   the customer to merge to
     * @return the merged customer
     */
    private Party checkMerge(final Party from, final Party to) {
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus transactionStatus) {
                customerRules.mergeCustomers(from, to);
                return null;
            }
        });

        // verify the from customer has been deleted
        assertNull(get(from));

        Party merged = get(to);
        assertNotNull(merged);
        return merged;
    }

    /**
     * Saves an invoice for a customer.
     *
     * @param startTime the invoice start time
     * @param amount    the invoice amount
     * @param customer  the customer
     * @param patient   the patient
     * @param product   the product
     */
    private void addInvoice(Date startTime, Money amount, Party customer,
                            Party patient, Product product) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(
                amount, customer, patient, product,
                ActStatus.POSTED);
        FinancialAct act = acts.get(0);
        act.setActivityStartTime(startTime);
        save(acts);
    }

    /**
     * Saves a payment for a customer.
     *
     * @param startTime the payment start time
     * @param amount    the payment amount
     * @param customer  the customer
     */
    private void addPayment(Date startTime, Money amount, Party customer) {
        Act act = FinancialTestHelper.createPayment(
                amount, customer, FinancialTestHelper.createTill());
        act.setActivityStartTime(startTime);
        save(act);
    }

    /**
     * Helper to create a new entity identity.
     *
     * @param identity the identity
     * @return a new entity identity
     * @throws ArchetypeServiceException for any error
     */
    private EntityIdentity createIdentity(String identity) {
        EntityIdentity id = (EntityIdentity) create("entityIdentity.code");
        id.setIdentity(identity);
        return id;
    }
}
