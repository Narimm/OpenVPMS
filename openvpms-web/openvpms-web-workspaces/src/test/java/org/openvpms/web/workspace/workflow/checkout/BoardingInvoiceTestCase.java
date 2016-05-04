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
        BigDecimal unitPrice = BigDecimal.TEN;
        BigDecimal firstPetProductDayPrice = BigDecimal.TEN;
        BigDecimal firstPetProductNightPrice = BigDecimal.valueOf(20);
        BigDecimal secondPetProductDayPrice = BigDecimal.valueOf(5);
        BigDecimal secondPetProductNightPrice = BigDecimal.valueOf(15);
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

        checkItem(items, patient1, firstPetProductDay, null, author, clinician, ONE, ZERO, unitPrice, ZERO,
                  firstPetProductDayPrice, ZERO, new BigDecimal("1.818"), firstPetProductDayPrice.add(unitPrice),
                  visit1.getEvent(), 0);
        checkItem(items, patient2, secondPetProductDay, null, author, clinician, ONE, ZERO, unitPrice, ZERO,
                  secondPetProductDayPrice, ZERO, new BigDecimal("1.364"), secondPetProductDayPrice.add(unitPrice),
                  visit2.getEvent(), 0);
    }

    /**
     * Tests invoicing of two patients boarding multiple days, with late checkout for the second pet.
     */
    @Test
    public void testInvoiceMultipleDays() {
        BigDecimal unitPrice = BigDecimal.TEN;
        BigDecimal firstPetProductDayPrice = BigDecimal.TEN;
        BigDecimal firstPetProductNightPrice = BigDecimal.valueOf(20);
        BigDecimal secondPetProductDayPrice = BigDecimal.valueOf(5);
        BigDecimal secondPetProductNightPrice = BigDecimal.valueOf(15);
        BigDecimal lateCheckoutProductPrice = BigDecimal.valueOf(10);
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
        BigDecimal unitPriceXQty = unitPrice.multiply(quantity);

        checkItem(items, patient1, firstPetProductNight, null, author, clinician, quantity, ZERO, unitPrice, ZERO,
                  firstPetProductNightPrice, ZERO, new BigDecimal("6.364"),
                  firstPetProductNightPrice.add(unitPriceXQty), visit1.getEvent(), 0);
        checkItem(items, patient2, secondPetProductNight, null, author, clinician, quantity, ZERO, unitPrice, ZERO,
                  secondPetProductNightPrice, ZERO, new BigDecimal("5.909"),
                  secondPetProductNightPrice.add(unitPriceXQty),
                  visit2.getEvent(), 0);
        checkItem(items, patient2, lateCheckoutProduct, null, author, clinician, ONE, ZERO, ZERO, ZERO,
                  lateCheckoutProductPrice, ZERO, new BigDecimal("0.909"), lateCheckoutProductPrice, visit2.getEvent(),
                  0);
    }

    /**
     * Verifies that a template can be used for the boarding product.
     */
    @Test
    public void testInvoiceMultipleDaysWithTemplateProduct() {
        BigDecimal unitPrice = BigDecimal.TEN;
        BigDecimal price1 = BigDecimal.TEN;
        BigDecimal price2 = BigDecimal.valueOf(20);
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
        BigDecimal unitPriceXQty = unitPrice.multiply(quantity);

        checkItem(items, patient1, product1, template, author, clinician, quantity, ZERO, unitPrice, ZERO,
                  price1, ZERO, new BigDecimal("5.455"), price1.add(unitPriceXQty), visit.getEvent(), 0);

        // the high quantity = 2 for product2, so need to double quantity
        BigDecimal two = BigDecimal.valueOf(2);
        checkItem(items, patient1, product2, template, author, clinician, quantity.multiply(two),
                  ZERO, unitPrice, ZERO, price2, ZERO, new BigDecimal("10.909"),
                  price2.add(unitPriceXQty.multiply(two)),
                  visit.getEvent(), 0);
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
