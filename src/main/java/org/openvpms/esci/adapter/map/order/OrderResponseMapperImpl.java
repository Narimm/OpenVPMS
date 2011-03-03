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

import org.openvpms.archetype.rules.supplier.OrderStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.AbstractUBLMapper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.order.OrderResponseSimpleType;

import javax.annotation.Resource;


/**
 * Maps UBL order responses to their corresponding orders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderResponseMapperImpl extends AbstractUBLMapper implements OrderResponseMapper {

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;


    /**
     * Registers the bean factory.
     *
     * @param factory the bean factory
     */
    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * Maps an <tt>OrderResponseSimpleType</tt> to its corresponding order.
     *
     * @param response      the reponse
     * @param supplier      the supplier that submitted the response
     * @param stockLocation the stock location
     * @param accountId     the supplier assigned account identifier. May be <tt>null</tt>
     * @return the corresponding order
     */
    public FinancialAct map(OrderResponseSimpleType response, Party supplier, Party stockLocation, String accountId) {
        UBLOrderResponseSimple wrapper = new UBLOrderResponseSimple(response, getArchetypeService());
        checkUBLVersion(wrapper);

        // check that the response is for the expected supplier and stock location
        wrapper.checkSupplier(supplier, accountId);
        wrapper.checkStockLocation(stockLocation, accountId);
        FinancialAct order = wrapper.getOrder();

        // check that the associated order is for the expected supplier and stock location
        checkOrder(order, supplier, stockLocation, wrapper);

        // check that the response isn't a duplicate
        checkDuplicateResponse(order, response);

        Message message;
        String status;
        if (wrapper.isAccepted()) {
            status = OrderStatus.ACCEPTED;
            message = ESCIAdapterMessages.orderAccepted();
        } else {
            status = OrderStatus.REJECTED;
            String note = wrapper.getRejectionNote();
            if (note != null) {
                message = ESCIAdapterMessages.orderRejected(note);
            } else {
                message = ESCIAdapterMessages.orderRejectedNoReason();
            }
        }
        ActBean bean = factory.createActBean(order);
        bean.setValue("status", status);
        bean.setValue("supplierResponse", message.getMessage());
        return order;
    }

    /**
     * Checks to see if an order response is a duplicate.
     *
     * @param order    the order
     * @param response the order response
     * @throws ESCIAdapterException if the response is a duplicate
     */
    private void checkDuplicateResponse(FinancialAct order, OrderResponseSimpleType response) {
        if (order.getStatus().equals(OrderStatus.ACCEPTED) || order.getStatus().equals(OrderStatus.REJECTED)) {
            throw new ESCIAdapterException(ESCIAdapterMessages.duplicateOrderResponse(order.getId(),
                                                                                      response.getID().getValue()));
        }
    }

}
