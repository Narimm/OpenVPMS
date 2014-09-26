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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.esci.adapter.map.invoice;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.common.CurrencyCodeContentType;
import org.openvpms.esci.ubl.common.aggregate.AllowanceChargeType;
import org.openvpms.esci.ubl.common.aggregate.CustomerPartyType;
import org.openvpms.esci.ubl.common.aggregate.InvoiceLineType;
import org.openvpms.esci.ubl.common.aggregate.ItemType;
import org.openvpms.esci.ubl.common.aggregate.MonetaryTotalType;
import org.openvpms.esci.ubl.common.aggregate.OrderLineReferenceType;
import org.openvpms.esci.ubl.common.aggregate.OrderReferenceType;
import org.openvpms.esci.ubl.common.aggregate.PriceType;
import org.openvpms.esci.ubl.common.aggregate.SupplierPartyType;
import org.openvpms.esci.ubl.common.aggregate.TaxCategoryType;
import org.openvpms.esci.ubl.common.aggregate.TaxSchemeType;
import org.openvpms.esci.ubl.common.aggregate.TaxSubtotalType;
import org.openvpms.esci.ubl.common.aggregate.TaxTotalType;
import org.openvpms.esci.ubl.common.basic.ChargeIndicatorType;
import org.openvpms.esci.ubl.common.basic.LineIDType;
import org.openvpms.esci.ubl.common.basic.NoteType;
import org.openvpms.esci.ubl.common.basic.PackQuantityType;
import org.openvpms.esci.ubl.common.basic.UBLVersionIDType;
import org.openvpms.esci.ubl.invoice.Invoice;
import org.openvpms.esci.ubl.io.UBLDocumentContext;
import org.openvpms.esci.ubl.io.UBLDocumentReader;
import org.openvpms.esci.ubl.io.UBLDocumentWriter;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests the {@link InvoiceMapperImpl} class.
 *
 * @author Tim Anderson
 */
public class InvoiceMapperTestCase extends AbstractInvoiceTest {

    /**
     * 'Each' unit of measure code.
     */
    private static final String EACH_UNITS = "EACH";

    /**
     * 'Each' unit code.
     */
    private static final String EACH_UNIT_CODE = "EA";

    /**
     * 'Case' unit of measure code.
     */
    private static final String CASE_UNITS = "CASE";

    /**
     * 'Case' unit code.
     */
    private static final String CASE_UNIT_CODE = "CS";

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Lookup each = TestHelper.getLookup("lookup.uom", EACH_UNITS);
        IMObjectBean eachBean = new IMObjectBean(each);
        eachBean.setValue("unitCode", EACH_UNIT_CODE);
        eachBean.save();

        Lookup caseUnits = TestHelper.getLookup("lookup.uom", CASE_UNITS);
        IMObjectBean caseBean = new IMObjectBean(caseUnits);
        caseBean.setValue("unitCode", CASE_UNIT_CODE);
        caseBean.save();
    }

    /**
     * Tests simple mapping.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMap() throws Exception {
        Product product1 = TestHelper.createProduct();
        BigDecimal price1 = new BigDecimal(100);
        BigDecimal listPrice1 = new BigDecimal(105);
        BigDecimal quantity1 = BigDecimal.ONE;
        BigDecimal lineExtension1 = price1.multiply(quantity1);
        BigDecimal tax1 = lineExtension1.multiply(new BigDecimal("0.10"));
        BigDecimal total1 = lineExtension1.add(tax1);
        String reorder1 = "product1";
        String reorderDesc1 = "product1 name";

        Product product2 = TestHelper.createProduct();
        BigDecimal price2 = new BigDecimal(40);
        BigDecimal listPrice2 = new BigDecimal(41);
        BigDecimal quantity2 = BigDecimal.ONE;
        BigDecimal lineExtension2 = price2.multiply(quantity2);
        BigDecimal tax2 = lineExtension2.multiply(new BigDecimal("0.10"));
        BigDecimal total2 = lineExtension2.add(tax2);
        String reorder2 = "product2";
        String reorderDesc2 = "product2 name";

        BigDecimal chargeAmount = new BigDecimal("10");
        BigDecimal chargeTax = new BigDecimal("1");
        String chargeReason = "Freight";

        BigDecimal lineExtensionTotal = lineExtension1.add(lineExtension2);
        BigDecimal taxExAmount = lineExtensionTotal.add(chargeAmount);
        BigDecimal totalTax = tax1.add(tax2).add(chargeTax);
        BigDecimal payableAmount = taxExAmount.add(totalTax);

        Invoice invoice = new Invoice();
        SupplierPartyType supplierType = createSupplier(getSupplier());
        CustomerPartyType customerType = createCustomer(getStockLocation());
        MonetaryTotalType monetaryTotal = createMonetaryTotal(lineExtensionTotal, chargeAmount, taxExAmount,
                                                              payableAmount);

        invoice.setUBLVersionID(UBLHelper.initID(new UBLVersionIDType(), "2.0"));
        invoice.setID(UBLHelper.createID(12345));
        Date issueDatetime = new Date();
        invoice.setIssueDate(createIssueDate(issueDatetime));
        invoice.setIssueTime(createIssueTime(issueDatetime));
        invoice.getNote().add(UBLHelper.initText(new NoteType(), "a note"));
        invoice.getNote().add(UBLHelper.initText(new NoteType(), "another note"));
        invoice.setAccountingSupplierParty(supplierType);
        invoice.setAccountingCustomerParty(customerType);
        invoice.setLegalMonetaryTotal(monetaryTotal);
        invoice.getTaxTotal().add(createTaxTotal(totalTax, false));
        InvoiceLineType item1 = createInvoiceLine("1", product1, reorder1, reorderDesc1, listPrice1, price1, quantity1,
                                                  PACKAGE_UNIT_CODE, lineExtension1, tax1);
        InvoiceLineType item2 = createInvoiceLine("2", product2, reorder2, reorderDesc2, listPrice2, price2, quantity2,
                                                  PACKAGE_UNIT_CODE, lineExtension2, tax2);
        invoice.getInvoiceLine().add(item1);
        invoice.getInvoiceLine().add(item2);

        invoice.getAllowanceCharge().add(createCharge(chargeAmount, chargeTax, chargeReason, new BigDecimal("10.00")));

        invoice = serialize(invoice);
        InvoiceMapper mapper = createMapper();
        Delivery mapped = mapper.map(invoice, getSupplier(), getStockLocation(), null);
        List<FinancialAct> acts = mapped.getActs();
        assertEquals(4, acts.size());
        getArchetypeService().save(acts);

        checkDelivery(mapped.getDelivery(), "12345", issueDatetime, "a note\nanother note", payableAmount, totalTax);
        checkDeliveryItem(mapped.getDeliveryItems().get(0), "1", issueDatetime, product1, quantity1, listPrice1, price1,
                          "BOX", reorder1, reorderDesc1, total1, tax1);
        checkDeliveryItem(mapped.getDeliveryItems().get(1), "2", issueDatetime, product2, quantity2, listPrice2, price2,
                          "BOX", reorder2, reorderDesc2, total2, tax2);
        checkDeliveryItem(mapped.getDeliveryItems().get(2), null, issueDatetime, null, BigDecimal.ONE, BigDecimal.ZERO,
                          chargeAmount, null, null, chargeReason, chargeAmount.add(chargeTax), chargeTax);
    }

    /**
     * Verifies that the order references a correctly determined when mapping an invoice associated with an order.
     */
    @Test
    public void testMapWithOrder() {
        // create an order
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        FinancialAct order = createOrder(orderItem);
        save(order, orderItem);

        // create an invoice that references the order
        Invoice invoice = createInvoice(order, orderItem, PACKAGE_UNIT_CODE);

        // map the invoice to a delivery
        Delivery delivery = map(invoice, order);
        assertEquals(1, delivery.getDeliveryItems().size());
        FinancialAct deliveryItem = delivery.getDeliveryItems().get(0);

        // verify there is a relationship between the delivery and the order, and the delivery item and the order item
        checkDeliveryOrderRelationship(delivery.getDelivery(), order);
        checkDeliveryOrderItemRelationship(deliveryItem, orderItem);
    }

    /**
     * Verifies that the package units and size in the delivery is populated from the original order.
     */
    @Test
    public void testPackageInfoDefaultsFromOrder() {
        // set up default product/supplier package units - these should not be used
        addProductSupplierRelationship(getProduct(), getSupplier(), 3, PACKAGE_UNITS);

        // create an order
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        ActBean bean = new ActBean(orderItem);
        int packageSize = 25;
        bean.setValue("packageSize", packageSize);
        bean.setValue("packageUnits", EACH_UNITS);
        FinancialAct order = createOrder(orderItem);
        save(order, orderItem);

        // create an invoice and set the package units - these also should not be used
        Invoice invoice = createInvoice(order, orderItem, CASE_UNIT_CODE);
        ItemType item = invoice.getInvoiceLine().get(0).getItem();
        item.setPackQuantity(UBLHelper.initQuantity(new PackQuantityType(), new BigDecimal("1"), CASE_UNIT_CODE));
        item.setPackSizeNumeric(UBLHelper.createPackSizeNumeric(new BigDecimal(4)));

        // check that the package size and units in the mapped delivery come from the order
        checkMapPackageUnits(packageSize, EACH_UNITS, invoice, order, orderItem);
    }

    /**
     * Verifies that the package units and size in the delivery is populated from the product/supplier relationship,
     * if they are not specified in the original order, nor the invoice.
     */
    @Test
    public void testPackageInfoDefaultsFromProductSupplier() {
        int packageSize = 48;

        // set up a product/supplier relationship for the package info that should appear in the delivery
        addProductSupplierRelationship(getProduct(), getSupplier(), packageSize, EACH_UNITS);

        // create an order
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        FinancialAct order = createOrder(orderItem);
        ActBean bean = new ActBean(orderItem);
        bean.setValue("packageSize", 0);   // package size and units not populated
        bean.setValue("packageUnits", null);
        save(order, orderItem);

        // create an invoice and set the package units - these also should not be used
        Invoice invoice = createInvoice(order, orderItem, CASE_UNIT_CODE);
        ItemType item = invoice.getInvoiceLine().get(0).getItem();
        item.setPackQuantity(UBLHelper.initQuantity(new PackQuantityType(), new BigDecimal("1"), CASE_UNIT_CODE));
        item.setPackSizeNumeric(UBLHelper.createPackSizeNumeric(new BigDecimal(4)));

        // check that the package size and units in the mapped delivery come from the product/supplier relationship
        checkMapPackageUnits(packageSize, EACH_UNITS, invoice, order, orderItem);
    }

    /**
     * Verifies that the package units and size are populated from the invoice, if they were not specified in the
     * order, and no product/supplier relationship exists.
     */
    @Test
    public void testPackageInfoDefaultsFromInvoice() {
        // create an order
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        FinancialAct order = createOrder(orderItem);
        ActBean bean = new ActBean(orderItem);
        bean.setValue("packageSize", 0);   // package size and units not populated
        bean.setValue("packageUnits", null);
        save(order, orderItem);

        // create an invoice and set the expected package units
        Invoice invoice = createInvoice(order, orderItem, PACKAGE_UNIT_CODE);
        ItemType item = invoice.getInvoiceLine().get(0).getItem();
        item.setPackQuantity(UBLHelper.initQuantity(new PackQuantityType(), new BigDecimal("1"), PACKAGE_UNIT_CODE));
        item.setPackSizeNumeric(UBLHelper.createPackSizeNumeric(new BigDecimal(4)));

        // check that the package size and units in the mapped delivery come from the invoice
        checkMapPackageUnits(4, PACKAGE_UNITS, invoice, order, orderItem);
    }

    /**
     * Verifies that the author node of <em>act.supplierDelivery is populated correctly.
     */
    @Test
    public void testAuthor() {
        InvoiceMapper mapper = createMapper();

        // create an invoice that doesn't reference an order.
        // No author should appear on the resulting delivery
        Invoice invoice1 = createInvoice();
        Delivery delivery1 = mapper.map(invoice1, getSupplier(), getStockLocation(), null);
        ActBean bean1 = new ActBean(delivery1.getDelivery());
        assertNull(bean1.getNodeParticipant("author"));

        // create an invoice that doesn't reference an order, but add a defaultAuthor to the stock location.
        // This should appear on the resulting delivery
        User defaultAuthor = TestHelper.createUser();
        EntityBean locBean = new EntityBean(getStockLocation());
        locBean.addNodeRelationship("defaultAuthor", defaultAuthor);
        locBean.save();

        Invoice invoice2 = createInvoice();
        Delivery delivery2 = mapper.map(invoice2, getSupplier(), getStockLocation(), null);
        ActBean deliveryBean2 = new ActBean(delivery2.getDelivery());
        assertEquals(defaultAuthor, deliveryBean2.getNodeParticipant("author"));

        // create an invoice that references an order, with an author.
        // This should appear on the resulting delivery
        FinancialAct order3 = createOrder();
        ActBean orderBean = new ActBean(order3);
        User author = TestHelper.createUser();
        orderBean.addNodeParticipation("author", author);
        orderBean.save();

        Invoice invoice3 = createInvoice();
        invoice3.setOrderReference(UBLHelper.createOrderReference(order3.getId()));
        Delivery delivery3 = mapper.map(invoice3, getSupplier(), getStockLocation(), null);
        ActBean deliveryBean3 = new ActBean(delivery3.getDelivery());
        assertEquals(author, deliveryBean3.getNodeParticipant("author"));

        // create an invoice that references an order, with no author.
        // The stock location default author should be used
        FinancialAct order4 = createOrder();
        Invoice invoice4 = createInvoice();
        invoice4.setOrderReference(UBLHelper.createOrderReference(order4.getId()));
        Delivery delivery4 = mapper.map(invoice4, getSupplier(), getStockLocation(), null);
        ActBean deliveryBean4 = new ActBean(delivery4.getDelivery());
        assertEquals(defaultAuthor, deliveryBean4.getNodeParticipant("author"));
    }

    /**
     * Verifies that multiple invoices can be generated for the one order.
     */
    @Test
    public void testOrderWithMultipleInvoices() {
        InvoiceMapper mapper = createMapper();
        Product product = getProduct();

        // create an order with two items
        FinancialAct item1 = createOrderItem(product, BigDecimal.ONE, 1, BigDecimal.ONE, "PRODUCTA");
        FinancialAct item2 = createOrderItem(product, BigDecimal.ONE, 1, BigDecimal.ONE, "PRODUCTA");
        FinancialAct order = createOrder(item1, item2);

        // create an invoice that references the order
        InvoiceLineType line1 = createInvoiceLine("1", item1);
        line1.getOrderLineReference().add(createOrderLineReference(item1));

        Invoice invoice1 = createInvoice(getSupplier(), getStockLocation(), line1);
        invoice1.getID().setValue("1");
        invoice1.setOrderReference(UBLHelper.createOrderReference(order.getId()));

        // map the invoice to a delivery
        Delivery delivery1 = mapper.map(invoice1, getSupplier(), getStockLocation(), null);
        assertEquals(order, delivery1.getOrder());
        save(delivery1.getActs());
        assertEquals(1, delivery1.getDeliveryItems().size());
        FinancialAct deliveryItem1 = delivery1.getDeliveryItems().get(0);
        ActBean itemBean1 = new ActBean(deliveryItem1);

        // verify there is a relationship between the delivery the order, and the delivery item and the order item
        checkDeliveryOrderRelationship(delivery1.getDelivery(), order);
        checkDeliveryOrderItemRelationship(deliveryItem1, item1);
        assertTrue(itemBean1.hasRelationship(SupplierArchetypes.DELIVERY_ORDER_ITEM_RELATIONSHIP, item1));

        // create another invoice that references item2
        InvoiceLineType line2 = createInvoiceLine("2", item2);
        line2.getOrderLineReference().add(createOrderLineReference(item2));

        Invoice invoice2 = createInvoice(getSupplier(), getStockLocation(), line2);
        invoice2.getID().setValue("2");
        invoice2.setOrderReference(createOrderReference(order));

        // map the invoice to a delivery
        Delivery delivery2 = mapper.map(invoice2, getSupplier(), getStockLocation(), null);
        assertEquals(order, delivery2.getOrder());
        save(delivery2.getActs());
        assertEquals(1, delivery2.getDeliveryItems().size());
        FinancialAct deliveryItem2 = delivery2.getDeliveryItems().get(0);

        // verify there is a relationship between the delivery and the order, and the delivery item and the order item
        checkDeliveryOrderRelationship(delivery2.getDelivery(), order);
        checkDeliveryOrderItemRelationship(deliveryItem2, item2);
    }

    /**
     * Verifies that an invoice can reference multiple orders.
     */
    @Test
    public void testInvoiceWithMultipleOrders() {
        InvoiceMapper mapper = createMapper();
        Product product = getProduct();

        // create two orders, each with one item.
        FinancialAct item1 = createOrderItem(product, BigDecimal.ONE, 1, BigDecimal.ONE, "product1");
        FinancialAct order1 = createOrder(item1);

        FinancialAct item2 = createOrderItem(product, BigDecimal.ONE, 1, BigDecimal.ONE, "product1");
        FinancialAct order2 = createOrder(item2);

        // create an invoice that references the orders
        InvoiceLineType line1 = createInvoiceLine("1", item1);
        line1.getOrderLineReference().add(createOrderLineReference(item1, order1));

        InvoiceLineType line2 = createInvoiceLine("2", item2);
        line2.getOrderLineReference().add(createOrderLineReference(item2, order2));

        Invoice invoice1 = createInvoice(getSupplier(), getStockLocation(), line1, line2);

        // map the invoice to a delivery
        Delivery delivery = mapper.map(invoice1, getSupplier(), getStockLocation(), null);
        assertNull(delivery.getOrder()); // won't have a document level order
        save(delivery.getActs());
        assertEquals(2, delivery.getDeliveryItems().size());
        FinancialAct deliveryItem1 = delivery.getDeliveryItems().get(0);
        FinancialAct deliveryItem2 = delivery.getDeliveryItems().get(1);

        // verify there is a relationship between the delivery and the orders, and the delivery items and order items
        checkDeliveryOrderRelationship(delivery.getDelivery(), order1);
        checkDeliveryOrderRelationship(delivery.getDelivery(), order2);
        checkDeliveryOrderItemRelationship(deliveryItem1, item1);
        checkDeliveryOrderItemRelationship(deliveryItem2, item2);
    }

    /**
     * Verifies that an invoice can reference multiple orders, with partial delivery of order items.
     */
    @Test
    public void testInvoiceWithMultipleOrdersForPartialDelivery() {
        InvoiceMapper mapper = createMapper();
        Product product1 = getProduct();
        Product product2 = TestHelper.createProduct();

        // create orders
        FinancialAct order1Item1 = createOrderItem(product1, BigDecimal.TEN, 1, BigDecimal.ONE, "product1");
        FinancialAct order1Item2 = createOrderItem(product2, BigDecimal.TEN, 1, BigDecimal.ONE, "product2");
        FinancialAct order1 = createOrder(order1Item1, order1Item2);

        // create another order
        FinancialAct order2Item1 = createOrderItem(product1, BigDecimal.TEN, 1, BigDecimal.ONE, "product1");
        FinancialAct order2Item2 = createOrderItem(product2, BigDecimal.TEN, 1, BigDecimal.ONE, "product2");
        FinancialAct order2 = createOrder(order2Item1, order2Item2);

        // create an invoice that references order item1, for half the amount
        BigDecimal five = new BigDecimal("5.0");
        InvoiceLineType line1 = createInvoiceLine("1", five, order1Item1);
        line1.getOrderLineReference().add(createOrderLineReference(order1Item1, order1));

        Invoice invoice1 = createInvoice(getSupplier(), getStockLocation(), line1);

        // map the invoice to a delivery
        Delivery delivery1 = mapper.map(invoice1, getSupplier(), getStockLocation(), null);
        assertNull(delivery1.getOrder()); // won't have a document level order
        save(delivery1.getActs());
        assertEquals(1, delivery1.getDeliveryItems().size());
        FinancialAct deliveryItem1 = delivery1.getDeliveryItems().get(0);
        FinancialAct delivery1Act = delivery1.getDelivery();
        checkDeliveryOrderRelationship(delivery1Act, order1);

        checkDeliveryOrderItemRelationship(deliveryItem1, order1Item1);
        checkEquals(five, deliveryItem1.getQuantity());
        order1 = get(order1);
        checkDeliveryStatus(order1, DeliveryStatus.PENDING);
        delivery1Act.setStatus(ActStatus.POSTED);
        save(delivery1Act);
        order1 = get(order1);
        checkDeliveryStatus(order1, DeliveryStatus.PART);

        // now invoice the remaining portion of order1
        InvoiceLineType invoice2Line1 = createInvoiceLine("1", five, order1Item1);
        invoice2Line1.getOrderLineReference().add(createOrderLineReference(order1Item1, order1));

        Invoice invoice2 = createInvoice(getSupplier(), getStockLocation(), invoice2Line1);
        invoice2.setID(UBLHelper.createID(78910));

        Delivery delivery2 = mapper.map(invoice2, getSupplier(), getStockLocation(), null);
        assertNull(delivery2.getOrder()); // won't have a document level order
        save(delivery2.getActs());
        assertEquals(1, delivery2.getDeliveryItems().size());
        FinancialAct delivery2Item1 = delivery2.getDeliveryItems().get(0);
        FinancialAct delivery2Act = delivery2.getDelivery();
        checkDeliveryOrderRelationship(delivery2Act, order1);
        checkDeliveryOrderItemRelationship(delivery2Item1, order1Item1);

        order1 = get(order1);
        checkDeliveryStatus(order1, DeliveryStatus.PART);

        delivery2Act.setStatus(ActStatus.POSTED);
        save(delivery2Act);

        order1 = get(order1);
        checkDeliveryStatus(order1, DeliveryStatus.PART);

        // now invoice order1 item 2, and all of order 2
        InvoiceLineType invoice3Line1 = createInvoiceLine("1", BigDecimal.TEN, order1Item2);
        invoice3Line1.getOrderLineReference().add(createOrderLineReference(order1Item2, order1));
        InvoiceLineType invoice3Line2 = createInvoiceLine("2", BigDecimal.TEN, order2Item1);
        invoice3Line2.getOrderLineReference().add(createOrderLineReference(order2Item1, order2));
        InvoiceLineType invoice3Line3 = createInvoiceLine("3", BigDecimal.TEN, order2Item2);
        invoice3Line3.getOrderLineReference().add(createOrderLineReference(order2Item2, order2));

        Invoice invoice3 = createInvoice(getSupplier(), getStockLocation(), invoice3Line1, invoice3Line2,
                                         invoice3Line3);
        invoice3.setID(UBLHelper.createID(11121314));

        Delivery delivery3 = mapper.map(invoice3, getSupplier(), getStockLocation(), null);
        assertNull(delivery3.getOrder()); // won't have a document level order
        save(delivery3.getActs());
        assertEquals(3, delivery3.getDeliveryItems().size());
        FinancialAct delivery3Item1 = delivery3.getDeliveryItems().get(0);
        FinancialAct delivery3Item2 = delivery3.getDeliveryItems().get(1);
        FinancialAct delivery3Item3 = delivery3.getDeliveryItems().get(2);
        FinancialAct delivery3Act = delivery3.getDelivery();
        checkDeliveryOrderRelationship(delivery3Act, order1);
        checkDeliveryOrderRelationship(delivery3Act, order2);
        checkDeliveryOrderItemRelationship(delivery3Item1, order1Item2);
        checkDeliveryOrderItemRelationship(delivery3Item2, order2Item1);
        checkDeliveryOrderItemRelationship(delivery3Item3, order2Item2);

        delivery3Act.setStatus(ActStatus.POSTED);
        save(delivery3Act);

        order1 = get(order1);
        order2 = get(order2);
        checkDeliveryStatus(order1, DeliveryStatus.FULL);
        checkDeliveryStatus(order2, DeliveryStatus.FULL);
    }

    /**
     * Verifies that invoices may reference an order without referencing specific OrderLines.
     * <p/>
     * Product matching should occur to link delivery items with order items.
     */
    @Test
    public void testMatchingForInvoiceWithNoOrderLineReferences() {
        InvoiceMapper mapper = createMapper();
        Product product1 = getProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();

        // create an order with 3 items
        FinancialAct item1 = createOrderItem(product1, BigDecimal.TEN, 1, BigDecimal.ONE, "product1");
        FinancialAct item2 = createOrderItem(product2, BigDecimal.TEN, 1, BigDecimal.ONE, "product2");
        FinancialAct item3 = createOrderItem(product3, BigDecimal.TEN, 1, BigDecimal.ONE, "product3");
        FinancialAct order = createOrder(item1, item2, item3);

        // create an invoice that references the order, but not the order items.
        // Different quantities are invoiced for product2 and product3 - the corresponding delivery items should
        // still be linked to the order items 
        InvoiceLineType line1 = createInvoiceLine("1", item1);
        InvoiceLineType line2 = createInvoiceLine("2", BigDecimal.ONE, item2); // invoicing 1, instead of 10
        InvoiceLineType line3 = createInvoiceLine("3", new BigDecimal("15"), item3); // invoicing 15, instead of 10
        Invoice invoice = createInvoice(getSupplier(), getStockLocation(), line1, line2, line3);
        invoice.setOrderReference(createOrderReference(order));

        // map the invoice to a delivery
        Delivery delivery = mapper.map(invoice, getSupplier(), getStockLocation(), null);
        assertEquals(order, delivery.getOrder());
        save(delivery.getActs());
        assertEquals(3, delivery.getDeliveryItems().size());
        FinancialAct deliveryItem1 = delivery.getDeliveryItems().get(0);
        FinancialAct deliveryItem2 = delivery.getDeliveryItems().get(1);
        FinancialAct deliveryItem3 = delivery.getDeliveryItems().get(2);

        // verify there is a relationship between the delivery and the order, and the delivery items and order items
        checkDeliveryOrderRelationship(delivery.getDelivery(), order);
        checkDeliveryOrderItemRelationship(deliveryItem1, item1);
        checkDeliveryOrderItemRelationship(deliveryItem2, item2);
        checkDeliveryOrderItemRelationship(deliveryItem3, item3);
    }

    /**
     * Verifies that invoices may references multiple orders, without referencing specific OrderLines.
     * <p/>
     * This is done by specifying -1 for the OrderLineReference LineID.
     * <p/>
     * Product matching should occur to link delivery items with order items.
     */
    @Test
    public void testMatchingForInvoiceWithMultipleOrdersAndNoOrderLineReferences() {
        InvoiceMapper mapper = createMapper();
        Product product1 = getProduct();
        Product product2 = TestHelper.createProduct();

        // create two orders, each with one item.
        FinancialAct item1 = createOrderItem(product1, BigDecimal.ONE, 1, BigDecimal.ONE, "product1");
        FinancialAct order1 = createOrder(item1);

        FinancialAct item2 = createOrderItem(product2, BigDecimal.ONE, 1, BigDecimal.ONE, "product2");
        FinancialAct order2 = createOrder(item2);

        // create an invoice that references the orders, but specifies -1 (unknown) for the LineIDs
        InvoiceLineType line1 = createInvoiceLine("1", item1);
        line1.getOrderLineReference().add(createOrderLineReference(-1, order1));

        InvoiceLineType line2 = createInvoiceLine("2", item2);
        line2.getOrderLineReference().add(createOrderLineReference(-1, order2));

        Invoice invoice1 = createInvoice(getSupplier(), getStockLocation(), line1, line2);

        // map the invoice to a delivery
        Delivery delivery = mapper.map(invoice1, getSupplier(), getStockLocation(), null);
        assertNull(delivery.getOrder()); // won't have a document level order
        save(delivery.getActs());
        assertEquals(2, delivery.getDeliveryItems().size());
        FinancialAct deliveryItem1 = delivery.getDeliveryItems().get(0);
        FinancialAct deliveryItem2 = delivery.getDeliveryItems().get(1);

        // verify there is a relationship between the delivery and the orders, and the delivery items and order items
        checkDeliveryOrderRelationship(delivery.getDelivery(), order1);
        checkDeliveryOrderRelationship(delivery.getDelivery(), order2);
        checkDeliveryOrderItemRelationship(deliveryItem1, item1);
        checkDeliveryOrderItemRelationship(deliveryItem2, item2);
    }

    /**
     * Verifies that any order item relationship is ignored where a different product is referenced to that ordered.
     */
    @Test
    public void testNoOrderItemRelationshipForSubstitutedProduct() {
        // create an order
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        FinancialAct order = createOrder(orderItem);
        save(order, orderItem);

        // create an invoice for a different product
        Product substitute = TestHelper.createProduct();
        Invoice invoice = createInvoice(order, orderItem, CASE_UNIT_CODE);
        ItemType item = invoice.getInvoiceLine().get(0).getItem();
        item.getBuyersItemIdentification().setID(UBLHelper.createID(substitute.getId()));

        // perform the mapping
        Delivery delivery = map(invoice, order);
        assertEquals(1, delivery.getDeliveryItems().size());
        FinancialAct deliveryItem = delivery.getDeliveryItems().get(0);
        ActBean itemBean = new ActBean(deliveryItem);

        // verify there is no relationship between the delivery item and the order item
        assertFalse(itemBean.hasRelationship(SupplierArchetypes.DELIVERY_ORDER_ITEM_RELATIONSHIP, orderItem));
    }

    /**
     * Verifies that an inactive product is not used if it matches on reorder code.
     */
    @Test
    public void testInactiveProductMatchOnReorderCode() {
        InvoiceMapper mapper = createMapper();
        Product product = getProduct();

        String reorderCode = "ABCD";

        product.setActive(false);
        addProductSupplierRelationship(product, getSupplier(), reorderCode, 1, PACKAGE_UNITS, new BigDecimal("1.4"));

        FinancialAct item = createOrderItem(product, BigDecimal.ONE, 1, BigDecimal.ONE, reorderCode);
        FinancialAct order = createOrder(item);

        // create an invoice that references the order, but not the order items, and exclude the BuyersItemID.
        // This forces order item and product matching on reorder code.
        InvoiceLineType line1 = createInvoiceLine("1", item);
        assertEquals(0, line1.getOrderLineReference().size());
        line1.getOrderLineReference().add(createOrderLineReference(-1, order));
        line1.getItem().setBuyersItemIdentification(null);
        assertEquals(reorderCode, line1.getItem().getSellersItemIdentification().getID().getValue());
        Invoice invoice = createInvoice(getSupplier(), getStockLocation(), line1);

        // map the invoice to a delivery
        Delivery delivery = mapper.map(invoice, getSupplier(), getStockLocation(), null);
        assertNull(delivery.getOrder());
        save(delivery.getActs());
        assertEquals(1, delivery.getDeliveryItems().size());
        FinancialAct deliveryItem = delivery.getDeliveryItems().get(0);

        // verify the delivery item has no product, and that the reorder code is populated
        ActBean bean = new ActBean(deliveryItem);
        assertNull(bean.getNodeParticipantRef("product"));
        assertEquals(reorderCode, bean.getString("reorderCode"));

        // verify there is a relationship between the delivery and the order, but not the delivery item and order item
        checkDeliveryOrderRelationship(delivery.getDelivery(), order);
        assertFalse(bean.hasRelationship(SupplierArchetypes.DELIVERY_ORDER_ITEM_RELATIONSHIP, item));
    }

    /**
     * Verifies that if there are two order lines for the same product, and the invoice only invoices one,
     * it is matched on quantity.
     */
    @Test
    public void testDuplicateOrderLinesSingleInvoiceLineNoOrderLineReferences() {
        InvoiceMapper mapper = createMapper();
        Product product = getProduct();

        // create an order with 2 items for the same product
        FinancialAct item1 = createOrderItem(product, BigDecimal.TEN, 1, BigDecimal.ONE, "product1");
        FinancialAct item2 = createOrderItem(product, BigDecimal.ONE, 1, BigDecimal.ONE, "product1");
        FinancialAct order = createOrder(item1, item2);

        // create an invoice that references the order, but not the order items.
        InvoiceLineType line1 = createInvoiceLine("1", item2);
        assertEquals(0, line1.getOrderLineReference().size());
        line1.getOrderLineReference().add(createOrderLineReference(-1, order));
        Invoice invoice = createInvoice(getSupplier(), getStockLocation(), line1);

        // map the invoice to a delivery
        Delivery delivery = mapper.map(invoice, getSupplier(), getStockLocation(), null);
        assertNull(delivery.getOrder());
        save(delivery.getActs());
        assertEquals(1, delivery.getDeliveryItems().size());
        FinancialAct deliveryItem1 = delivery.getDeliveryItems().get(0);

        // verify there is a relationship between the delivery and the order, and the delivery items and order item2
        checkDeliveryOrderRelationship(delivery.getDelivery(), order);
        checkDeliveryOrderItemRelationship(deliveryItem1, item2);
    }

    /**
     * Verifies that the list price in delivery items is set to:
     * <ol>
     * <li>the wholesale price from the invoice line, if present; or
     * <li>the list price from product/supplier relationship; or
     * <li>the unit price, if neither of the above is present or are zero
     * </ol>
     */
    @Test
    public void testListPrice() {
        Product product1 = getProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();

        BigDecimal listPrice1 = new BigDecimal("1.5");
        BigDecimal listPrice2 = BigDecimal.ZERO;
        BigDecimal listPrice3 = null;
        addProductSupplierRelationship(product1, getSupplier(), 1, PACKAGE_UNITS, new BigDecimal("1.4"));
        addProductSupplierRelationship(product2, getSupplier(), 1, PACKAGE_UNITS, new BigDecimal("1.1"));
        addProductSupplierRelationship(product3, getSupplier(), 1, PACKAGE_UNITS, null);

        InvoiceLineType line1 = createInvoiceLine("1", product1, "PRODUCT1", null, listPrice1, BigDecimal.ONE,
                                                  BigDecimal.ONE, PACKAGE_UNIT_CODE);
        InvoiceLineType line2 = createInvoiceLine("2", product2, "PRODUCT2", null, listPrice2, BigDecimal.ONE,
                                                  BigDecimal.ONE, PACKAGE_UNIT_CODE);
        InvoiceLineType line3 = createInvoiceLine("3", product3, "PRODUCT3", null, listPrice3, BigDecimal.ONE,
                                                  BigDecimal.ONE, PACKAGE_UNIT_CODE);

        Invoice invoice = createInvoice(getSupplier(), getStockLocation(), line1, line2, line3);

        // perform the mapping
        InvoiceMapper mapper = createMapper();
        Delivery delivery = mapper.map(invoice, getSupplier(), getStockLocation(), null);

        assertEquals(3, delivery.getDeliveryItems().size());

        // verify product1 list price is that from line1
        FinancialAct deliveryItem1 = delivery.getDeliveryItems().get(0);
        ActBean itemBean1 = new ActBean(deliveryItem1);
        assertTrue(itemBean1.getBigDecimal("listPrice").compareTo(listPrice1) == 0);

        // verify product2 list price is that from the product/supplier relationship
        FinancialAct deliveryItem2 = delivery.getDeliveryItems().get(1);
        ActBean itemBean2 = new ActBean(deliveryItem2);
        assertTrue(itemBean2.getBigDecimal("listPrice").compareTo(new BigDecimal("1.1")) == 0);

        // verify product3 list price is the same as the unit prioe
        FinancialAct deliveryItem3 = delivery.getDeliveryItems().get(2);
        ActBean itemBean3 = new ActBean(deliveryItem3);
        assertTrue(itemBean3.getBigDecimal("listPrice").compareTo(BigDecimal.ONE) == 0);
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if a document-level order reference is specified but
     * the invoice lines reference different orders.
     */
    @Test
    public void testMultipleOrdersWithDocLevelOrderReference() {
        // create two orders, each with one item.
        FinancialAct item1 = createOrderItem(getProduct(), BigDecimal.ONE, 1, BigDecimal.ONE, "PRODUCTA");
        FinancialAct order1 = createOrder(item1);

        FinancialAct item2 = createOrderItem(getProduct(), BigDecimal.ONE, 1, BigDecimal.ONE, "PRODUCTA");
        FinancialAct order2 = createOrder(item2);

        // create an invoice that references the orders,
        InvoiceLineType line1 = createInvoiceLine("1", item1);
        line1.getOrderLineReference().add(createOrderLineReference(item1, order1));

        InvoiceLineType line2 = createInvoiceLine("2", item2);
        line2.getOrderLineReference().add(createOrderLineReference(item2, order2));

        Invoice invoice = createInvoice(getSupplier(), getStockLocation(), line1, line2);
        invoice.setOrderReference(createOrderReference(order1));
        checkMappingException(invoice, "ESCIA-0108: Invalid Order: " + order2.getId()
                                       + " referenced by Invoice: 12345");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if a required element is missing.
     */
    @Test
    public void testRequiredElementMissing() {
        Invoice invoice = createInvoice();
        invoice.getLegalMonetaryTotal().setPayableAmount(null);
        checkMappingException(invoice, "ESCIA-0100: Required element: Invoice/LegalMonetaryTotal/PayableAmount missing "
                                       + "in Invoice: 12345");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if a collection has a different no. of elements to that
     * expected.
     */
    @Test
    public void testInvalidCardinality() {
        Invoice invoice = createInvoice();
        invoice.getTaxTotal().add(new TaxTotalType());
        checkMappingException(invoice, "ESCIA-0101: Invalid cardinality for Invoice/TaxTotal in Invoice: 12345. "
                                       + "Expected 1 but got 2");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an ID element contains an invalid identifier
     * (i.e one that is non-numeric).
     */
    @Test
    public void testInvalidIdentifier() {
        // check invalid identifier for Invoice
        Invoice invoice1 = createInvoice();
        invoice1.getAccountingCustomerParty().getCustomerAssignedAccountID().setValue("abc");
        checkMappingException(invoice1, "ESCIA-0102: Invalid identifier: abc for "
                                        + "Invoice/AccountingCustomerParty/CustomerAssignedAccountID in Invoice: "
                                        + "12345");

        // check invalid identifier for InvoiceLine
        Invoice invoice2 = createInvoice();
        InvoiceLineType item = invoice2.getInvoiceLine().get(0);
        item.getItem().getBuyersItemIdentification().getID().setValue("cde");
        checkMappingException(invoice2, "ESCIA-0102: Invalid identifier: cde for "
                                        + "InvoiceLine/Item/BuyersItemIdentification/ID in InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the UBL version doesn't match that expected.
     */
    @Test
    public void testInvalidUBLVersion() {
        Invoice invoice = createInvoice();
        invoice.getUBLVersionID().setValue("2.1");
        checkMappingException(invoice, "ESCIA-0103: Expected 2.0 for UBLVersionID in Invoice: 12345 but got 2.1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an amount is invalid.
     */
    @Test
    public void testInvalidAmount() {
        Invoice invoice = createInvoice();
        invoice.getInvoiceLine().get(0).getPrice().getPriceAmount().setValue(new BigDecimal(-1));
        checkMappingException(invoice, "ESCIA-0104: Invalid amount: -1 for InvoiceLine/Price/PriceAmount in "
                                       + "InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an amount's currency doesn't match the practice currency.
     */
    @Test
    public void testInvalidCurrency() {
        Invoice invoice = createInvoice();
        InvoiceLineType item = invoice.getInvoiceLine().get(0);
        item.getLineExtensionAmount().setCurrencyID(CurrencyCodeContentType.USD);
        checkMappingException(invoice, "ESCIA-0105: Invalid currencyID for InvoiceLine/LineExtensionAmount in "
                                       + "InvoiceLine: 1. Expected AUD but got USD");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an amount is invalid.
     */
    @Test
    public void testInvalidQuantity() {
        Invoice invoice = createInvoice();
        invoice.getInvoiceLine().get(0).getInvoicedQuantity().setValue(new BigDecimal(-2));
        checkMappingException(invoice, "ESCIA-0106: Invalid quantity: -2 for InvoiceLine/InvoicedQuantity in "
                                       + "InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the
     * <em>Invoice/AccountingSupplierParty/CustomerAssignedAccountID</em> doesn't correspond to a valid supplier.
     */
    @Test
    public void testInvalidSupplier() {
        Invoice invoice = createInvoice();
        invoice.getAccountingSupplierParty().getCustomerAssignedAccountID().setValue("0");
        checkMappingException(invoice, "ESCIA-0107: Invalid supplier: 0 referenced by Invoice: 12345, "
                                       + "element Invoice/AccountingSupplierParty/CustomerAssignedAccountID");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised a tax scheme is unrecognised.
     */
    @Test
    public void testInvalidTaxScheme() {
        Invoice invoice = createInvoice();
        InvoiceLineType line = invoice.getInvoiceLine().get(0);
        TaxTotalType taxTotal = line.getTaxTotal().get(0);
        TaxSubtotalType subtotal = taxTotal.getTaxSubtotal().get(0);
        TaxCategoryType category = subtotal.getTaxCategory();
        TaxSchemeType scheme = category.getTaxScheme();
        scheme.getTaxTypeCode().setValue("Sales tax");
        checkMappingException(invoice, "ESCIA-0110: TaxScheme/TaxTypeCode: Sales tax and TaxCategory/ID: S referenced "
                                       + "by InvoiceLine/TaxTotal/TaxSubtotal/TaxCategory in InvoiceLine: 1 do not "
                                       + "correspond to a known tax type");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if neither the BuyersItemIdentification nor
     * SellersItemIdentification are provided.
     */
    @Test
    public void testNoProduct() {
        Invoice invoice = createInvoice();
        InvoiceLineType item = invoice.getInvoiceLine().get(0);
        item.getItem().setBuyersItemIdentification(null);
        item.getItem().setSellersItemIdentification(null);
        checkMappingException(invoice, "ESCIA-0601: Neither Item/BuyersItemIdentification nor "
                                       + "Item/SellersItemIdentification provided in InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the payable amount is incorrect.
     */
    @Test
    public void testInvalidPayableAmount() {
        Invoice invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getPayableAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0602: Calculated payable amount: 110 for Invoice: 12345 does not match "
                                       + "LegalMonetaryTotal/PayableAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the line extension amounts are inconsistent.
     */
    @Test
    public void testInvalidLineExtensionAmount() {
        Invoice invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getLineExtensionAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0603: Sum of InvoiceLine/LineExtensionAmount: 100 for Invoice: 12345 "
                                       + "does not match Invoice/LegalMonetaryTotal/LineExtensionAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the tax totals are inconsistent.
     */
    @Test
    public void testInvalidTax() {
        Invoice invoice = createInvoice();
        TaxTotalType total = invoice.getTaxTotal().get(0);
        total.getTaxAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0604: Sum of InvoiceLine taxes and charge taxes: 10 for Invoice: 12345 "
                                       + "does not match TaxTotal/TaxAmount: 1");
    }

    /**
     * Verifies than an {@link ESCIAdapterException} is raised if a line extension amount for an InvoiceLine doesn't
     * match that expected.
     */
    @Test
    public void testInvalidItemLineExtensionAmount() {
        Invoice invoice = createInvoice();
        InvoiceLineType item = invoice.getInvoiceLine().get(0);
        item.getLineExtensionAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0605: Calculated line extension amount: 100 for InvoiceLine: 1 "
                                       + "does not match LineExtensionAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an invoice references an order which was not sent to
     * the supplier.
     */
    @Test
    public void testInvalidOrder() {
        Party anotherSupplier = TestHelper.createSupplier();

        // create an order with a single item
        Act order = createOrder(anotherSupplier);

        Invoice invoice = createInvoice();
        invoice.setOrderReference(UBLHelper.createOrderReference(order.getId()));
        checkMappingException(invoice, getSupplier(), "ESCIA-0108: Invalid Order: " + order.getId()
                                                      + " referenced by Invoice: 12345");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an invoice references an order item which doesn't
     * exist.
     */
    @Test
    public void testInvalidOrderItem() {
        // create an order with a single item
        Act order = createOrder();

        Invoice invoice = createInvoice();
        invoice.setOrderReference(UBLHelper.createOrderReference(order.getId()));
        OrderLineReferenceType lineRef = new OrderLineReferenceType();
        lineRef.setLineID(UBLHelper.initID(new LineIDType(), "0"));
        invoice.getInvoiceLine().get(0).getOrderLineReference().add(lineRef);
        checkMappingException(invoice, "ESCIA-0606: Invalid OrderLine: 0 referenced by InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an invoice references an order item which exists
     * but is not related to the order.
     */
    @Test
    public void testInvalidOrderItemForOrder() {
        // create an order with a single item
        Act order = createOrder();
        FinancialAct invalid = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        save(invalid);

        Invoice invoice = createInvoice();
        invoice.setOrderReference(UBLHelper.createOrderReference(order.getId()));
        OrderLineReferenceType lineRef = createOrderLineReference(invalid);
        invoice.getInvoiceLine().get(0).getOrderLineReference().add(lineRef);
        checkMappingException(invoice, "ESCIA-0606: Invalid OrderLine: " + invalid.getId()
                                       + " referenced by InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an order item is referenced by an InvoiceLine, but
     * there is no document level OrderReference.
     */
    @Test
    public void testOrderItemForNoOrder() {
        Invoice invoice = createInvoice();
        OrderLineReferenceType lineRef = new OrderLineReferenceType();
        lineRef.setLineID(UBLHelper.initID(new LineIDType(), "151"));
        invoice.getInvoiceLine().get(0).getOrderLineReference().add(lineRef);
        checkMappingException(invoice, "ESCIA-0101: Invalid cardinality for OrderLineReference in InvoiceLine: 1. "
                                       + "Expected 0 but got 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if no stock location is provided in the invoice.
     */
    @Test
    public void testNoSupplier() {
        Invoice invoice = createInvoice();
        invoice.getAccountingSupplierParty().setCustomerAssignedAccountID(null);
        checkMappingException(invoice, "ESCIA-0111: One of CustomerAssignedAccountID or AdditionalAccountID is "
                                       + "required for Invoice/AccountingSupplierParty in Invoice: 12345");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the supplier in the invoice is different to that
     * submitting the invoice.
     */
    @Test
    public void testSupplierMismatch() {
        Party anotherSupplier = TestHelper.createSupplier();
        Invoice invoice = createInvoice(anotherSupplier);
        Party expected = getSupplier();
        checkMappingException(invoice,
                              "ESCIA-0109: Expected supplier " + expected.getName() + " (" + expected.getId()
                              + ") for Invoice/AccountingSupplierParty/CustomerAssignedAccountID in Invoice: 12345, "
                              + "but got " + anotherSupplier.getName() + " (" + anotherSupplier.getId() + ")");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if no stock location is provided in the invoice.
     */
    @Test
    public void testNoStockLocation() {
        Invoice invoice = createInvoice();
        invoice.getAccountingCustomerParty().setCustomerAssignedAccountID(null);
        checkMappingException(invoice, "ESCIA-0112: One of CustomerAssignedAccountID or SupplierAssignedAccountID is "
                                       + "required for Invoice/AccountingCustomerParty in Invoice: 12345");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the
     * Invoice/AccountingCustomerParty/CustomerAssignedAccountID doesn't correspond to a valid stock location.
     */
    @Test
    public void testInvalidStockLocation() {
        Invoice invoice = createInvoice();
        invoice.getAccountingCustomerParty().getCustomerAssignedAccountID().setValue("0");
        checkMappingException(invoice, "ESCIA-0113: Invalid stock location: 0 referenced by Invoice: 12345, element "
                                       + "Invoice/AccountingCustomerParty/CustomerAssignedAccountID");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the stock location in the response doesn't match that
     * expected.
     */
    @Test
    public void testStockLocationMismatch() {
        Party supplier = getSupplier();
        Party expected = getStockLocation();
        Party stockLocation = createStockLocation();
        Invoice invoice = createInvoice(supplier, stockLocation, PACKAGE_UNIT_CODE);
        checkMappingException(invoice,
                              "ESCIA-0114: Expected stock location " + expected.getName() + " (" + expected.getId()
                              + ") for Invoice/AccountingCustomerParty/CustomerAssignedAccountID in Invoice: 12345, "
                              + "but got " + stockLocation.getName() + " (" + stockLocation.getId() + ")");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an alternative condition price is supplied but doesn't
     * specify a wholesale PriceTypeCode.
     */
    @Test
    public void testIncorrectWholesalePrice() {
        Invoice invoice = createInvoice();
        InvoiceLineType line = invoice.getInvoiceLine().get(0);
        PriceType price = line.getPricingReference().getAlternativeConditionPrice().get(0);
        price.getPriceTypeCode().setValue("RS");
        checkMappingException(invoice, "ESCIA-0103: Expected WH for "
                                       + "InvoiceLine/PricingReference/AlternativeConditionPrice/PriceTypeCode "
                                       + "in InvoiceLine: 1 but got RS");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an AllowanceCharge has a ChargeIndicator set false
     * (i.e is an allowance).
     */
    @Test
    public void testAllowanceChargeSpecifiedAsAllowance() {
        Invoice invoice = createInvoice();
        AllowanceChargeType allowance = new AllowanceChargeType();
        ChargeIndicatorType charge = new ChargeIndicatorType();
        charge.setValue(false);
        allowance.setChargeIndicator(charge);
        invoice.getAllowanceCharge().add(allowance);
        checkMappingException(invoice, "ESCIA-0607: Invoice 12345 contains an AllowanceCharge with ChargeIndicator "
                                       + "set false");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the charge total amount doesn't match the calculated
     * charge total.
     */
    @Test
    public void testInvalidChargeTotalAmount() {
        Invoice invoice = createInvoice();
        invoice.getAllowanceCharge().add(createCharge(BigDecimal.ONE, BigDecimal.ZERO, "Foo", new BigDecimal("10.00")));
        checkMappingException(invoice, "ESCIA-0608: Sum of charge AllowanceCharge/Amount: 1 for Invoice: 12345 "
                                       + "does not match Invoice/LegalMonetaryTotal/ChargeTotalAmount: 0");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an invoice is processed twice and the invoice
     * references no orders.
     */
    @Test
    public void testDuplicateInvoiceForNoOrder() {
        InvoiceMapper mapper = createMapper();

        // create an invoice
        Invoice invoice = createInvoice();

        Delivery delivery = mapper.map(invoice, getSupplier(), getStockLocation(), null);
        save(delivery.getActs());

        checkMappingException(invoice, "ESCIA-0609: Duplicate Invoice: 12345. Corresponding Delivery is: "
                                       + delivery.getDelivery().getId());
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an invoice is processed twice and the invoice
     * references an order.
     */
    @Test
    public void testDuplicateInvoiceForOrder() {
        InvoiceMapper mapper = createMapper();

        // create an order
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        FinancialAct order = createOrder(orderItem);
        save(order, orderItem);

        // create an invoice that references the order
        Invoice invoice = createInvoice();
        invoice.setOrderReference(UBLHelper.createOrderReference(order.getId()));

        Delivery delivery = mapper.map(invoice, getSupplier(), getStockLocation(), null);
        save(delivery.getActs());

        // adjust the invoice timestamp so that it is skipped by the simple duplicate check
        invoice.setIssueDate(createIssueDate(DateRules.getDate(new Date(), 1, DateUnits.MONTHS)));

        checkMappingException(invoice, "ESCIA-0610: Duplicate Invoice: 12345 received for Order: " + order.getId());
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the tax exclusive amount is inconsistent with that
     * of the item tax ex amounts.
     */
    @Test
    public void testInvalidTaxExclusiveAmount() {
        Invoice invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getTaxExclusiveAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0611: Calculated tax exclusive amount: 100 for Invoice: 12345 does not "
                                       + "match Invoice/LegalMonetaryTotal/TaxExcusiveAmount: 1");
    }

    /**
     * Checks the delivery status of an order.
     *
     * @param order  the order
     * @param status the expected status
     */
    private void checkDeliveryStatus(FinancialAct order, DeliveryStatus status) {
        ActBean bean = new ActBean(order);
        assertEquals(status.toString(), bean.getString("deliveryStatus"));
    }

    /**
     * Verifies a relationship exists between a delivery and an order.
     *
     * @param delivery the delivery item
     * @param order    the order item
     */
    private void checkDeliveryOrderRelationship(FinancialAct delivery, FinancialAct order) {
        ActBean bean = new ActBean(delivery);
        assertTrue(bean.hasRelationship(SupplierArchetypes.DELIVERY_ORDER_RELATIONSHIP, order));
    }

    /**
     * Verifies a relationship exists between a delivery item and an order item.
     *
     * @param deliveryItem the delivery item
     * @param orderItem    the order item
     */
    private void checkDeliveryOrderItemRelationship(FinancialAct deliveryItem, FinancialAct orderItem) {
        ActBean bean = new ActBean(deliveryItem);
        assertTrue(bean.hasRelationship(SupplierArchetypes.DELIVERY_ORDER_ITEM_RELATIONSHIP, orderItem));
    }

    /**
     * Verifies that the package size and units in a delivery match that expected.
     *
     * @param packageSize  the expected package size
     * @param packageUnits the expected package units
     * @param invoice      the invoice
     * @param order        the order
     * @param orderItem    the order item
     */
    private void checkMapPackageUnits(int packageSize, String packageUnits, Invoice invoice, FinancialAct order,
                                      FinancialAct orderItem) {
        // map the invoice to a delivery
        Delivery delivery = map(invoice, order);
        assertEquals(1, delivery.getDeliveryItems().size());
        FinancialAct deliveryItem = delivery.getDeliveryItems().get(0);
        ActBean itemBean = new ActBean(deliveryItem);

        // verify there is a relationship between the delivery item and the order item
        assertTrue(itemBean.hasRelationship(SupplierArchetypes.DELIVERY_ORDER_ITEM_RELATIONSHIP, orderItem));
        assertEquals(packageSize, itemBean.getValue("packageSize"));
        assertEquals(packageUnits, itemBean.getValue("packageUnits"));
    }

    /**
     * Adds a product/supplier relatiobship.
     *
     * @param product      the product
     * @param supplier     the supplier
     * @param packageSize  the package size
     * @param packageUnits the package units
     */
    private void addProductSupplierRelationship(Product product, Party supplier, int packageSize, String packageUnits) {
        addProductSupplierRelationship(product, supplier, packageSize, packageUnits, null);
    }

    /**
     * Adds a product/supplier relationship.
     *
     * @param product      the product
     * @param supplier     the supplier
     * @param packageSize  the package size
     * @param packageUnits the package units
     * @param listPrice    the list price. May be {@code null}
     */
    private void addProductSupplierRelationship(Product product, Party supplier, int packageSize, String packageUnits,
                                                BigDecimal listPrice) {
        addProductSupplierRelationship(product, supplier, null, packageSize, packageUnits, listPrice);
    }

    /**
     * Adds a product/supplier relationship.
     *
     * @param product      the product
     * @param supplier     the supplier
     * @param reorderCode  the reorder code. May be {@code null}
     * @param packageSize  the package size
     * @param packageUnits the package units
     * @param listPrice    the list price. May be {@code null}
     */
    private void addProductSupplierRelationship(Product product, Party supplier, String reorderCode, int packageSize,
                                                String packageUnits, BigDecimal listPrice) {
        ProductRules rules = new ProductRules();
        ProductSupplier ps = rules.createProductSupplier(product, supplier);
        ps.setReorderCode(reorderCode);
        ps.setPackageSize(packageSize);
        ps.setPackageUnits(packageUnits);
        ps.setListPrice(listPrice);
        save(product, supplier);
    }

    /**
     * Helper to map an invoice.
     *
     * @param invoice the invoice
     * @param order   the order expected to be referenced by the delivery
     * @return the delivery
     */
    private Delivery map(Invoice invoice, FinancialAct order) {
        InvoiceMapper mapper = createMapper();
        Delivery delivery = mapper.map(invoice, getSupplier(), getStockLocation(), null);
        assertEquals(order, delivery.getOrder());
        save(delivery.getActs());
        return delivery;
    }

    /**
     * Creates a single invoice linked to an order and item.
     *
     * @param order     the order
     * @param orderItem the order item
     * @param unitCode  the invoiced quantity unit code
     * @return a new invoice
     */
    private Invoice createInvoice(FinancialAct order, FinancialAct orderItem, String unitCode) {
        Invoice invoice = createInvoice(getSupplier(), getStockLocation(), unitCode);
        invoice.setOrderReference(UBLHelper.createOrderReference(order.getId()));

        // reference the order item in the invoice line
        InvoiceLineType line = invoice.getInvoiceLine().get(0);
        OrderLineReferenceType itemRef = createOrderLineReference(orderItem);
        line.getOrderLineReference().add(itemRef);
        return invoice;
    }

    /**
     * Creates an order reference.
     *
     * @param order the order
     * @return a new {@code OrderReferenceType}
     */
    private OrderReferenceType createOrderReference(FinancialAct order) {
        return UBLHelper.createOrderReference(order.getId());
    }

    /**
     * Creates an order line reference.
     *
     * @param orderItem the order item
     * @return a new {@code OrderLineReferenceType}
     */
    private OrderLineReferenceType createOrderLineReference(FinancialAct orderItem) {
        return createOrderLineReference(orderItem, null);
    }

    /**
     * Creates an order line reference that optionally references the order.
     *
     * @param orderItem the order item
     * @param order     the order. May be {@code null}
     * @return a new {@code OrderLineReferenceType}
     */
    private OrderLineReferenceType createOrderLineReference(FinancialAct orderItem, FinancialAct order) {
        return createOrderLineReference(orderItem.getId(), order);
    }

    /**
     * Creates an order line reference that optionally references the order.
     *
     * @param lineId the order line identifier, or {@code -1} if it is not known
     * @param order  the order. May be {@code null}
     * @return a new {@code OrderLineReferenceType}
     */
    private OrderLineReferenceType createOrderLineReference(long lineId, FinancialAct order) {
        OrderLineReferenceType result = new OrderLineReferenceType();
        result.setLineID(UBLHelper.initID(new LineIDType(), lineId));
        if (order != null) {
            result.setOrderReference(createOrderReference(order));
        }
        return result;
    }

    /**
     * Serializes and deserializes an invoice to ensure its validitity.
     *
     * @param invoice the invoice
     * @return the deserialized invoice
     * @throws JAXBException for any JAXB exception
     * @throws SAXException  for any SAX exception
     */
    private Invoice serialize(Invoice invoice) throws JAXBException, SAXException {
        UBLDocumentContext context = new UBLDocumentContext();
        UBLDocumentWriter writer = context.createWriter();
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        writer.setFormat(true);
        writer.write(invoice, o);
        writer.write(invoice, System.out);
        UBLDocumentReader reader = context.createReader();
        return (Invoice) reader.read(new ByteArrayInputStream(o.toByteArray()));
    }

    /**
     * Maps an invalid invoice and verifies the expected exception is thrown.
     *
     * @param invoice         the invoice to map
     * @param expectedMessage the expected error message
     */
    private void checkMappingException(Invoice invoice, String expectedMessage) {
        checkMappingException(invoice, getSupplier(), expectedMessage);
    }

    /**
     * Maps an invalid invoice and verifies the expected exception is thrown.
     *
     * @param invoice         the invoice to map
     * @param supplier        the supplier submitting the invoice
     * @param expectedMessage the expected error message
     */
    private void checkMappingException(Invoice invoice, Party supplier, String expectedMessage) {
        InvoiceMapper mapper = createMapper();
        try {
            mapper.map(invoice, supplier, getStockLocation(), null);
            fail("Expected mapping to fail");
        } catch (ESCIAdapterException expected) {
            assertEquals(expectedMessage, expected.getMessage());
        }
    }

    /**
     * Verifies the contents of an <em>act.supplierDelivery</em>
     *
     * @param delivery  the delivery to check
     * @param invoiceId the expected supplier invoice identifier
     * @param startTime the expected start time
     * @param notes     expected supplier notes
     * @param total     the expected total
     * @param tax       the expected tax
     */
    private void checkDelivery(FinancialAct delivery, String invoiceId, Date startTime, String notes, BigDecimal total,
                               BigDecimal tax) {
        ActBean bean = new ActBean(delivery);
        assertEquals(startTime, bean.getDate("startTime"));
        assertEquals(invoiceId, bean.getString("supplierInvoiceId"));
        assertEquals(notes, bean.getString("supplierNotes"));
        checkEquals(total, bean.getBigDecimal("amount"));
        checkEquals(tax, bean.getBigDecimal("tax"));
    }

    /**
     * Verifies the contents of an <em>act.supplierDeliveryItem</em>
     *
     * @param item               the delivery item to check
     * @param id                 the expected supplier invoice line identifier
     * @param startTime          the expected start time
     * @param product            the expected product
     * @param quantity           the expected quantity
     * @param listPrice          the expected list price
     * @param unitPrice          the expected unit price
     * @param packageUnits       the expected package units
     * @param reorderCode        the expected re-order code
     * @param reorderDescription the expected re-order description
     * @param total              the expected total
     * @param tax                the expected tax
     */
    private void checkDeliveryItem(FinancialAct item, String id, Date startTime, Product product, BigDecimal quantity,
                                   BigDecimal listPrice, BigDecimal unitPrice, String packageUnits, String reorderCode,
                                   String reorderDescription, BigDecimal total, BigDecimal tax) {
        ActBean bean = new ActBean(item);
        assertEquals(id, bean.getString("supplierInvoiceLineId"));
        assertEquals(startTime, bean.getDate("startTime"));
        if (product == null) {
            assertNull(bean.getNodeParticipantRef("product"));
        } else {
            assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        }
        checkEquals(quantity, bean.getBigDecimal("quantity"));
        checkEquals(listPrice, bean.getBigDecimal("listPrice"));
        checkEquals(unitPrice, bean.getBigDecimal("unitPrice"));
        assertEquals(packageUnits, bean.getString("packageUnits"));
        assertEquals(reorderCode, bean.getString("reorderCode"));
        assertEquals(reorderDescription, bean.getString("reorderDescription"));
        checkEquals(total, item.getTotal());
        checkEquals(tax, item.getTaxAmount());
    }

}
