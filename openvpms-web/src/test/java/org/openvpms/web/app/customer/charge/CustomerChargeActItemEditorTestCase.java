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
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
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

/**
 * Tests the {@link CustomerChargeActItemEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerChargeActItemEditorTestCase extends AbstractAppTest {
    private Party practice;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        practice = TestHelper.getPractice();
    }

    /**
     * Tests populating an invoice item with a medication.
     */
    @Test
    public void testInvoiceItemMedication() {
        LayoutContext context = new DefaultLayoutContext();
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        BigDecimal cost = BigDecimal.valueOf(5);
        BigDecimal price = BigDecimal.valueOf(10);
        BigDecimal quantity = BigDecimal.valueOf(2);
        Product product = createMedication(cost, price);
        User author = TestHelper.createUser();
        context.getContext().setUser(author); // to propagate to medication acts
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new Money(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);

        CustomerChargeActItemEditor editor = new CustomerChargeActItemEditor(item, invoice, context);
        editor.getComponent();
        assertFalse(editor.isValid());

        // register a handler for act popups
        EditorManager mgr = new EditorManager();
        editor.setPopupEditorManager(mgr);

        // populate quantity, patient, product (medication). The product should trigger a patient medication editor
        // popup
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setProduct(product);
        assertFalse(editor.isValid());

        checkSavePopup(mgr, PatientArchetypes.PATIENT_MEDICATION); // save the popup editor - should be a medication

        // editor should now be valid
        assertTrue(editor.isValid());

        // save it
        checkSave(invoice, editor);

        invoice = get(invoice);
        item = get(item);
        assertNotNull(invoice);
        assertNotNull(item);

        checkItem(item, quantity, new BigDecimal("0"), new BigDecimal("10"),
                  BigDecimal.ZERO, BigDecimal.ZERO, cost, BigDecimal.ZERO, new BigDecimal("20"));
        checkMedication(item, patient, product, author);
    }


    /**
     * Verifies an item's properties match that expected.
     *
     * @param item       the item to check
     * @param quantity   the expected quantity
     * @param fixedPrice the expected fixed price
     * @param unitPrice  the expected unit price
     * @param discount   the expected discount
     * @param fixedCost  the expected fixed cost
     * @param unitCost   the expected un it cost
     * @param tax        the expected tax
     * @param total      the expected total
     */
    private void checkItem(FinancialAct item, BigDecimal quantity, BigDecimal fixedPrice, BigDecimal unitPrice,
                           BigDecimal discount, BigDecimal fixedCost, BigDecimal unitCost, BigDecimal tax,
                           BigDecimal total) {
        ActBean bean = new ActBean(item);
        checkEquals(quantity, bean.getBigDecimal("quantity"));
        checkEquals(fixedPrice, bean.getBigDecimal("fixedPrice"));
        checkEquals(unitPrice, bean.getBigDecimal("unitPrice"));
        checkEquals(discount, bean.getBigDecimal("discount"));
        checkEquals(fixedCost, bean.getBigDecimal("fixedCost"));
        checkEquals(unitCost, bean.getBigDecimal("unitCost"));
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
     * Helper to create a medication product.
     *
     * @param cost  the product cost
     * @param price the product price
     * @return a new medication
     */
    private Product createMedication(BigDecimal cost, BigDecimal price) {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, null, false);
        product.addProductPrice(createUnitPrice(product, cost, price));
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
        ProductPrice result = (ProductPrice) create(ProductArchetypes.UNIT_PRICE);
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
