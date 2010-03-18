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
package org.openvpms.esci.adapter;

import org.oasis.ubl.OrderType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.esci.service.OrderService;


/**
 * Implementation of {@link OrderServiceAdapter} that adapts <em>act.supplierOrder</em> and submits them to
 * the corresponding supplier's {@link OrderService}, using JAX-WS.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderWebServiceAdapter implements OrderServiceAdapter {

    /**
     * The order mapper.
     */
    private final OrderMapper mapper;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The supplier web service locator.
     */
    private final SupplierServiceLocator<OrderService> locator;

    /**
     * Constructs a <tt>SOAPOrderServiceAdapter</tt>.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     */
    public OrderWebServiceAdapter(IArchetypeService service, ILookupService lookupService) {
        this.service = service;
        mapper = new OrderMapper(service, lookupService);
        locator = new SupplierServiceLocator<OrderService>(OrderService.class, service);
    }

    /**
     * Submits an order to a supplier.
     *
     * @param order the <em>act.supplierOrder</em> to submit
     * @throws org.openvpms.component.system.common.exception.OpenVPMSException
     *          for any error
     */
    public void submitOrder(Act order) {
        ActBean bean = new ActBean(order, service);
        Party supplier = (Party) bean.getNodeParticipant("supplier");
        OrderService orderService = locator.getService(supplier);
        OrderType orderType = mapper.map(order);
        orderService.submitOrder(orderType);
    }

}
