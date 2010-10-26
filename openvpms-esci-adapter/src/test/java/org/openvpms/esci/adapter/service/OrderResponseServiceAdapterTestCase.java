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
package org.openvpms.esci.adapter.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.oasis.ubl.OrderResponseSimpleType;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.FutureValue;
import org.openvpms.esci.adapter.map.order.AbstractOrderResponseTest;
import org.openvpms.esci.adapter.map.order.OrderResponseMapper;
import org.openvpms.esci.exception.ESCIException;


/**
 * Tests the {@link OrderResponseServiceAdapter} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderResponseServiceAdapterTestCase extends AbstractOrderResponseTest {

    /**
     * Tests submitting an order response.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSubmitOrderResponseSimple() throws Exception {
        // create a default author to receive system messages
        User author = initDefaultAuthor();

        // set up the ESCI user
        User user = createESCIUser(getSupplier());
        initSecurityContext(user);

        // set up the listener
        final FutureValue<FinancialAct> future = new FutureValue<FinancialAct>();
        SystemMessageOrderResponseListener listener = new SystemMessageOrderResponseListener() {
            @Override
            public void receivedResponse(FinancialAct order) {
                super.receivedResponse(order);
                future.set(order);
            }
        };
        listener.setBeanFactory(new IMObjectBeanFactory(getArchetypeService()));

        // set up the service
        OrderResponseServiceAdapter service = createService();
        service.setOrderResponseListener(listener);

        // create an order and refer to it in the response
        FinancialAct order = createOrder();
        OrderResponseSimpleType response = createOrderResponseSimple(order.getId(), true);
        service.submitSimpleResponse(response);

        FinancialAct updatedOrder = future.get(1000);
        assertNotNull(updatedOrder);
        assertEquals(order, updatedOrder);

        // verify a system message was sent to the author
        checkSystemMessage(author, updatedOrder, SystemMessageReason.ORDER_ACCEPTED);
    }

    /**
     * Verifies that an {@link ESCIException} is raised if there is no ESCI user registered when submitting the order
     * response.
     */
    @Test
    public void testSubmitWithNoESCIUser() {
        OrderResponseSimpleType response = createOrderResponseSimple(1234, true);
        OrderResponseServiceAdapter service = createService();
        String expected = "ESCIA-0200: No ESCI user";

        try {
            service.submitSimpleResponse(response);
        } catch (ESCIException exception) {
            assertEquals(expected, exception.getMessage());
        }

        User user = TestHelper.createUser(); // not an ESCI user
        initSecurityContext(user);

        try {
            service.submitSimpleResponse(response);
        } catch (ESCIException exception) {
            assertEquals(expected, exception.getMessage());
        }
    }

    /**
     * Verifies that an {@link ESCIException} is raised if {@link OrderResponseServiceAdapter#submitSimpleResponse}
     * encounters an unexpected exception.
     */
    @Test
    public void testFailedToSubmitResponse() {
        OrderResponseSimpleType response = createOrderResponseSimple(1234, true);
        OrderResponseServiceAdapter service = new OrderResponseServiceAdapter() {
            @Override
            protected User getUser() {
                throw new IllegalStateException("Foo");
            }
        };
        service.setArchetypeService(getArchetypeService());
        service.setOrderResponseMapper(createOrderResponseMapper());
        service.setUserRules(new UserRules());

        String expected = "ESCIA-0500: Failed to submit OrderResponseSimple: Foo";
        try {
            service.submitSimpleResponse(response);
        } catch (ESCIException exception) {
            assertEquals(expected, exception.getMessage());
        }
    }

    /**
     * Creates a new order response service.
     *
     * @return a new service
     */
    private OrderResponseServiceAdapter createService() {
        OrderResponseMapper mapper = createOrderResponseMapper();
        OrderResponseServiceAdapter service = new OrderResponseServiceAdapter();
        service.setArchetypeService(getArchetypeService());
        service.setOrderResponseMapper(mapper);
        service.setUserRules(new UserRules());
        return service;
    }

}
