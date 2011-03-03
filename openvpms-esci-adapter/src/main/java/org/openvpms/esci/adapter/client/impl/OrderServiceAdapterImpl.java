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
package org.openvpms.esci.adapter.client.impl;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.esci.adapter.client.OrderServiceAdapter;
import org.openvpms.esci.adapter.client.SupplierServiceLocator;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.map.order.OrderMapper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.exception.DuplicateOrderException;
import org.openvpms.esci.service.OrderService;
import org.openvpms.esci.ubl.order.OrderType;

import javax.annotation.Resource;


/**
 * Implementation of {@link OrderServiceAdapter} that adapts <em>act.supplierOrder</em> and submits them to
 * the corresponding supplier's {@link OrderService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderServiceAdapterImpl implements OrderServiceAdapter {

    /**
     * The order mapper.
     */
    private OrderMapper mapper;

    /**
     * The supplier web service locator.
     */
    private SupplierServiceLocator locator;

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;


    /**
     * Constructs an <tt>OrderWebServiceAdapter</tt>.
     */
    public OrderServiceAdapterImpl() {
    }

    /**
     * Sets the order mapper.
     *
     * @param mapper the order mapper
     */
    @Resource
    public void setOrderMapper(OrderMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Sets the supplier service locator.
     *
     * @param locator the supplier service locator
     */
    @Resource
    public void setSupplierServiceLocator(SupplierServiceLocator locator) {
        this.locator = locator;
    }

    /**
     * Sets the bean factory.
     *
     * @param factory the bean factory
     */
    @Resource
    public void setFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * Submits an order to a supplier.
     *
     * @param order the <em>act.supplierOrder</em> to submit
     * @throws OpenVPMSException for any error
     */
    public void submitOrder(FinancialAct order) {
        ActBean bean = factory.createActBean(order);
        Party supplier = (Party) bean.getNodeParticipant("supplier");
        if (supplier == null) {
            throw new IllegalStateException("Argument 'order' has no supplier participant");
        }
        Party stockLocation = (Party) bean.getNodeParticipant("stockLocation");
        if (stockLocation == null) {
            throw new IllegalStateException("Argument 'order' has no stock location participant");
        }
        OrderService orderService = locator.getOrderService(supplier, stockLocation);
        OrderType orderType = mapper.map(order);
        try {
            orderService.submitOrder(orderType);
        } catch (DuplicateOrderException exception) {
            throw new ESCIAdapterException(ESCIAdapterMessages.duplicateOrder(order.getId(), supplier), exception);
        }
    }

}
