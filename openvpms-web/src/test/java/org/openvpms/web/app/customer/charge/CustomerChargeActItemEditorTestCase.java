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

package org.openvpms.web.app.customer.charge;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

/**
 * Tests the {@link CustomerChargeActItemEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerChargeActItemEditorTestCase extends AbstractAppTest {
    private Party practice;
    private Party customer;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        practice = TestHelper.getPractice();
        practice.addClassification(createTaxType());
        customer = TestHelper.createCustomer();
        save(practice);
    }

    /**
     * Tests populating an invoice item with a medication.
     */
    @Test
    public void testInvoiceItemMedication() {
        checkInvoiceItem(ProductArchetypes.MEDICATION);
    }

    /**
     * Tests populating an invoice item with a merchandise product.
     */
    @Test
    public void testInvoiceItemMerchandise() {
        checkInvoiceItem(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Tests populating an invoice item with a service product.
     */
    @Test
    public void testInvoiceItemService() {
        checkInvoiceItem(ProductArchetypes.SERVICE);
    }

    /**
     * Tests populating a counter sale item with a medication product.
     */
    @Test
    public void testCounterSaleItemMedication() {
        checkCounterSaleItem(ProductArchetypes.MEDICATION);
    }

    /**
     * Tests populating a counter sale item with a merchandise product.
     */
    @Test
    public void testCounterSaleItemMerchandise() {
        checkCounterSaleItem(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Tests populating a counter sale item with a service product.
     */
    @Test
    public void testCounterSaleItemService() {
        checkCounterSaleItem(ProductArchetypes.SERVICE);
    }

    /**
     * Tests populating a credit item with a medication product.
     */
    @Test
    public void testCreditItemMedication() {
        checkCreditItem(ProductArchetypes.MEDICATION);
    }

    /**
     * Tests populating a credit item with a merchandise product.
     */
    @Test
    public void testCreditItemMerchandise() {
        checkCreditItem(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Tests populating a credit item with a service product.
     */
    @Test
    public void testCreditItemService() {
        checkCreditItem(ProductArchetypes.SERVICE);
    }

    /**
     * Checks populating an invoice item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkInvoiceItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new Money(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItem(invoice, item, productShortName);
    }

    /**
     * Checks populating a counter sale item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkCounterSaleItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new Money(100), customer, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct counterSale = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItem(counterSale, item, productShortName);
    }

    /**
     * Checks populating a credit item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkCreditItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new Money(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct credit = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItem(credit, item, productShortName);
    }

    /**
     * Checks populating a charge item with a product.
     *
     * @param charge           the charge
     * @param item             the charge item
     * @param productShortName the product archetype short name
     */
    private void checkItem(FinancialAct charge, FinancialAct item, String productShortName) {
        LayoutContext context = new DefaultLayoutContext();
        Party patient = TestHelper.createPatient();
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");
        Product product = createProduct(productShortName, fixedCost, fixedPrice, unitCost, unitPrice);
        User author = TestHelper.createUser();
        context.getContext().setUser(author); // to propagate to acts

        CustomerChargeActItemEditor editor = new CustomerChargeActItemEditor(item, charge, context);
        editor.getComponent();
        assertFalse(editor.isValid());

        // register a handler for act popups
        EditorManager mgr = new EditorManager();
        editor.setPopupEditorManager(mgr);

        // populate quantity, patient, product. If the product is a medication, it should trigger a patient medication
        // editor popup
        editor.setQuantity(quantity);

        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient);
        }
        editor.setProduct(product);

        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)
            && TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            // invoice items have a dispensing node
            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(mgr, PatientArchetypes.PATIENT_MEDICATION); // save the popup editor - should be a medication
        }
        // editor should now be valid
        assertTrue(editor.isValid());

        // save it
        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        checkItem(item, quantity, unitCost, unitPrice, fixedCost, fixedPrice, discount, tax, total);
        ActBean itemBean = new ActBean(item);
        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)
            && TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            // verify there is a medication act
            checkMedication(item, patient, product, author);
        } else {
            // verify there are no medication acts
            assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
        }
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param item       the item to check
     * @param quantity   the expected quantity
     * @param unitCost   the expected unit cost
     * @param unitPrice  the expected unit price
     * @param fixedCost  the expected fixed cost
     * @param fixedPrice the expected fixed price
     * @param discount   the expected discount
     * @param tax        the expected tax
     * @param total      the expected total
     */
    private void checkItem(FinancialAct item, BigDecimal quantity, BigDecimal unitCost, BigDecimal unitPrice,
                           BigDecimal fixedCost, BigDecimal fixedPrice, BigDecimal discount, BigDecimal tax,
                           BigDecimal total) {
        ActBean bean = new ActBean(item);
        checkEquals(quantity, bean.getBigDecimal("quantity"));
        checkEquals(fixedCost, bean.getBigDecimal("fixedCost"));
        checkEquals(fixedPrice, bean.getBigDecimal("fixedPrice"));
        checkEquals(unitPrice, bean.getBigDecimal("unitPrice"));
        checkEquals(unitCost, bean.getBigDecimal("unitCost"));
        checkEquals(discount, bean.getBigDecimal("discount"));
        checkEquals(tax, bean.getBigDecimal("tax"));
        checkEquals(total, bean.getBigDecimal("total"));
    }

    /**
     * Verifies a patient medication act matches that expected.
     *
     * @param item    the charge item, linked to the medication
     * @param patient the expected patient
     * @param product the expected product
     * @param author  the expected author
     */
    private void checkMedication(Act item, Party patient, Product product, User author) {
        ActBean itemBean = new ActBean(item);
        List<Act> dispensing = itemBean.getNodeActs("dispensing");
        assertEquals(1, dispensing.size());

        Act medication = dispensing.get(0);
        ActBean bean = new ActBean(medication);
        assertEquals(item.getActivityStartTime(), medication.getActivityStartTime());
        assertEquals(item.getActivityEndTime(), medication.getActivityEndTime());
        assertTrue(bean.isA(PatientArchetypes.PATIENT_MEDICATION));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
    }

    /**
     * Saves the current popup editor.
     *
     * @param mgr       the popup editor manager
     * @param shortName the expected archetype short name of the object being edited
     */
    private void checkSavePopup(EditorManager mgr, String shortName) {
        EditDialog dialog = mgr.getCurrent();
        assertNotNull(dialog);
        assertTrue(TypeHelper.isA(dialog.getEditor().getObject(), shortName));
        clickDialogOK(dialog);
        assertTrue(dialog.getEditor().isValid());
    }

    /**
     * Saves a charge and charge item editor in a single transaction, verifying the save was successful.
     *
     * @param charge the charge
     * @param editor the charge item editor
     */
    private void checkSave(final FinancialAct charge, final CustomerChargeActItemEditor editor) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        boolean result = template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                return SaveHelper.save(charge) && editor.save();
            }
        });
        assertTrue(result);
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedCost  the fixed cost
     * @param fixedPrice the fixed price
     * @param unitCost   the unit cost
     * @param unitPrice  the unit price
     * @return a new product
     */
    private Product createProduct(String shortName, BigDecimal fixedCost, BigDecimal fixedPrice, BigDecimal unitCost,
                                  BigDecimal unitPrice) {
        Product product = TestHelper.createProduct(shortName, null, false);
        product.addProductPrice(createFixedPrice(product, fixedCost, fixedPrice));
        product.addProductPrice(createUnitPrice(product, unitCost, unitPrice));
        save(product);
        return product;
    }

    /**
     * Helper to create a new unit price.
     *
     * @param product the product
     * @param cost    the cost price
     * @param price   the price after markup
     * @return a new unit price
     */
    private ProductPrice createUnitPrice(Product product, BigDecimal cost, BigDecimal price) {
        return createPrice(product, ProductArchetypes.UNIT_PRICE, cost, price);
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param product the product
     * @param cost    the cost price
     * @param price   the price after markup
     * @return a new unit price
     */
    private ProductPrice createFixedPrice(Product product, BigDecimal cost, BigDecimal price) {
        return createPrice(product, ProductArchetypes.FIXED_PRICE, cost, price);
    }

    /**
     * Helper to create a new product price.
     *
     * @param product   the product
     * @param shortName the product price archetype short name
     * @param cost      the cost price
     * @param price     the price after markup
     * @return a new unit price
     */
    private ProductPrice createPrice(Product product, String shortName, BigDecimal cost, BigDecimal price) {
        ProductPrice result = (ProductPrice) create(shortName);
        ProductPriceRules rules = new ProductPriceRules();
        BigDecimal markup = rules.getMarkup(product, cost, price, practice);
        result.setName("XPrice");
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("cost", cost);
        bean.setValue("markup", markup);
        bean.setValue("price", price);
        return result;
    }


    /**
     * Helper to click OK on an edit dialog.
     *
     * @param dialog the dialog
     */
    private void clickDialogOK(EditDialog dialog) {
        Button ok = dialog.getButtons().getButton(PopupDialog.OK_ID);
        assertNotNull(ok);
        assertTrue(ok.isEnabled());
        ok.fireActionPerformed(new ActionEvent(ok, ok.getActionCommand()));
    }

    /**
     * Helper to create and save a new tax type classification.
     *
     * @return a new tax classification
     */
    private Lookup createTaxType() {
        Lookup tax = (Lookup) create("lookup.taxType");
        IMObjectBean bean = new IMObjectBean(tax);
        bean.setValue("code", "XTAXRULESTESTCASE_CLASSIFICATION_"
                              + Math.abs(new Random().nextInt()));
        bean.setValue("rate", new BigDecimal(10));
        save(tax);
        return tax;
    }

    private static class EditorManager extends PopupEditorManager {

        /**
         * The current edit dialog.
         */
        private EditDialog current;

        /**
         * Returns the current popup dialog.
         *
         * @return the current popup dialog. May be <tt>null</tt>
         */
        public EditDialog getCurrent() {
            return current;
        }

        /**
         * Displays an edit dialog.
         *
         * @param dialog the dialog
         */
        @Override
        protected void edit(EditDialog dialog) {
            super.edit(dialog);
            current = dialog;
        }
    }
}
