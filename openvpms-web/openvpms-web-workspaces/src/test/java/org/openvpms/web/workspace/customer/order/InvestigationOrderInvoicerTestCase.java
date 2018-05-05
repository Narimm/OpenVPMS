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

package org.openvpms.web.workspace.customer.order;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditorTest;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper;
import org.openvpms.web.workspace.customer.charge.TestChargeEditor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link InvestigationOrderInvoicer}.
 *
 * @author Tim Anderson
 */
public class InvestigationOrderInvoicerTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The order rules.
     */
    private OrderRules rules;

    /**
     * The context.
     */
    private Context context;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The product.
     */
    private Product product;

    /**
     * The investigation type.
     */
    private Entity investigationType;


    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();

        // create a product linked to a laboratory
        Party location = TestHelper.createLocation();
        Entity laboratory = CustomerChargeTestHelper.createLaboratory(location);
        product = createProduct(ProductArchetypes.MEDICATION);
        investigationType = ProductTestHelper.createInvestigationType(laboratory, "123456789");
        ProductTestHelper.addInvestigationType(product, investigationType);

        User clinician = TestHelper.createClinician();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        context = new LocalContext();
        context.setPractice(getPractice());

        context.setLocation(location);

        User author = TestHelper.createUser();
        context.setUser(author);
        context.setClinician(clinician);
        context.setPatient(patient);
        context.setCustomer(customer);
        rules = new OrderRules(getArchetypeService());
    }

    /**
     * Verifies that a return to an IN_PROGRESS invoice cancels the investigation.
     */
    @Test
    public void testReturnToInProgressInvoice() {
        TestChargeEditor editor = createInvoice(product);
        editor.setStatus(ActStatus.IN_PROGRESS);
        assertTrue(SaveHelper.save(editor));

        FinancialAct invoiceItem = getInvoiceItem(editor);
        Act investigation = getInvestigation(editor);
        assertEquals(ActStatus.IN_PROGRESS, investigation.getStatus());

        FinancialAct order = createReturn(customer, patient, product, investigationType, invoiceItem, investigation);
        InvestigationOrderInvoicer charger = new TestInvestigationOrderInvoicer(order, rules);
        assertTrue(charger.canCharge(editor));
        charger.charge(editor);

        editor.setStatus(ActStatus.POSTED);
        assertTrue(SaveHelper.save(editor));

        assertEquals(ActStatus.CANCELLED, investigation.getStatus());
    }

    /**
     * Verifies that a return cannot be made to a POSTED invoice.
     */
    @Test
    public void testReturnToPostedInvoice() {
        TestChargeEditor editor1 = createInvoice(product);

        FinancialAct invoiceItem = getInvoiceItem(editor1);
        Act investigation = getInvestigation(editor1);
        assertEquals(ActStatus.IN_PROGRESS, investigation.getStatus());

        editor1.setStatus(ActStatus.POSTED);
        assertTrue(SaveHelper.save(editor1));

        FinancialAct order = createReturn(customer, patient, product, investigationType, invoiceItem, investigation);
        InvestigationOrderInvoicer charger1 = new TestInvestigationOrderInvoicer(order, rules);
        assertFalse(charger1.canCharge(editor1));
    }

    /**
     * Verifies a validation error is produced if a return is missing a customer.
     */
    @Test
    public void testMissingCustomer() {
        String expected = "Customer is required";
        FinancialAct act = createReturn(null, patient, product, investigationType, null, null);
        checkRequired(act, expected);
    }

    /**
     * Verifies a validation error is produced if a return is missing a patient.
     */
    @Test
    public void testMissingPatient() {
        String expected = "Patient is required";
        FinancialAct act = createReturn(customer, null, product, investigationType, null, null);
        checkRequired(act, expected);
    }

    /**
     * Verifies a validation error is produced if a return has no linked investigation.
     */
    @Test
    public void testMissingInvestigationType() {
        String expected = "Investigation is required";
        FinancialAct act = createReturn(customer, patient, product, null, null, null);
        checkRequired(act, expected);
    }

    /**
     * Verifies that a validation error is raised if a required field is missing.
     * <p/>
     * Validation cannot occur using the archetype as as the delivery processor must be able to save incomplete/invalid
     * orders and returns.
     *
     * @param act      the return
     * @param expected the expected validation error
     */
    private void checkRequired(FinancialAct act, String expected) {
        InvestigationOrderInvoicer charger = new InvestigationOrderInvoicer(act, rules);
        assertFalse(charger.isValid());
        Validator validator = new DefaultValidator();
        assertFalse(charger.validate(validator));
        assertEquals(1, validator.getInvalid().size());
        Modifiable modifiable = validator.getInvalid().iterator().next();
        List<ValidatorError> errors = validator.getErrors(modifiable);
        assertEquals(1, errors.size());
        assertEquals(expected, errors.get(0).getMessage());
    }

    /**
     * Creates a new invoice in an editor.
     *
     * @param product the product to invoice
     * @return the invoice editor
     */
    private TestChargeEditor createInvoice(Product product) {
        TestChargeEditor editor = createEditor();
        addItem(editor, patient, product, BigDecimal.ONE, editor.getQueue());
        return editor;
    }

    /**
     * Creates a new invoice editor.
     *
     * @return a new editor
     */
    private TestChargeEditor createEditor() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        LayoutContext layoutContext = new DefaultLayoutContext(context, new HelpContext("foo", null));
        TestChargeEditor editor = new TestChargeEditor(charge, layoutContext, false);
        editor.getComponent();
        return editor;
    }

    /**
     * Creates an investigation return.
     *
     * @param customer          the customer. May be {@code null}
     * @param patient           the patient. May be {@code null}
     * @param product           the product. May be {@code null}
     * @param investigationType the investigation type. Mayh be {@code null}
     * @param invoiceItem       the related invoice item. May be {@code null}
     * @param investigation     the related investigation. May be {@code null}
     * @return a new return
     */
    private FinancialAct createReturn(Party customer, Party patient, Product product, Entity investigationType,
                                      FinancialAct invoiceItem, Act investigation) {
        FinancialAct act = (FinancialAct) create(OrderArchetypes.INVESTIGATION_RETURN);
        FinancialAct item = (FinancialAct) create(OrderArchetypes.INVESTIGATION_RETURN_ITEM);
        ActBean bean = new ActBean(act);
        if (customer != null) {
            bean.addNodeParticipation("customer", customer);
        }
        bean.addNodeRelationship("items", item);

        ActBean itemBean = new ActBean(item);
        if (patient != null) {
            itemBean.addNodeParticipation("patient", patient);
        }
        if (product != null) {
            itemBean.addNodeParticipation("product", product);
        }
        if (investigationType != null) {
            itemBean.addNodeParticipation("investigationType", investigationType);
        }
        if (invoiceItem != null) {
            itemBean.setValue("sourceInvoiceItem", invoiceItem.getObjectReference());
        }
        if (investigation != null) {
            itemBean.setValue("sourceInvestigation", investigation.getObjectReference());
        }
        save(act, item);
        return act;
    }

    /**
     * Returns the invoice item from an invoice editor.
     *
     * @param editor the editor
     * @return the invoice item
     */
    private FinancialAct getInvoiceItem(TestChargeEditor editor) {
        List<Act> acts = editor.getItems().getCurrentActs();
        assertEquals(1, acts.size());
        return (FinancialAct) acts.get(0);
    }

    /**
     * Returns the investigation from an invoice editor.
     *
     * @param editor the editor
     * @return the investigation
     */
    private Act getInvestigation(TestChargeEditor editor) {
        List<Act> acts = editor.getItems().getCurrentActs();
        assertEquals(1, acts.size());
        CustomerChargeActItemEditor itemEditor = editor.getItems().getEditor(acts.get(0));
        List<Act> investigations = itemEditor.getInvestigations();
        assertEquals(1, investigations.size());
        return investigations.get(0);
    }

    private static class TestInvestigationOrderInvoicer extends InvestigationOrderInvoicer {

        /**
         * Constructs a {@link TestInvestigationOrderInvoicer}.
         *
         * @param act   the return act
         * @param rules the order rules
         */
        public TestInvestigationOrderInvoicer(FinancialAct act, OrderRules rules) {
            super(act, rules);
        }

        /**
         * Creates a new {@link CustomerChargeActEditor}.
         *
         * @param charge  the charge
         * @param context the layout context
         * @return a new charge editor
         */
        @Override
        protected CustomerChargeActEditor createChargeEditor(FinancialAct charge, LayoutContext context) {
            return new TestChargeEditor(charge, context, true);
        }
    }

}
