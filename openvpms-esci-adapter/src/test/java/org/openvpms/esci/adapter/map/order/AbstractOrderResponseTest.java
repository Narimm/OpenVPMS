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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.esci.adapter.dispatcher.InboxDocument;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.ubl.common.aggregate.DocumentReferenceType;
import org.openvpms.esci.ubl.common.basic.AcceptedIndicatorType;
import org.openvpms.esci.ubl.common.basic.UBLVersionIDType;
import org.openvpms.esci.ubl.order.OrderResponseSimpleType;

/**
 * Base class for order response test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractOrderResponseTest extends AbstractESCITest {

    /**
     * Creates a new order response.
     *
     * @param orderId  the order identifier to associate the response with
     * @param accepted if <tt>true</tt> indicates that the order was accepted, otherwise indicates that it was rejected
     * @return a new order response
     */
    protected OrderResponseSimpleType createOrderResponseSimple(long orderId, boolean accepted) {
        return createOrderResponseSimple(orderId, getSupplier(), getStockLocation(), accepted);
    }

    /**
     * Creates a new order response.
     *
     * @param orderId       the order identifier to associate the response with
     * @param accepted      if <tt>true</tt> indicates that the order was accepted, otherwise indicates that it was
     *                      rejected
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @return a new order response
     */
    protected OrderResponseSimpleType createOrderResponseSimple(long orderId, Party supplier, Party stockLocation,
                                                                boolean accepted) {
        OrderResponseSimpleType result = new OrderResponseSimpleType();
        result.setID(UBLHelper.createID("12345"));
        result.setUBLVersionID(UBLHelper.initID(new UBLVersionIDType(), "2.0"));
        result.setOrderReference(UBLHelper.createOrderReference(orderId));
        AcceptedIndicatorType indicator = new AcceptedIndicatorType();
        indicator.setValue(accepted);
        result.setAcceptedIndicator(indicator);
        result.setBuyerCustomerParty(createCustomer(stockLocation));
        result.setSellerSupplierParty(createSupplier(supplier));
        return result;
    }

    /**
     * Creates a new <tt>OrderResponseSimpleType</tt> wrapped in a document.
     *
     * @param orderId  the order identifier to associate the response with
     * @param accepted if <tt>true</tt> indicates that the order was accepted, otherwise indicates that it was rejected
     * @return a new order response wrapped in a document
     */
    protected InboxDocument createOrderResponseDocument(long orderId, boolean accepted) {
        return new InboxDocument(new DocumentReferenceType(), createOrderResponseSimple(orderId, accepted));
    }

    /**
     * Returns a new order response mapper.
     *
     * @return a new order response mapper
     */
    protected OrderResponseMapper createOrderResponseMapper() {
        OrderResponseMapperImpl result = new OrderResponseMapperImpl();
        result.setBeanFactory(new IMObjectBeanFactory(getArchetypeService()));
        result.setArchetypeService(getArchetypeService());
        return result;
    }
}
