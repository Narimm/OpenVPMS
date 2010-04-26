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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.esci.service.OrderService;


/**
 * Returns proxies for supplier web services.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface SupplierServiceLocator {

    /**
     * Returns a proxy for a supplier's {@link OrderService}.
     * <p/>
     * This uses the <em>entity.ESCIConfigurationSOAP</em> associated with the supplier to lookup the web service.
     *
     * @param supplier the supplier
     * @return a proxy for the service provided by the supplier
     * @throws ESCIAdapterException if the associated <tt>serviceURL</tt> is invalid
     */
    OrderService getOrderService(Party supplier);

    /**
     * Returns a proxy for a supplier's {@link OrderService}.
     *
     * @param serviceURL the WSDL document URL of the service
     * @param username   the username to connect to the service with
     * @param password   the password to connect  to the service with
     * @return a proxy for the service provided by the supplier
     * @throws ESCIAdapterException if <tt>serviceURL</tt> is invalid
     */
    OrderService getOrderService(String serviceURL, String username, String password);
}
