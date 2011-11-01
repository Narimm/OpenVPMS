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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.customer.estimation;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.EstimationActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.estimation.EstimationArchetypes;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.app.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.app.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.app.customer.charge.PopupEditorManager;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

import java.math.BigDecimal;
import java.util.List;


/**
 * Tests the {@link EstimationInvoicer} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class EstimationInvoicerTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * Tests invoicing of estimations.
     */
    @Test
    public void testInvoice() {
        Party customer = TestHelper.createCustomer();
        DefaultLayoutContext context = new DefaultLayoutContext(true);
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createClinician();
        User clinician = TestHelper.createClinician();

        context.getContext().setUser(author);
        context.getContext().setClinician(clinician);
        context.getContext().setLocation(TestHelper.createLocation());

        Product product1 = TestHelper.createProduct(ProductArchetypes.MEDICATION, null);
        Product product2 = TestHelper.createProduct(ProductArchetypes.SERVICE, null);
        Product product3 = TestHelper.createProduct(ProductArchetypes.MERCHANDISE, null);
        Product product4 = TestHelper.createProduct(ProductArchetypes.MEDICATION, null);

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

        TaxRules taxRules = new TaxRules(context.getContext().getPractice());
        BigDecimal tax1 = taxRules.calculateTax(amount1, product1, true);
        BigDecimal tax2 = taxRules.calculateTax(amount2, product2, true);
        BigDecimal tax3 = taxRules.calculateTax(amount3, product3, true);
        BigDecimal tax4 = taxRules.calculateTax(amount4, product4, true);

        Act item1 = createEstimationItem(patient, product1, author, quantity1, price1);
        Act item2 = createEstimationItem(patient, product2, author, quantity2, price2);
        Act item3 = createEstimationItem(patient, product3, author, quantity3, price3);
        Act item4 = createEstimationItem(patient, product4, author, quantity4, price4);
        Act estimation = createEstimation(customer, author, item1, item2, item3, item4);

        save(estimation, item1, item2, item3, item4);

        EstimationInvoicer invoicer = new TestEstimationInvoicer();

        CustomerChargeActEditDialog dialog = invoicer.invoice(estimation, context);
        IMObjectEditor editor = dialog.getEditor();
        assertTrue(SaveHelper.save(editor));

        FinancialAct invoice = (FinancialAct) editor.getObject();

        BigDecimal total = sum(amount1, amount2, amount3, amount4);
        BigDecimal tax = sum(tax1, tax2, tax3, tax4);
        assertTrue(total.compareTo(invoice.getTotal()) == 0);
        assertEquals(EstimationActStatus.INVOICED, estimation.getStatus());

        ActBean bean = new ActBean(invoice);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(4, items.size());
        checkCharge(invoice, customer, author, clinician, tax, total);

        BigDecimal discount = BigDecimal.ZERO;
        checkItem(items, patient, product1, author, clinician, quantity1, BigDecimal.ZERO, price1,
                  BigDecimal.ZERO, BigDecimal.ZERO, discount, tax1, amount1, null, 1);
        checkItem(items, patient, product2, author, clinician, quantity2, BigDecimal.ZERO, price2,
                  BigDecimal.ZERO, BigDecimal.ZERO, discount, tax2, amount2, null, 0);
        checkItem(items, patient, product3, author, clinician, quantity3, BigDecimal.ZERO, price3,
                  BigDecimal.ZERO, BigDecimal.ZERO, discount, tax3, amount3, null, 0);
        checkItem(items, patient, product4, author, clinician, quantity4, BigDecimal.ZERO, price4,
                  BigDecimal.ZERO, BigDecimal.ZERO, discount, tax4, amount4, null, 1);
    }

    /**
     * Sums a set of amounts.
     * NOTE: this rounds first, which is the same behaviour as {@link ActCalculator#sum}. Not sure if this is correct.
     * TODO.
     *
     * @param amounts the amounts
     * @return the sum of the amounts
     */
    private BigDecimal sum(BigDecimal... amounts) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) {
            result = result.add(MathRules.round(amount));
        }
        return result;
    }

    /**
     * Creates an estimation.
     *
     * @param customer the customer
     * @param author   the author
     * @param items    the estimation items
     * @return a new estimation
     */
    private Act createEstimation(Party customer, User author, Act... items) {
        Act estimation = (Act) create(EstimationArchetypes.ESTIMATION);
        ActBean bean = new ActBean(estimation);
        bean.setParticipant(CustomerArchetypes.CUSTOMER_PARTICIPATION, customer);
        bean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION, author);
        BigDecimal lowTotal = BigDecimal.ZERO;
        BigDecimal highTotal = BigDecimal.ZERO;
        for (Act item : items) {
            ActBean itemBean = new ActBean(item);
            lowTotal = lowTotal.add(itemBean.getBigDecimal("lowTotal"));
            highTotal = highTotal.add(itemBean.getBigDecimal("highTotal"));
            bean.addRelationship(EstimationArchetypes.ESTIMATION_ITEM_RELATIONSHIP, item);
        }
        bean.setValue("lowTotal", lowTotal);
        bean.setValue("highTotal", highTotal);
        return estimation;
    }

    /**
     * Creates an estimation item.
     *
     * @param patient   the patient
     * @param product   the product
     * @param author    the author
     * @param quantity  the quantity
     * @param unitPrice the unit price
     * @return a new estimation item
     */
    private Act createEstimationItem(Party patient, Product product, User author, BigDecimal quantity,
                                     BigDecimal unitPrice) {
        Act item = (Act) create(EstimationArchetypes.ESTIMATION_ITEM);
        ActBean itemBean = new ActBean(item);
        itemBean.setParticipant(PatientArchetypes.PATIENT_PARTICIPATION, patient);
        itemBean.setParticipant(ProductArchetypes.PRODUCT_PARTICIPATION, product);
        itemBean.setParticipant(UserArchetypes.AUTHOR_PARTICIPATION, author);
        itemBean.setValue("highQty", quantity);
        itemBean.setValue("highUnitPrice", unitPrice);
        getArchetypeService().deriveValues(item);
        return item;
    }

    private static class TestEstimationInvoicer extends EstimationInvoicer {

        /**
         * Creates a new <tt>TestEstimationInvoicer</tt>.
         *
         * @param invoice the invoice
         * @param context the layout context
         * @return a new charge editor
         */
        @Override
        protected ChargeEditor createChargeEditor(FinancialAct invoice, LayoutContext context) {
            final PopupEditorManager manager = new PopupEditorManager() {
                @Override
                protected void edit(EditDialog dialog) {
                    super.edit(dialog);
                    Button ok = dialog.getButtons().getButton(PopupDialog.OK_ID);
                    assertNotNull(ok);
                    assertTrue(ok.isEnabled());
                    ok.fireActionPerformed(new ActionEvent(ok, ok.getActionCommand()));
                }
            };
            return new ChargeEditor(invoice, context) {
                @Override
                protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
                    ActRelationshipCollectionEditor editor = super.createItemsEditor(act, items);
                    if (editor instanceof ChargeItemRelationshipCollectionEditor) {
                        // register a handler for act popups
                        ((ChargeItemRelationshipCollectionEditor) editor).setPopupEditorManager(manager);
                    }
                    return editor;
                }

            };
        }
    }

}
