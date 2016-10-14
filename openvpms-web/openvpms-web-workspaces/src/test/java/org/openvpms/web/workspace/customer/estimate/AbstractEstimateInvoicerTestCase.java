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

package org.openvpms.web.workspace.customer.estimate;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.archetype.rules.finance.estimate.EstimateTestHelper;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.customer.charge.DefaultEditorQueue;
import org.openvpms.web.workspace.customer.charge.EditorQueue;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.test.EchoTestHelper.fireDialogButton;

/**
 * Base class for {@link EstimateInvoicer} tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractEstimateInvoicerTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * Tax rules.
     */
    private TaxRules taxRules;

    /**
     * The test customer.
     */
    private Party customer;

    /**
     * The test patient.
     */
    private Party patient;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        taxRules = new TaxRules(getPractice(), getArchetypeService());

        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
    }

    /**
     * Tests invoicing of estimates.
     */
    @Test
    public void testInvoice() {
        User author = TestHelper.createClinician();
        User clinician = TestHelper.createClinician();

        DefaultLayoutContext context = new DefaultLayoutContext(true, new LocalContext(), new HelpContext("foo", null));
        context.getContext().setPractice(getPractice());
        context.getContext().setUser(author);
        context.getContext().setClinician(clinician);
        context.getContext().setLocation(TestHelper.createLocation());
        context.getContext().setCustomer(customer);
        context.getContext().setPatient(patient);

        Product product1 = TestHelper.createProduct(ProductArchetypes.MEDICATION, null);
        Product product2 = TestHelper.createProduct(ProductArchetypes.SERVICE, null);
        Product product3 = TestHelper.createProduct(ProductArchetypes.MERCHANDISE, null);
        Product product4 = TestHelper.createProduct(ProductArchetypes.MEDICATION, null);
        Product template = TestHelper.createProduct(ProductArchetypes.TEMPLATE, null);
        IMObjectBean templateBean = new IMObjectBean(template);
        templateBean.setValue("invoiceNote", "Template Invoice Note");
        templateBean.setValue("visitNote", "Template Visit Note");
        save(template);

        BigDecimal price1 = new BigDecimal("10.00");
        BigDecimal price2 = new BigDecimal("20.00");
        BigDecimal price3 = new BigDecimal("30.00");
        BigDecimal price4 = new BigDecimal("20.00");

        BigDecimal quantity1 = BigDecimal.ONE;
        BigDecimal quantity2 = BigDecimal.ONE;
        BigDecimal quantity3 = BigDecimal.valueOf(2);
        BigDecimal quantity4 = BigDecimal.valueOf(4);

        BigDecimal amount1 = quantity1.multiply(price1);
        BigDecimal amount2 = quantity2.multiply(price2);
        BigDecimal amount3 = quantity3.multiply(price3);
        BigDecimal amount4 = quantity4.multiply(price4);

        BigDecimal tax1 = calculateTax(product1, amount1);
        BigDecimal tax2 = calculateTax(product2, amount2);
        BigDecimal tax3 = calculateTax(product3, amount3);
        BigDecimal tax4 = calculateTax(product4, amount4);

        Act item1 = createEstimateItem(patient, product1, author, quantity1, price1);
        Act item2 = createEstimateItem(patient, product2, author, quantity2, price2);
        Act item3 = createEstimateItem(patient, product3, template, author, quantity3, price3);
        Act item4 = createEstimateItem(patient, product4, template, author, quantity4, price4);
        Act estimation = EstimateTestHelper.createEstimate(customer, author, item1, item2, item3, item4);

        save(estimation, item1, item2, item3, item4);

        EstimateInvoicer invoicer = createEstimateInvoicer();

        EditDialog dialog = invoicer.invoice(estimation, null, context);
        IMObjectEditor editor = dialog.getEditor();
        assertTrue(SaveHelper.save(editor));

        FinancialAct invoice = (FinancialAct) editor.getObject();

        BigDecimal total = sum(amount1, amount2, amount3, amount4);
        BigDecimal tax = sum(tax1, tax2, tax3, tax4);
        assertTrue(total.compareTo(invoice.getTotal()) == 0);
        assertEquals(EstimateActStatus.INVOICED, estimation.getStatus());

        ActBean bean = new ActBean(invoice);
        assertEquals("Template Invoice Note", bean.getString("notes"));
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(4, items.size());
        checkCharge(invoice, customer, author, clinician, tax, total);

        BigDecimal discount = BigDecimal.ZERO;
        checkItem(items, patient, product1, null, author, clinician, BigDecimal.ONE, quantity1, BigDecimal.ZERO, price1,
                  BigDecimal.ZERO, BigDecimal.ZERO, discount, tax1, amount1, null, 1);
        checkItem(items, patient, product2, null, author, clinician, BigDecimal.ONE, quantity2, BigDecimal.ZERO, price2,
                  BigDecimal.ZERO, BigDecimal.ZERO, discount, tax2, amount2, null, 0);
        checkItem(items, patient, product3, template, author, clinician, BigDecimal.ONE, quantity3, BigDecimal.ZERO,
                  price3, BigDecimal.ZERO, BigDecimal.ZERO, discount, tax3, amount3, null, 0);
        checkItem(items, patient, product4, template, author, clinician, BigDecimal.ONE, quantity4, BigDecimal.ZERO,
                  price4, BigDecimal.ZERO, BigDecimal.ZERO, discount, tax4, amount4, null, 1);
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     */
    protected Party getPatient() {
        return patient;
    }

    /**
     * Creates a new {@link EstimateInvoicer}.
     *
     * @return a new estimate invoicer
     */
    protected abstract EstimateInvoicer createEstimateInvoicer();

    /**
     * Helper to calculate tax for a product and amount.
     *
     * @param product the product
     * @param amount  the amount
     * @return the tax
     */
    protected BigDecimal calculateTax(Product product, BigDecimal amount) {
        return taxRules.calculateTax(amount, product, true);
    }

    /**
     * Sums a set of amounts.
     * NOTE: this rounds first, which is the same behaviour as {@link ActCalculator#sum}. Not sure if this is correct.
     * TODO.
     *
     * @param amounts the amounts
     * @return the sum of the amounts
     */
    protected BigDecimal sum(BigDecimal... amounts) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) {
            result = result.add(MathRules.round(amount));
        }
        return result;
    }

    /**
     * Creates an estimate item.
     *
     * @param patient   the patient
     * @param product   the product
     * @param author    the author
     * @param quantity  the quantity
     * @param unitPrice the unit price
     * @return a new estimation item
     */
    protected Act createEstimateItem(Party patient, Product product, User author, BigDecimal quantity,
                                     BigDecimal unitPrice) {
        Act item = EstimateTestHelper.createEstimateItem(patient, product, author, quantity, unitPrice);
        ActBean bean = new ActBean(item);
        bean.setValue("lowQty", BigDecimal.ONE);
        return item;
    }

    /**
     * Creates an estimate item.
     *
     * @param patient   the patient
     * @param product   the product
     * @param template  the template
     * @param author    the author
     * @param quantity  the quantity
     * @param unitPrice the unit price
     * @return a new estimation item
     */
    protected Act createEstimateItem(Party patient, Product product, Product template, User author,
                                     BigDecimal quantity, BigDecimal unitPrice) {
        Act item = EstimateTestHelper.createEstimateItem(patient, product, template, author, quantity, unitPrice);
        ActBean bean = new ActBean(item);
        bean.setValue("lowQty", BigDecimal.ONE);
        return item;
    }

    /**
     * Creates an editor queue that closes dialogs on display.
     *
     * @param context the context
     * @return a new queue
     */
    protected EditorQueue createEditorQueue(Context context) {
        return new DefaultEditorQueue(context) {
            @Override
            protected void edit(EditDialog dialog) {
                super.edit(dialog);
                fireDialogButton(dialog, PopupDialog.OK_ID);
            }
        };
    }
}
