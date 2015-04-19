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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.map.order;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.order.OrderResponseSimpleType;
import org.openvpms.esci.ubl.common.basic.RejectionNoteType;


/**
 * Tests the {@link OrderResponseMapperImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderResponseMapperTestCase extends AbstractOrderResponseTest {

    /**
     * Verifies that the order is updated with the correct status and message for an 'accepted' order response.
     */
    @Test
    public void testAcceptedOrder() {
        checkMapping(true, null, OrderStatus.ACCEPTED, "Order accepted");
    }

    /**
     * Verifies that the order is updated with the correct status and message for a 'rejected' order response
     * where the response supplies a rejection message.
     */
    @Test
    public void testRejectedOrder() {
        checkMapping(false, "some reason", OrderStatus.REJECTED, "Order rejected: some reason");
    }

    /**
     * Verifies that the order is updated with the correct status and message for a 'rejected' order response
     * where the response supplies no rejection message.
     */
    @Test
    public void testRejectedOrderNoReason() {
        checkMapping(false, null, OrderStatus.REJECTED, "Order rejected. No reason supplied");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the associated order doesn't exist.
     */
    @Test
    public void testMapResponseNoOrder() {
        OrderResponseSimpleType response = createOrderResponseSimple(934, true);
        checkMappingException(response, getSupplier(),
                              "ESCIA-0108: Invalid Order: 934 referenced by OrderResponseSimple: 12345");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the supplier in the response doesn't match that
     * which submitted the response.
     */
    @Test
    public void testSupplierMismatch() {
        Party expected = TestHelper.createSupplier();
        Party supplier = getSupplier();
        FinancialAct order = createOrder();
        OrderResponseSimpleType response = createOrderResponseSimple(order.getId(), true);
        checkMappingException(response, expected,
                              "ESCIA-0109: Expected supplier " + expected.getName() + " (" + expected.getId()
                              + ") for OrderResponseSimple/SellerSupplierParty/CustomerAssignedAccountID in "
                              + "OrderResponseSimple: 12345, but got " + supplier.getName()
                              + " (" + supplier.getId() + ")");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if no stock location is provided in the invoice.
     */
    @Test
    public void testNoStockLocation() {
        FinancialAct order = createOrder();
        OrderResponseSimpleType response = createOrderResponseSimple(order.getId(), true);
        response.getBuyerCustomerParty().setCustomerAssignedAccountID(null);
        checkMappingException(response, getSupplier(),
                              "ESCIA-0112: One of CustomerAssignedAccountID or SupplierAssignedAccountID is required "
                              + "for OrderResponseSimple/BuyerCustomerParty in OrderResponseSimple: 12345");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the
     * Invoice/AccountingCustomerParty/CustomerAssignedAccountID doesn't correspond to a valid stock location.
     */
    @Test
    public void testInvalidStockLocation() {
        FinancialAct order = createOrder();
        OrderResponseSimpleType response = createOrderResponseSimple(order.getId(), true);

        response.getBuyerCustomerParty().getCustomerAssignedAccountID().setValue("0");
        checkMappingException(response, getSupplier(),
                              "ESCIA-0113: Invalid stock location: 0 referenced by OrderResponseSimple: 12345, "
                              + "element OrderResponseSimple/BuyerCustomerParty/CustomerAssignedAccountID");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the stock location in the response doesn't match that
     * expected.
     */
    @Test
    public void testStockLocationMismatch() {
        Party supplier = getSupplier();
        Party expected = getStockLocation();
        FinancialAct order = createOrder();
        Party stockLocation = createStockLocation();
        OrderResponseSimpleType response = createOrderResponseSimple(order.getId(), supplier,
                                                                     stockLocation, true);
        checkMappingException(response, supplier,
                              "ESCIA-0114: Expected stock location " + expected.getName() + " (" + expected.getId()
                              + ") for OrderResponseSimple/BuyerCustomerParty/CustomerAssignedAccountID in "
                              + "OrderResponseSimple: 12345, but got " + stockLocation.getName()
                              + " (" + stockLocation.getId() + ")");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the UBL version doesn't match that expected.
     */
    @Test
    public void testInvalidUBLVersion() {
        OrderResponseSimpleType response = createOrderResponseSimple(123, true);
        response.getUBLVersionID().setValue("2.1");
        checkMappingException(response, getSupplier(),
                              "ESCIA-0103: Expected 2.0 for UBLVersionID in OrderResponseSimple: 12345 but got 2.1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if a duplicate response is received.
     */
    @Test
    public void testDuplicateResponse() {
        FinancialAct order = createOrder();
        OrderResponseMapper mapper = createOrderResponseMapper();
        OrderResponseSimpleType response = createOrderResponseSimple(order.getId(), true);
        FinancialAct act = mapper.map(response, getSupplier(), getStockLocation(), null);
        save(act);

        checkMappingException(response, getSupplier(),
                              "ESCIA-0403: Duplicate response: 12345 received for order: " + order.getId());
    }

    /**
     * Checks mapping of an order response.
     *
     * @param accepted      determines if the order should be accepted or rejected
     * @param rejectionNote the rejection note, for rejected orders. May be <tt>null</tt>
     * @param status        the expected status
     * @param message       the expected message
     */
    private void checkMapping(boolean accepted, String rejectionNote, String status, String message) {
        FinancialAct order = createOrder();
        OrderResponseMapper mapper = createOrderResponseMapper();
        OrderResponseSimpleType response = createOrderResponseSimple(order.getId(), accepted);
        response.setRejectionNote(UBLHelper.initText(new RejectionNoteType(), rejectionNote));
        FinancialAct act = mapper.map(response, getSupplier(), getStockLocation(), null);
        save(act);

        assertEquals(order.getObjectReference(), act.getObjectReference());
        ActBean bean = new ActBean(act);

        assertEquals(status, bean.getString("status"));
        assertEquals(message, bean.getString("supplierResponse"));
    }

    /**
     * Verifies that mapping an invalid response fails with an appropriate exception.
     *
     * @param response        the invalid response
     * @param supplier        the supplier submitting the response
     * @param expectedMessage the expected exception message
     */
    private void checkMappingException(OrderResponseSimpleType response, Party supplier, String expectedMessage) {
        OrderResponseMapper mapper = createOrderResponseMapper();
        try {
            mapper.map(response, supplier, getStockLocation(), null);
            fail("Expected mapping to fail");
        } catch (ESCIAdapterException expected) {
            assertEquals(expectedMessage, expected.getMessage());
        }
    }

}
