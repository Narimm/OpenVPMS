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
import org.oasis.ubl.OrderResponseSimpleType;
import org.oasis.ubl.common.basic.RejectionNoteType;
import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.exception.ESCIException;


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
     * Verifies that an {@link ESCIException} is raised if the associated order doesn't exist.
     */
    @Test
    public void testMapResponseNoOrder() {
        User user = createESCIUser(getSupplier());
        OrderResponseSimpleType response = createOrderResponseSimple(934, true);
        checkMappingException(response, user,
                              "ESCIA-0108: Invalid Order: 934 referenced by OrderResponseSimple: 12345");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the associated order doesn't exist.
     */
    @Test
    public void testMapResponseNoUserSupplierRelationship() {
        User user = createESCIUser();
        FinancialAct order = createOrder();
        OrderResponseSimpleType response = createOrderResponseSimple(order.getId(), true);
        String expectedMessage = "ESCIA-0109: User Foo (" + user.getId()
                                 + ") has no relationship to supplier Xsupplier (" + getSupplier().getId() + ")";
        checkMappingException(response, user, expectedMessage);
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the UBL version doesn't match that expected.
     */
    @Test
    public void testInvalidUBLVersion() {
        OrderResponseSimpleType response = createOrderResponseSimple(123, true);
        response.getUBLVersionID().setValue("2.1");
        checkMappingException(response, createESCIUser(),
                              "ESCIA-0103: Expected 2.0 for UBLVersionID in OrderResponseSimple: 12345 but got 2.1");
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
        User user = createESCIUser(getSupplier());
        OrderResponseMapper mapper = createOrderResponseMapper();
        OrderResponseSimpleType response = createOrderResponseSimple(order.getId(), accepted);
        response.setRejectionNote(UBLHelper.initText(new RejectionNoteType(), rejectionNote));
        FinancialAct act = mapper.map(response, user);
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
     * @param user            the ESCI user
     * @param expectedMessage the expected exception message
     */
    private void checkMappingException(OrderResponseSimpleType response, User user, String expectedMessage) {
        OrderResponseMapper mapper = createOrderResponseMapper();
        try {
            mapper.map(response, user);
            fail("Expected mapping to fail");
        } catch (ESCIException expected) {
            assertEquals(expectedMessage, expected.getMessage());
        }
    }

}
