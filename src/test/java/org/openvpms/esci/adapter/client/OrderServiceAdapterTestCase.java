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

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.oasis.ubl.OrderType;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.FutureValue;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.esci.adapter.client.impl.OrderServiceAdapterImpl;
import org.openvpms.esci.adapter.map.order.OrderMapper;
import org.openvpms.esci.service.DelegatingOrderService;
import org.openvpms.esci.service.DelegatingRegistryService;
import org.openvpms.esci.service.OrderService;
import org.openvpms.esci.service.RegistryService;
import org.openvpms.esci.service.client.ServiceLocatorFactory;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;


/**
 * Tests the {@link OrderServiceAdapterImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Verifies that the order service adapter can map an OpenVPMS <em>act.supplierOrder</em> to an UBL Order,
     * and submit it the the Order Service.
     *
     * @throws Exception for any error
     */
    @Test
    public void testOrderServiceAdapter() throws Exception {
        applicationContext.getBean("registryService"); // force registation of the registry dispatcher
        applicationContext.getBean("orderService");    // force registation of the order dispatcher

        // add a supplier/stock location relationship for ESCI
        String wsdl = getWSDL("wsdl/RegistryService.wsdl");
        addESCIConfiguration(getSupplier(), getStockLocation(), wsdl);

        InVMSupplierServiceLocator vmLocator = createSupplierServiceLocator();
        OrderServiceAdapterImpl adapter = new OrderServiceAdapterImpl();
        adapter.setFactory(factory);
        adapter.setOrderMapper(mapper);
        adapter.setSupplierServiceLocator(vmLocator);

        delegatingRegistryService.setRegistry(new RegistryService() {
            public String getInboxService() {
                return getWSDL("wsdl/InboxService.wsdl");
            }

            public String getOrderService() {
                return getWSDL("wsdl/OrderService.wsdl");
            }
        });

        final FutureValue<OrderType> future = new FutureValue<OrderType>();
        delegatingOrderService.setOrderService(new OrderService() {
            public void submitOrder(OrderType order) {
                future.set(order);
            }
        });

        FinancialAct order = createOrder();
        adapter.submitOrder(order);

        OrderType received = future.get(1000);
        assertNotNull(received);
    }

    /**
     * Helper to create a new <tt>InVMSupplierServiceLocator</tt>.
     *
     * @return a new <tt>InVMSupplierServiceLocator</tt>
     */
    private InVMSupplierServiceLocator createSupplierServiceLocator() {
        InVMSupplierServiceLocator vmLocator = new InVMSupplierServiceLocator();
        vmLocator.setSupplierRules(new SupplierRules());
        vmLocator.setBeanFactory(factory);
        vmLocator.setServiceLocatorFactory(serviceLocatorFactory);
        return vmLocator;
    }

}
