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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.esci.adapter.map.invoice;

import org.junit.Before;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.ubl.common.AmountType;
import org.openvpms.esci.ubl.common.aggregate.AllowanceChargeType;
import org.openvpms.esci.ubl.common.aggregate.CustomerPartyType;
import org.openvpms.esci.ubl.common.aggregate.InvoiceLineType;
import org.openvpms.esci.ubl.common.aggregate.ItemIdentificationType;
import org.openvpms.esci.ubl.common.aggregate.ItemType;
import org.openvpms.esci.ubl.common.aggregate.MonetaryTotalType;
import org.openvpms.esci.ubl.common.aggregate.PriceType;
import org.openvpms.esci.ubl.common.aggregate.PricingReferenceType;
import org.openvpms.esci.ubl.common.aggregate.SupplierPartyType;
import org.openvpms.esci.ubl.common.aggregate.TaxCategoryType;
import org.openvpms.esci.ubl.common.aggregate.TaxSchemeType;
import org.openvpms.esci.ubl.common.aggregate.TaxSubtotalType;
import org.openvpms.esci.ubl.common.aggregate.TaxTotalType;
import org.openvpms.esci.ubl.common.basic.AllowanceChargeReasonType;
import org.openvpms.esci.ubl.common.basic.ChargeIndicatorType;
import org.openvpms.esci.ubl.common.basic.ChargeTotalAmountType;
import org.openvpms.esci.ubl.common.basic.CustomerAssignedAccountIDType;
import org.openvpms.esci.ubl.common.basic.InvoicedQuantityType;
import org.openvpms.esci.ubl.common.basic.IssueDateType;
import org.openvpms.esci.ubl.common.basic.IssueTimeType;
import org.openvpms.esci.ubl.common.basic.LineExtensionAmountType;
import org.openvpms.esci.ubl.common.basic.PayableAmountType;
import org.openvpms.esci.ubl.common.basic.PriceAmountType;
import org.openvpms.esci.ubl.common.basic.PriceTypeCodeType;
import org.openvpms.esci.ubl.common.basic.TaxAmountType;
import org.openvpms.esci.ubl.common.basic.TaxExclusiveAmountType;
import org.openvpms.esci.ubl.common.basic.TaxTypeCodeType;
import org.openvpms.esci.ubl.common.basic.UBLVersionIDType;
import org.openvpms.esci.ubl.invoice.Invoice;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.util.Date;


/**
 * Base class for invoice test cases.
 *
 * @author Tim Anderson
 */
public class AbstractInvoiceTest extends AbstractESCITest {

    /**
     * XML data type factory.
     */
    private DatatypeFactory factory;

    /**
     * The practice-wide currency.
     */
    private Currency currency;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();

        // get the practice currency
        Party practice = getPractice();
        IMObjectBean bean = new IMObjectBean(practice);
        Currencies currencies = new Currencies(getArchetypeService(), getLookupService());
        currency = currencies.getCurrency(bean.getString("currency"));

        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException error) {
            throw new IllegalStateException(error);
        }

        Lookup taxType = TestHelper.getLookup("lookup.taxType", "GST", false);
        IMObjectBean taxBean = new IMObjectBean(taxType);
        taxBean.setValue("taxScheme", "GST");
        taxBean.setValue("taxCategory", "S");
        taxBean.setValue("rate", new BigDecimal("10.00"));
        taxBean.save();

        // make sure there is a UN/CEFACT unit code mapping for BOX
        Lookup uom = TestHelper.getLookup("lookup.uom", "BOX");
        IMObjectBean uomBean = new IMObjectBean(uom);
        uomBean.setValue("unitCode", "BX");
        uomBean.save();
    }

    /**
     * Creates an order item.
     *
     * @param product     the product
     * @param quantity    the quantity
     * @param packageSize the package size
     * @param unitPrice   the unit price
     * @param reorderCode the supplier reorder code
     * @return a new order item
     */
    protected FinancialAct createOrderItem(Product product, BigDecimal quantity, int packageSize, BigDecimal unitPrice,
                                           String reorderCode) {
        FinancialAct result = createOrderItem(product, quantity, packageSize, unitPrice);
        ActBean bean = new ActBean(result);
        bean.setValue("reorderCode", reorderCode);
        return result;
    }

    /**
     * Helper to create an <tt>Invoice</tt> with a single line item.
     *
     * @return a new <Tt>Invoice</tt>
     */
    protected Invoice createInvoice() {
        return createInvoice(getSupplier());
    }

    /**
     * Helper to create an <tt>Invoice</tt> with a single line item.
     *
     * @param supplier the supplier
     * @return a new <Tt>Invoice</tt>
     */
    protected Invoice createInvoice(Party supplier) {
        return createInvoice(supplier, getStockLocation(), PACKAGE_UNIT_CODE);
    }

    /**
     * Helper to create an <tt>Invoice</tt> with a single line item.
     *
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @return a new <Tt>Invoice</tt>
     */
    protected Invoice createInvoice(Party supplier, Party stockLocation) {
        return createInvoice(supplier, stockLocation, PACKAGE_UNIT_CODE);
    }

    /**
     * Helper to create an <tt>Invoice</tt> with a single line item.
     *
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @param unitCode      the invoice quantity unit code
     * @return a new <Tt>Invoice</tt>
     */
    protected Invoice createInvoice(Party supplier, Party stockLocation, String unitCode) {
        Product product = getProduct();
        InvoiceLineType item1 = createInvoiceLine("1", product, "aproduct1", "aproduct name", new BigDecimal(105),
                                                  new BigDecimal(100), BigDecimal.ONE, unitCode, new BigDecimal(100),
                                                  new BigDecimal(10));

        return createInvoice(supplier, stockLocation, item1);
    }

    /**
     * Helper to create an <tt>Invoice</tt> with multiple line items.
     *
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @param lines         the invoice lines
     * @return a new <Tt>Invoice</tt>
     */
    protected Invoice createInvoice(Party supplier, Party stockLocation, InvoiceLineType... lines) {
        Invoice invoice = new Invoice();
        SupplierPartyType supplierType = createSupplier(supplier);
        CustomerPartyType customerType = createCustomer(stockLocation);

        invoice.setUBLVersionID(UBLHelper.initID(new UBLVersionIDType(), "2.0"));
        invoice.setID(UBLHelper.createID(12345));
        Date issueDatetime = new Date();
        invoice.setIssueDate(createIssueDate(issueDatetime));
        invoice.setIssueTime(createIssueTime(issueDatetime));
        invoice.setAccountingSupplierParty(supplierType);
        invoice.setAccountingCustomerParty(customerType);
        BigDecimal lineExtensionAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (InvoiceLineType line : lines) {
            lineExtensionAmount = lineExtensionAmount.add(line.getLineExtensionAmount().getValue());
            for (TaxTotalType tax : line.getTaxTotal()) {
                taxAmount = taxAmount.add(tax.getTaxAmount().getValue());
            }
            invoice.getInvoiceLine().add(line);
        }

        BigDecimal taxExAmount = lineExtensionAmount;
        BigDecimal payableAmount = lineExtensionAmount.add(taxAmount);
        MonetaryTotalType monetaryTotal = createMonetaryTotal(lineExtensionAmount, BigDecimal.ZERO, taxExAmount,
                                                              payableAmount);
        invoice.setLegalMonetaryTotal(monetaryTotal);
        invoice.getTaxTotal().add(createTaxTotal(taxAmount, false));
        return invoice;
    }

    /**
     * Helper to create an <tt>InvoiceLineType</tt>, that corresponds to an order item.
     *
     * @param id   the invoice line identifier
     * @param item the order item
     * @return a new <tt>InvoiceLineType</tt>
     */
    protected InvoiceLineType createInvoiceLine(String id, FinancialAct item) {
        ActBean bean = new ActBean(item);
        BigDecimal quantity = bean.getBigDecimal("quantity");
        return createInvoiceLine(id, quantity, item);
    }

    /**
     * Helper to create an <tt>InvoiceLineType</tt>, that corresponds to an order item with the exception of the
     * quantity.
     *
     * @param id       the invoice line identifier
     * @param quantity the quantity
     * @param item     the order item
     * @return a new <tt>InvoiceLineType</tt>
     */
    protected InvoiceLineType createInvoiceLine(String id, BigDecimal quantity, FinancialAct item) {
        ActBean bean = new ActBean(item);
        Product product = (Product) bean.getNodeParticipant("product");
        String supplierId = bean.getString("reorderCode");
        String supplierName = bean.getString("reorderDescription");
        BigDecimal listPrice = bean.getBigDecimal("listPrice");
        BigDecimal unitPrice = bean.getBigDecimal("unitPrice");
        String packageUnits = bean.getString("packageUnits");
        Lookup lookup = getLookupService().getLookup("lookup.uom", packageUnits);
        String unitCode = null;
        if (lookup != null) {
            IMObjectBean uom = new IMObjectBean(lookup);
            unitCode = uom.getString("unitCode");
        }
        return createInvoiceLine(id, product, supplierId, supplierName, listPrice, unitPrice, quantity, unitCode);
    }

    /**
     * Helper to create an <tt>InvoiceLineType</tt>.
     *
     * @param id           the invoice line identifier
     * @param product      the product
     * @param supplierId   the supplier's identifier for the product
     * @param supplierName the supplier's name for the product
     * @param listPrice    the list (or wholesale) price
     * @param price        the price
     * @param quantity     the quantity
     * @param unitCode     the invoiced quantity unit code
     * @return a new <tt>InvoiceLineType</tt>
     */
    protected InvoiceLineType createInvoiceLine(String id, Product product, String supplierId, String supplierName,
                                                BigDecimal listPrice, BigDecimal price, BigDecimal quantity,
                                                String unitCode) {
        BigDecimal lineExtensionAmount = price.multiply(quantity);
        BigDecimal tax = lineExtensionAmount.multiply(new BigDecimal("0.1"));
        return createInvoiceLine(id, product, supplierId, supplierName, listPrice, price, quantity, unitCode,
                                 lineExtensionAmount, tax);
    }

    /**
     * Helper to create an <tt>InvoiceLineType</tt>.
     *
     * @param id                  the invoice line identifier
     * @param product             the product
     * @param supplierId          the supplier's identifier for the product
     * @param supplierName        the supplier's name for the product
     * @param listPrice           the list (or wholesale) price. May be <tt>null</tt>
     * @param price               the price
     * @param quantity            the quantity
     * @param unitCode            the invoiced quantity unit code
     * @param lineExtensionAmount the line extension amount
     * @param tax                 the tax
     * @return a new <tt>InvoiceLineType</tt>
     */
    protected InvoiceLineType createInvoiceLine(String id, Product product, String supplierId, String supplierName,
                                                BigDecimal listPrice, BigDecimal price, BigDecimal quantity,
                                                String unitCode, BigDecimal lineExtensionAmount, BigDecimal tax) {
        InvoiceLineType result = new InvoiceLineType();
        result.setID(UBLHelper.createID(id));
        result.setInvoicedQuantity(UBLHelper.initQuantity(new InvoicedQuantityType(), quantity, unitCode));
        result.setLineExtensionAmount(initAmount(new LineExtensionAmountType(), lineExtensionAmount));
        result.setItem(createItem(product, supplierId, supplierName));
        result.setPrice(createPrice(price));
        if (listPrice != null) {
            PricingReferenceType pricingRef = new PricingReferenceType();
            PriceType priceType = createPrice(listPrice);
            priceType.setPriceTypeCode(UBLHelper.initCode(new PriceTypeCodeType(), "WH"));
            pricingRef.getAlternativeConditionPrice().add(priceType);
            result.setPricingReference(pricingRef);
        }
        result.getTaxTotal().add(createTaxTotal(tax, true));
        return result;
    }

    /**
     * Helper to create a charge.
     *
     * @param amount  the charge amount
     * @param tax     the tax in the amount
     * @param reason  the reason for the charge
     * @param taxRate the tax rate, as a percentage
     * @return the charge
     */
    protected AllowanceChargeType createCharge(BigDecimal amount, BigDecimal tax, String reason, BigDecimal taxRate) {
        AllowanceChargeType charge = new AllowanceChargeType();
        TaxCategoryType category = createTaxCategory(taxRate);
        charge.getTaxCategory().add(category);
        ChargeIndicatorType flag = new ChargeIndicatorType();
        flag.setValue(true);
        charge.setChargeIndicator(flag);
        charge.setAmount(initAmount(new org.openvpms.esci.ubl.common.basic.AmountType(), amount));
        TaxTotalType taxTotal = new TaxTotalType();
        taxTotal.setTaxAmount(initAmount(new TaxAmountType(), tax));
        charge.setTaxTotal(taxTotal);
        charge.setAllowanceChargeReason(UBLHelper.initText(new AllowanceChargeReasonType(), reason));
        return charge;
    }

    /**
     * Helper to create an <tt>ItemType</tt> for a product.
     *
     * @param product      the product
     * @param supplierId   the supplier's identifier for the product
     * @param supplierName the supplier's name for the product
     * @return a new <tt>ItemType</tt>
     */
    protected ItemType createItem(Product product, String supplierId, String supplierName) {
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
     * @param price the price
     * @return a new <tt>PriceType</tt>
     */
    protected PriceType createPrice(BigDecimal price) {
        PriceType result = new PriceType();
        result.setPriceAmount(initAmount(new PriceAmountType(), price));
        return result;
    }

    /**
     * Helper to create a <tt>MonetaryTotalType</tt>
     *
     * @param lineExtensionAmount the line extension amount
     * @param chargeAmount        the total charge amount
     * @param taxExAmount         the tax exclusive amount
     * @param payableAmount       the payable amount
     * @return a new <tt>MonetaryTotalType</tt>
     */
    protected MonetaryTotalType createMonetaryTotal(BigDecimal lineExtensionAmount, BigDecimal chargeAmount,
                                                    BigDecimal taxExAmount, BigDecimal payableAmount) {
        MonetaryTotalType result = new MonetaryTotalType();
        result.setPayableAmount(initAmount(new PayableAmountType(), payableAmount));
        result.setLineExtensionAmount(initAmount(new LineExtensionAmountType(), lineExtensionAmount));
        result.setChargeTotalAmount(initAmount(new ChargeTotalAmountType(), chargeAmount));
        result.setTaxExclusiveAmount(initAmount(new TaxExclusiveAmountType(), taxExAmount));
        return result;
    }

    /**
     * Helper to create a <tt>TaxTotalType</tt>.
     *
     * @param tax         the tax amount
     * @param addSubtotal if <tt>true</tt> add a sub-total
     * @return a new <tt>TaxTotalType</tt>
     */
    protected TaxTotalType createTaxTotal(BigDecimal tax, boolean addSubtotal) {
        TaxTotalType result = new TaxTotalType();
        result.setTaxAmount(initAmount(new TaxAmountType(), tax));
        if (addSubtotal) {
            TaxSubtotalType subtotal = new TaxSubtotalType();
            subtotal.setTaxAmount(initAmount(new TaxAmountType(), tax));
            TaxCategoryType category = createTaxCategory(new BigDecimal("10.00"));
            subtotal.setTaxCategory(category);
            result.getTaxSubtotal().add(subtotal);
        }
        return result;
    }

    /**
     * Helper to create a <tt>TaxCategoryType</tt>.
     *
     * @param taxRate the tax rate, as a percentage
     * @return a new <tt>TaxCategoryType</tt>
     */
    protected TaxCategoryType createTaxCategory(BigDecimal taxRate) {
        TaxCategoryType category = new TaxCategoryType();
        category.setID(UBLHelper.createID("S"));
        category.setPercent(UBLHelper.createPercent(taxRate));
        TaxSchemeType scheme = new TaxSchemeType();
        TaxTypeCodeType taxType = new TaxTypeCodeType();
        taxType.setValue("GST");
        scheme.setTaxTypeCode(taxType);
        category.setTaxScheme(scheme);
        return category;
    }

    /**
     * Helper to initalise a <tt>AmountType</tt>.
     *
     * @param amount the amount
     * @param value  the amount value
     * @return the amount
     */
    protected <T extends AmountType> T initAmount(T amount, BigDecimal value) {
        return UBLHelper.initAmount(amount, value, currency);
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
     * Creates a new invoice mapper.
     *
     * @return a new mapper
     */
    protected InvoiceMapperImpl createMapper() {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();
        InvoiceMapperImpl mapper = new InvoiceMapperImpl();
        mapper.setPracticeRules(new PracticeRules(service));
        mapper.setProductRules(new ProductRules(service));
        mapper.setLookupService(lookups);
        mapper.setCurrencies(new Currencies(service, lookups));
        mapper.setArchetypeService(service);
        mapper.setBeanFactory(new IMObjectBeanFactory(service));
        mapper.setSupplierRules(new SupplierRules(service));
        return mapper;
    }

    /**
     * Creates a new issue date.
     *
     * @param timestamp the timestamp to use
     * @return a new issue date
     */
    protected IssueDateType createIssueDate(Date timestamp) {
        return UBLHelper.createIssueDate(timestamp, factory);
    }

    /**
     * Creates a new issue time.
     *
     * @param timestamp the timestamp to use
     * @return a new issue time
     */
    protected IssueTimeType createIssueTime(Date timestamp) {
        return UBLHelper.createIssueTime(timestamp, factory);
    }

}
