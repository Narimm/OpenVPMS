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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.esci.adapter.client;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.FutureValue;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.esci.adapter.client.impl.OrderServiceAdapterImpl;
import org.openvpms.esci.adapter.map.order.OrderMapper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.service.DelegatingOrderService;
import org.openvpms.esci.service.DelegatingRegistryService;
import org.openvpms.esci.service.OrderService;
import org.openvpms.esci.service.RegistryService;
import org.openvpms.esci.service.client.ServiceLocatorFactory;
import org.openvpms.esci.ubl.order.Order;
import org.openvpms.esci.ubl.order.OrderType;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * Tests the {@link OrderServiceAdapterImpl} class.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("/OrderWebServiceTest-context.xml")
public class OrderServiceAdapterTestCase extends AbstractESCITest {

    /**
     * The order mapper.
     */
    @Resource
    private OrderMapper mapper;

    /**
     * The bean factory.
     */
    @Resource
    private IMObjectBeanFactory factory;

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
     * The service locator factory.
     */
    @Resource
    private ServiceLocatorFactory serviceLocatorFactory;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        applicationContext.getBean("registryService"); // force registration of the registry dispatcher
        applicationContext.getBean("orderService");    // force registration of the order dispatcher

        // add a supplier/stock location relationship for ESCI
        String wsdl = getWSDL("wsdl/RegistryService.wsdl");
        addESCIConfiguration(getSupplier(), getStockLocation(), wsdl);

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
     * Verifies that the order service adapter can map an OpenVPMS <em>act.supplierOrder</em> to an UBL Order,
     * and submit it the the Order Service.
     *
     * @throws Exception for any error
     */
    @Test
    public void testOrderServiceAdapter() throws Exception {
        InVMSupplierServiceLocator vmLocator = createSupplierServiceLocator(0);
        OrderServiceAdapterImpl adapter = new OrderServiceAdapterImpl();
        adapter.setFactory(factory);
        adapter.setOrderMapper(mapper);
        adapter.setSupplierServiceLocator(vmLocator);

        final FutureValue<OrderType> future = new FutureValue<>();
        delegatingOrderService.setOrderService(new OrderService() {
            public void submitOrder(Order order) {
                future.set(order);
            }
        });

        FinancialAct order = createOrder();
        adapter.submitOrder(order);

        OrderType received = future.get(1000);
        assertNotNull(received);
    }

    /**
     * Verifies an {@link ESCIAdapterException} is thrown if a connection times out.
     */
    @Test
    public void testConnectionTimeout() {
        InVMSupplierServiceLocator vmLocator = createSupplierServiceLocator(1);
        OrderServiceAdapterImpl adapter = new OrderServiceAdapterImpl();
        adapter.setFactory(factory);
        adapter.setOrderMapper(mapper);
        adapter.setSupplierServiceLocator(vmLocator);

        delegatingOrderService.setOrderService(new OrderService() {
            public void submitOrder(Order order) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {
                    // do nothing
                }
            }
        });

        FinancialAct order = createOrder();
        try {
            adapter.submitOrder(order);
            fail("expected call to time out");
        } catch (ESCIAdapterException expected) {
            String wsdl = getWSDL("wsdl/RegistryService.wsdl");
            Party supplier = getSupplier();
            assertEquals("ESCIA-0007: Web service did not respond in time " + wsdl + " for supplier "
                         + supplier.getName() + " (" + supplier.getId() + ")", expected.getMessage());
        }
    }

    /**
     * Helper to create a new {@link InVMSupplierServiceLocator}.
     *
     * @param timeout the timeout for making calls to web services in seconds, or {@code 0} to not time out
     * @return a new {@code InVMSupplierServiceLocator}
     */
    private InVMSupplierServiceLocator createSupplierServiceLocator(int timeout) {
        InVMSupplierServiceLocator vmLocator = new InVMSupplierServiceLocator(timeout);
        vmLocator.setSupplierRules(new SupplierRules(getArchetypeService()));
        vmLocator.setBeanFactory(factory);
        vmLocator.setServiceLocatorFactory(serviceLocatorFactory);
        return vmLocator;
    }

}
