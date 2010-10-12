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
import org.oasis.ubl.common.aggregate.PriceType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.oasis.ubl.common.basic.BaseQuantityType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.InvoicedQuantityType;
import org.oasis.ubl.common.basic.LineExtensionAmountType;
import org.oasis.ubl.common.basic.PayableAmountType;
import org.oasis.ubl.common.basic.PriceAmountType;
import org.oasis.ubl.common.basic.TaxAmountType;
import org.oasis.ubl.common.basic.UBLVersionIDType;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.supplier.AbstractSupplierTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.esci.exception.ESCIException;
import org.openvpms.ubl.io.UBLDocumentContext;
import org.openvpms.ubl.io.UBLDocumentWriter;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
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

    private DatatypeFactory factory;

    private Party supplier;

    private Party stockLocation;

    private Product product1;

    private Product product2;

    private CurrencyCodeContentType currency;


    @Test
    public void testMap() throws SAXException, JAXBException {
        InvoiceType invoice = new InvoiceType();
        SupplierPartyType supplierType = createSupplier();
        CustomerPartyType customerType = createCustomer();
        MonetaryTotalType monetaryTotal = createLegalMonetaryTotal(new BigDecimal(154), new BigDecimal(140));

        invoice.setUBLVersionID(UBLHelper.initID(new UBLVersionIDType(), "2.0"));
        invoice.setID(UBLHelper.createID(12345));
        Date issueDatetime = new Date();
        invoice.setIssueDate(UBLHelper.createIssueDate(issueDatetime, factory));
        invoice.setIssueTime(UBLHelper.createIssueTime(issueDatetime, factory));
        invoice.setAccountingSupplierParty(supplierType);
        invoice.setAccountingCustomerParty(customerType);
        invoice.setLegalMonetaryTotal(monetaryTotal);
        invoice.getTaxTotal().add(createTaxTotal(new BigDecimal(14)));
        InvoiceLineType item1 = createInvoiceLine(product1, new BigDecimal(100), BigDecimal.ONE, new BigDecimal(100),
                                                  new BigDecimal(10));
        InvoiceLineType item2 = createInvoiceLine(product2, new BigDecimal(40), BigDecimal.ONE, new BigDecimal(40),
                                                  new BigDecimal(4));
        invoice.getInvoiceLine().add(item1);
        invoice.getInvoiceLine().add(item2);

        UBLDocumentContext context = new UBLDocumentContext();
        UBLDocumentWriter writer = context.createWriter();
        writer.setFormat(true);
        writer.write(invoice, System.out);
        InvoiceMapper mapper = createMapper();
        Delivery mapped = mapper.map(invoice);
        List<FinancialAct> acts = mapped.getActs();
        assertEquals(3, acts.size());
        getArchetypeService().save(acts);

        checkDelivery(mapped.getDelivery(), "12345", new BigDecimal(154), new BigDecimal(14));
        checkDeliveryItem(mapped.getDeliveryItems().get(0), new BigDecimal(110), new BigDecimal(10));
        checkDeliveryItem(mapped.getDeliveryItems().get(1), new BigDecimal(44), new BigDecimal(4));
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

    @Test
    public void testInvalidSupplier() {
        InvoiceType invoice = createInvoice();
        invoice.getAccountingSupplierParty().getCustomerAssignedAccountID().setValue("0");
        checkMappingException(invoice, "ESCIA-0103: Invalid supplier: 0 referenced by Invoice: 12345, "
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
        checkMappingException(invoice, "ESCIA-0104: Invalid stock location: 0 referenced by Invoice: 12345, element "
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
        checkMappingException(invoice, "ESCIA-0105: Neither Item/BuyersItemIdentification nor "
                                       + "Item/SellersItemIdentification provided in InvoiceLine: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the payable amount is incorrect.
     */
    @Test
    public void testInvalidPayableAmount() {
        InvoiceType invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getPayableAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0106: Calculated payable amount: 110 for Invoice: 12345 does not match "
                                       + "LegalMonetaryTotal/PayableAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the line extension amounts are inconsistent.
     */
    @Test
    public void testInvalidLineExtensionAmount() {
        InvoiceType invoice = createInvoice();
        invoice.getLegalMonetaryTotal().getLineExtensionAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0107: Sum of InvoiceLine/LineExtensionAmount: 100 for Invoice: 12345 "
                                       + "does not match Invoice/LegalMonetaryTotal/LineExtensionAmount: 1");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if the tax totals are inconsistent.
     */
    @Test
    public void testInvalidTax() {
        InvoiceType invoice = createInvoice();
        invoice.getTaxTotal().get(0).getTaxAmount().setValue(BigDecimal.ONE);
        checkMappingException(invoice, "ESCIA-0108: Sum of InvoiceLine/TaxTotal/TaxAmount: 10 for Invoice: 12345 "
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
        checkMappingException(invoice, "ESCIA-0109: Invalid LineExtensionAmount for InvoiceLine: 1. "
                                       + "Got 1 but expected 100");
    }

    /**
     * Verifies that an {@link ESCIException} is raised if an amount's currency doesn't match the practice currency.
     */
    @Test
    public void testInvalidCurrency() {
        InvoiceType invoice = createInvoice();
        InvoiceLineType item = invoice.getInvoiceLine().get(0);
        item.getLineExtensionAmount().setCurrencyID(CurrencyCodeContentType.USD);
        checkMappingException(invoice, "ESCIA-0110: Invalid currencyID for LineExtensionAmount in InvoiceLine: 1. "
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
        checkMappingException(invoice, "ESCIA-0111: InvoicedQuantity/unitCode: PK and BaseQuantity/unitCode: "
                                       + "BX must be the same when specified in InvoiceLine: 1");
    }

    private void checkMappingException(InvoiceType invoice, String expectedMessage) {
        InvoiceMapper mapper = createMapper();
        try {
            mapper.map(invoice);
            fail("Expected mapping to fail");
        } catch (ESCIException expected) {
            assertEquals(expectedMessage, expected.getMessage());
        }
    }

    private void checkDelivery(FinancialAct delivery, String invoiceId, BigDecimal total, BigDecimal tax) {
        ActBean bean = new ActBean(delivery);
        assertEquals(invoiceId, bean.getString("supplierInvoiceId"));
        checkEquals(total, delivery.getTotal());
        checkEquals(tax, delivery.getTaxAmount());
    }

    private void checkDeliveryItem(FinancialAct item, BigDecimal total, BigDecimal tax) {
        checkEquals(total, item.getTotal());
        checkEquals(tax, item.getTaxAmount());
    }

    /**
     * Helper to create an invoice with a single line item.
     *
     * @return a new invoice
     */
    private InvoiceType createInvoice() {
        InvoiceType invoice = new InvoiceType();
        SupplierPartyType supplierType = createSupplier();
        CustomerPartyType customerType = createCustomer();
        MonetaryTotalType monetaryTotal = createLegalMonetaryTotal(new BigDecimal(110), new BigDecimal(100));

        invoice.setUBLVersionID(UBLHelper.initID(new UBLVersionIDType(), "2.0"));
        invoice.setID(UBLHelper.createID(12345));
        Date issueDatetime = new Date();
        invoice.setIssueDate(UBLHelper.createIssueDate(issueDatetime, factory));
        invoice.setIssueTime(UBLHelper.createIssueTime(issueDatetime, factory));
        invoice.setAccountingSupplierParty(supplierType);
        invoice.setAccountingCustomerParty(customerType);
        invoice.setLegalMonetaryTotal(monetaryTotal);
        invoice.getTaxTotal().add(createTaxTotal(new BigDecimal(10)));
        InvoiceLineType item1 = createInvoiceLine(product1, new BigDecimal(100), BigDecimal.ONE, new BigDecimal(100),
                                                  new BigDecimal(10));
        invoice.getInvoiceLine().add(item1);
        return invoice;
    }

    private InvoiceLineType createInvoiceLine(Product product, BigDecimal price, BigDecimal quantity,
                                              BigDecimal lineExtensionAmount, BigDecimal tax) {
        InvoiceLineType result = new InvoiceLineType();
        result.setID(UBLHelper.createID(1)); // TODO - required by UBL
        result.setInvoicedQuantity(UBLHelper.initQuantity(new InvoicedQuantityType(), BigDecimal.ONE, "BX"));
        result.setLineExtensionAmount(UBLHelper.initAmount(new LineExtensionAmountType(), lineExtensionAmount, currency));
        result.setItem(createItem(product));
        result.setPrice(createPrice(price, quantity));
        result.getTaxTotal().add(createTaxTotal(tax));
        return result;
    }

    private ItemType createItem(Product product) {
        ItemType result = new ItemType();
        ItemIdentificationType buyerId = new ItemIdentificationType();
        buyerId.setID(UBLHelper.createID(product.getId()));
        result.setBuyersItemIdentification(buyerId);
        return result;
    }

    private PriceType createPrice(BigDecimal price, BigDecimal quantity) {
        PriceType result = new PriceType();
        result.setBaseQuantity(UBLHelper.initQuantity(new BaseQuantityType(), quantity, "BX"));
        result.setPriceAmount(UBLHelper.initAmount(new PriceAmountType(), price, currency));
        return result;
    }

    private TaxTotalType createTaxTotal(BigDecimal tax) {
        TaxTotalType result = new TaxTotalType();
        result.setTaxAmount(UBLHelper.initAmount(new TaxAmountType(), tax, currency));
        return result;
    }

    private MonetaryTotalType createLegalMonetaryTotal(BigDecimal payableAmount, BigDecimal lineExtesionAmount) {
        MonetaryTotalType result = new MonetaryTotalType();
        result.setPayableAmount(UBLHelper.initAmount(new PayableAmountType(), payableAmount, currency));
        result.setLineExtensionAmount(UBLHelper.initAmount(new LineExtensionAmountType(), lineExtesionAmount,
                                                           currency));
        return result;
    }

    private SupplierPartyType createSupplier() {
        SupplierPartyType supplierType = new SupplierPartyType();
        CustomerAssignedAccountIDType supplierId = UBLHelper.initID(new CustomerAssignedAccountIDType(),
                                                                    supplier.getId());
        supplierType.setCustomerAssignedAccountID(supplierId);
        return supplierType;
    }

    private CustomerPartyType createCustomer() {
        CustomerPartyType customerType = new CustomerPartyType();
        CustomerAssignedAccountIDType customerId = UBLHelper.initID(new CustomerAssignedAccountIDType(),
                                                                    stockLocation.getId());
        customerType.setCustomerAssignedAccountID(customerId);
        return customerType;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Party practice = TestHelper.getPractice();
        IMObjectBean bean = new IMObjectBean(practice);
        currency = CurrencyCodeContentType.fromValue(bean.getString("currency"));

        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException error) {
            throw new IllegalStateException(error);
        }
        supplier = TestHelper.createSupplier();
        stockLocation = createStockLocation();
        product1 = TestHelper.createProduct();
        product2 = TestHelper.createProduct();
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
