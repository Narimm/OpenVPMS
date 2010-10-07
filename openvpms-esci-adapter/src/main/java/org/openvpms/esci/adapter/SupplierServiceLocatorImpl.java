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
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.service.OrderService;
import org.openvpms.esci.service.client.ServiceLocator;
import org.openvpms.esci.service.client.ServiceLocatorFactory;

import javax.annotation.Resource;
import java.net.MalformedURLException;


/**
 * Returns proxies for supplier web services.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SupplierServiceLocatorImpl implements SupplierServiceLocator {

    /**
     * The service locator factory.
     */
    private ServiceLocatorFactory locatorFactory;

    /**
     * The bean factory
     */
    private IMObjectBeanFactory factory;

    /**
     * Supplier rules.
     */
    private SupplierRules rules;

    /**
     * Sets the service locator factory.
     *
     * @param factory the service lcoator factory
     */
    @Resource
    public void setServiceLocatorFactory(ServiceLocatorFactory factory) {
        locatorFactory = factory;
    }

    /**
     * Sets the supplier rules.
     *
     * @param rules the supplier rules
     */
    @Resource
    public void setSupplierRules(SupplierRules rules) {
        this.rules = rules;
    }

    /**
     * Sets the bean factory.
     *
     * @param factory the bean factory
     */
    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

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
    public OrderService getOrderService(Party supplier, Party stockLocation) {
        EntityRelationship config = rules.getSupplierStockLocation(supplier, stockLocation);
        if (config == null) {
            throw new ESCIAdapterException(ESCIAdapterMessages.ESCINotConfigured(supplier, stockLocation));
        }
        if (!TypeHelper.isA(config, SupplierArchetypes.SUPPLIER_STOCK_LOCATION_RELATIONSHIP_ESCI)) {
            String shortName = config.getArchetypeId().getShortName();
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidSupplierServiceLocatorConfig(shortName));
        }

        IMObjectBean bean = factory.createBean(config);
        String username = bean.getString("username");
        String password = bean.getString("password");

        String serviceURL = bean.getString("orderServiceURL");
        if (StringUtils.isEmpty(serviceURL)) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidSupplierURL(supplier, serviceURL));
        }
        try {
            ServiceLocator<OrderService> locator
                    = locatorFactory.getServiceLocator(OrderService.class, serviceURL, username, password);
            return locator.getService();
        } catch (MalformedURLException exception) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidSupplierURL(supplier, serviceURL), exception);
        }
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
    public OrderService getOrderService(String serviceURL, String username, String password) {
/*
        StringBuilder wsdl = new StringBuilder(serviceURL);
        if (!serviceURL.endsWith("/")) {
            wsdl.append('/');
        }
        wsdl.append(locator.getServiceName());
        wsdl.append("?wsdl");
*/

        try {
            ServiceLocator<OrderService> locator
                    = locatorFactory.getServiceLocator(OrderService.class, serviceURL, username, password);
            return locator.getService();
        } catch (MalformedURLException exception) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidServiceURL(serviceURL), exception);
        }
    }
}

