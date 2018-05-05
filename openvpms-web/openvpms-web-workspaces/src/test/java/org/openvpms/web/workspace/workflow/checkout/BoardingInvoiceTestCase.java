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

package org.openvpms.web.workspace.workflow.checkout;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.customer.charge.TestChargeEditor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.workspace.workflow.checkout.BoardingTestHelper.createVisit;

/**
 * Tests the {@link BoardingInvoicer}.
 *
 * @author Tim Anderson
 */
public class BoardingInvoiceTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The patient rules.
     */
    @Autowired
    PatientRules patientRules;

    /**
     * The appointment rules.
     */
    AppointmentRules appointmentRules;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The first test patient.
     */
    private Party patient1;

    /**
     * The second test patient.
     */
    private Party patient2;

    /**
     * The location.
     */
    private Party location;

    /**
     * The author.
     */
    private User author;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();

        customer = TestHelper.createCustomer();
        patient1 = TestHelper.createPatient(customer);
        patient2 = TestHelper.createPatient(customer);
        location = TestHelper.createLocation();
        author = TestHelper.createClinician();
        clinician = TestHelper.createClinician();

        // IntelliJ complains if this is autowired
        appointmentRules = applicationContext.getBean(AppointmentRules.class);

    }

    /**
     * Tests invoicing two patients for a single day.
     */
    @Test
    public void testInvoiceSingleDay() {
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal firstPetProductDayPrice = new BigDecimal("9.09");
        BigDecimal firstPetProductDayPriceIncTax = BigDecimal.TEN;
        BigDecimal firstPetProductNightPrice = new BigDecimal("18.18");
        BigDecimal secondPetProductDayPrice = new BigDecimal("4.54");
        BigDecimal secondPetProductDayPriceIncTax = new BigDecimal("4.99");
        BigDecimal secondPetProductNightPrice = new BigDecimal("13.64");
        Product firstPetProductDay = ProductTestHelper.createService(firstPetProductDayPrice, unitPrice);
        Product firstPetProductNight = ProductTestHelper.createService(firstPetProductNightPrice, unitPrice);
        Product secondPetProductDay = ProductTestHelper.createService(secondPetProductDayPrice, unitPrice);
        Product secondPetProductNight = ProductTestHelper.createService(secondPetProductNightPrice, unitPrice);
        Entity cageType = ScheduleTestHelper.createCageType("Z Test Cage", firstPetProductDay, firstPetProductNight,
                                                            secondPetProductDay, secondPetProductNight);

        Party schedule = ScheduleTestHelper.createSchedule(location, cageType);

        Visits visits = new Visits(customer, appointmentRules, patientRules);
        Visit visit1 = createVisit("2016-03-24 10:00:00", "2016-03-24 17:00:00", schedule, customer, patient1, visits);
        Visit visit2 = createVisit("2016-03-24 10:00:00", "2016-03-24 17:00:00", schedule, customer, patient2, visits);
        visits.add(visit1);
        visits.add(visit2);
        visit1.setFirstPet(true);
        visit2.setFirstPet(false);

        FinancialAct invoice = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createEditor(invoice);

        BoardingInvoicer invoicer = new BoardingInvoicer();
        invoicer.invoice(visits, editor);

        assertTrue(SaveHelper.save(editor));
        invoice = get(invoice);
        ActBean bean = new ActBean(invoice);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(2, items.size());

        checkItem(items, patient1, firstPetProductDay, null, author, clinician, BigDecimal.ZERO, ONE, ZERO,
                  unitPriceIncTax, ZERO, firstPetProductDayPriceIncTax, ZERO, new BigDecimal("1.818"),
                  firstPetProductDayPriceIncTax.add(unitPriceIncTax), visit1.getEvent(), 0);
        checkItem(items, patient2, secondPetProductDay, null, author, clinician, BigDecimal.ZERO, ONE, ZERO,
                  unitPriceIncTax, ZERO, secondPetProductDayPriceIncTax, ZERO, new BigDecimal("1.363"),
                  secondPetProductDayPriceIncTax.add(unitPriceIncTax), visit2.getEvent(), 0);
    }

    /**
     * Tests invoicing of two patients boarding multiple days, with late checkout for the second pet.
     */
    @Test
    public void testInvoiceMultipleDays() {
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal firstPetProductDayPrice = new BigDecimal("9.09");
        BigDecimal firstPetProductNightPrice = new BigDecimal("18.18");
        BigDecimal firstPetProductNightPriceIncTax = BigDecimal.valueOf(20);
        BigDecimal secondPetProductDayPrice = new BigDecimal("4.54");
        BigDecimal secondPetProductNightPrice = new BigDecimal("13.64");
        BigDecimal secondPetProductNightPriceIncTax = BigDecimal.valueOf(15);
        BigDecimal lateCheckoutProductPrice = new BigDecimal("9.09");
        BigDecimal lateCheckoutProductPriceIncTax = BigDecimal.TEN;
        Product firstPetProductDay = ProductTestHelper.createService(firstPetProductDayPrice, unitPrice);
        Product firstPetProductNight = ProductTestHelper.createService(firstPetProductNightPrice, unitPrice);
        Product secondPetProductDay = ProductTestHelper.createService(secondPetProductDayPrice, unitPrice);
        Product secondPetProductNight = ProductTestHelper.createService(secondPetProductNightPrice, unitPrice);
        Product lateCheckoutProduct = ProductTestHelper.createService(lateCheckoutProductPrice, BigDecimal.ZERO);
        Entity cageType = ScheduleTestHelper.createCageType("Z Test Cage", firstPetProductDay, firstPetProductNight,
                                                            secondPetProductDay, secondPetProductNight,
                                                            Time.valueOf("18:00:00"), lateCheckoutProduct);
        Party schedule = ScheduleTestHelper.createSchedule(location, cageType);

        Visits visits = new Visits(customer, appointmentRules, patientRules);
        Visit visit1 = createVisit("2016-03-24 10:00:00", "2016-03-29 17:45:00", schedule, customer, patient1, visits);
        Visit visit2 = createVisit("2016-03-24 10:00:00", "2016-03-29 18:30:00", schedule, customer, patient2, visits);
        visits.add(visit1);
        visits.add(visit2);
        visit1.setFirstPet(true);
        visit2.setFirstPet(false);

        FinancialAct invoice = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createEditor(invoice);

        BoardingInvoicer invoicer = new BoardingInvoicer();
        invoicer.invoice(visits, editor);

        assertTrue(SaveHelper.save(editor));
        invoice = get(invoice);
        ActBean bean = new ActBean(invoice);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());

        BigDecimal quantity = BigDecimal.valueOf(5); // 5 nights
        BigDecimal unitPriceXQty = unitPriceIncTax.multiply(quantity);

        checkItem(items, patient1, firstPetProductNight, null, author, clinician, ZERO, quantity, ZERO, unitPriceIncTax,
                  ZERO, firstPetProductNightPriceIncTax, ZERO, new BigDecimal("6.364"),
                  firstPetProductNightPriceIncTax.add(unitPriceXQty), visit1.getEvent(), 0);
        checkItem(items, patient2, secondPetProductNight, null, author, clinician, ZERO, quantity, ZERO,
                  unitPriceIncTax, ZERO, secondPetProductNightPriceIncTax, ZERO, new BigDecimal("5.909"),
                  secondPetProductNightPriceIncTax.add(unitPriceXQty), visit2.getEvent(), 0);
        checkItem(items, patient2, lateCheckoutProduct, null, author, clinician, ZERO, ONE, ZERO, ZERO, ZERO,
                  lateCheckoutProductPriceIncTax, ZERO, new BigDecimal("0.909"), lateCheckoutProductPriceIncTax,
                  visit2.getEvent(), 0);
    }

    /**
     * Verifies that a template can be used for the boarding product.
     */
    @Test
    public void testInvoiceMultipleDaysWithTemplateProduct() {
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal price1 = new BigDecimal("9.09");
        BigDecimal price1IncTax = BigDecimal.TEN;
        BigDecimal price2 = new BigDecimal("18.18");
        BigDecimal price2IncTax = BigDecimal.valueOf(20);

        Product template = ProductTestHelper.createTemplate("Z Boarding Template");
        Product product1 = ProductTestHelper.createService(price1, unitPrice);
        Product product2 = ProductTestHelper.createService(price2, unitPrice);
        ProductTestHelper.addInclude(template, product1, 1, 1);
        ProductTestHelper.addInclude(template, product2, 1, 2);
        Entity cageType = ScheduleTestHelper.createCageType("Z Test Cage", template, null, null, null);
        Party schedule = ScheduleTestHelper.createSchedule(location, cageType);

        Visits visits = new Visits(customer, appointmentRules, patientRules);
        Visit visit = createVisit("2016-03-24 10:00:00", "2016-03-29 17:45:00", schedule, customer, patient1, visits);
        visits.add(visit);
        visit.setFirstPet(true);

        FinancialAct invoice = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createEditor(invoice);

        BoardingInvoicer invoicer = new BoardingInvoicer();
        invoicer.invoice(visits, editor);

        assertTrue(SaveHelper.save(editor));
        invoice = get(invoice);
        ActBean bean = new ActBean(invoice);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(2, items.size());

        BigDecimal quantity = BigDecimal.valueOf(5); // 5 nights
        BigDecimal unitPriceXQty = unitPriceIncTax.multiply(quantity);

        checkItem(items, patient1, product1, template, author, clinician, quantity, quantity, ZERO, unitPriceIncTax,
                  ZERO, price1IncTax, ZERO, new BigDecimal("5.455"), price1IncTax.add(unitPriceXQty), visit.getEvent(), 0);

        // the high quantity = 2 for product2, so need to double quantity
        BigDecimal two = BigDecimal.valueOf(2);
        checkItem(items, patient1, product2, template, author, clinician, quantity, quantity.multiply(two),
                  ZERO, unitPriceIncTax, ZERO, price2IncTax, ZERO, new BigDecimal("10.909"),
                  price2IncTax.add(unitPriceXQty.multiply(two)),
                  visit.getEvent(), 0);
    }


    /**
     * Verifies if a pet stays less than 24 hours overnight, the overnight products are charged.
     */
    @Test
    public void testInvoiceOvernightLessThan24Hours() {
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal firstPetProductDayPrice = new BigDecimal("9.09");
        BigDecimal firstPetProductNightPrice = new BigDecimal("18.18");
        BigDecimal firstPetProductNightPriceIncTax = new BigDecimal("20.00");
        BigDecimal secondPetProductDayPrice = new BigDecimal("4.54");
        BigDecimal secondPetProductNightPrice = new BigDecimal("13.64");
        BigDecimal secondPetProductNightPriceIncTax = new BigDecimal("15.00");
        Product firstPetProductDay = ProductTestHelper.createService(firstPetProductDayPrice, unitPrice);
        Product firstPetProductNight = ProductTestHelper.createService(firstPetProductNightPrice, unitPrice);
        Product secondPetProductDay = ProductTestHelper.createService(secondPetProductDayPrice, unitPrice);
        Product secondPetProductNight = ProductTestHelper.createService(secondPetProductNightPrice, unitPrice);
        Entity cageType = ScheduleTestHelper.createCageType("Z Test Cage", firstPetProductDay, firstPetProductNight,
                                                            secondPetProductDay, secondPetProductNight);

        Party schedule = ScheduleTestHelper.createSchedule(location, cageType);

        Visits visits = new Visits(customer, appointmentRules, patientRules);
        Visit visit1 = createVisit("2016-03-24 10:00:00", "2016-03-25 09:00:00", schedule, customer, patient1, visits);
        Visit visit2 = createVisit("2016-03-24 10:00:00", "2016-03-25 09:00:00", schedule, customer, patient2, visits);
        visits.add(visit1);
        visits.add(visit2);
        visit1.setFirstPet(true);
        visit2.setFirstPet(false);

        FinancialAct invoice = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createEditor(invoice);

        BoardingInvoicer invoicer = new BoardingInvoicer();
        invoicer.invoice(visits, editor);

        assertTrue(SaveHelper.save(editor));
        invoice = get(invoice);
        ActBean bean = new ActBean(invoice);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(2, items.size());

        checkItem(items, patient1, firstPetProductNight, null, author, clinician, BigDecimal.ZERO, ONE, ZERO,
                  unitPriceIncTax, ZERO, firstPetProductNightPriceIncTax, ZERO, new BigDecimal("2.727"),
                  firstPetProductNightPriceIncTax.add(unitPriceIncTax), visit1.getEvent(), 0);
        checkItem(items, patient2, secondPetProductNight, null, author, clinician, BigDecimal.ZERO, ONE, ZERO,
                  unitPriceIncTax, ZERO, secondPetProductNightPriceIncTax, ZERO, new BigDecimal("2.273"),
                  secondPetProductNightPriceIncTax.add(unitPriceIncTax), visit2.getEvent(), 0);
    }

    /**
     * Creates an editor for an invoice.
     *
     * @param invoice the invoice
     * @return a new editor
     */
    protected TestChargeEditor createEditor(FinancialAct invoice) {
        DefaultLayoutContext context = new DefaultLayoutContext(true, new LocalContext(), new HelpContext("foo", null));

        context.getContext().setCustomer(customer);
        context.getContext().setPractice(getPractice());
        context.getContext().setUser(author);
        context.getContext().setClinician(clinician);
        context.getContext().setLocation(location);

        TestChargeEditor editor = new TestChargeEditor(invoice, context, false);
        editor.getComponent();
        return editor;
    }

}
