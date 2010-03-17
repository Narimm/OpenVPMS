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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.esci.service.client.ServiceLocator;

import java.net.MalformedURLException;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SOAPServiceLocator<T> {

    @SuppressWarnings("unchecked")
    private final IArchetypeService service;

    private final Class<T> serviceInterface;

    public SOAPServiceLocator(Class<T> serviceInterface, IArchetypeService service) {
        this.serviceInterface = serviceInterface;
        this.service = service;
    }

    public T getService(Party supplier) {
        Entity config = getESCIConfiguration(supplier);
        if (config == null) {
            throw new ESCIAdapterException(ESCIAdapterException.ErrorCode.ESCINotConfigured, supplier.getId(),
                                           supplier.getName());
        }
        if (!TypeHelper.isA(config, "esci.ESCIConfigurationSOAP")) {
            throw new IllegalStateException("UBLOrderService cannot support configurations of type: "
                                            + config.getArchetypeId().getShortName());
        }

        IMObjectBean bean = new IMObjectBean(config, service);
        String username = bean.getString("username");
        String password = bean.getString("password");
        String url = bean.getString("esciURL");
        String wsdl = url + "/OrderService?wsdl";
        ServiceLocator<T> locator;
        try {
            locator = ServiceLocator.create(serviceInterface, wsdl);
        } catch (MalformedURLException exception) {
            throw new ESCIAdapterException(ESCIAdapterException.ErrorCode.InvalidServiceURL, exception,
                                           supplier.getId(), supplier.getName(), wsdl);
        }
        if (!StringUtils.isEmpty(username)) {
            locator.setUsername(username);
        }
        if (!StringUtils.isEmpty(password)) {
            locator.setPassword(password);
        }
        return locator.getService();
    }

    private Entity getESCIConfiguration(Party supplier) {
        Entity result = null;
        EntityBean bean = new EntityBean(supplier, service);
        if (bean.hasNode("esci")) {
            result = bean.getNodeTargetEntity("esci");
        }
        return result;
    }

}
