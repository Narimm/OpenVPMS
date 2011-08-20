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

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.dialog.PopupDialog;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Random;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;

/**
 * Abstract base class for customer charge act editor tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractCustomerChargeActEditorTest extends AbstractAppTest {

    /**
     * The practice.
     */
    private Party practice;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        practice = TestHelper.getPractice();
        practice.addClassification(createTaxType());
        save(practice);
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param item       the item to check
     * @param patient    the expected patient
     * @param product    the expected product
     * @param author     the expected author
     * @param clinician  the expected clinician
     * @param quantity   the expected quantity
     * @param unitCost   the expected unit cost
     * @param unitPrice  the expected unit price
     * @param fixedCost  the expected fixed cost
     * @param fixedPrice the expected fixed price
     * @param discount   the expected discount
     * @param tax        the expected tax
     * @param total      the expected total
     */
    protected void checkItem(FinancialAct item, Party patient, Product product, User author, User clinician,
                             BigDecimal quantity, BigDecimal unitCost, BigDecimal unitPrice, BigDecimal fixedCost,
                             BigDecimal fixedPrice, BigDecimal discount, BigDecimal tax, BigDecimal total) {
        ActBean bean = new ActBean(item);
        if (bean.hasNode("patient")) {
            org.junit.Assert.assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        }
        org.junit.Assert.assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        org.junit.Assert.assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        if (bean.hasNode("clinician")) {
            org.junit.Assert.assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        }
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
     * Saves the current popup editor.
     *
     * @param mgr       the popup editor manager
     * @param shortName the expected archetype short name of the object being edited
     */
    protected void checkSavePopup(EditorManager mgr, String shortName) {
        EditDialog dialog = mgr.getCurrent();
        assertNotNull(dialog);
        IMObjectEditor editor = dialog.getEditor();
        assertTrue(TypeHelper.isA(editor.getObject(), shortName));
        assertTrue(editor.isValid());
        clickDialogOK(dialog);
    }

    /**
     * Helper to click OK on an edit dialog.
     *
     * @param dialog the dialog
     */
    protected void clickDialogOK(EditDialog dialog) {
        Button ok = dialog.getButtons().getButton(PopupDialog.OK_ID);
        assertNotNull(ok);
        assertTrue(ok.isEnabled());
        ok.fireActionPerformed(new ActionEvent(ok, ok.getActionCommand()));
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param unitCost   the unit cost
     * @param unitPrice  the unit price
     * @return a new product
     */
    protected Product createProduct(String shortName, BigDecimal unitCost, BigDecimal unitPrice) {
        Product product = TestHelper.createProduct(shortName, null, false);
        product.addProductPrice(createUnitPrice(product, unitCost, unitPrice));
        save(product);
        return product;
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
    protected Product createProduct(String shortName, BigDecimal fixedCost, BigDecimal fixedPrice, BigDecimal unitCost,
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
     * Helper to create and save a new tax type classification.
     *
     * @return a new tax classification
     */
    private Lookup createTaxType() {
        Lookup tax = (Lookup) create("lookup.taxType");
        IMObjectBean bean = new IMObjectBean(tax);
        bean.setValue("code", "XTAXRULESTESTCASE_CLASSIFICATION_" + Math.abs(new Random().nextInt()));
        bean.setValue("rate", new BigDecimal(10));
        save(tax);
        return tax;
    }

    protected static class EditorManager extends PopupEditorManager {

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

        /**
         * Invoked when the edit is completed.
         */
        @Override
        protected void editCompleted() {
            super.editCompleted();
            current = null;
        }
    }
}
