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
package org.openvpms.esci.adapter.client.jaxws;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.esci.adapter.client.SupplierServiceLocator;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.service.InboxService;
import org.openvpms.esci.service.OrderService;
import org.openvpms.esci.service.RegistryService;
import org.openvpms.esci.service.client.ServiceLocator;
import org.openvpms.esci.service.client.ServiceLocatorFactory;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;


/**
 * Returns proxies for supplier web services.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SupplierWebServiceLocator implements SupplierServiceLocator {

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
     * @throws ESCIAdapterException if the associated <tt>serviceURL</tt> is invalid, or the supplier-stock
     *                              location relationship is not supported, or the service prxy can't be created
     */
    public OrderService getOrderService(Party supplier, Party stockLocation) {
        SupplierServices services = new SupplierServices(supplier, stockLocation);
        return services.getOrderService();
    }

    /**
     * Returns a proxy for a supplier's {@link OrderService}.
     *
     * @param serviceURL the WSDL document URL of the service
     * @param username   the username to connect to the service with
     * @param password   the password to connect  to the service with
     * @return a proxy for the service provided by the supplier
     * @throws ESCIAdapterException if the associated <tt>serviceURL</tt> is invalid or the proxy cannot be created
     */
    public OrderService getOrderService(String serviceURL, String username, String password) {
        SupplierServices services = new SupplierServices(serviceURL, username, password);
        return services.getOrderService();
    }

    /**
     * Returns a proxy for the supplier's {@link InboxService}.
     * <p/>
     * This uses the <em>entityRelationship.supplierStockLocationESCI</em> associated with the supplier and stock
     * location to lookup the web service.
     *
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @return a proxy for the service provided by the supplier
     * @throws ESCIAdapterException if the associated <tt>serviceURL</tt> is invalid, the supplier-stock
     *                              location relationship is not supported, or the proxy cannot be created
     */
    public InboxService getInboxService(Party supplier, Party stockLocation) {
        SupplierServices services = new SupplierServices(supplier, stockLocation);
        return services.getInboxService();
    }

    /**
     * Returns the service locator factory.
     *
     * @return the service locator factory
     */
    protected ServiceLocatorFactory getServiceLocatorFactory() {
        return locatorFactory;
    }

    /**
     * Helper class to access supplier services.
     */
    protected class SupplierServices {

        /**
         * |
         * The supplier.
         */
        private final Party supplier;

        /**
         * The registry service URL.
         */
        private final String serviceURL;

        /**
         * The user to connect as.
         */
        private final String username;

        /**
         * The user's password.
         */
        private final String password;


        /**
         * Constructs a <tt>SupplierServices</tt> from an <em>entityRelationship.supplierStockLocationESCI</em>
         * associated with the supplier and stock location.
         *
         * @param supplier      the supplier services
         * @param stockLocation the stock location
         * @throws ESCIAdapterException if there is no relationship or the URL is invalid
         */
        public SupplierServices(Party supplier, Party stockLocation) {
            this.supplier = supplier;
            EntityRelationship config = rules.getSupplierStockLocation(supplier, stockLocation);
            if (!TypeHelper.isA(config, SupplierArchetypes.SUPPLIER_STOCK_LOCATION_RELATIONSHIP_ESCI)) {
                throw new ESCIAdapterException(ESCIAdapterMessages.ESCINotConfigured(supplier, stockLocation));
            }

            IMObjectBean bean = factory.createBean(config);
            username = bean.getString("username");
            password = bean.getString("password");

            serviceURL = bean.getString("serviceURL");
            if (StringUtils.isEmpty(serviceURL)) {
                throw new ESCIAdapterException(ESCIAdapterMessages.invalidSupplierURL(supplier, serviceURL));
            }
        }

        /**
         * Constructs a <tt>SupplierServices</tt>.
         *
         * @param serviceURL the registry service URL.
         * @param username   the user name to connect as
         * @param password   the user's password
         */
        public SupplierServices(String serviceURL, String username, String password) {
            this.supplier = null;
            this.serviceURL = serviceURL;
            this.username = username;
            this.password = password;
        }

        /**
         * Returns the registry service proxy.
         *
         * @return the registry service proxy
         */
        public RegistryService getRegistryService() {
            return getRegistryService(null);
        }

        /**
         * Returns the registry service proxy.
         *
         * @param endpointAddress the endpoint address. May be <tt>null</tt>
         * @return the registry service proxy
         * @throws ESCIAdapterException for any error
         */
        public RegistryService getRegistryService(String endpointAddress) {
            return getService(RegistryService.class, serviceURL, endpointAddress);
        }

        /**
         * Returns the order service proxy.
         *
         * @return the order service proxy
         */
        public OrderService getOrderService() {
            return getOrderService(null, null);
        }

        /**
         * Returns the order service proxy.
         *
         * @param orderEndpointAddress    the order endpoint address. May be <tt>null</tt>
         * @param registryEndpointAddress the registry endpoint address. May be <tt>null</tt>
         * @return the order service proxy
         * @throws ESCIAdapterException for any error
         */
        public OrderService getOrderService(String orderEndpointAddress, String registryEndpointAddress) {
            RegistryService registry = getRegistryService(registryEndpointAddress);
            String order = null;
            try {
                order = registry.getOrderService();
            } catch (Throwable exception) {
                throwConnectionFailed(exception);
            }
            return getService(OrderService.class, order, orderEndpointAddress);
        }

        /**
         * Returns the inbox service proxy.
         *
         * @return the inbox service proxy
         * @throws ESCIAdapterException for any error
         */
        public InboxService getInboxService() {
            return getInboxService(null);
        }

        /**
         * Returns the inbox service proxy.
         *
         * @param endpointAddress the inbox service endpoint address. May be <tt>null</tt>
         * @return the inbox service proxy
         * @throws ESCIAdapterException for any error
         */
        public InboxService getInboxService(String endpointAddress) {
            RegistryService registry = getRegistryService();
            String inbox = null;
            try {
                inbox = registry.getInboxService();
            } catch (Throwable exception) {
                throwConnectionFailed(exception);
            }
            return getService(InboxService.class, inbox, endpointAddress);
        }

        /**
         * Reutrns a service proxy.
         *
         * @param clazz           the proxy class
         * @param url             the service URL
         * @param endpointAddress the endpoint address. May be <tt>null</tt>
         * @return the service proxy
         * @throws ESCIAdapterException for any error
         */
        private <T> T getService(Class<T> clazz, String url, String endpointAddress) {
            ServiceLocator<T> locator;
            try {
                locator = locatorFactory.getServiceLocator(clazz, url, endpointAddress, username, password);
            } catch (MalformedURLException exception) {
                if (supplier != null) {
                    throw new ESCIAdapterException(ESCIAdapterMessages.invalidSupplierURL(supplier, serviceURL),
                                                   exception);
                } else {
                    throw new ESCIAdapterException(ESCIAdapterMessages.invalidServiceURL(serviceURL), exception);
                }
            }
            T service = null;
            try {
                service = locator.getService();
            } catch (WebServiceException exception) {
                throwConnectionFailed(exception);
            }
            return service;
        }

        /**
         * Helper to throw an {@link ESCIAdapterException} for a connection failure.
         *
         * @param exception the cause
         * @throws ESCIAdapterException with appropriate message and the cause
         */
        private void throwConnectionFailed(Throwable exception) {
            if (supplier != null) {
                throw new ESCIAdapterException(ESCIAdapterMessages.connectionFailed(supplier, serviceURL),
                                               exception);
            } else {
                throw new ESCIAdapterException(ESCIAdapterMessages.connectionFailed(serviceURL), exception);
            }
        }

    }

}

