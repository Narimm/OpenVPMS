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

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.AbstractSupplierTest;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.ubl.common.aggregate.CustomerPartyType;
import org.openvpms.esci.ubl.common.aggregate.SupplierPartyType;
import org.openvpms.esci.ubl.common.basic.CustomerAssignedAccountIDType;
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
     * Helper to create a POSTED order with a single item.
     *
     * @return a new order
     */
    protected FinancialAct createOrder() {
        return createOrder(getSupplier());
    }

    /**
     * Helper to create a POSTED order with a single item.
     *
     * @param supplier the supplier
     * @return a new order
     */
    protected FinancialAct createOrder(Party supplier) {
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        ActBean itemBean = new ActBean(orderItem);
        itemBean.setValue("reorderCode", "AREORDERCODE");
        itemBean.setValue("reorderDescription", "A reorder description");
        FinancialAct order = createOrder(supplier, orderItem);
        order.setStatus(ActStatus.POSTED);
        save(order, orderItem);
        return order;
    }

    /**
     * Helper to create a <tt>CustomerPartyType</tt>.
     *
     * @param stockLocation the stock location
     * @return a new <tt>CustomerPartyType</tt>
     */
    protected CustomerPartyType createCustomer(Party stockLocation) {
        CustomerPartyType result = new CustomerPartyType();
        result.setCustomerAssignedAccountID(UBLHelper.initID(new CustomerAssignedAccountIDType(),
                                                             stockLocation.getId()));
        return result;
    }

    /**
     * Helper to create a <tt>SupplierPartyType</tt>.
     *
     * @param supplier the supplier
     * @return a new <tt>SupplierPartyType</tt>
     */
    protected SupplierPartyType createSupplier(Party supplier) {
        SupplierPartyType result = new SupplierPartyType();
        result.setCustomerAssignedAccountID(UBLHelper.initID(new CustomerAssignedAccountIDType(), supplier.getId()));
        return result;
    }

    /**
     * Helper to return the URL of a WSDL file, given its resource path.
     *
     * @param resourcePath the path to the WSDL resource
     * @return the URL of the WSDL resource
     * @throws RuntimeException if the URL is invalid
     */
    protected String getWSDL(String resourcePath) {
        ClassPathResource wsdl = new ClassPathResource(resourcePath);
        try {
            return wsdl.getURL().toString();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
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
        relBean.setValue("serviceURL", url);
        save(supplier, location);
    }

    /**
     * Verifies that an <em>act.systemMessage</em> has been created when an invoice is received.
     *
     * @param author   the author associated with the message
     * @param delivery the delivery associated with the message
     * @param reason   the expected reason
     */
    protected void checkSystemMessage(User author, FinancialAct delivery, String reason) {
        ArchetypeQuery query = new ArchetypeQuery(Constraints.shortName("act.systemMessage"));
        query.add(Constraints.join("to").add(Constraints.eq("entity", author.getObjectReference())));
        query.add(Constraints.join("item").add(Constraints.eq("target", delivery.getObjectReference())));
        IPage<IMObject> page = getArchetypeService().get(query);
        org.junit.Assert.assertEquals(1, page.getResults().size());
        Act message = (Act) page.getResults().get(0);
        org.junit.Assert.assertEquals(reason, message.getReason());
    }

    /**
     * Creates and associates an user with the stock location as the default author of invoices.
     *
     * @return the user
     */
    protected User initDefaultAuthor() {
        User author = TestHelper.createUser();
        EntityBean locBean = new EntityBean(getStockLocation());
        locBean.addNodeRelationship("defaultAuthor", author);
        locBean.save();
        return author;
    }
}
