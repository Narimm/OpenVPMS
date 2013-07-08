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
package org.openvpms.esci.adapter.dispatcher;

import org.junit.Test;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.FutureValue;
import org.openvpms.esci.adapter.dispatcher.order.OrderResponseProcessor;
import org.openvpms.esci.adapter.dispatcher.order.SystemMessageOrderResponseListener;
import org.openvpms.esci.adapter.map.order.AbstractOrderResponseTest;
import org.openvpms.esci.adapter.map.order.OrderResponseMapper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Tests the {@link OrderResponseProcessor} class.
 *
 * @author Tim Anderson
 */
public class OrderResponseProcessorTestCase extends AbstractOrderResponseTest {

    /**
     * Tests processing an order response.
     *
     * @throws Exception for any error
     */
    @Test
    public void testProcess() throws Exception {
        // create a default author to receive system messages
        User author = initDefaultAuthor();

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

        // set up the processor
        OrderResponseProcessor processor = createProcessor();
        processor.setOrderResponseListener(listener);

        // create an order and refer to it in the response
        FinancialAct order = createOrder();
        InboxDocument response = createOrderResponseDocument(order.getId(), true);
        String accountId = null;
        processor.process(response, getSupplier(), getStockLocation(), accountId);

        FinancialAct updatedOrder = future.get(1000);
        assertNotNull(updatedOrder);
        assertEquals(order, updatedOrder);

        // verify a system message was sent to the author
        checkSystemMessage(author, updatedOrder, SystemMessageReason.ORDER_ACCEPTED);
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if {@link DocumentProcessor#process}
     * encounters an unexpected exception.
     */
    @Test
    public void testFailedToProcess() {
        // create an order and refer to it in the response
        FinancialAct order = createOrder();
        InboxDocument response = createOrderResponseDocument(order.getId(), true);
        OrderResponseProcessor processor = new OrderResponseProcessor() {
            @Override
            protected void notifyListener(FinancialAct order) {
                throw new RuntimeException("Foo");
            }
        };
        processor.setArchetypeService(getArchetypeService());
        processor.setOrderResponseMapper(createOrderResponseMapper());

        String expected = "ESCIA-0500: Failed to process OrderResponseSimple: Foo";
        try {
            processor.process(response, getSupplier(), getStockLocation(), null);
        } catch (ESCIAdapterException exception) {
            assertEquals(expected, exception.getMessage());
        }
    }

    /**
     * Creates a new order response processor.
     *
     * @return a new processor
     */
    private OrderResponseProcessor createProcessor() {
        OrderResponseMapper mapper = createOrderResponseMapper();
        OrderResponseProcessor service = new OrderResponseProcessor();
        service.setArchetypeService(getArchetypeService());
        service.setOrderResponseMapper(mapper);
        return service;
    }

}
