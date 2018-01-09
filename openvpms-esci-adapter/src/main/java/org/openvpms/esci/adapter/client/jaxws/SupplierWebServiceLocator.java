/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.component.i18n.Message;
import org.openvpms.esci.adapter.client.SupplierServiceLocator;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.service.InboxService;
import org.openvpms.esci.service.OrderService;
import org.openvpms.esci.service.RegistryService;
import org.openvpms.esci.service.client.ServiceLocator;
import org.openvpms.esci.service.client.ServiceLocatorFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Returns proxies for supplier web services.
 * <p/>
 * This supports timing out calls that take to long.
 *
 * @author Tim Anderson
 */
public class SupplierWebServiceLocator implements SupplierServiceLocator, DisposableBean {

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
     * The executor service.
     */
    private ExecutorService executor;

    /**
     * The timeout for calls, in seconds or {@code 0} if calls shouldn't time out.
     */
    private final int timeout;

    /**
     * Constructs a {@link SupplierWebServiceLocator}.
     * <p/>
     * Calls will time out after 30 seconds.
     */
    public SupplierWebServiceLocator() {
        this(30);
    }

    /**
     * Constructs a {@link SupplierWebServiceLocator}.
     *
     * @param timeout the timeout for making calls to web services in seconds, or {@code 0} to not time out
     */
    public SupplierWebServiceLocator(int timeout) {
        this.timeout = timeout;
        executor = Executors.newCachedThreadPool(new CustomizableThreadFactory("ESCI-"));
    }

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
     * @throws ESCIAdapterException if the associated {@code serviceURL} is invalid, or the supplier-stock
     *                              location relationship is not supported, or the service proxy can't be created
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
     * @throws ESCIAdapterException if the associated {@code serviceURL} is invalid or the proxy cannot be created
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
     * @throws ESCIAdapterException if the associated {@code serviceURL} is invalid, the supplier-stock
     *                              location relationship is not supported, or the proxy cannot be created
     */
    public InboxService getInboxService(Party supplier, Party stockLocation) {
        SupplierServices services = new SupplierServices(supplier, stockLocation);
        return services.getInboxService();
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors
     */
    @Override
    public void destroy() throws Exception {
        executor.shutdown();
    }

    /**
     * Helper class to access supplier services.
     */
    protected class SupplierServices {

        /**
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
         * Constructs a {@code SupplierServices} from an <em>entityRelationship.supplierStockLocationESCI</em>
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
         * Constructs a {@link SupplierServices}.
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
         * @param endpointAddress the endpoint address. May be {@code null}
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
         * @param orderEndpointAddress    the order endpoint address. May be {@code null}
         * @param registryEndpointAddress the registry endpoint address. May be {@code null}
         * @return the order service proxy
         * @throws ESCIAdapterException for any error
         */
        public OrderService getOrderService(String orderEndpointAddress, String registryEndpointAddress) {
            RegistryService registry = getRegistryService(registryEndpointAddress);
            String order;
            try {
                order = registry.getOrderService();
            } catch (ESCIAdapterException exception) {
                throw exception;
            } catch (Throwable exception) {
                throw new ESCIAdapterException(getConnectionFailed(), exception);
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
         * @param endpointAddress the inbox service endpoint address. May be {@code null}
         * @return the inbox service proxy
         * @throws ESCIAdapterException for any error
         */
        public InboxService getInboxService(String endpointAddress) {
            RegistryService registry = getRegistryService();
            String inbox;
            try {
                inbox = registry.getInboxService();
            } catch (ESCIAdapterException exception) {
                throw exception;
            } catch (Throwable exception) {
                throw new ESCIAdapterException(getConnectionFailed(), exception);
            }
            return getService(InboxService.class, inbox, endpointAddress);
        }

        /**
         * Returns a service proxy.
         *
         * @param clazz           the proxy class
         * @param url             the service URL
         * @param endpointAddress the endpoint address. May be {@code null}
         * @return the service proxy
         * @throws ESCIAdapterException for any error
         */
        private <T> T getService(final Class<T> clazz, final String url, final String endpointAddress) {
            Callable<T> callable = new Callable<T>() {
                @Override
                public T call() throws Exception {
                    ServiceLocator<T> locator;
                    try {
                        locator = locatorFactory.getServiceLocator(clazz, url, endpointAddress, username, password);
                    } catch (MalformedURLException exception) {
                        Message message = (supplier != null) ?
                                          ESCIAdapterMessages.invalidSupplierURL(supplier, serviceURL)
                                                             : ESCIAdapterMessages.invalidServiceURL(serviceURL);
                        throw new ESCIAdapterException(message, exception);
                    }
                    try {
                        return locator.getService();
                    } catch (WebServiceException exception) {
                        throw new ESCIAdapterException(getConnectionFailed(), exception);
                    }
                }
            };
            T service;
            if (timeout > 0) {
                service = invokeWithTimeout(callable);
                service = proxy(service, clazz);
            } else {
                try {
                    service = callable.call();
                } catch (ESCIAdapterException exception) {
                    throw exception;
                } catch (Exception exception) {
                    throw new ESCIAdapterException(getConnectionFailed(), exception);
                }
            }
            return service;
        }

        /**
         * Proxies a web-service interface, forcing invocations to time out if they don't complete in time.
         *
         * @param service the service
         * @param type    the interface to proxy
         * @return the service proxy
         */
        @SuppressWarnings("unchecked")
        protected <T> T proxy(final T service, Class<T> type) {
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    Callable<Object> callable = new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            return method.invoke(service, args);
                        }
                    };
                    return invokeWithTimeout(callable);
                }
            };
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
        }

        /**
         * Makes a web-service call, timing out if it doesn't complete in time.
         *
         * @param callable the call to make
         * @return the result of the call
         * @throws ESCIAdapterException for any error, including connection timeout
         */
        private <T> T invokeWithTimeout(Callable<T> callable) {
            T result;

            try {
                result = executor.invokeAny(Collections.singletonList(callable), timeout, TimeUnit.SECONDS);
            } catch (ESCIAdapterException exception) {
                throw exception;
            } catch (Exception exception) {
                if (supplier != null) {
                    throw new ESCIAdapterException(ESCIAdapterMessages.connectionTimedOut(supplier, serviceURL),
                                                   exception);
                } else {
                    throw new ESCIAdapterException(ESCIAdapterMessages.connectionTimedOut(serviceURL), exception);
                }
            }
            return result;
        }

        /**
         * Helper to return a message for a connection failure.
         * @return a new message
         */
        private Message getConnectionFailed() {
            return (supplier != null) ? ESCIAdapterMessages.connectionFailed(supplier, serviceURL)
                                      : ESCIAdapterMessages.connectionFailed(serviceURL);
        }

    }

}

