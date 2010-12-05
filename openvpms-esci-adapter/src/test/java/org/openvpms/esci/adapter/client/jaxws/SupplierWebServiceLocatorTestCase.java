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

import static org.junit.Assert.*;
import org.junit.Test;
import org.oasis.ubl.OrderType;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.DelegatingOrderService;
import org.openvpms.esci.FutureValue;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.esci.adapter.client.InVMSupplierServiceLocator;
import org.openvpms.esci.adapter.client.SupplierServiceLocator;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.service.OrderService;
import org.openvpms.esci.service.client.DefaultServiceLocatorFactory;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;


/**
 * Tests the {@link SupplierWebServiceLocator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("/OrderWebServiceTest-context.xml")
public class SupplierWebServiceLocatorTestCase extends AbstractESCITest {

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
        FutureValue<OrderType> future = new FutureValue<OrderType>();
        registerOrderService(future);

        Party supplier = getSupplier();
        Party location = getStockLocation();
        String wsdl = getWSDL("wsdl/OrderService.wsdl");
        addESCIConfiguration(supplier, location, wsdl);

        SupplierServiceLocator locator = createLocator();
        OrderService service = locator.getOrderService(supplier, location);
        service.submitOrder(new OrderType());

        OrderType received = future.get(1000);
        assertNotNull(received);
    }

    /**
     * Verifies that the OrderService can be obtained with
     * {@link SupplierServiceLocator#getOrderService(String, String, String, String)} and its methods invoked.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetServiceByURL() throws Exception {
        FutureValue<OrderType> future = new FutureValue<OrderType>();
        registerOrderService(future);

        SupplierServiceLocator locator = createLocator();
        String wsdl = getWSDL("wsdl/OrderService.wsdl");
        OrderService service = locator.getOrderService(wsdl, "in-vm://orderService/", "foo", "bar");
        service.submitOrder(new OrderType());

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
            SupplierServiceLocator locator = createLocator();
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
            SupplierServiceLocator locator = createLocator();
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
            SupplierServiceLocator locator = createLocator();
            locator.getOrderService("invalidURL", "foo", "bar");
            fail("Expected getOrderService() to fail");
        } catch (ESCIAdapterException exception) {
            assertEquals("ESCIA-0003: invalidURL is not a valid service URL", exception.getMessage());
        }
    }

    /**
     * Creates a new supplier service locator.
     *
     * @return a new supplier service locator
     */
    private SupplierWebServiceLocator createLocator() {
        SupplierWebServiceLocator locator = new InVMSupplierServiceLocator(); // to test method invocation
        locator.setBeanFactory(new IMObjectBeanFactory(getArchetypeService()));
        locator.setServiceLocatorFactory(new DefaultServiceLocatorFactory());
        locator.setSupplierRules(new SupplierRules());
        return locator;
    }

    /**
     * Registers the order service.
     *
     * @param future the value that will be updated when an order is received
     */
    private void registerOrderService(final FutureValue<OrderType> future) {
        applicationContext.getBean("orderService"); // force registation of the order service

        delegatingOrderService.setOrderService(new OrderService() {
            public void submitOrder(OrderType order) {
                future.set(order);
            }
        });
    }

}