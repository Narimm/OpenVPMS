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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.oasis.ubl.InvoiceType;
import org.oasis.ubl.common.AmountType;
import org.oasis.ubl.common.CurrencyCodeContentType;
import org.oasis.ubl.common.IdentifierType;
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
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.InvoicedQuantityType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.IssueTimeType;
import org.oasis.ubl.common.basic.LineIDType;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.exception.ESCIException;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Maps UBL invoices to <em>act.supplierDelivery</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceMapperImpl implements InvoiceMapper {

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
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * Order archetype id.
     */
    private static final ArchetypeId ORDER = new ArchetypeId(SupplierArchetypes.ORDER);

    /**
     * Order item archetype id.
     */
    private static final ArchetypeId ORDER_ITEM = new ArchetypeId(SupplierArchetypes.ORDER_ITEM);

    /**
     * Stock location archetype id.
     */
    private static final ArchetypeId STOCK_LOCATION = new ArchetypeId(StockArchetypes.STOCK_LOCATION);

    private static final String UBL_VERSION = "2.0";


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
     * Registers the archetype service.
     *
     * @param service the archetype service
     */
    @Resource
    public void setArchetypeService(IArchetypeService service) {
        this.service = service;
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
     * @param invoice the invoice to map
     * @return the acts produced in the mapping. The first element is always the <em>act.supplierDelivery</em>
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the invoice cannot be mapped
     * @throws org.openvpms.component.system.common.exception.OpenVPMSException
     *          for any OpenVPMS error
     */
    public Delivery map(InvoiceType invoice) {
        Delivery result = new Delivery();
        String invoiceId = getInvoiceId(invoice);
        checkUBLVersion(invoice, invoiceId);
        OrderReferenceType orderReference = invoice.getOrderReference();
        if (orderReference != null) {
            IMObjectReference ref = getReference(ORDER, orderReference.getID(), "OrderReference", "Invoice", invoiceId);
            result.setOrder(ref);
        }
        Date issueDatetime = getIssueDatetime(invoice, invoiceId);
        Party supplier = getSupplier(invoice, invoiceId);
        Party stockLocation = getStockLocation(invoice, invoiceId);
        MonetaryTotalType monetaryTotal = invoice.getLegalMonetaryTotal();
        if (monetaryTotal == null) {
            Message message = ESCIAdapterMessages.ublElementRequired("LegalMonetaryTotal", "Invoice", invoiceId);
            throw new ESCIException(message.toString());
        }
        String practiceCurrency = UBLHelper.getCurrencyCode(practiceRules, factory);
        BigDecimal payableAmount = getAmount(monetaryTotal.getPayableAmount(), practiceCurrency,
                                             "LegalMonetaryTotal/PayableAmount", "Invoice", invoiceId);
        BigDecimal invoiceLineExtensionAmount = getAmount(monetaryTotal.getLineExtensionAmount(), practiceCurrency,
                                                          "LegalMonetaryTotal/LineExtensionAmount",
                                                          "Invoice", invoiceId);
        BigDecimal tax = getTax(invoice, practiceCurrency, invoiceId);

        ActBean delivery = factory.createActBean(SupplierArchetypes.DELIVERY);
        delivery.setValue("startTime", issueDatetime);
        delivery.setValue("amount", payableAmount);
        delivery.setValue("tax", tax);
        delivery.setValue("supplierInvoiceId", invoiceId);
        delivery.addNodeParticipation("supplier", supplier);
        delivery.addNodeParticipation("stockLocation", stockLocation);

        result.setDelivery((FinancialAct) delivery.getAct());
        List<InvoiceLineType> lines = invoice.getInvoiceLine();
        if (lines == null || lines.isEmpty()) {
            Message message = ESCIAdapterMessages.ublInvalidCardinality("InvoiceLine", "Invoice", invoiceId, "1..*",
                                                                        0);
            throw new ESCIException(message.toString());
        }
        BigDecimal itemTotal = BigDecimal.ZERO;
        BigDecimal itemTax = BigDecimal.ZERO;
        BigDecimal itemLineExtensionAmount = BigDecimal.ZERO;
        for (InvoiceLineType line : lines) {
            String lineId = getInvoiceLineId(line);
            BigDecimal amount = getAmount(line.getLineExtensionAmount(), practiceCurrency,
                                          "LineExtensionAmount", "InvoiceLine", lineId);
            FinancialAct item = map(line, issueDatetime, practiceCurrency, supplier, lineId);
            service.deriveValues(item);
            delivery.addNodeRelationship("items", item);
            result.addDeliveryItem(item);
            itemLineExtensionAmount = itemLineExtensionAmount.add(amount);
            itemTax = itemTax.add(item.getTaxAmount());
            itemTotal = itemTotal.add(item.getTotal());

            IMObjectReference orderItem = getOrderItem(line, lineId);
            if (orderItem != null) {
                result.addDeliveryOrderMapping(item, orderItem);
            }
        }
        if (payableAmount.compareTo(itemTotal) != 0) {
            Message message = ESCIAdapterMessages.invoiceInvalidPayableAmount(invoiceId, payableAmount, itemTotal);
            throw new ESCIException(message.toString());
        }
        if (tax.compareTo(itemTax) != 0) {
            Message message = ESCIAdapterMessages.invoiceInvalidTax(invoiceId, tax, itemTax);
            throw new ESCIException(message.toString());
        }
        if (itemLineExtensionAmount.compareTo(invoiceLineExtensionAmount) != 0) {
            Message message = ESCIAdapterMessages.invoiceInvalidLineExtensionAmount(
                    invoiceId, invoiceLineExtensionAmount, itemLineExtensionAmount);
            throw new ESCIException(message.toString());
        }
        return result;
    }

    /**
     * Returns the associated order item for an invoice line.
     *
     * @param line          the invoice line
     * @param invoiceLineId the invoice line identifier
     * @return the corresponding order item reference, or <tt>null</tt> if none is present
     * @throws ESCIException if the order reference was inccrrectly specified
     */
    protected IMObjectReference getOrderItem(InvoiceLineType line, String invoiceLineId) {
        IMObjectReference result = null;
        List<OrderLineReferenceType> list = line.getOrderLineReference();
        if (list != null && !list.isEmpty()) {
            if (list.size() != 1) {
                Message message = ESCIAdapterMessages.ublInvalidCardinality("OrderLineReference", "InvoiceLine",
                                                                            invoiceLineId, "1", list.size());
                throw new ESCIException(message.toString());
            }
            LineIDType id = list.get(0).getLineID();
            result = getReference(ORDER_ITEM, id, "OrderLineReference/LineID", "InvoiceLine", invoiceLineId);
        }
        return result;
    }

    /**
     * Returns the invoice supplier.
     *
     * @param invoice   the invoice
     * @param invoiceId the invoice identifier
     * @return the supplier
     * @throws ESCIException if the supplier was not found
     */
    protected Party getSupplier(InvoiceType invoice, String invoiceId) {
        SupplierPartyType supplierType = invoice.getAccountingSupplierParty();
        if (supplierType == null) {
            Message message = ESCIAdapterMessages.ublElementRequired("AccountingSupplierParty", "Invoice",
                                                                     invoiceId);
            throw new ESCIException(message.toString());
        }
        CustomerAssignedAccountIDType accountId = supplierType.getCustomerAssignedAccountID();
        long id = getNumericId(accountId, "AccountingSupplierParty/CustomerAssignedAccountID", "Invoice", invoiceId);
        Party supplier = (Party) getObject(id, "party.supplier*");
        if (supplier == null) {
            Message message = ESCIAdapterMessages.invoiceInvalidSupplier(
                    "AccountingSupplierParty/CustomerAssignedAccountID", invoiceId, accountId.getValue());
            throw new ESCIException(message.toString());
        }
        return supplier;
    }

    /**
     * Returns the invoice stock location.
     *
     * @param invoice   the invoice
     * @param invoiceId the invoice identifier
     * @return the stock location
     * @throws ESCIException if the stock location was not found
     */
    protected Party getStockLocation(InvoiceType invoice, String invoiceId) {
        CustomerPartyType customerType = invoice.getAccountingCustomerParty();
        if (customerType == null) {
            Message message = ESCIAdapterMessages.ublElementRequired("AccountingCustomerParty", "Invoice",
                                                                     invoiceId);
            throw new ESCIException(message.toString());
        }
        CustomerAssignedAccountIDType accountId = customerType.getCustomerAssignedAccountID();
        IMObjectReference ref = getReference(STOCK_LOCATION, accountId,
                                             "AccountingCustomerParty/CustomerAssignedAccountID", "Invoice", invoiceId);
        Party location = (Party) service.get(ref);
        if (location == null) {
            Message message = ESCIAdapterMessages.invoiceInvalidStockLocation(
                    "AccountingCustomerParty/CustomerAssignedAccountID", invoiceId, accountId.getValue());
            throw new ESCIException(message.toString());
        }
        return location;
    }

    /**
     * Maps an invoice line to an <em>act.supplierDeliveryItem</em>.
     *
     * @param line             the invoice line
     * @param startTime        the invoice start time
     * @param practiceCurrency the practice currency. All amounts must be expressed in this currency
     * @param supplier         the supplier
     * @param invoiceLineId    the invoice line identifier
     * @return a new <em>act.supplierDeliveryItem</em> corresponding to the invoice line
     */
    protected FinancialAct map(InvoiceLineType line, Date startTime, String practiceCurrency, Party supplier,
                               String invoiceLineId) {
        ActBean deliveryItem = factory.createActBean(SupplierArchetypes.DELIVERY_ITEM);
        InvoicedQuantityType invoicedQuantity = line.getInvoicedQuantity();
        BigDecimal quantity = BigDecimal.ONE;
        String invoicedUnitCode = null;

        if (invoicedQuantity != null) {
            quantity = invoicedQuantity.getValue();
            invoicedUnitCode = invoicedQuantity.getUnitCode();
        }
        Product product = getProduct(line, supplier, invoiceLineId);
        BigDecimal lineExtensionAmount = getAmount(line.getLineExtensionAmount(), practiceCurrency,
                                                   "LineExtensionAmount", "InvoiceLine", invoiceLineId);
        ItemType item = line.getItem();
        if (item == null) {
            Message message = ESCIAdapterMessages.ublElementRequired("Item", "InvoiceLine", invoiceLineId);
            throw new ESCIException(message.toString());
        }
        String reorderCode = getSellerItemId(item);
        String reorderDescription = getSellerItemName(item);
        PriceType price = line.getPrice();
        if (price == null) {
            Message message = ESCIAdapterMessages.ublElementRequired("Price", "InvoiceLine", invoiceLineId);
            throw new ESCIException(message.toString());
        }
        BigDecimal unitPrice = getAmount(price.getPriceAmount(), practiceCurrency, "Price/PriceAmount",
                                         "InvoiceLine", invoiceLineId);
        BigDecimal tax = getTax(line, practiceCurrency, invoiceLineId);
        BaseQuantityType baseQuantity = price.getBaseQuantity();
        String packageUnits = null;
        if (baseQuantity != null) {
            String baseUnitCode = baseQuantity.getUnitCode();
            if (baseUnitCode != null) {
                if (invoicedUnitCode != null && !StringUtils.equals(baseUnitCode, invoicedUnitCode)) {
                    Message message = ESCIAdapterMessages.invoiceUnitCodeMismatch(invoiceLineId, invoicedUnitCode,
                                                                                  baseUnitCode);
                    throw new ESCIException(message.toString());
                }
                packageUnits = UBLHelper.getUnitOfMeasure(baseUnitCode, lookupService, factory);
            }
        }
        BigDecimal calcLineExtensionAmount = unitPrice.multiply(quantity);
        if (calcLineExtensionAmount.compareTo(lineExtensionAmount) != 0) {
            Message message = ESCIAdapterMessages.invoiceLineInvalidLineExtensionAmount(
                    invoiceLineId, lineExtensionAmount, calcLineExtensionAmount);
            throw new ESCIException(message.toString());
        }

        deliveryItem.setValue("supplierInvoiceLineId", invoiceLineId);
        deliveryItem.setValue("startTime", startTime);
        deliveryItem.setValue("quantity", quantity);
        deliveryItem.setValue("unitPrice", unitPrice);
        deliveryItem.setValue("tax", tax);
        deliveryItem.setValue("packageUnits", packageUnits);
        if (product != null) {
            deliveryItem.addNodeParticipation("product", product);
        }
        deliveryItem.setValue("reorderCode", reorderCode);
        deliveryItem.setValue("reorderDescription", reorderDescription);
        service.deriveValues(deliveryItem.getObject());
        return (FinancialAct) deliveryItem.getAct();
    }

    /**
     * Returns the total tax for the invoice.
     *
     * @param invoice          the invoice
     * @param practiceCurrency the practice currency. All amounts must be expressed in this currency
     * @param invoiceId        the invoice identifier
     * @return the total tax
     * @throws ESCIException if the tax is incorrectly specified
     */
    protected BigDecimal getTax(InvoiceType invoice, String practiceCurrency, String invoiceId) {
        return getTax(invoice.getTaxTotal(), practiceCurrency, "Invoice", invoiceId);
    }

    /**
     * Returns the tax for an invoice line.
     *
     * @param line             the invoice line
     * @param invoiceLineId    the invoice line identifier
     * @param practiceCurrency the practice currency. All amounts must be expressed in this currency
     * @return the invoice line tax
     * @throws ESCIException if the tax is incorrectly specified
     */
    protected BigDecimal getTax(InvoiceLineType line, String practiceCurrency, String invoiceLineId) {
        return getTax(line.getTaxTotal(), practiceCurrency, "InvoiceLine", invoiceLineId);
    }

    /**
     * Returns the tax amount.
     * <p/>
     * This implementation expects that the supplied <tt>tax</tt> contains 0..1 elements.
     *
     * @param tax              the tax totals
     * @param practiceCurrency the practice currency. All amounts must be expressed in this currency
     * @param parent           the       name of the parent element for error reporting
     * @param id               the parent element identifier
     * @return the tax amount
     */
    protected BigDecimal getTax(List<TaxTotalType> tax, String practiceCurrency, String parent, String id) {
        BigDecimal result = BigDecimal.ZERO;
        if (tax != null && !tax.isEmpty()) {
            if (tax.size() != 1) {
                Message message = ESCIAdapterMessages.ublInvalidCardinality("TaxTotal", parent, id, "1",
                                                                            tax.size());
                throw new ESCIException(message.toString());
            }
            TaxTotalType total = tax.get(0);
            result = getAmount(total.getTaxAmount(), practiceCurrency, "TaxTotal/TaxAmount", parent, id);
        }
        return result;
    }

    /**
     * Returns the product referenced by an invoice line.
     * <p/>
     * This implementation requires that one of Item/BuyersItemIdentification and Item/SellersItemIdentification is
     * provided. It will use BuyersItemIdentification in preference to SellersItemIdentification.
     *
     * @param line          the invoice line
     * @param supplier      the supplier
     * @param invoiceLineId the invoice line identifier
     * @return the corresponding product, or <tt>null</tt> if the reference product is not found
     * @throws ESCIException if the product is incorrectly specified
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *                       for any archetype service error
     */
    protected Product getProduct(InvoiceLineType line, Party supplier, String invoiceLineId) {
        Product result = null;
        ItemType item = line.getItem();
        if (item == null) {
            Message message = ESCIAdapterMessages.ublElementRequired("Item", "InvoiceLine", invoiceLineId);
            throw new ESCIException(message.toString());
        }
        ItemIdentificationType buyerId = item.getBuyersItemIdentification();
        ItemIdentificationType sellerId = item.getSellersItemIdentification();
        if (buyerId == null && sellerId == null) {
            Message message = ESCIAdapterMessages.invoiceNoProduct(invoiceLineId);
            throw new ESCIException(message.toString());
        }
        if (buyerId != null) {
            // find the product by buyer id.
            IDType id = buyerId.getID();
            long productId = getNumericId(id, "Item/BuyersItemIdentification/ID", "InvoiceLine", invoiceLineId);
            result = getProduct(productId);
        }
        if (result == null && sellerId != null) {
            // try and find the product by seller id.
            String id = getId(sellerId.getID(), "Item/SellersItemIdentification/ID", "InvoiceLine", invoiceLineId);
            result = getProduct(id, supplier);
        }
        return result;
    }

    /**
     * Returns the seller's item id for an invoice line.
     *
     * @param item the invoice line item
     * @return the seller's item id, or <tt>null</tt> if it is not present
     */
    protected String getSellerItemId(ItemType item) {
        String result = null;
        ItemIdentificationType sellerId = item.getSellersItemIdentification();
        if (sellerId != null) {
            result = getId(sellerId.getID());
        }
        return result;
    }

    /**
     * Returns the seller's item name for an invoice line.
     *
     * @param item the invoice line item
     * @return the seller's item name or <tt>null</tt> if it is not present
     */
    protected String getSellerItemName(ItemType item) {
        return (item.getName() != null) ? item.getName().getValue() : null;
    }

    /**
     * Returns the invoice identifier.
     *
     * @param invoice the invoice
     * @return the invoice identifier
     * @throws ESCIException if the identifier isn't set
     */
    protected String getInvoiceId(InvoiceType invoice) {
        return getId(invoice.getID(), "ID", "Invoice", null);
    }

    /**
     * Returns the identifier for an invoice line.
     *
     * @param line the invoice line
     * @return the identifier
     * @throws ESCIException if the identifier isn't set
     */
    protected String getInvoiceLineId(InvoiceLineType line) {
        return getId(line.getID(), "ID", "InvoiceLine", null);
    }

    /**
     * Verifies that the UBL version matches that expected.
     *
     * @param invoice   the invoice
     * @param invoiceId the invoice identifier
     */
    protected void checkUBLVersion(InvoiceType invoice, String invoiceId) {
        String value = getId(invoice.getUBLVersionID(), "UBLVersionID", "Invoice", invoiceId);
        if (!UBL_VERSION.equals(value)) {
            Message message = ESCIAdapterMessages.ublInvalidValue("UBLVersionID", "Invoice", invoiceId,
                                                                   UBL_VERSION, value);
            throw new ESCIException(message.toString());
        }
    }

    /**
     * Returns the invoice issue date/time.
     *
     * @param invoice   the invoice
     * @param invoiceId the invoice identifier, for error reporting
     * @return the issue date/time
     * @throws ESCIException if the issue date isn't set
     */
    protected Date getIssueDatetime(InvoiceType invoice, String invoiceId) {
        IssueDateType issueDate = invoice.getIssueDate();
        XMLGregorianCalendar calendar = (issueDate != null) ? issueDate.getValue() : null;
        if (calendar == null) {
            Message message = ESCIAdapterMessages.ublElementRequired("IssueDate", "Invoice", invoiceId);
            throw new ESCIException(message.toString());
        }

        IssueTimeType issueTime = invoice.getIssueTime();
        if (issueTime != null) {
            XMLGregorianCalendar time = issueTime.getValue();
            if (time != null) {
                calendar.setHour(time.getHour());
                calendar.setMinute(time.getMinute());
                calendar.setSecond(time.getSecond());
                calendar.setMillisecond(time.getMillisecond());
            }
        }
        return calendar.toGregorianCalendar().getTime();
    }

    /**
     * Gets the value from an amount, verifying the practice currency.
     *
     * @param amount           the amount
     * @param practiceCurrency the practice currency. All amounts must be expressed in this
     * @param path             the path to the element for error reporting
     * @param parent           the parent element
     * @param id               the parent element identfier
     * @return the amount value
     */
    protected BigDecimal getAmount(AmountType amount, String practiceCurrency, String path, String parent, String id) {
        if (amount == null) {
            Message message = ESCIAdapterMessages.ublElementRequired(path, parent, id);
            throw new ESCIException(message.toString());
        }
        CurrencyCodeContentType currency = amount.getCurrencyID();
        if (currency == null || StringUtils.isEmpty(currency.value())) {
            Message message = ESCIAdapterMessages.ublElementRequired(path + "/currencyID", parent, id);
            throw new ESCIException(message.toString());
        }
        if (!ObjectUtils.equals(practiceCurrency, currency.value())) {
            Message message = ESCIAdapterMessages.invoiceInvalidCurrency(path, parent, id, practiceCurrency,
                                                                         currency.value());
            throw new ESCIException(message.toString());
        }
        return amount.getValue();
    }

    /**
     * Returns a product given its id.
     *
     * @param id the product identifier
     * @return the corresponding product, or <tt>null</tt> if it can't be found
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    protected Product getProduct(long id) {
        return (Product) getObject(id, ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE,
                                   ProductArchetypes.SERVICE);
    }

    /**
     * Returns a product for a given supplier, given its re-order code.
     * <p/>
     * This returns the first product maching the re-order code.
     *
     * @param reorderCode the product's reorder code
     * @param supplier    the supplier
     * @return the corresponding product, or <tt>null</tt> if it can't be found
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    protected Product getProduct(String reorderCode, Party supplier) {
        Product result = null;
        List<ProductSupplier> list = supplierRules.getProductSuppliers(supplier);
        for (ProductSupplier ps : list) {
            if (StringUtils.equals(reorderCode, ps.getReorderCode())) {
                result = ps.getProduct();
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns an object given its id.
     *
     * @param id         the object identifier
     * @param shortNames the possible archetype short names for the object
     * @return the corresponding object or <tt>null</tt> if it is not found
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    protected IMObject getObject(long id, String... shortNames) {
        IMObject result = null;
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        query.add(Constraints.eq("id", id));
        IPage<IMObject> page = service.get(query);
        if (page.getResults().size() == 1) {
            result = page.getResults().get(0);
        }
        return result;
    }

    /**
     * Returns an <tt>IMObjectReference</tt> for a given archetype id and <tt>IdentfierType</tt>.
     *
     * @param archetypeId the archetype identifier
     * @param id          the identifier
     * @param path        the identifier element path
     * @param parent      the parent element
     * @param parentId    the parent element identifier
     * @return the corresponding reference
     * @throws ESCIException if <tt>id</tt> is null or is not a valid identifier
     */
    protected IMObjectReference getReference(ArchetypeId archetypeId, IdentifierType id, String path, String parent,
                                             String parentId) {
        long objectId = getNumericId(id, path, parent, parentId);
        return new IMObjectReference(archetypeId, objectId);
    }

    /**
     * Returns the numeric value of an <tt>IdentifierType</tt>.
     *
     * @param id       the identifier
     * @param path     the identifier element path
     * @param parent   the parent element
     * @param parentId the parent element identifier
     * @return the numeric value of <tt>id</tt>
     * @throws ESCIException if <tt>id</tt> is null or is not a valid identifier
     */
    protected long getNumericId(IdentifierType id, String path, String parent, String parentId) {
        String value = getId(id, path, parent, parentId);
        long result = NumberUtils.toLong(value, -1);
        if (result == -1) {
            Message message = ESCIAdapterMessages.ublInvalidIdentifier(path, parent, parentId, id.getValue());
            throw new ESCIException(message.toString());
        }
        return result;
    }

    /**
     * Returns the string value of an <tt>IdentifierType</tt>.
     *
     * @param id       the identifier
     * @param path     the identifier element path
     * @param parent   the parent element
     * @param parentId the parent element identifier
     * @return the value of <tt>id</tt>
     * @throws ESCIException if <tt>id</tt> is null or is not a valid identifier
     */
    protected String getId(IdentifierType id, String path, String parent, String parentId) {
        String result = getId(id);
        if (result == null) {
            Message message = ESCIAdapterMessages.ublElementRequired(path, parent, parentId);
            throw new ESCIException(message.toString());
        }
        return result;
    }

    /**
     * Returns the string value of an identifier.
     *
     * @param id the identifier. May be <tt>null</tt>
     * @return the identifier value. May be <tt>null</tt>
     */
    protected String getId(IdentifierType id) {
        String result = null;
        if (id != null) {
            result = StringUtils.trimToNull(id.getValue());
        }
        return result;
    }

}
