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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.map.AbstractUBLMapper;
import org.openvpms.esci.adapter.map.ErrorContext;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.invoice.InvoiceType;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Maps UBL invoices to <em>act.supplierDelivery</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceMapperImpl extends AbstractUBLMapper implements InvoiceMapper {

    /**
     * The practice rules.
     */
    private PracticeRules practiceRules;

    /**
     * The lookup service.
     */
    private ILookupService lookupService;

    /**
     * The supplier rules.
     */
    private SupplierRules supplierRules;

    /**
     * The product rules.
     */
    private ProductRules productRules;

    /**
     * The currencies.
     */
    private Currencies currencies;

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * The package helper.
     */
    private PackageHelper packageHelper;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(InvoiceMapperImpl.class);

    /**
     * Default constructor.
     */
    public InvoiceMapperImpl() {
    }

    /**
     * Registers the practice rules.
     *
     * @param rules the practice rules
     */
    @Resource
    public void setPracticeRules(PracticeRules rules) {
        practiceRules = rules;
    }

    /**
     * Registers the lookup service.
     *
     * @param service the lookup service
     */
    @Resource
    public void setLookupService(ILookupService service) {
        lookupService = service;
    }

    /**
     * Registers the supplier rules.
     *
     * @param rules the supplier rules
     */
    @Resource
    public void setSupplierRules(SupplierRules rules) {
        supplierRules = rules;
    }

    /**
     * Registers the product rules.
     *
     * @param rules the product rules
     */
    @Resource
    public void setProductRules(ProductRules rules) {
        productRules = rules;
    }

    /**
     * Registers the currencies.
     *
     * @param currencies the currencies
     */
    @Resource
    public void setCurrencies(Currencies currencies) {
        this.currencies = currencies;
    }

    /**
     * Registers the bean factory.
     *
     * @param factory the bean factory
     */
    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * Maps an UBL invoice to an <em>act.supplierDelivery</em>.
     *
     * @param invoice       the invoice to map
     * @param supplier      the supplier that submitted the invoice
     * @param stockLocation the stock location
     * @param accountId     the supplier assigned account identifier. May be <tt>null</tt>
     * @return the acts produced in the mapping. The first element is always the <em>act.supplierDelivery</em>
     * @throws ESCIAdapterException if the invoice cannot be mapped
     * @throws OpenVPMSException    for any OpenVPMS error
     */
    public Delivery map(InvoiceType invoice, Party supplier, Party stockLocation, String accountId) {
        Delivery result = new Delivery();
        Currency practiceCurrency = UBLHelper.getCurrency(practiceRules, currencies, factory);
        packageHelper = new PackageHelper(productRules, lookupService, factory);
        TaxRates rates = new TaxRates(lookupService, factory);
        UBLInvoice wrapper = new UBLInvoice(invoice, practiceCurrency.getCode(), getArchetypeService(), supplierRules);
        String invoiceId = wrapper.getID();
        checkUBLVersion(wrapper);
        Date issueDatetime = wrapper.getIssueDatetime();
        String notes = wrapper.getNotes();
        wrapper.checkSupplier(supplier, accountId);
        wrapper.checkStockLocation(stockLocation, accountId);

        checkDuplicateInvoice(supplier, invoiceId, issueDatetime);

        InvoiceOrderMatcher matcher = new InvoiceOrderMatcher(getArchetypeService(), factory);
        MappingContext context = matcher.match(wrapper, supplier, stockLocation);

        result.setOrder(context.getDocumentOrder());

        BigDecimal payableAmount = wrapper.getPayableAmount();
        BigDecimal invoiceLineExtensionAmount = wrapper.getLineExtensionAmount();
        BigDecimal chargeTotal = wrapper.getChargeTotal();
        BigDecimal taxExclusiveAmount = wrapper.getTaxExclusiveAmount();
        BigDecimal taxTotal = wrapper.getTaxAmount();

        ActBean delivery = factory.createActBean(SupplierArchetypes.DELIVERY);
        delivery.setValue("startTime", issueDatetime);
        delivery.setValue("supplierNotes", notes);
        delivery.setValue("amount", payableAmount);
        delivery.setValue("tax", taxTotal);
        delivery.setValue("supplierInvoiceId", invoiceId);
        delivery.addNodeParticipation("supplier", supplier);
        delivery.addNodeParticipation("stockLocation", stockLocation);
        result.setDelivery((FinancialAct) delivery.getAct());
        List<InvoiceLineState> lines = context.getInvoiceLines();
        if (lines.isEmpty()) {
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                    "InvoiceLine", "Invoice", invoiceId, "1..*", 0));
        }
        BigDecimal itemTaxIncTotal;
        BigDecimal itemTaxExTotal = BigDecimal.ZERO;
        BigDecimal itemTax = BigDecimal.ZERO;
        BigDecimal itemLineExtensionAmount = BigDecimal.ZERO;
        BigDecimal itemCharge = BigDecimal.ZERO;

        for (InvoiceLineState state : lines) {
            UBLInvoiceLine line = state.getLine();
            BigDecimal amount = line.getLineExtensionAmount();
            FinancialAct item = mapInvoiceLine(state, issueDatetime, rates, context);
            getArchetypeService().deriveValues(item);
            delivery.addNodeRelationship("items", item);
            result.addDeliveryItem(item);

            itemLineExtensionAmount = itemLineExtensionAmount.add(amount);
            itemTax = itemTax.add(item.getTaxAmount());
            itemTaxExTotal = itemTaxExTotal.add(item.getTotal().subtract(item.getTaxAmount()));
        }
        for (FinancialAct relatedOrder : context.getOrders()) {
            delivery.addNodeRelationship("orders", relatedOrder);
        }
        Entity author = getAuthor(context);
        if (author != null) {
            delivery.addNodeParticipation("author", author);
        }

        // map any charges
        for (UBLAllowanceCharge allowanceCharge : wrapper.getAllowanceCharges()) {
            // only support charges at present
            FinancialAct item = mapCharge(allowanceCharge, issueDatetime, invoiceId, rates);
            getArchetypeService().deriveValues(item);
            delivery.addNodeRelationship("items", item);
            result.addDeliveryItem(item);

            itemTax = itemTax.add(item.getTaxAmount());
            itemCharge = itemCharge.add(item.getTotal()).subtract(item.getTaxAmount());
        }
        if (chargeTotal.compareTo(itemCharge) != 0) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invoiceInvalidChargeTotal(invoiceId, chargeTotal,
                                                                                         itemCharge));
        }

        itemTaxExTotal = itemTaxExTotal.add(itemCharge);
        itemTaxIncTotal = itemTaxExTotal.add(itemTax);

        if (taxTotal.compareTo(itemTax) != 0) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invoiceInvalidTax(invoiceId, taxTotal, itemTax));
        }
        if (itemLineExtensionAmount.compareTo(invoiceLineExtensionAmount) != 0) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invoiceInvalidLineExtensionAmount(
                    invoiceId, invoiceLineExtensionAmount, itemLineExtensionAmount));
        }
        if (itemTaxExTotal.compareTo(taxExclusiveAmount) != 0) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invoiceInvalidTaxExclusiveAmount(
                    invoiceId, taxExclusiveAmount, itemTaxExTotal));
        }
        if (payableAmount.compareTo(itemTaxIncTotal) != 0) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invoiceInvalidPayableAmount(invoiceId, payableAmount,
                                                                                           itemTaxIncTotal));
        }
        return result;
    }

    /**
     * Determines if the invoice is a duplicate. This is limited to checking against deliveries for the supplier around
     * the invoice time, as there are no key fields to query.
     * <p/>
     * This will detect simple duplication (e.g failure to acknowlege an invoice), but will fail to detect invoices
     * that have been re-issued on a different day.
     * <p/>
     * The latter can be detected by {@link #checkOrder} if:
     * <ul>
     * <li>the invoice refers to orders; and
     * <li>the invoice doesn't refer to different orders to those previously
     * </ul>
     * Ideally, we'd keep track of the identifiers of all received documents, and reject any duplicates that way,
     * but there is currently no facility to do this. TODO
     *
     * @param supplier      the supplier
     * @param invoiceId     the invoice identifier
     * @param issueDatetime the invoice issue timestamp
     * @throws ESCIAdapterException if the invoice is a duplicate
     */
    protected void checkDuplicateInvoice(Party supplier, String invoiceId, Date issueDatetime) {
        ArchetypeQuery query = new ArchetypeQuery(SupplierArchetypes.DELIVERY);
        query.add(Constraints.join("supplier")
                .add(Constraints.eq("entity", supplier.getObjectReference())));
        Date from = DateRules.getDate(issueDatetime);
        Date to = DateRules.getDate(DateRules.getDate(from, 1, DateUnits.DAYS));
        query.add(Constraints.between("startTime", from, to));
        IMObjectQueryIterator<FinancialAct> iter = new IMObjectQueryIterator<FinancialAct>(getArchetypeService(),
                                                                                           query);
        while (iter.hasNext()) {
            FinancialAct delivery = iter.next();
            ActBean bean = factory.createActBean(delivery);
            String supplierInvoiceId = bean.getString("supplierInvoiceId");
            if (ObjectUtils.equals(supplierInvoiceId, invoiceId)) {
                throw new ESCIAdapterException(ESCIAdapterMessages.duplicateInvoice(invoiceId, delivery.getId()));
            }
        }
    }

    /**
     * Verifies that the TaxTotal/TaxAmount corresponds to that of the TaxTotal/TaxSubtotal.
     * <p/>
     * This implementation only supports a single TaxTotal/Subtotal and expects the TaxTotal/Subtotal/TaxableAmount
     * to match that supplied.
     *
     * @param line  the invoice line
     * @param rates the tax rates
     * @throws ESCIAdapterException if the tax is incorrectly specified
     */
    protected void checkTax(UBLInvoiceLine line, TaxRates rates) {
        BigDecimal expectedTaxAmount = line.getTaxAmount();
        UBLTaxSubtotal subtotal = line.getTaxSubtotal();
        checkTax(subtotal, expectedTaxAmount, line.getLineExtensionAmount(), rates);
    }

    /**
     * Verfies that a tax subtotal matches that expected, and has a valid rate.
     *
     * @param subtotal          the subtotal
     * @param expectedTaxAmount the expected tax amount
     * @param rates             the tax rates
     * @param amount            the line extension amount
     * @throws ESCIAdapterException if the subtotal is invalid
     */
    protected void checkTax(UBLTaxSubtotal subtotal, BigDecimal expectedTaxAmount, BigDecimal amount, TaxRates rates) {
        if (subtotal != null) {
            BigDecimal taxAmount = subtotal.getTaxAmount();
            if (expectedTaxAmount.compareTo(taxAmount) != 0) {
                ErrorContext context = new ErrorContext(subtotal, "TaxAmount");
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                        context.getPath(), context.getType(), context.getID(), expectedTaxAmount.toString(),
                        taxAmount.toString()));
            }
            UBLTaxCategory category = subtotal.getTaxCategory();
            BigDecimal rate = checkTaxCategory(category, rates);
            BigDecimal divisor = BigDecimal.valueOf(100);
            if (taxAmount.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal calc = MathRules.divide(amount.multiply(rate), divisor, 2);
                if (calc.compareTo(expectedTaxAmount) != 0) {
                    ErrorContext context = new ErrorContext(subtotal, "TaxTotal/TaxAmount");
                    throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                            context.getPath(), context.getType(), context.getID(), calc.toString(),
                            expectedTaxAmount.toString()));
                }
            }
        } else if (expectedTaxAmount.compareTo(BigDecimal.ZERO) != 0) {
            ErrorContext context = new ErrorContext(subtotal.getParent(), "TaxTotal");
            throw new ESCIAdapterException(ESCIAdapterMessages.ublElementRequired(context.getPath(), context.getType(),
                                                                                  context.getID()));
        }
    }

    /**
     * Verifies that a tax category matches that expected.
     *
     * @param category the tax category
     * @param rates    the tax rates
     * @return the tax rate used
     * @throws ESCIAdapterException if the category is invalid
     */
    protected BigDecimal checkTaxCategory(UBLTaxCategory category, TaxRates rates) {
        String taxCategory = category.getID();
        BigDecimal rate = category.getTaxRate();
        String taxScheme = category.getTaxTypeCode();
        BigDecimal expectedRate = rates.getTaxRate(taxScheme, taxCategory);
        if (expectedRate == null) {
            ErrorContext context = new ErrorContext(category);
            throw new ESCIAdapterException(ESCIAdapterMessages.invalidTaxSchemeAndCategory(
                    context.getPath(), context.getType(), context.getID(), taxScheme, taxCategory));
        }
        if (expectedRate.compareTo(rate) != 0) {
            ErrorContext context = new ErrorContext(category, "Percent");
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                    context.getPath(), context.getType(), context.getID(), expectedRate.toString(), rate.toString()));
        }
        return rate;
    }

    /**
     * Returns the associated order item for an invoice line, if an order line reference is specified.
     * <p/>
     * If there is:
     * <ul>
     * <li>a document-level order reference, then all order lines must reference this order
     * <li>no document level order reference, order references must be fully qualified i.e must specify both
     * the order line and order
     * <li>a document-level order reference, but no order line reference, the first order item matching the invoice item
     * will be returned
     * <ul>
     *
     * @param line the invoice line
     * @return the corresponding order item, or <tt>null</tt> if none is present
     */
    protected FinancialAct mapOrderItem(InvoiceLineState line) {
        FinancialAct item = line.getOrderItem();
        Product invoiced = line.getProduct();
        if (item != null && invoiced != null) {
            // verify that the invoice item has the same product as ordered. If not, don't want to refer
            // to the order item in the delivery item.
            ActBean itemBean = factory.createActBean(item);
            IMObjectReference orderedProduct = itemBean.getNodeParticipantRef("product");
            if (!ObjectUtils.equals(orderedProduct, invoiced.getObjectReference())) {
                log.warn("Invoiced product doesn't refer to that ordered"); // TODO - log context
                item = null;
            }
        }
        return item;
    }

    /**
     * Maps an <tt>InvoiceLineType</tt> to an <em>act.supplierDeliveryItem</em>.
     *
     * @param lineState the invoice line state
     * @param startTime the invoice start time
     * @param rates     the tax rates
     * @param context   the mapping context
     * @return a new <em>act.supplierDeliveryItem</em> corresponding to the invoice line
     * @throws ESCIAdapterException      if the order wasn't submitted by the supplier
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected FinancialAct mapInvoiceLine(InvoiceLineState lineState, Date startTime, TaxRates rates,
                                          MappingContext context) {
        ActBean deliveryItem = factory.createActBean(SupplierArchetypes.DELIVERY_ITEM);

        UBLInvoiceLine line = lineState.getLine();
        BigDecimal quantity = line.getInvoicedQuantity();
        String invoicedUnitCode = line.getInvoicedQuantityUnitCode();
        checkPackQuantity(line, invoicedUnitCode);
        checkBaseQuantity(line, invoicedUnitCode);

        Party supplier = context.getSupplier();
        Product product = lineState.getProduct();
        BigDecimal lineExtensionAmount = line.getLineExtensionAmount();
        String reorderCode = line.getSellersItemID();
        String reorderDescription = line.getItemName();

        BigDecimal tax = line.getTaxAmount();

        FinancialAct orderItem = mapOrderItem(lineState);

        Package pkg = packageHelper.getPackage(orderItem, product, supplier);
        ProductSupplier ps = (pkg != null) ? pkg.getProductSupplier() : null;

        BigDecimal unitPrice = line.getPriceAmount();
        String packageUnits = getPackageUnits(invoicedUnitCode, pkg);
        int packageSize = getPackageSize(line, pkg);
        BigDecimal listPrice = getListPrice(line, product, ps, packageSize, packageUnits, context);

        BigDecimal calcLineExtensionAmount = unitPrice.multiply(quantity);
        if (calcLineExtensionAmount.compareTo(lineExtensionAmount) != 0) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invoiceLineInvalidLineExtensionAmount(
                    line.getID(), lineExtensionAmount, calcLineExtensionAmount));
        }
        checkTax(line, rates);

        deliveryItem.setValue("supplierInvoiceLineId", line.getID());
        deliveryItem.setValue("startTime", startTime);
        deliveryItem.setValue("quantity", quantity);
        deliveryItem.setValue("unitPrice", unitPrice);
        deliveryItem.setValue("listPrice", listPrice);
        deliveryItem.setValue("tax", tax);
        deliveryItem.setValue("packageUnits", packageUnits);
        deliveryItem.setValue("packageSize", packageSize);
        if (product != null) {
            deliveryItem.addNodeParticipation("product", product);
        }
        deliveryItem.setValue("reorderCode", reorderCode);
        deliveryItem.setValue("reorderDescription", reorderDescription);

        if (orderItem != null) {
            deliveryItem.addNodeRelationship("order", orderItem);
        }

        getArchetypeService().deriveValues(deliveryItem.getObject());
        return (FinancialAct) deliveryItem.getAct();
    }

    /**
     * Returns the list price.
     * <p/>
     * This is available as the wholesale price in the invoice line.
     * <p/>
     * If it is not present:
     * <ol>
     * <li>it will be defaulted to that from <tt>ps</tt>, if available
     * <li>if ps is not present or the value is 0, it will be defaulted to the unit price from the invoice line.
     * </ol>
     *
     * @param line         the invoice line
     * @param product      the product. May be <tt>null</tt>
     * @param ps           the product/supplier relationship. May be <tt>null</tt>
     * @param packageSize  the package size
     * @param packageUnits the package units
     * @param context      the mapping context
     * @return the list price
     */
    private BigDecimal getListPrice(UBLInvoiceLine line, Product product, ProductSupplier ps,
                                    int packageSize, String packageUnits, MappingContext context) {
        BigDecimal listPrice = line.getWholesalePrice();
        if (listPrice != null && listPrice.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Received 0.0 list price from supplier.");
            listPrice = null;
        }
        if (listPrice == null) {
            if (ps == null && product != null) {
                ps = productRules.getProductSupplier(product, context.getSupplier(), packageSize, packageUnits);
            }
            if (ps != null) {
                listPrice = ps.getListPrice();
            }
            if (listPrice == null || listPrice.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Product/supplier list price is 0.0");
            }
        }
        if (listPrice == null || listPrice.compareTo(BigDecimal.ZERO) == 0) {
            // no list price in the invoice line, nor from a product/supplier. Default to the unit price
            listPrice = line.getPriceAmount();
        }
        return listPrice;
    }

    /**
     * Returns the package units.
     *
     * @param invoicedUnitCode the invoiced quantity unit code
     * @param pkg              the package information. May be <tt>null</tt>
     * @return the package units, or <tt>null</tt> if they are not known
     */
    private String getPackageUnits(String invoicedUnitCode, Package pkg) {
        String result = null;
        String expected = (pkg != null) ? pkg.getPackageUnits() : null;

        List<String> matches = packageHelper.getPackageUnits(invoicedUnitCode);
        if (expected != null) {
            if (!matches.contains(expected)) {
                log.warn("Invoice package units (" + StringUtils.join(matches.iterator(), ", ") // TODO - log context
                         + ") don't match that expected: " + expected);
            }
            result = expected;
        } else if (matches.size() == 1) {
            result = matches.get(0);
        } else if (matches.size() > 1) {
            log.warn("Cannot determine package units. " + matches.size()       // TODO - log context
                     + " package units match unit code: " + invoicedUnitCode);
        }
        return result;
    }

    /**
     * Returns the package size.
     *
     * @param line the invoice line
     * @param pkg  the expected package, or <tt>null</tt> if it is not known
     * @return the package size, or <tt>0</tt> if it is not known
     * @throws ESCIAdapterException if the package size is incorrectly specified
     */
    private int getPackageSize(UBLInvoiceLine line, Package pkg) {
        int result;
        BigDecimal packageSize = line.getPackSizeNumeric();
        int expectedSize = (pkg != null) ? pkg.getPackageSize() : 0;
        int invoiceSize;
        try {
            invoiceSize = packageSize.intValueExact();
        } catch (ArithmeticException exception) {
            ErrorContext context = new ErrorContext(line, "PackSizeNumeric");
            String intValue = Integer.toString(packageSize.intValue());
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                    context.getPath(), context.getType(), context.getID(), intValue, packageSize.toString()));
        }
        if (expectedSize != 0) {
            if (invoiceSize != 0 && invoiceSize != expectedSize) {
                log.warn("Different package size received for invoice. Expected package size=" + expectedSize
                         + ", invoiced package size=" + invoiceSize); // TODO - log context
            }
            result = expectedSize;
        } else {
            result = invoiceSize;
        }
        return result;
    }

    /**
     * Verifies that the invoice line item's <em>PackQuantity</em> is specified correctly, if present.
     *
     * @param line     the invoice line
     * @param unitCode the expected unit code
     */
    private void checkPackQuantity(UBLInvoiceLine line, String unitCode) {
        BigDecimal quantity = line.getPackQuantity();
        if (quantity != null) {
            if (quantity.compareTo(BigDecimal.ONE) != 0) {
                ErrorContext context = new ErrorContext(line, "PackQuantity");
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                        context.getPath(), context.getType(), context.getID(), "1", quantity.toString()));
            }
            String packageUnits = line.getPackQuantityUnitCode();
            if (packageUnits != null && !ObjectUtils.equals(unitCode, packageUnits)) {
                ErrorContext context = new ErrorContext(line, "PackQuantity@unitCode");
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                        context.getPath(), context.getType(), context.getID(), unitCode, packageUnits));
            }
        }
    }

    /**
     * Verifies that the invoice line's <em>BaseQuantity</em> is specified correctly, if present.
     *
     * @param line     the invoice line
     * @param unitCode the expected unit code
     */
    private void checkBaseQuantity(UBLInvoiceLine line, String unitCode) {
        BigDecimal quantity = line.getBaseQuantity();
        if (quantity != null) {
            if (quantity.compareTo(BigDecimal.ONE) != 0) {
                ErrorContext context = new ErrorContext(line, "BaseQuantity");
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                        context.getPath(), context.getType(), context.getID(), "1", quantity.toString()));
            }
            String baseQuantityUnitCode = line.getBaseQuantityUnitCode();
            if (!StringUtils.equals(unitCode, baseQuantityUnitCode)) {
                ErrorContext context = new ErrorContext(line, "BaseQuantity@unitCode");
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                        context.getPath(), context.getType(), context.getID(), unitCode, baseQuantityUnitCode));
            }
        }
    }

    /**
     * Maps a charge to a delivery item.
     *
     * @param charge    the allowance/charge
     * @param startTime the invoice start time
     * @param invoiceId the invoice identifier
     * @param rates     the tax rates
     * @return a new delivery item
     * @throws ESCIAdapterException if the allowance/charge cannot be mapped
     */
    protected FinancialAct mapCharge(UBLAllowanceCharge charge, Date startTime, String invoiceId, TaxRates rates) {
        if (!charge.isCharge()) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invoiceAllowanceNotSupported(invoiceId));
        }
        BigDecimal unitPrice = charge.getAmount();
        ActBean deliveryItem = factory.createActBean(SupplierArchetypes.DELIVERY_ITEM);
        BigDecimal tax = charge.getTaxAmount();
        BigDecimal rate = checkTaxCategory(charge.getTaxCategory(), rates);
        BigDecimal divisor = BigDecimal.valueOf(100);
        if (tax.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal expectedTax = MathRules.divide(unitPrice.multiply(rate), divisor, 2);
            if (expectedTax.compareTo(tax) != 0) {
                ErrorContext context = new ErrorContext(charge, "TaxTotal/TaxAmount");
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                        context.getPath(), context.getType(), context.getID(), expectedTax.toString(), tax.toString()));
            }
        }
        deliveryItem.setValue("startTime", startTime);
        deliveryItem.setValue("quantity", BigDecimal.ONE);
        deliveryItem.setValue("packageUnits", null); // override default
        deliveryItem.setValue("unitPrice", unitPrice);
        deliveryItem.setValue("tax", tax);
        deliveryItem.setValue("reorderDescription", charge.getAllowanceChargeReason());  // TODO - not ideal

        getArchetypeService().deriveValues(deliveryItem.getObject());
        return (FinancialAct) deliveryItem.getAct();
    }

    /**
     * Verifies that the UBL version matches that expected.
     *
     * @param invoice the invoice
     */
    protected void checkUBLVersion(UBLInvoice invoice) {
        if (!UBL_VERSION.equals(invoice.getUBLVersionID())) {
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                    "UBLVersionID", "Invoice", invoice.getID(), UBL_VERSION, invoice.getUBLVersionID()));
        }
    }

    /**
     * Returns an author to associate with the delivery.
     * <p/>
     * This returns the author of the original order, if present. If not, it returns that from the stock location.
     *
     * @param context the mapping context
     * @return the author reference, or <tt>null</tt> if none is available
     */
    private Entity getAuthor(MappingContext context) {
        Entity result = null;
        FinancialAct order = context.getDocumentOrder();
        if (order == null) {
            // no primary order. If there is ony one secondary order, use that (which is essentially the primary order)
            List<FinancialAct> orders = context.getOrders();
            if (orders.size() == 1) {
                order = orders.get(0);
            }
        }

        if (order != null) {
            ActBean bean = factory.createActBean(order);
            result = bean.getNodeParticipant("author");
        }
        if (result == null) {
            EntityBean bean = factory.createEntityBean(context.getStockLocation());
            result = bean.getNodeTargetEntity("defaultAuthor");
        }
        return result;
    }

}
