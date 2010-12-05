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

import org.junit.Before;
import org.oasis.ubl.InvoiceType;
import org.oasis.ubl.common.AmountType;
import org.oasis.ubl.common.CurrencyCodeContentType;
import org.oasis.ubl.common.aggregate.AllowanceChargeType;
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.InvoiceLineType;
import org.oasis.ubl.common.aggregate.ItemIdentificationType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.MonetaryTotalType;
import org.oasis.ubl.common.aggregate.PriceType;
import org.oasis.ubl.common.aggregate.PricingReferenceType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.aggregate.TaxCategoryType;
import org.oasis.ubl.common.aggregate.TaxSchemeType;
import org.oasis.ubl.common.aggregate.TaxSubtotalType;
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.oasis.ubl.common.basic.AllowanceChargeReasonType;
import org.oasis.ubl.common.basic.ChargeIndicatorType;
import org.oasis.ubl.common.basic.ChargeTotalAmountType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.InvoicedQuantityType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.IssueTimeType;
import org.oasis.ubl.common.basic.LineExtensionAmountType;
import org.oasis.ubl.common.basic.PayableAmountType;
import org.oasis.ubl.common.basic.PriceAmountType;
import org.oasis.ubl.common.basic.PriceTypeCodeType;
import org.oasis.ubl.common.basic.TaxAmountType;
import org.oasis.ubl.common.basic.TaxExclusiveAmountType;
import org.oasis.ubl.common.basic.UBLVersionIDType;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.esci.adapter.map.UBLHelper;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.util.Date;


/**
 * Base class for invoice test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractInvoiceTest extends AbstractESCITest {

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

        // get the practice currency
        Party practice = getPractice();
        IMObjectBean bean = new IMObjectBean(practice);
        currency = CurrencyCodeContentType.fromValue(bean.getString("currency"));

        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException error) {
            throw new IllegalStateException(error);
        }

        Lookup taxType = TestHelper.getLookup("lookup.taxType", "GST");
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
     * Helper to create an <tt>Invoice</tt> with a single line item.
     *
     * @return a new <Tt>Invoice</tt>
     */
    protected InvoiceType createInvoice() {
        return createInvoice(getSupplier());
    }

    /**
     * Helper to create an <tt>Invoice</tt> with a single line item.
     *
     * @param supplier the supplier
     * @return a new <Tt>Invoice</tt>
     */
    protected InvoiceType createInvoice(Party supplier) {
        InvoiceType invoice = new InvoiceType();
        SupplierPartyType supplierType = createSupplier(supplier);
        CustomerPartyType customerType = createCustomer();
        Product product = TestHelper.createProduct();
        MonetaryTotalType monetaryTotal = createMonetaryTotal(new BigDecimal(100), BigDecimal.ZERO,
                                                              new BigDecimal(100), new BigDecimal(110));

        invoice.setUBLVersionID(UBLHelper.initID(new UBLVersionIDType(), "2.0"));
        invoice.setID(UBLHelper.createID(12345));
        Date issueDatetime = new Date();
        invoice.setIssueDate(UBLHelper.createIssueDate(issueDatetime, factory));
        invoice.setIssueTime(UBLHelper.createIssueTime(issueDatetime, factory));
        invoice.setAccountingSupplierParty(supplierType);
        invoice.setAccountingCustomerParty(customerType);
        invoice.setLegalMonetaryTotal(monetaryTotal);
        invoice.getTaxTotal().add(createTaxTotal(new BigDecimal(10), false));
        InvoiceLineType item1 = createInvoiceLine("1", product, "aproduct1", "aproduct name", new BigDecimal(105),
                                                  new BigDecimal(100), BigDecimal.ONE, new BigDecimal(100),
                                                  new BigDecimal(10));
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
     * @param listPrice           the list (or wholesale) price
     * @param price               the price
     * @param quantity            the quantity
     * @param lineExtensionAmount the line extension amount
     * @param tax                 the tax
     * @return a new <tt>InvoiceLineType</tt>
     */
    protected InvoiceLineType createInvoiceLine(String id, Product product, String supplierId, String supplierName,
                                                BigDecimal listPrice, BigDecimal price, BigDecimal quantity,
                                                BigDecimal lineExtensionAmount, BigDecimal tax) {
        InvoiceLineType result = new InvoiceLineType();
        result.setID(UBLHelper.createID(id));
        result.setInvoicedQuantity(UBLHelper.initQuantity(new InvoicedQuantityType(), quantity, "BX"));
        result.setLineExtensionAmount(initAmount(new LineExtensionAmountType(), lineExtensionAmount));
        result.setItem(createItem(product, supplierId, supplierName));
        result.setPrice(createPrice(price));
        PricingReferenceType pricingRef = new PricingReferenceType();
        PriceType priceType = createPrice(listPrice);
        priceType.setPriceTypeCode(UBLHelper.initCode(new PriceTypeCodeType(), "WS"));
        pricingRef.getAlternativeConditionPrice().add(priceType);
        result.setPricingReference(pricingRef);
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
        charge.setAmount(initAmount(new org.oasis.ubl.common.basic.AmountType(), amount));
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
        scheme.setID(UBLHelper.createID("GST"));
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
     * @return a new <tt>CustomerPartyType</tt>
     */
    protected CustomerPartyType createCustomer() {
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
    protected InvoiceMapperImpl createMapper() {
        InvoiceMapperImpl mapper = new InvoiceMapperImpl();
        mapper.setPracticeRules(new PracticeRules());
        mapper.setLookupService(LookupServiceHelper.getLookupService());
        mapper.setArchetypeService(getArchetypeService());
        mapper.setBeanFactory(new IMObjectBeanFactory(getArchetypeService()));
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