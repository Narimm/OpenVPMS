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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import nextapp.echo2.app.event.WindowPaneListener;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.finance.estimate.EstimateTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link EstimateItemEditor} class.
 *
 * @author Tim Anderson
 */
public class EstimateItemEditorTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<String>();

    /**
     * The context.
     */
    private Context context;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();

        // register an ErrorHandler to collect errors
        ErrorHandler.setInstance(new ErrorHandler() {
            @Override
            public void error(Throwable cause) {
                errors.add(cause.getMessage());
            }

            public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
                errors.add(message);
            }
        });
        context = new LocalContext();
        context.setPractice(getPractice());
    }


    /**
     * Verifies that prices and totals are correct when the customer has tax exemptions.
     */
    @Test
    public void testTaxExemption() {
        IMObjectBean bean = new IMObjectBean(getPractice());
        List<Lookup> taxes = bean.getValues("taxes", Lookup.class);
        assertEquals(1, taxes.size());
        customer.addClassification(taxes.get(0));
        save(customer);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createUser();

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal discount = BigDecimal.ZERO;
        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // set up the context
        layout.getContext().setUser(author); // to propagate to acts

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate, layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        // populate quantity, patient, clinician.
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(estimate, editor);

        estimate = get(estimate);
        item = get(item);
        assertNotNull(estimate);
        assertNotNull(item);

        // verify the item matches that expected
        BigDecimal fixedPriceExTax = new BigDecimal("1.82");
        BigDecimal unitPriceExTax = new BigDecimal("9.09");
        BigDecimal totalExTax = new BigDecimal("20");
        checkItem(item, patient, product, author, quantity, unitPriceExTax, fixedPriceExTax, discount, totalExTax);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param item       the item to check
     * @param patient    the expected patient
     * @param product    the expected product
     * @param author     the expected author
     * @param quantity   the expected quantity
     * @param unitPrice  the expected unit price
     * @param fixedPrice the expected fixed price
     * @param discount   the expected discount
     * @param total      the expected total
     */
    protected void checkItem(Act item, Party patient, Product product, User author,
                             BigDecimal quantity, BigDecimal unitPrice, BigDecimal fixedPrice, BigDecimal discount,
                             BigDecimal total) {
        ActBean bean = new ActBean(item);
        if (bean.hasNode("patient")) {
            assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        }
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        checkEquals(quantity, bean.getBigDecimal("lowQty"));
        checkEquals(quantity, bean.getBigDecimal("highQty"));
        checkEquals(fixedPrice, bean.getBigDecimal("fixedPrice"));
        checkEquals(unitPrice, bean.getBigDecimal("lowUnitPrice"));
        checkEquals(unitPrice, bean.getBigDecimal("highUnitPrice"));
        checkEquals(discount, bean.getBigDecimal("discount"));
        checkEquals(total, bean.getBigDecimal("lowTotal"));
        checkEquals(total, bean.getBigDecimal("highTotal"));
    }

    /**
     * Saves an estimate and estimate item editor in a single transaction.
     *
     * @param estimate the estimate
     * @param editor   the charge item editor
     */
    private void checkSave(final Act estimate, final EstimateItemEditor editor) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        boolean saved = template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                return SaveHelper.save(estimate) && editor.save();
            }
        });
        assertTrue(saved);
    }

}
