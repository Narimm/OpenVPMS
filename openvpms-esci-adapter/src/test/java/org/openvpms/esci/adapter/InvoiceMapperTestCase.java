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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.oasis.ubl.InvoiceType;
import org.oasis.ubl.common.CurrencyCodeContentType;
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.InvoiceLineType;
import org.oasis.ubl.common.aggregate.ItemIdentificationType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.MonetaryTotalType;
import org.oasis.ubl.common.aggregate.OrderLineReferenceType;
import org.oasis.ubl.common.aggregate.OrderReferenceType;
import org.oasis.ubl.common.aggregate.PriceType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.oasis.ubl.common.basic.BaseQuantityType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.InvoicedQuantityType;
import org.oasis.ubl.common.basic.LineExtensionAmountType;
import org.oasis.ubl.common.basic.LineIDType;
import org.oasis.ubl.common.basic.PayableAmountType;
import org.oasis.ubl.common.basic.PriceAmountType;
import org.oasis.ubl.common.basic.TaxAmountType;
import org.oasis.ubl.common.basic.UBLVersionIDType;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.supplier.AbstractSupplierTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.esci.exception.ESCIException;
import org.openvpms.ubl.io.UBLDocumentContext;
import org.openvpms.ubl.io.UBLDocumentWriter;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link InvoiceMapperImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceMapperTestCase extends AbstractSupplierTest {

    /**
     * XML data type factory.
     */
    private DatatypeFactory factory;

    /**
     * The practice-wide currency.
     */
    private CurrencyCodeContentType currency;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Party practice = getPractice();
        IMObjectBean bean = new IMObjectBean(practice);
        currency = CurrencyCodeContentType.fromValue(bean.getString("currency"));

        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException error) {
            throw new IllegalStateException(error);
        }
        Lookup lookup = TestHelper.getLookup("lookup.uom", "BOX");
        IMObjectBean lookupBean = new IMObjectBean(lookup);
        lookupBean.setValue("unitCode", "BX");
        save(lookup);
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
        BigDecimal quantity1 = BigDecimal.ONE;
        BigDecimal lineExtension1 = price1.multiply(quantity1);
        BigDecimal tax1 = lineExtension1.multiply(new BigDecimal("0.10"));
        BigDecimal total1 = lineExtension1.add(tax1);
        String reorder1 = "product1";
        String reorderDesc1 = "product1 name";

        Product product2 = TestHelper.createProduct();
        BigDecimal price2 = new BigDecimal(40);
        BigDecimal quantity2 = BigDecimal.ONE;
        BigDecimal lineExtension2 = price2.multiply(quantity2);
        BigDecimal tax2 = lineExtension2.multiply(new BigDecimal("0.10"));
        BigDecimal total2 = lineExtension2.add(tax2);
        String reorder2 = "product2";
        String reorderDesc2 = "product2 name";

        BigDecimal totalTax = tax1.add(tax2);
        BigDecimal total = total1.add(total2);

        InvoiceType invoice = new InvoiceType();
        SupplierPartyType supplierType = createSupplier(getSupplier());
        CustomerPartyType customerType = createCustomer();
        MonetaryTotalType monetaryTotal = createMonetaryTotal(new BigDecimal(154), new BigDecimal(140));

        invoice.setUBLVersionID(UBLHelper.initID(new UBLVersionIDType(), "2.0"));
        invoice.setID(UBLHelper.createID(12345));
        Date issueDatetime = new Date();
        invoice.setIssueDate(UBLHelper.createIssueDate(issueDatetime, factory));
        invoice.setIssueTime(UBLHelper.createIssueTime(issueDatetime, factory));
        invoice.setAccountingSupplierParty(supplierType);
        invoice.setAccountingCustomerParty(customerType);
        invoice.setLegalMonetaryTotal(monetaryTotal);
        invoice.getTaxTotal().add(createTaxTotal(new BigDecimal(14)));
        InvoiceLineType item1 = createInvoiceLine("1", product1, reorder1, reorderDesc1, price1, quantity1,
                                                  lineExtension1, tax1);
        InvoiceLineType item2 = createInvoiceLine("2", product2, reorder2, reorderDesc2, price2, quantity2,
                                                  lineExtension2, tax2);
        invoice.getInvoiceLine().add(item1);
        invoice.getInvoiceLine().add(item2);

        UBLDocumentContext context = new UBLDocumentContext();
        UBLDocumentWriter writer = context.createWriter();
        writer.setFormat(true);
        writer.write(invoice, System.out);
        InvoiceMapper mapper = createMapper();
        Delivery mapped = mapper.map(invoice, null);
        List<FinancialAct> acts = mapped.getActs();
        assertEquals(3, acts.size());
        getArchetypeService().save(acts);

        checkDelivery(mapped.getDelivery(), "12345", issueDatetime, total, totalTax);
        checkDeliveryItem(mapped.getDeliveryItems().get(0), "1", issueDatetime, quantity1, price1, "BOX", reorder1,
                          reorderDesc1, total1, tax1);
        checkDeliveryItem(mapped.getDeliveryItems().get(1), "2", issueDatetime, quantity2, price2, "BOX", reorder2,
                          reorderDesc2, total2, tax2);
    }

    /**
     * Verifies that an {@link ESCIException} is raised if a required element is missing.
     */
    @Test
    public void testRequiredElementMissing() {
        InvoiceType invoice = createInvoice();
        invoice.getLegalMonetaryTotal().setPayableAmount(null);
        checkMappingException(invoice, "ESCIA-0100: Required element: LegalMonetaryTotal/PayableAmount missing in "
                                       + "Invoice: 12345");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if a collection has a different no. of elements to that
     * expected.
     */
    @Test
    public void testInvalidCardinality() {
        InvoiceType invoice = createInvoice();
        invoice.getTaxTotal().add(new TaxTotalType());
        checkMappingException(invoice, "ESCIA-0101: Invalid cardinality for TaxTotal in Invoice: 12345. "
                                       + "Expected 1 but got 2");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if an ID element contains an invalid identifier
     * (i.e one that is non-numeric).
     */
    @Test
    public void testInvalidIdentifier() {
        // check invalid identifier for Invoice
        InvoiceType invoice1 = createInvoice();
        invoice1.getAccountingCustomerParty().getCustomerAssignedAccountID().setValue("abc");
        checkMappingException(invoice1, "ESCIA-0102: Invalid identifier: abc for "
                                        + "AccountingCustomerParty/CustomerAssignedAccountID in Invoice: 12345");

        // check invalid identifier for InvoiceLine
        InvoiceType invoice2 = createInvoice();
        InvoiceLineType item = invoice2.getInvoiceLine().get(0);
        item.getItem().getBuyersItemIdentification().getID().setValue("cde");
        checkMappingException(invoice2, "ESCIA-0102: Invalid identifier: cde for "
                                        + "Item/BuyersItemIdentification/ID in InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the UBL version doesn't match that expected.
     */
    @Test
    public void testInvalidUBLVersion() {
        InvoiceType invoice = createInvoice();
        invoice.getUBLVersionID().setValue("2.1");
        checkMappingException(invoice, "ESCIA-0103: Expected 2.0 for UBLVersionID in Invoice: 12345 but got 2.1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the
     * <em>Invoice/AccountingSupplierParty/CustomerAssignedAccountID</em> doesn't correspond to a valid supplier.
     */
    @Test
    public void testInvalidSupplier() {
        InvoiceType invoice = createInvoice();
        invoice.getAccountingSupplierParty().getCustomerAssignedAccountID().setValue("0");
        checkMappingException(invoice, "ESCIA-0300: Invalid supplier: 0 referenced by Invoice: 12345, "
                                       + "element AccountingSupplierParty/CustomerAssignedAccountID");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the Invoice/AccountingCustomerParty/CustomerAssignedAccountID
     * doesn't correspond to a valid stock location.
     */
    @Test
    public void testInvalidStockLocation() {
        InvoiceType invoice = createInvoice();
        invoice.getAccountingCustomerParty().getCustomerAssignedAccountID().setValue("0");
        checkMappingException(invoice, "ESCIA-0301: Invalid stock location: 0 referenced by Invoice: 12345, element "
                                       + "AccountingCustomerParty/CustomerAssignedAccountID");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if neither the BuyersItemIdentification nor
     * SellersItemIdentification are provided.
     */
    @Test
    public void testNoProduct() {
        InvoiceType invoice = createInvoice();
        InvoiceLineType item = invoice.getInvoiceLine().get(0);
        item.getItem().setBuyersItemIdentification(null);
        item.getItem().setSellersItemIdentification(null);
        checkMappingException(invoice, "ESCIA-0302: Neither Item/BuyersItemIdentification nor "
                                       + "Item/SellersItemIdentification provided in InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the payable amount is incorrect.
     */
    @Test
    public void testInvalidPayableAmount() {
        InvoiceType invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getPayableAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0303: Calculated payable amount: 110 for Invoice: 12345 does not match "
                                       + "LegalMonetaryTotal/PayableAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the line extension amounts are inconsistent.
     */
    @Test
    public void testInvalidLineExtensionAmount() {
        InvoiceType invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getLineExtensionAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0304: Sum of InvoiceLine/LineExtensionAmount: 100 for Invoice: 12345 "
                                       + "does not match Invoice/LegalMonetaryTotal/LineExtensionAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the tax totals are inconsistent.
     */
    @Test
    public void testInvalidTax() {
        InvoiceType invoice = createInvoice();
        invoice.getTaxTotal().get(0).getTaxAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0305: Sum of InvoiceLine/TaxTotal/TaxAmount: 10 for Invoice: 12345 "
                                       + "does not match TaxTotal/TaxAmount: 1");
    }

    /**
     * Verifies than an {@link ESCIException} is raised if a line extension amount for an InvoiceLine doesn't match
     * that expected.
     */
    @Test
    public void testInvalidItemLineExtensionAmount() {
        InvoiceType invoice = createInvoice();
        InvoiceLineType item = invoice.getInvoiceLine().get(0);
        item.getLineExtensionAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0306: Calculated line extension amount: 100 for InvoiceLine: 1 "
                                       + "does not match LineExtensionAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if an amount's currency doesn't match the practice currency.
     */
    @Test
    public void testInvalidCurrency() {
        InvoiceType invoice = createInvoice();
        InvoiceLineType item = invoice.getInvoiceLine().get(0);
        item.getLineExtensionAmount().setCurrencyID(CurrencyCodeContentType.USD);
        checkMappingException(invoice, "ESCIA-0307: Invalid currencyID for LineExtensionAmount in InvoiceLine: 1. "
                                       + "Expected AUD but got USD");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if both InvoicedQuantity and BaseQuantity unit codes are
     * specified but different.
     */
    @Test
    public void testMismatchUnitCodes() {
        InvoiceType invoice = createInvoice();
        InvoiceLineType item = invoice.getInvoiceLine().get(0);
        item.getInvoicedQuantity().setUnitCode("PK");
        item.getPrice().getBaseQuantity().setUnitCode("BX");
        checkMappingException(invoice, "ESCIA-0308: InvoicedQuantity/unitCode: PK and BaseQuantity/unitCode: "
                                       + "BX must be the same when specified in InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if an invoice references an order which was not sent to the
     * supplier.
     */
    @Test
    public void testInvalidOrder() {
        Party anotherSupplier = TestHelper.createSupplier();

        // create an order with a single item
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        Act order = createOrder(orderItem);
        save(order, orderItem);

        InvoiceType invoice = createInvoice(anotherSupplier);
        OrderReferenceType orderRef = new OrderReferenceType();
        orderRef.setID(UBLHelper.createID(order.getId()));
        invoice.setOrderReference(orderRef);
        checkMappingException(invoice, "ESCIA-0309: Invalid Order: " + order.getId() + " referenced by Invoice: 12345");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if an invoice references an order item which doesn't exist.
     */
    @Test
    public void testInvalidOrderItem() {
        // create an order with a single item
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        Act order = createOrder(orderItem);
        save(order, orderItem);

        InvoiceType invoice = createInvoice();
        OrderReferenceType orderRef = new OrderReferenceType();
        orderRef.setID(UBLHelper.createID(order.getId()));
        invoice.setOrderReference(orderRef);
        OrderLineReferenceType lineRef = new OrderLineReferenceType();
        lineRef.setLineID(UBLHelper.initID(new LineIDType(), "114"));
        invoice.getInvoiceLine().get(0).getOrderLineReference().add(lineRef);
        checkMappingException(invoice, "ESCIA-0310: Invalid OrderLine: 114 referenced by InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if an order item is referenced by an InvoiceLine, but there
     * is no document level OrderReference.
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
     * Maps an invalid invoice and verifies the expected exception is thrown.
     *
     * @param invoice         the invoice to map
     * @param expectedMessage the expected error message
     */
    private void checkMappingException(InvoiceType invoice, String expectedMessage) {
        InvoiceMapper mapper = createMapper();
        try {
            mapper.map(invoice, null);
            fail("Expected mapping to fail");
        } catch (ESCIException expected) {
            assertEquals(expectedMessage, expected.getMessage());
        }
    }

    /**
     * Verifies the contents of an <em>act.supplierDelivery</em>
     *
     * @param delivery  the delivery to check
     * @param invoiceId the expected supplier invoice identifier
     * @param startTime the expected start time
     * @param total     the expected total
     * @param tax       the expected tax
     */
    private void checkDelivery(FinancialAct delivery, String invoiceId, Date startTime, BigDecimal total,
                               BigDecimal tax) {
        ActBean bean = new ActBean(delivery);
        assertEquals(startTime, bean.getDate("startTime"));
        assertEquals(invoiceId, bean.getString("supplierInvoiceId"));
        checkEquals(total, bean.getBigDecimal("amount"));
        checkEquals(tax, bean.getBigDecimal("tax"));
    }

    /**
     * Verifies the contents of an <em>act.supplierDeliveryItem</em>
     *
     * @param item               the delivery item to check
     * @param id                 the expected supplier invoice line identifier
     * @param startTime          the expected start time
     * @param quantity           the expected quantity
     * @param unitPrice          the expected unit price
     * @param packageUnits       the expected package units
     * @param reorderCode        the expected re-order code
     * @param reorderDescription the expected re-order description
     * @param total              the expected total
     * @param tax                the expected tax
     */
    private void checkDeliveryItem(FinancialAct item, String id, Date startTime, BigDecimal quantity,
                                   BigDecimal unitPrice, String packageUnits, String reorderCode,
                                   String reorderDescription, BigDecimal total, BigDecimal tax) {
        ActBean bean = new ActBean(item);
        assertEquals(id, bean.getString("supplierInvoiceLineId"));
        assertEquals(startTime, bean.getDate("startTime"));
        checkEquals(quantity, bean.getBigDecimal("quantity"));
        checkEquals(unitPrice, bean.getBigDecimal("unitPrice"));
        assertEquals(packageUnits, bean.getString("packageUnits"));
        assertEquals(reorderCode, bean.getString("reorderCode"));
        assertEquals(reorderDescription, bean.getString("reorderDescription"));
        checkEquals(total, item.getTotal());
        checkEquals(tax, item.getTaxAmount());
    }

    /**
     * Helper to create an <tt>Invoice</tt> with a single line item.
     *
     * @return a new <Tt>Invoice</tt>
     */
    private InvoiceType createInvoice() {
        return createInvoice(getSupplier());
    }

    /**
     * Helper to create an <tt>Invoice</tt> with a single line item.
     *
     * @param supplier the supplier
     * @return a new <Tt>Invoice</tt>
     */
    private InvoiceType createInvoice(Party supplier) {
        InvoiceType invoice = new InvoiceType();
        SupplierPartyType supplierType = createSupplier(supplier);
        CustomerPartyType customerType = createCustomer();
        Product product = TestHelper.createProduct();
        MonetaryTotalType monetaryTotal = createMonetaryTotal(new BigDecimal(110), new BigDecimal(100));

        invoice.setUBLVersionID(UBLHelper.initID(new UBLVersionIDType(), "2.0"));
        invoice.setID(UBLHelper.createID(12345));
        Date issueDatetime = new Date();
        invoice.setIssueDate(UBLHelper.createIssueDate(issueDatetime, factory));
        invoice.setIssueTime(UBLHelper.createIssueTime(issueDatetime, factory));
        invoice.setAccountingSupplierParty(supplierType);
        invoice.setAccountingCustomerParty(customerType);
        invoice.setLegalMonetaryTotal(monetaryTotal);
        invoice.getTaxTotal().add(createTaxTotal(new BigDecimal(10)));
        InvoiceLineType item1 = createInvoiceLine("1", product, "aproduct1", "aproduct name", new BigDecimal(100),
                                                  BigDecimal.ONE, new BigDecimal(100), new BigDecimal(10));
        invoice.getInvoiceLine().add(item1);
        return invoice;
    }

    /**
     * Helper to create an <tt>InvoiceLineType</tt>.
     *
     * @param id                  the invoice line identifier
     * @param product             the product
     * @param supplierId          the supplier's identifier for the product
     * @param supplierName        the supplier's name for the product
     * @param price               the price
     * @param quantity            the quantity
     * @param lineExtensionAmount the line extension amount
     * @param tax                 the tax
     * @return a new <tt>InvoiceLineType</tt>
     */
    private InvoiceLineType createInvoiceLine(String id, Product product, String supplierId, String supplierName,
                                              BigDecimal price, BigDecimal quantity,
                                              BigDecimal lineExtensionAmount, BigDecimal tax) {
        InvoiceLineType result = new InvoiceLineType();
        result.setID(UBLHelper.createID(id));
        result.setInvoicedQuantity(UBLHelper.initQuantity(new InvoicedQuantityType(), quantity, "BX"));
        result.setLineExtensionAmount(UBLHelper.initAmount(new LineExtensionAmountType(), lineExtensionAmount,
                                                           currency));
        result.setItem(createItem(product, supplierId, supplierName));
        result.setPrice(createPrice(price, quantity));
        result.getTaxTotal().add(createTaxTotal(tax));
        return result;
    }

    /**
     * Helper to create an <tt>ItemType</tt> for a product.
     *
     * @param product      the product
     * @param supplierId   the supplier's identifier for the product
     * @param supplierName the supplier's name for the product
     * @return a new <tt>ItemType</tt>
     */
    private ItemType createItem(Product product, String supplierId, String supplierName) {
        ItemType result = new ItemType();
        ItemIdentificationType buyerId = new ItemIdentificationType();
        buyerId.setID(UBLHelper.createID(product.getId()));
        result.setBuyersItemIdentification(buyerId);

        ItemIdentificationType sellerId = new ItemIdentificationType();
        sellerId.setID(UBLHelper.createID(supplierId));
        result.setSellersItemIdentification(sellerId);
        result.setName(UBLHelper.createName(supplierName));
        return result;
    }

    /**
     * Helper to create a <tt>PriceType</tt>.
     *
     * @param price    the price
     * @param quantity the quantity
     * @return a new <tt>PriceType</tt>
     */
    private PriceType createPrice(BigDecimal price, BigDecimal quantity) {
        PriceType result = new PriceType();
        result.setBaseQuantity(UBLHelper.initQuantity(new BaseQuantityType(), quantity, "BX"));
        result.setPriceAmount(UBLHelper.initAmount(new PriceAmountType(), price, currency));
        return result;
    }

    /**
     * Helper to create a <tt>TaxTotalType</tt>.
     *
     * @param tax the tax amount
     * @return a new <tt>TaxTotalType</tt>
     */
    private TaxTotalType createTaxTotal(BigDecimal tax) {
        TaxTotalType result = new TaxTotalType();
        result.setTaxAmount(UBLHelper.initAmount(new TaxAmountType(), tax, currency));
        return result;
    }

    /**
     * Helper to create a <tt>MonetaryTotalType</tt>
     *
     * @param payableAmount       the payable amount
     * @param lineExtensionAmount the line extension amount
     * @return a new <tt>MonetaryTotalType</tt>
     */
    private MonetaryTotalType createMonetaryTotal(BigDecimal payableAmount, BigDecimal lineExtensionAmount) {
        MonetaryTotalType result = new MonetaryTotalType();
        result.setPayableAmount(UBLHelper.initAmount(new PayableAmountType(), payableAmount, currency));
        result.setLineExtensionAmount(UBLHelper.initAmount(new LineExtensionAmountType(), lineExtensionAmount,
                                                           currency));
        return result;
    }

    /**
     * Helper to create a <tt>SupplierPartyType</tt>.
     *
     * @param supplier the supplier
     * @return a new <tt>SupplierPartyType</tt>
     */
    private SupplierPartyType createSupplier(Party supplier) {
        SupplierPartyType supplierType = new SupplierPartyType();
        CustomerAssignedAccountIDType supplierId = UBLHelper.initID(new CustomerAssignedAccountIDType(),
                                                                    supplier.getId());
        supplierType.setCustomerAssignedAccountID(supplierId);
        return supplierType;
    }

    /**
     * Helper to create a <tt>CustomerPartyType</tt>.
     *
     * @return a new <tt>CustomerPartyType</tt>
     */
    private CustomerPartyType createCustomer() {
        CustomerPartyType customerType = new CustomerPartyType();
        CustomerAssignedAccountIDType customerId = UBLHelper.initID(new CustomerAssignedAccountIDType(),
                                                                    getStockLocation().getId());
        customerType.setCustomerAssignedAccountID(customerId);
        return customerType;
    }

    /**
     * Creates a new invoice mapper.
     *
     * @return a new mapper
     */
    private InvoiceMapperImpl createMapper() {
        InvoiceMapperImpl mapper = new InvoiceMapperImpl();
        mapper.setPracticeRules(new PracticeRules());
        mapper.setLookupService(LookupServiceHelper.getLookupService());
        mapper.setArchetypeService(getArchetypeService());
        mapper.setBeanFactory(new IMObjectBeanFactory(getArchetypeService()));
        return mapper;
    }

}
