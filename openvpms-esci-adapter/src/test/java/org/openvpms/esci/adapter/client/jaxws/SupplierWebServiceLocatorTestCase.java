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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.esci.adapter.client.jaxws;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.esci.FutureValue;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.esci.adapter.client.InVMSupplierServiceLocator;
import org.openvpms.esci.adapter.client.SupplierServiceLocator;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.service.DelegatingOrderService;
import org.openvpms.esci.service.DelegatingRegistryService;
import org.openvpms.esci.service.OrderService;
import org.openvpms.esci.service.RegistryService;
import org.openvpms.esci.service.client.DefaultServiceLocatorFactory;
import org.openvpms.esci.service.exception.DuplicateOrderException;
import org.openvpms.esci.ubl.order.Order;
import org.openvpms.esci.ubl.order.OrderType;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * Tests the {@link SupplierWebServiceLocator} class.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("/OrderWebServiceTest-context.xml")
public class SupplierWebServiceLocatorTestCase extends AbstractESCITest {

    /**
     * The delegating registry service.
     */
    @Resource
    private DelegatingRegistryService delegatingRegistryService;

    /**
     * The delegating order service.
     */
    @Resource
    private DelegatingOrderService delegatingOrderService;


    /**
     * Verifies that the OrderService can be obtained with
     * {@link SupplierServiceLocator#getOrderService(Party, Party)} and its methods invoked.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetServiceByESCIConfiguration() throws Exception {
        FutureValue<OrderType> future = new FutureValue<>();
        registerOrderService(future);

        Party supplier = getSupplier();
        Party location = getStockLocation();
        String wsdl = getWSDL("wsdl/RegistryService.wsdl");
        addESCIConfiguration(supplier, location, wsdl);

        SupplierServiceLocator locator = createLocator(0);
        OrderService service = locator.getOrderService(supplier, location);
        service.submitOrder(new Order());

        OrderType received = future.get(1000);
        assertNotNull(received);
    }

    /**
     * Verifies that the OrderService can be obtained with
     * {@link SupplierServiceLocator#getOrderService(String, String, String)} and its methods invoked.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetServiceByURL() throws Exception {
        FutureValue<OrderType> future = new FutureValue<>();
        registerOrderService(future);

        SupplierServiceLocator locator = createLocator(0);
        String wsdl = getWSDL("wsdl/RegistryService.wsdl");
        OrderService service = locator.getOrderService(wsdl, "foo", "bar");
        service.submitOrder(new Order());

        OrderType received2 = future.get(1000);
        assertNotNull(received2);
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is thrown if there is no
     * <em>entityRelationship.supplierStockLocationESCI</em> between the supplier and stock location.
     */
    @Test
    public void testNotConfigured() {
        Party supplier = getSupplier();
        Party location = getStockLocation();
        try {
            SupplierServiceLocator locator = createLocator(0);
            locator.getOrderService(supplier, location);
            fail("Expected getOrderService() to fail");
        } catch (ESCIAdapterException exception) {
            assertEquals("ESCIA-0001: e-Supply Chain Interface support is not configured for " + supplier.getName()
                         + " (" + supplier.getId() + ") and " + location.getName() + " (" + location.getId() + ")",
                         exception.getMessage());
        }
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is thrown if the supplier order service URL is invalid.
     */
    @Test
    public void testInvalidOrderServiceURLForSupplier() {
        Party supplier = getSupplier();
        Party location = getStockLocation();
        addESCIConfiguration(supplier, location, "invalidURL");
        try {
            SupplierServiceLocator locator = createLocator(0);
            locator.getOrderService(supplier, location);
            fail("Expected getOrderService() to fail");
        } catch (ESCIAdapterException exception) {
            assertEquals("ESCIA-0002: invalidURL is not a valid service URL for supplier " + supplier.getName() + " ("
                         + supplier.getId() + ")", exception.getMessage());
        }
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is thrown if the supplier URL is invalid.
     */
    @Test
    public void testInvalidURL() {
        try {
            SupplierServiceLocator locator = createLocator(0);
            locator.getOrderService("invalidURL", "foo", "bar");
            fail("Expected getOrderService() to fail");
        } catch (ESCIAdapterException exception) {
            assertEquals("ESCIA-0003: invalidURL is not a valid service URL", exception.getMessage());
        }
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is thrown if a service cannot be contacted.
     */
    @Test
    public void testConnectionFailed() {
        try {
            SupplierServiceLocator locator = createLocator(0);
            locator.getOrderService("http://localhost:8888", "foo", "bar");
            fail("Expected getOrderService() to fail");
        } catch (ESCIAdapterException exception) {
            String message = "ESCIA-0004: Failed to connect to web service http://localhost:8888";
            assertEquals(message, exception.getMessage());
        }
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is thrown if a supplier service cannot be contacted.
     */
    @Test
    public void testConnectionFailedForSupplierService() {
        Party supplier = getSupplier();
        Party location = getStockLocation();
        addESCIConfiguration(supplier, location, "http://localhost:8888");
        try {
            SupplierServiceLocator locator = createLocator(0);
            locator.getOrderService(supplier, location);
            fail("Expected getOrderService() to fail");
        } catch (ESCIAdapterException exception) {
            String message = "ESCIA-0005: Failed to connect to web service http://localhost:8888 for supplier "
                             + supplier.getName() + " (" + supplier.getId() + ")";
            assertEquals(message, exception.getMessage());
        }
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is thrown if a call cannot be made in the required time..
     *
     * @throws Exception for any error
     */
    @Test
    public void testConnectionTimeout() throws Exception {
        delegatingOrderService.setOrderService(order -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignore) {
                // do nothing
            }
        });
        Party supplier = getSupplier();
        Party location = getStockLocation();
        String wsdl = getWSDL("wsdl/RegistryService.wsdl");
        addESCIConfiguration(supplier, location, wsdl);

        SupplierServiceLocator locator = createLocator(1);
        OrderService service = locator.getOrderService(supplier, location);
        try {
            service.submitOrder(new Order());
            fail("Expected submitOrder() to fail");
        } catch (ESCIAdapterException expected) {
            assertEquals("ESCIA-0007: Web service did not respond in time " + wsdl + " for supplier "
                         + supplier.getName() + " (" + supplier.getId() + ")", expected.getMessage());
        }
    }

    /**
     * Verifies that {@link DuplicateOrderException} are propagated back to the client.
     */
    @Test
    public void testDuplicateOrderException() {
        delegatingOrderService.setOrderService(order -> {
            throw new DuplicateOrderException("Duplicate order");
        });
        Party supplier = getSupplier();
        Party location = getStockLocation();
        String wsdl = getWSDL("wsdl/RegistryService.wsdl");
        addESCIConfiguration(supplier, location, wsdl);

        SupplierServiceLocator locator = createLocator(1);
        OrderService service = locator.getOrderService(supplier, location);
        try {
            service.submitOrder(new Order());
            fail("Expected submitOrder() to fail");
        } catch (DuplicateOrderException expected) {
            assertEquals("Duplicate order", expected.getMessage());
        }
    }

    /**
     * Verifies that server failures that trigger {@code SOAPFaultException} are rethrown on the client as
     * {@link ESCIAdapterException}.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSoapException() throws Exception {
        delegatingOrderService.setOrderService(order -> {
            throw new RuntimeException("Server failure");
        });
        Party supplier = getSupplier();
        Party location = getStockLocation();
        String wsdl = getWSDL("wsdl/RegistryService.wsdl");
        addESCIConfiguration(supplier, location, wsdl);

        SupplierServiceLocator locator = createLocator(1);
        OrderService service = locator.getOrderService(supplier, location);
        try {
            service.submitOrder(new Order());
            fail("Expected submitOrder() to fail");
        } catch (ESCIAdapterException expected) {
            assertEquals("ESCIA-0008: Web service error: Server failure", expected.getMessage());
        }
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        applicationContext.getBean("registryService"); // force registration of the registry dispatcher
        applicationContext.getBean("orderService");    // force registration of the order dispatcher
        delegatingRegistryService.setRegistry(new RegistryService() {
            public String getInboxService() {
                return getWSDL("wsdl/InboxService.wsdl");
            }

            public String getOrderService() {
                return getWSDL("wsdl/OrderService.wsdl");
            }
        });
    }

    /**
     * Creates a new supplier service locator.
     *
     * @param timeout the conn
     * @return a new supplier service locator
     */
    private SupplierWebServiceLocator createLocator(int timeout) {
        SupplierWebServiceLocator locator = new InVMSupplierServiceLocator(timeout); // to test method invocation
        locator.setArchetypeService(getArchetypeService());
        locator.setServiceLocatorFactory(new DefaultServiceLocatorFactory());
        locator.setSupplierRules(new SupplierRules(getArchetypeService()));
        return locator;
    }

    /**
     * Registers the order service.
     *
     * @param future the value that will be updated when an order is received
     */
    private void registerOrderService(final FutureValue<OrderType> future) {
        delegatingOrderService.setOrderService(future::set);
    }

}
