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
package org.openvpms.esci.adapter.client;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.esci.adapter.client.jaxws.SupplierWebServiceLocator;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.service.OrderService;


/**
 * Helper to look up an OrderService in the current VM.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InVMSupplierServiceLocator extends SupplierWebServiceLocator {

    /**
     * Returns a proxy for a supplier's {@link OrderService}.
     * <p/>
     * This uses the <em>entityRelationship.supplierStockLocationESCI</em> associated with the supplier and stock
     * location to lookup the web service.
     *
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @return a proxy for the service provided by the supplier
     * @throws ESCIAdapterException if the associated <tt>orderServiceURL</tt> is invalid, or the supplier-stock
     *                              location relationship is not supported
     */
    @Override
    public OrderService getOrderService(Party supplier, Party stockLocation) {
        SupplierServices services = new SupplierServices(supplier, stockLocation);
        return services.getOrderService("in-vm://orderService", "in-vm://registryService");
    }

    /**
     * Returns a proxy for a supplier's {@link OrderService}.
     *
     * @param serviceURL the WSDL document URL of the service
     * @param username   the username to connect to the service with
     * @param password   the password to connect  to the service with
     * @return a proxy for the service provided by the supplier
     * @throws ESCIAdapterException if <tt>serviceURL</tt> is invalid
     */
    @Override
    public OrderService getOrderService(String serviceURL, String username, String password) {
        SupplierServices services = new SupplierServices(serviceURL, username, password);
        return services.getOrderService("in-vm://orderService", "in-vm://registryService");
    }
}
