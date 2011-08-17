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

import org.openvpms.esci.adapter.client.jaxws.SupplierWebServiceLocator;

import java.net.MalformedURLException;


/**
 * Helper to look up an OrderService in the current VM.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InVMSupplierServiceLocator extends SupplierWebServiceLocator {

    /**
     * Verifies that an endpoint address is valid.
     *
     * @param endpointAddress the endpoint address to check
     * @throws MalformedURLException if the endpoint address is invalid
     */
    @Override
    protected void checkEndpointAddress(String endpointAddress) throws MalformedURLException {
        if (!endpointAddress.startsWith("in-vm://")) {
            super.checkEndpointAddress(endpointAddress);
        }
    }
}
