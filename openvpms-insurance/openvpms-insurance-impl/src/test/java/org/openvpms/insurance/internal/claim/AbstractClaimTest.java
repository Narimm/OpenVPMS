package org.openvpms.insurance.internal.claim;

import org.junit.Before;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.insurance.InsuranceTestHelper;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.policy.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.finance.account.FinancialTestHelper.createChargesInvoice;
import static org.openvpms.archetype.rules.patient.PatientTestHelper.createPatient;

/**
 * Base class for insurance claim tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractClaimTest extends ArchetypeServiceTest {

    /**
     * The test practice.
     */
    protected Party practice;

    /**
     * The test customer.
     */
    protected Party customer;

    /**
     * The test patient.
     */
    protected Party patient;

    /**
     * Patient date of birth.
     */
    protected Date dateOfBirth;

    /**
     * The test clinician.
     */
    protected User clinician;

    /**
     * The practice location.
     */
    protected Party location;

    /**
     * Practice location phone.
     */
    protected Contact locationPhone;

    /**
     * Practice location email.
     */
    protected Contact locationEmail;

    /**
     * The claim handler.
     */
    protected User user;

    /**
     * The policy.
     */
    protected Act policyAct;

    /**
     * The test insurer.
     */
    protected Party insurer1;

    /**
     * The insurance rules.
     */
    protected InsuranceRules insuranceRules;

    /**
     * The customer rules.
     */
    @Autowired
    private CustomerRules customerRules;

    /**
     * The customer account rules.
     */
    @Autowired
    private CustomerAccountRules customerAccountRules;

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules patientRules;

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // set up a 10% tax rate on the practice
        practice = TestHelper.getPractice();
        practice.addClassification(TestHelper.createTaxType(BigDecimal.TEN));

        // customer
        customer = TestHelper.createCustomer("MS", "J", "Bloggs", "12 Broadwater Avenue", "CAPE_WOOLAMAI", "VIC",
                                             "3925", "9123456", "98765432", "04987654321", "foo@test.com");
        handlers = new DocumentHandlers(getArchetypeService());

        insuranceRules = new InsuranceRules((IArchetypeRuleService) getArchetypeService(), transactionManager);

        // practice location
        locationPhone = TestHelper.createPhoneContact(null, "5123456", false);
        locationEmail = TestHelper.createEmailContact("vetsrus@test.com");
        location = TestHelper.createLocation();
        location.addContact(locationEmail);
        location.addContact(locationPhone);
        save(location);

        // clinician
        clinician = TestHelper.createClinician();

        // claim handler
        user = TestHelper.createUser("Z", "Smith");

        // patient
        dateOfBirth = DateRules.getDate(DateRules.getToday(), -1, DateUnits.YEARS);
        patient = createPatient("Fido", "CANINE", "PUG", "MALE", dateOfBirth, "123454321", "BLACK", customer);

        // insurer
        insurer1 = (Party) InsuranceTestHelper.createInsurer(TestHelper.randomName("ZInsurer-"));

        // policy
        policyAct = (Act) InsuranceTestHelper.createPolicy(customer, patient, insurer1, "POL123456");
        save(policyAct);

        // diagnosis codes
        InsuranceTestHelper.createDiagnosis("VENOM_328", "Abcess", "328");
    }

    /**
     * Verifies a policy matches that expected.
     *
     * @param policy       the policy
     * @param insurer      the expected insurer
     * @param policyNumber the expected policy number
     */
    protected void checkPolicy(Policy policy, Party insurer, String policyNumber) {
        assertEquals(insurer, policy.getInsurer());
        assertEquals(policyNumber, policy.getPolicyNumber());
    }

    /**
     * Creates a claim.
     *
     * @param act the claim act
     * @return a new claim
     */
    protected Claim createClaim(Act act) {
        return new ClaimImpl(act, (IArchetypeRuleService) getArchetypeService(), insuranceRules, customerRules,
                             patientRules, handlers, transactionManager);
    }

    /**
     * Creates a gap claim.
     *
     * @param act the claim act
     * @return a new gap claim
     */
    protected GapClaimImpl createGapClaim(FinancialAct act) {
        return new GapClaimImpl(act, (IArchetypeRuleService) getArchetypeService(), insuranceRules, customerRules,
                                customerAccountRules, patientRules, handlers, transactionManager);
    }

    /**
     * Creates an invoice item.
     *
     * @param date     the date
     * @param product  the product
     * @param quantity the quantity
     * @param price    the unit price
     * @param discount the discount
     * @param tax      the tax
     * @return the new invoice item
     */
    protected FinancialAct createInvoiceItem(Date date, Product product, BigDecimal quantity, BigDecimal price,
                                             BigDecimal discount, BigDecimal tax) {
        return FinancialTestHelper.createInvoiceItem(date, patient, clinician, product, quantity, ZERO, price,
                                                     discount, tax);
    }

    /**
     * Creates and saves a POSTED invoice.
     *
     * @param date  the invoice date
     * @param items the invoice items
     * @return the invoice acs
     */
    protected List<FinancialAct> createInvoice(Date date, FinancialAct... items) {
        List<FinancialAct> invoice = createChargesInvoice(customer, clinician, ActStatus.POSTED, items);
        invoice.get(0).setActivityStartTime(date);
        save(invoice);
        return invoice;
    }

}
