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
import org.openvpms.esci.service.OrderService;
import org.openvpms.esci.service.client.ServiceLocator;
import org.openvpms.esci.service.client.ServiceLocatorFactory;

import java.net.MalformedURLException;


/**
 * Helper to look up an OrderService in the current VM..
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InVMSupplierServiceLocator extends SupplierWebServiceLocator {

    /**
     * Returns a proxy for a supplier's {@link org.openvpms.esci.service.OrderService}.
     *
     * @param supplier   the supplier
     * @param serviceURL the order service WSDL URL
     * @param username   the user name to connect with
     * @param password   the password to connect with
     * @return a proxy for the service provided by the supplier
     * @throws org.openvpms.esci.adapter.util.ESCIAdapterException
     *                                        if the associated <tt>orderServiceURL</tt> is invalid
     * @throws java.net.MalformedURLException if <tt>serviceURL</tt> is invalid
     */
    @Override
    protected OrderService getOrderService(Party supplier, String serviceURL, String username, String password)
            throws MalformedURLException {
        ServiceLocatorFactory factory = getServiceLocatorFactory();
        ServiceLocator<OrderService> locator = factory.getServiceLocator(OrderService.class, serviceURL,
                                                                         "in-vm://orderService", username,
                                                                         password);
        return locator.getService();
    }
}
