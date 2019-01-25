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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.pharmacy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.invoice.InvoiceItemStatus;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.hl7.laboratory.LaboratoryOrderService;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;
import org.openvpms.web.workspace.customer.charge.OrderServices;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link PharmacyOrderDiscontinuationJob}.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderDiscontinuationJobTestCase extends ArchetypeServiceTest {

    /**
     * The practice service.
     */
    private PracticeService practiceService;

    /**
     * The practice rules.
     */
    @Autowired
    private PracticeRules practiceRules;

    /**
     * Order discontinuer.
     */
    private OrderDiscontinuer discontinuer;

    /**
     * Test patient.
     */
    private Party patient;

    /**
     * Test customer.
     */
    private Party customer;

    /**
     * Test clinician.
     */
    private User clinician;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        IMObjectBean practice = getBean(TestHelper.getPractice());

        User user = TestHelper.createUser();
        practice.setTarget("serviceUser", user);
        practice.setValue("pharmacyOrderDiscontinuePeriod", 1);
        practice.setValue("pharmacyOrderDiscontinuePeriodUnits", "MINUTES");
        practice.save();

        IArchetypeService service = getArchetypeService();

        IArchetypeService archetypeService
                = applicationContext.getBean("defaultArchetypeService", IArchetypeService.class);
        OrderServices orderServices = new OrderServices(mock(PharmacyOrderService.class), mock(Pharmacies.class),
                                                        mock(LaboratoryOrderService.class), mock(Laboratories.class),
                                                        mock(PatientContextFactory.class),
                                                        mock(PatientInformationService.class),
                                                        mock(MedicalRecordRules.class), practiceRules);
        discontinuer = new OrderDiscontinuer(orderServices, archetypeService);
        practiceService = new PracticeService(service, practiceRules, null);

        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        clinician = TestHelper.createClinician();
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown() {
        practiceService.dispose();
    }

    /**
     * Tests the job.
     *
     * @throws Exception for any error
     */
    @Test
    public void testJob() throws Exception {
        Entity config = (Entity) create(PharmacyOrderDiscontinuationJob.JOB_ARCHETYPE);
        Date now = new Date();
        Date past = DateRules.getDate(now, -1, DateUnits.HOURS);

        FinancialAct item1a = createItem(past, true);
        FinancialAct item1b = createItem(past, false);
        FinancialAct invoice1 = createInvoice(past, ActStatus.IN_PROGRESS, item1a, item1b);

        FinancialAct item2a = createItem(past, true);
        FinancialAct item2b = createItem(past, false);
        createInvoice(past, ActStatus.POSTED, item2a, item2b);

        FinancialAct item3a = createItem(now, true);
        FinancialAct item3b = createItem(now, false);
        createInvoice(now, ActStatus.POSTED, item3a, item3b);

        // check statuses prior to running the job
        checkItem(item1a, InvoiceItemStatus.ORDERED);
        checkItem(item1b, null);
        checkItem(item2a, InvoiceItemStatus.ORDERED);
        checkItem(item2b, null);
        checkItem(item3a, InvoiceItemStatus.ORDERED);
        checkItem(item3b, null);

        PharmacyOrderDiscontinuationJob job = new PharmacyOrderDiscontinuationJob(
                config, (IArchetypeRuleService) getArchetypeService(), practiceService, discontinuer);

        job.execute(null);

        // check statuses. Only invoice2 should have changed
        checkItem(item1a, InvoiceItemStatus.ORDERED);
        checkItem(item1b, null);
        checkItem(item2a, InvoiceItemStatus.DISCONTINUED);
        checkItem(item2b, null);
        checkItem(item3a, InvoiceItemStatus.ORDERED);
        checkItem(item3b, null);

        // POST invoice1, and re-run the job
        invoice1.setStatus(ActStatus.POSTED);
        invoice1.setActivityEndTime(past);
        save(invoice1);
        job.execute(null);

        checkItem(item1a, InvoiceItemStatus.DISCONTINUED);
        checkItem(item1b, null);
        checkItem(item2a, InvoiceItemStatus.DISCONTINUED);
        checkItem(item2b, null);
        checkItem(item3a, InvoiceItemStatus.ORDERED);
        checkItem(item3b, null);
    }

    /**
     * Verifies an invoice item has the expected status.
     *
     * @param item   the item
     * @param status the expected status. May be {@code null}
     */
    private void checkItem(FinancialAct item, String status) {
        item = get(item);
        assertEquals(status, item.getStatus());
    }

    /**
     * Creates an invoice.
     *
     * @param date   the date
     * @param status the status
     * @param items  the items
     * @return the invoice
     */
    private FinancialAct createInvoice(Date date, String status, FinancialAct... items) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(customer, clinician, status, items);
        FinancialAct invoice = acts.get(0);
        invoice.setActivityStartTime(date);
        if (status.equals(ActStatus.POSTED)) {
            invoice.setActivityEndTime(date);
        }
        save(acts);
        return invoice;
    }

    /**
     * Creates an invoice item.
     *
     * @param date    the date
     * @param ordered if {@code true}, set the status to ORDERED
     * @return a new invoice
     */
    private FinancialAct createItem(Date date, boolean ordered) {
        FinancialAct item = FinancialTestHelper.createInvoiceItem(
                date, patient, clinician, TestHelper.createProduct(), ONE, TEN, ZERO, ZERO, ZERO);
        if (ordered) {
            item.setStatus(InvoiceItemStatus.ORDERED);
        }
        return item;
    }

}
