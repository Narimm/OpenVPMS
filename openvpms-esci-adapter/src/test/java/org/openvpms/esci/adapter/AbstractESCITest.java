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

import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.supplier.AbstractSupplierTest;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;


/**
 * Base class for ESCI test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractESCITest extends AbstractSupplierTest {

    /**
     * Creates a new <em>user.esci</em>, linked to a supplier.
     *
     * @param supplier the supplier
     * @return a new user
     */
    protected User createESCIUser(Party supplier) {
        User user = createESCIUser();
        EntityBean userBean = new EntityBean(user);
        userBean.addNodeRelationship("supplier", supplier);
        save(user, supplier);
        return user;
    }

    /**
     * Creates a new <em>user.esci</em>.
     *
     * @return a new user
     */
    protected User createESCIUser() {
        User user = (User) create(UserArchetypes.ESCI_USER);
        EntityBean userBean = new EntityBean(user);
        userBean.setValue("username", "z" + System.currentTimeMillis());
        userBean.setValue("name", "foo");
        userBean.setValue("password", "bar");
        save(user);
        return user;
    }

    /**
     * Helper to create a POSTED order with a single item.
     *
     * @return a new order
     */
    protected FinancialAct createOrder() {
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        FinancialAct order = createOrder(orderItem);
        order.setStatus(ActStatus.POSTED);
        save(order, orderItem);
        return order;
    }

    /**
     * Helper to create a <tt>SupplierPartyType</tt>.
     *
     * @param supplier the supplier
     * @return a new <tt>SupplierPartyType</tt>
     */
    protected SupplierPartyType createSupplier(Party supplier) {
        SupplierPartyType supplierType = new SupplierPartyType();
        CustomerAssignedAccountIDType supplierId = UBLHelper.initID(new CustomerAssignedAccountIDType(),
                                                                    supplier.getId());
        supplierType.setCustomerAssignedAccountID(supplierId);
        return supplierType;
    }

    /**
     * Helper to return the URL of a WSDL file, given its resource path.
     *
     * @param resourcePath the path to the WSDL resource
     * @return the URL of the WSDL resource
     * @throws java.io.IOException if the URL is invalid
     */
    protected String getWSDL(String resourcePath) throws IOException {
        ClassPathResource wsdl = new ClassPathResource(resourcePath);
        return wsdl.getURL().toString();
    }

    /**
     * Adds a product/supplier relationship.
     *
     * @param product            the product
     * @param supplier           the supplier
     * @param reorderCode        the reorder code
     * @param reorderDescription the reorder description
     * @return the product/supplier relationship
     */
    protected ProductSupplier addProductSupplierRelationship(Product product, Party supplier, String reorderCode,
                                                             String reorderDescription) {
        ProductRules productRules = new ProductRules();
        ProductSupplier productSupplier = productRules.createProductSupplier(product, supplier);
        productSupplier.setReorderCode(reorderCode);
        productSupplier.setReorderDescription(reorderDescription);
        save(product, supplier);
        return productSupplier;
    }

    /**
     * Adds an <em>entityRelationship.supplierStockLocationESCI</em> relationship between the supplier and stock
     * location.
     *
     * @param supplier the supplier
     * @param location the stock location
     * @param url      the order service URL
     */
    protected void addESCIConfiguration(Party supplier, Party location, String url) {
        EntityBean bean = new EntityBean(supplier);
        EntityRelationship relationship =
                bean.addRelationship(SupplierArchetypes.SUPPLIER_STOCK_LOCATION_RELATIONSHIP_ESCI, location);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("orderServiceURL", url);
        save(supplier, location);
    }

}
