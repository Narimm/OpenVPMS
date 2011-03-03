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
package org.openvpms.esci.adapter.map.invoice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.common.CurrencyCodeContentType;
import org.openvpms.esci.ubl.common.aggregate.AllowanceChargeType;
import org.openvpms.esci.ubl.common.aggregate.CustomerPartyType;
import org.openvpms.esci.ubl.common.aggregate.InvoiceLineType;
import org.openvpms.esci.ubl.common.aggregate.MonetaryTotalType;
import org.openvpms.esci.ubl.common.aggregate.OrderLineReferenceType;
import org.openvpms.esci.ubl.common.aggregate.PriceType;
import org.openvpms.esci.ubl.common.aggregate.SupplierPartyType;
import org.openvpms.esci.ubl.common.aggregate.TaxCategoryType;
import org.openvpms.esci.ubl.common.aggregate.TaxSchemeType;
import org.openvpms.esci.ubl.common.aggregate.TaxSubtotalType;
import org.openvpms.esci.ubl.common.aggregate.TaxTotalType;
import org.openvpms.esci.ubl.common.basic.ChargeIndicatorType;
import org.openvpms.esci.ubl.common.basic.LineIDType;
import org.openvpms.esci.ubl.common.basic.NoteType;
import org.openvpms.esci.ubl.common.basic.UBLVersionIDType;
import org.openvpms.esci.ubl.invoice.InvoiceType;
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


/**
 * Tests the {@link InvoiceMapperImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceMapperTestCase extends AbstractInvoiceTest {

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

        InvoiceType invoice = new InvoiceType();
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
                                                  lineExtension1, tax1);
        InvoiceLineType item2 = createInvoiceLine("2", product2, reorder2, reorderDesc2, listPrice2, price2, quantity2,
                                                  lineExtension2, tax2);
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
        InvoiceMapper mapper = createMapper();

        // create an order
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        FinancialAct order = createOrder(orderItem);
        save(order, orderItem);

        // create an invoice that references the order
        InvoiceType invoice = createInvoice();
        invoice.setOrderReference(UBLHelper.createOrderReference(order.getId()));

        // reference the order item in the invoice line
        InvoiceLineType line = invoice.getInvoiceLine().get(0);
        OrderLineReferenceType itemRef = new OrderLineReferenceType();
        itemRef.setLineID(UBLHelper.initID(new LineIDType(), orderItem.getId()));
        line.getOrderLineReference().add(itemRef);

        // map the invoice to a delivery
        Delivery delivery = mapper.map(invoice, getSupplier(), getStockLocation(), null);
        assertEquals(order, delivery.getOrder());
        save(delivery.getActs());
        assertEquals(1, delivery.getDeliveryItems().size());
        FinancialAct deliveryItem = delivery.getDeliveryItems().get(0);
        ActBean itemBean = new ActBean(deliveryItem);

        // verify there is a relationship between the delivery item and the order item
        assertTrue(itemBean.hasRelationship(SupplierArchetypes.DELIVERY_ORDER_ITEM_RELATIONSHIP, orderItem));
    }

    /**
     * Verifies that the author node of <em>act.supplierDelivery is populated correctly.
     */
    @Test
    public void testAuthor() {
        InvoiceMapper mapper = createMapper();

        // create an invoice that doesn't reference an order.
        // No author should appear on the resulting delivery
        InvoiceType invoice1 = createInvoice();
        Delivery delivery1 = mapper.map(invoice1, getSupplier(), getStockLocation(), null);
        ActBean bean1 = new ActBean(delivery1.getDelivery());
        assertNull(bean1.getNodeParticipant("author"));

        // create an invoice that doesn't reference an order, but add a defaultAuthor to the stock location.
        // This should appear on the resulting delivery
        User defaultAuthor = TestHelper.createUser();
        EntityBean locBean = new EntityBean(getStockLocation());
        locBean.addNodeRelationship("defaultAuthor", defaultAuthor);
        locBean.save();

        InvoiceType invoice2 = createInvoice();
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

        InvoiceType invoice3 = createInvoice();
        invoice3.setOrderReference(UBLHelper.createOrderReference(order3.getId()));
        Delivery delivery3 = mapper.map(invoice3, getSupplier(), getStockLocation(), null);
        ActBean deliveryBean3 = new ActBean(delivery3.getDelivery());
        assertEquals(author, deliveryBean3.getNodeParticipant("author"));

        // create an invoice that references an order, with no author.
        // The stock location default author should be used
        FinancialAct order4 = createOrder();
        InvoiceType invoice4 = createInvoice();
        invoice4.setOrderReference(UBLHelper.createOrderReference(order4.getId()));
        Delivery delivery4 = mapper.map(invoice4, getSupplier(), getStockLocation(), null);
        ActBean deliveryBean4 = new ActBean(delivery4.getDelivery());
        assertEquals(defaultAuthor, deliveryBean4.getNodeParticipant("author"));
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if a required element is missing.
     */
    @Test
    public void testRequiredElementMissing() {
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice1 = createInvoice();
        invoice1.getAccountingCustomerParty().getCustomerAssignedAccountID().setValue("abc");
        checkMappingException(invoice1, "ESCIA-0102: Invalid identifier: abc for "
                                        + "Invoice/AccountingCustomerParty/CustomerAssignedAccountID in Invoice: "
                                        + "12345");

        // check invalid identifier for InvoiceLine
        InvoiceType invoice2 = createInvoice();
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
        InvoiceType invoice = createInvoice();
        invoice.getUBLVersionID().setValue("2.1");
        checkMappingException(invoice, "ESCIA-0103: Expected 2.0 for UBLVersionID in Invoice: 12345 but got 2.1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an amount is invalid.
     */
    @Test
    public void testInvalidAmount() {
        InvoiceType invoice = createInvoice();
        invoice.getInvoiceLine().get(0).getPrice().getPriceAmount().setValue(new BigDecimal(-1));
        checkMappingException(invoice, "ESCIA-0104: Invalid amount: -1 for InvoiceLine/Price/PriceAmount in "
                                       + "InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an amount's currency doesn't match the practice currency.
     */
    @Test
    public void testInvalidCurrency() {
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
        invoice.getAccountingSupplierParty().getCustomerAssignedAccountID().setValue("0");
        checkMappingException(invoice, "ESCIA-0107: Invalid supplier: 0 referenced by Invoice: 12345, "
                                       + "element Invoice/AccountingSupplierParty/CustomerAssignedAccountID");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised a tax scheme is unrecognised.
     */
    @Test
    public void testInvalidTaxScheme() {
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getPayableAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0602: Calculated payable amount: 110 for Invoice: 12345 does not match "
                                       + "LegalMonetaryTotal/PayableAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the line extension amounts are inconsistent.
     */
    @Test
    public void testInvalidLineExtensionAmount() {
        InvoiceType invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getLineExtensionAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0603: Sum of InvoiceLine/LineExtensionAmount: 100 for Invoice: 12345 "
                                       + "does not match Invoice/LegalMonetaryTotal/LineExtensionAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if the tax totals are inconsistent.
     */
    @Test
    public void testInvalidTax() {
        InvoiceType invoice = createInvoice();
        TaxTotalType total = invoice.getTaxTotal().get(0);
        total.getTaxAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0604: Sum of InvoiceLine taxes and charge taxes: 10 for Invoice: 12345 "
                                       + "does not match TaxTotal/TaxAmount: 1");
    }

    /**
     * Verifies than an {@link ESCIAdapterException} is raised if a line extension amount for an InvoiceLine doesn't match
     * that expected.
     */
    @Test
    public void testInvalidItemLineExtensionAmount() {
        InvoiceType invoice = createInvoice();
        InvoiceLineType item = invoice.getInvoiceLine().get(0);
        item.getLineExtensionAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0605: Calculated line extension amount: 100 for InvoiceLine: 1 "
                                       + "does not match LineExtensionAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if an invoice references an order which was not sent to the
     * supplier.
     */
    @Test
    public void testInvalidOrder() {
        Party anotherSupplier = TestHelper.createSupplier();

        // create an order with a single item
        Act order = createOrder(anotherSupplier);

        InvoiceType invoice = createInvoice();
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

        InvoiceType invoice = createInvoice();
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

        InvoiceType invoice = createInvoice();
        invoice.setOrderReference(UBLHelper.createOrderReference(order.getId()));
        OrderLineReferenceType lineRef = new OrderLineReferenceType();
        lineRef.setLineID(UBLHelper.initID(new LineIDType(), invalid.getId()));
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice(anotherSupplier);
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice(supplier, stockLocation);
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();

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
        InvoiceType invoice = createInvoice();
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
        InvoiceType invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getTaxExclusiveAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0611: Calculated tax exclusive amount: 100 for Invoice: 12345 does not "
                                       + "match Invoice/LegalMonetaryTotal/TaxExcusiveAmount: 1");
    }

    /**
     * Serializes and deserializes an invoice to ensure its validitity.
     *
     * @param invoice the invoice
     * @return the deserialized invoice
     * @throws JAXBException for any JAXB exception
     * @throws SAXException  for any SAX exception
     */
    private InvoiceType serialize(InvoiceType invoice) throws JAXBException, SAXException {
        UBLDocumentContext context = new UBLDocumentContext();
        UBLDocumentWriter writer = context.createWriter();
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        writer.setFormat(true);
        writer.write(invoice, o);
        writer.write(invoice, System.out);
        UBLDocumentReader reader = context.createReader();
        return (InvoiceType) reader.read(new ByteArrayInputStream(o.toByteArray()));
    }

    /**
     * Maps an invalid invoice and verifies the expected exception is thrown.
     *
     * @param invoice         the invoice to map
     * @param expectedMessage the expected error message
     */
    private void checkMappingException(InvoiceType invoice, String expectedMessage) {
        checkMappingException(invoice, getSupplier(), expectedMessage);
    }

    /**
     * Maps an invalid invoice and verifies the expected exception is thrown.
     *
     * @param invoice         the invoice to map
     * @param supplier        the supplier submitting the invoice
     * @param expectedMessage the expected error message
     */
    private void checkMappingException(InvoiceType invoice, Party supplier, String expectedMessage) {
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
