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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.app.customer.account.AdjustmentActEditor;
import org.openvpms.web.app.workflow.messaging.UserMessageActEditor;
import org.openvpms.web.component.im.edit.act.DefaultParticipationEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.edit.act.ProductParticipationEditor;
import org.openvpms.web.component.im.edit.estimation.EstimationEditor;
import org.openvpms.web.component.im.edit.estimation.EstimationItemEditor;
import org.openvpms.web.component.im.edit.invoice.CustomerInvoiceItemEditor;
import org.openvpms.web.component.im.edit.invoice.InvoiceEditor;
import org.openvpms.web.component.im.edit.invoice.SupplierInvoiceItemEditor;
import org.openvpms.web.component.im.edit.order.OrderEditor;
import org.openvpms.web.component.im.edit.order.OrderItemEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentItemEditor;
import org.openvpms.web.component.im.edit.payment.PaymentEditor;
import org.openvpms.web.component.im.edit.payment.SupplierPaymentItemEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipEditor;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;


/**
 * {@link IMObjectEditorFactory} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectEditorFactoryTestCase extends AbstractAppTest {

    /**
     * The archetype service.
     */
    private IArchetypeService _service;


    /**
     * Verifies that a {@link DefaultIMObjectEditor} is returned when no other
     * class is configured.
     */
    public void testCreateDefaultEditor() {
        checkCreate("party.customerperson", DefaultIMObjectEditor.class);
    }

    /**
     * Verifies that a {@link EntityRelationshipEditor} is returned for
     * <em>entityRelationship.*</em> short names.
     */
    public void testCreateRelationshipEditor() {
        String[] shortNames
                = DescriptorHelper.getShortNames("entityRelationship.*");
        for (String shortName : shortNames) {
            checkCreate(shortName, EntityRelationshipEditor.class);
        }
    }

    /**
     * Verifies that a {@link DefaultParticipationEditor} is returned for
     * participation.author</em>
     */
    public void testCreateDefaultParticipationEditor() {
        String[] shortNames = {"participation.author"};
        for (String shortName : shortNames) {
            checkCreate(shortName, "act.customerEstimationItem",
                        DefaultParticipationEditor.class);
        }
    }

    /**
     * Verifies that a {@link PatientParticipationEditor} is returned for
     * <em>participation.patient</em>
     */
    public void testCreatePatientParticipationEditor() {
        checkCreate("participation.patient", "act.customerEstimationItem",
                    PatientParticipationEditor.class);
    }

    /**
     * Verifies that a {@link ProductParticipationEditor} is returned for
     * <em>participation.product</em>
     */
    public void testCreateProductParticipationEditor() {
        checkCreate("participation.product", "act.customerEstimationItem",
                    ProductParticipationEditor.class);
    }

    /**
     * Verifies that a {@link EstimationEditor} is returned for
     * <em>act.customerEstimationEditor</em>.
     */
    public void testCreateEstimationEditor() {
        checkCreate("act.customerEstimation", EstimationEditor.class);
    }

    /**
     * Verfies that a {@link EstimationItemEditor} is returned for
     * <em>act.customerEstimationItem</em>
     */
    public void testCreateEstimationItemEditor() {
        checkCreate("act.customerEstimationItem", "act.customerEstimation",
                    EstimationItemEditor.class);
    }

    /**
     * Verifies that a {@link InvoiceEditor} is returned for
     * <em>act.customerAccountChargesInvoice, act.customerAccountChargesCredit,
     * act.customerAccountChargesCounter, act.supplierAccountChargesInvoice, and
     * act.supplierAccountChargesCredit</em>
     */
    public void testCreateInvoiceEditor() {
        checkCreate("act.customerAccountChargesInvoice", InvoiceEditor.class);
        checkCreate("act.customerAccountChargesCredit", InvoiceEditor.class);
        checkCreate("act.customerAccountChargesCounter", InvoiceEditor.class);
        checkCreate("act.supplierAccountChargesInvoice", InvoiceEditor.class);
        checkCreate("act.supplierAccountChargesCredit", InvoiceEditor.class);
    }

    /**
     * Verfies that a {@link CustomerInvoiceItemEditor} is created for
     * <em>act.customerAccountInvoiceItem, act.customerAccountCreditItem and
     * act.customerAccountCounterItem</em>
     */
    public void testCreateCustomerIvoiceItemEditor() {
        checkCreate("act.customerAccountInvoiceItem",
                    "act.customerAccountChargesInvoice",
                    CustomerInvoiceItemEditor.class);
        checkCreate("act.customerAccountCreditItem",
                    "act.customerAccountChargesCredit",
                    CustomerInvoiceItemEditor.class);
        checkCreate("act.customerAccountCounterItem",
                    "act.customerAccountChargesCounter",
                    CustomerInvoiceItemEditor.class);
    }

    /**
     * Verfies that a {@link PaymentEditor} is created for
     * <em>act.customerAccountPayment, act.customerAccountRefund,
     * act.supplierAccountPayment and act.supplierAccountRefund</em>
     */
    public void testCreatePaymentEditor() {
        checkCreate("act.customerAccountPayment", PaymentEditor.class);
        checkCreate("act.customerAccountRefund", PaymentEditor.class);
        checkCreate("act.supplierAccountPayment", PaymentEditor.class);
        checkCreate("act.supplierAccountRefund", PaymentEditor.class);
    }

    /**
     * Verfies that a {@link CustomerPaymentItemEditor} is created for
     * <em>act.customerAccountPayment.* and act.customerAccountRefund.*</em>
     */
    public void testCreateCustomerPaymentItemEditor() {
        checkCreate("act.customerAccountPaymentCash",
                    CustomerPaymentItemEditor.class);
        checkCreate("act.customerAccountPaymentCheque",
                    CustomerPaymentItemEditor.class);
        checkCreate("act.customerAccountPaymentCredit",
                    CustomerPaymentItemEditor.class);
        checkCreate("act.customerAccountPaymentEFT",
                    CustomerPaymentItemEditor.class);
        checkCreate("act.customerAccountRefundCash",
                    CustomerPaymentItemEditor.class);
        checkCreate("act.customerAccountRefundCheque",
                    CustomerPaymentItemEditor.class);
        checkCreate("act.customerAccountRefundCredit",
                    CustomerPaymentItemEditor.class);
        checkCreate("act.customerAccountRefundEFT",
                    CustomerPaymentItemEditor.class);
    }

    /**
     * Verifies that an {@link OrderEditor} is created for
     * <em>act.supplierOrder</em>
     */
    public void testCreateOrderEditor() {
        checkCreate("act.supplierOrder", OrderEditor.class);
    }

    /**
     * Verifies that an {@link OrderItemEditor} is created for
     * <em>act.supplierOrderItem</em>
     */
    public void testCreateOrderItemEditor() {
        checkCreate("act.supplierOrderItem", OrderItemEditor.class);
    }

    /**
     * Verifies that a {@link SupplierInvoiceItemEditor} is created for
     * <em>act.supplierAccountInvoiceItem and act.supplierAccountCreditItem</em>
     */
    public void testCreateSupplierInvoiceItemEditor() {
        checkCreate("act.supplierAccountInvoiceItem",
                    SupplierInvoiceItemEditor.class);
        checkCreate("act.supplierAccountCreditItem",
                    SupplierInvoiceItemEditor.class);
    }

    /**
     * Verifies that a {@link SupplierPaymentItemEditor} is created for
     * <em>act.supplierAccountPayment.* and act.supplierAccountRefund.*</em>
     */
    public void testCreateSupplierPaymentItemEditor() {
        checkCreate("act.supplierAccountPaymentCash",
                    SupplierPaymentItemEditor.class);
        checkCreate("act.supplierAccountPaymentCheque",
                    SupplierPaymentItemEditor.class);
        checkCreate("act.supplierAccountPaymentCredit",
                    SupplierPaymentItemEditor.class);
        checkCreate("act.supplierAccountPaymentEFT",
                    SupplierPaymentItemEditor.class);
        checkCreate("act.supplierAccountRefundCash",
                    SupplierPaymentItemEditor.class);
        checkCreate("act.supplierAccountRefundCheque",
                    SupplierPaymentItemEditor.class);
        checkCreate("act.supplierAccountRefundCredit",
                    SupplierPaymentItemEditor.class);
        checkCreate("act.supplierAccountRefundEFT",
                    SupplierPaymentItemEditor.class);
    }

    /**
     * Verifies that an {@link AdjustmentActEditor} is created for
     * <em>act.customerAccountBadDebt</em>,
     * <em>act.customerAccountDebitAdjust</em> and
     * <em>act.customerAccountCreditAdjust</em>
     */
    public void testCreateAdjustmentActEditor() {
        checkCreate("act.customerAccountBadDebt", AdjustmentActEditor.class);
        checkCreate("act.customerAccountCreditAdjust",
                    AdjustmentActEditor.class);
        checkCreate("act.customerAccountDebitAdjust",
                    AdjustmentActEditor.class);
    }

    /**
     * Verifies that an {@link UserMessageActEditor} is created for
     * <em>act.userMessage</em>
     */
    public void testCreateUserMessageActEditor() {
        checkCreate("act.userMessage", UserMessageActEditor.class);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        _service = ServiceHelper.getArchetypeService();
    }

    /**
     * Verifies that the editor returned by {@link IMObjectEditorFactory#create}
     * matches that expected.
     *
     * @param shortName name the archetype short name
     * @param type      the expected editor class
     */
    private void checkCreate(String shortName, Class type) {
        LayoutContext context = new DefaultLayoutContext();
        IMObject object = _service.create(shortName);
        assertNotNull("Failed to create object with shortname=" + shortName,
                      object);
        IMObjectEditor editor = IMObjectEditorFactory.create(object, context);
        assertNotNull("Failed to create editor", editor);
        assertEquals(type, editor.getClass());
    }

    /**
     * Verifies that the editor returned by {@link IMObjectEditorFactory#create}
     * matches that expected.
     *
     * @param shortName       name the archetype short name
     * @param parentShortName the parent archetype short name
     * @param type            the expected editor class
     */
    private void checkCreate(String shortName, String parentShortName,
                             Class type) {
        LayoutContext context = new DefaultLayoutContext();
        IMObject object = _service.create(shortName);
        assertNotNull("Failed to create object with shortname=" + shortName,
                      object);
        IMObject parent = _service.create(parentShortName);
        assertNotNull("Failed to create object with shortname="
                + parentShortName, parent);
        IMObjectEditor editor = IMObjectEditorFactory.create(object, parent,
                                                             context);
        assertNotNull("Failed to create editor", editor);
        assertEquals(type, editor.getClass());
    }

}
