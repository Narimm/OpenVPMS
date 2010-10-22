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

import org.apache.commons.lang.StringUtils;
import org.oasis.ubl.InvoiceType;
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.InvoiceLineType;
import org.oasis.ubl.common.aggregate.ItemIdentificationType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.MonetaryTotalType;
import org.oasis.ubl.common.aggregate.OrderLineReferenceType;
import org.oasis.ubl.common.aggregate.OrderReferenceType;
import org.oasis.ubl.common.aggregate.PriceType;
import org.oasis.ubl.common.aggregate.PricingReferenceType;
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.InvoicedQuantityType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.IssueTimeType;
import org.oasis.ubl.common.basic.LineIDType;
import org.oasis.ubl.common.basic.NoteType;
import org.oasis.ubl.common.basic.PriceTypeCodeType;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.AbstractUBLMapper;
import org.openvpms.esci.adapter.map.UBLHelper;
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

    /**
     * UN/CEFACT wholesale price type code identifier.
     * See http://www.unece.org/uncefact/codelist/standard/UNECE_PriceTypeCode_D09B.xsd
     */
    private static final String WHOLESALE = "WS";


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
     * @param user    the ESCI user that submitted the invoice
     * @return the acts produced in the mapping. The first element is always the <em>act.supplierDelivery</em>
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the invoice cannot be mapped
     * @throws org.openvpms.component.system.common.exception.OpenVPMSException
     *          for any OpenVPMS error
     */
    public Delivery map(InvoiceType invoice, User user) {
        Delivery result = new Delivery();
        String invoiceId = getInvoiceId(invoice);
        checkUBLVersion(invoice, invoiceId);
        OrderReferenceType orderReference = invoice.getOrderReference();
        Date issueDatetime = getIssueDatetime(invoice, invoiceId);
        String notes = getNotes(invoice);
        Party supplier = getSupplier(invoice, invoiceId);
        checkSupplier(supplier, user, factory);
        Party stockLocation = getStockLocation(invoice, invoiceId);

        ActBean order = null;

        if (orderReference != null) {
            order = mapOrderReference(orderReference, supplier, invoiceId);
            result.setOrder((FinancialAct) order.getAct());
        }

        MonetaryTotalType monetaryTotal = getRequired(invoice.getLegalMonetaryTotal(), "LegalMonetaryTotal", "Invoice",
                                                      invoiceId);
        String practiceCurrency = UBLHelper.getCurrencyCode(practiceRules, factory);
        BigDecimal payableAmount = getAmount(monetaryTotal.getPayableAmount(), practiceCurrency,
                                             "LegalMonetaryTotal/PayableAmount", "Invoice", invoiceId);
        BigDecimal invoiceLineExtensionAmount = getAmount(monetaryTotal.getLineExtensionAmount(), practiceCurrency,
                                                          "LegalMonetaryTotal/LineExtensionAmount",
                                                          "Invoice", invoiceId);
        BigDecimal tax = getTax(invoice, practiceCurrency, invoiceId);

        ActBean delivery = factory.createActBean(SupplierArchetypes.DELIVERY);
        delivery.setValue("startTime", issueDatetime);
        delivery.setValue("supplierNotes", notes);
        delivery.setValue("amount", payableAmount);
        delivery.setValue("tax", tax);
        delivery.setValue("supplierInvoiceId", invoiceId);
        delivery.addNodeParticipation("supplier", supplier);
        delivery.addNodeParticipation("stockLocation", stockLocation);
        Entity author = getAuthor(order, stockLocation);
        if (author != null) {
            delivery.addNodeParticipation("author", author);
        }
        result.setDelivery((FinancialAct) delivery.getAct());
        List<InvoiceLineType> lines = invoice.getInvoiceLine();
        if (lines == null || lines.isEmpty()) {
            Message message = ESCIAdapterMessages.ublInvalidCardinality("InvoiceLine", "Invoice", invoiceId, "1..*", 0);
            throw new ESCIException(message.toString());
        }
        BigDecimal itemTotal = BigDecimal.ZERO;
        BigDecimal itemTax = BigDecimal.ZERO;
        BigDecimal itemLineExtensionAmount = BigDecimal.ZERO;
        for (InvoiceLineType line : lines) {
            String lineId = getInvoiceLineId(line);
            BigDecimal amount = getAmount(line.getLineExtensionAmount(), practiceCurrency,
                                          "LineExtensionAmount", "InvoiceLine", lineId);
            FinancialAct item = mapInvoiceLine(line, issueDatetime, practiceCurrency, supplier, order, lineId);
            getArchetypeService().deriveValues(item);
            delivery.addNodeRelationship("items", item);
            result.addDeliveryItem(item);
            itemLineExtensionAmount = itemLineExtensionAmount.add(amount);
            itemTax = itemTax.add(item.getTaxAmount());
            itemTotal = itemTotal.add(item.getTotal());
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
     * <p/>
     * If no order is supplied, this verifies that there are no references to order items.
     *
     * @param order         the order. May be <tt>null</tt>
     * @param line          the invoice line
     * @param invoiceLineId the invoice line identifier
     * @return the corresponding order item, or <tt>null</tt> if none is present
     * @throws ESCIException if the order reference was inccrrectly specified
     */
    protected FinancialAct mapOrderItem(ActBean order, InvoiceLineType line, String invoiceLineId) {
        FinancialAct result = null;
        List<OrderLineReferenceType> list = line.getOrderLineReference();
        if (order == null) {
            if (list != null && !list.isEmpty()) {
                Message message = ESCIAdapterMessages.ublInvalidCardinality("OrderLineReference", "InvoiceLine",
                                                                            invoiceLineId, "0", list.size());
                throw new ESCIException(message.toString());
            }
        } else if (list != null && !list.isEmpty()) {
            if (list.size() != 1) {
                Message message = ESCIAdapterMessages.ublInvalidCardinality("OrderLineReference", "InvoiceLine",
                                                                            invoiceLineId, "1", list.size());
                throw new ESCIException(message.toString());
            }
            LineIDType id = list.get(0).getLineID();
            result = (FinancialAct) getObject(ORDER_ITEM, id, "OrderLineReference/LineID", "InvoiceLine",
                                              invoiceLineId);
            if (result == null || !order.hasRelationship(SupplierArchetypes.ORDER_ITEM_RELATIONSHIP, result)) {
                Message message = ESCIAdapterMessages.invoiceInvalidOrderItem(invoiceLineId, id.getValue());
                throw new ESCIException(message.toString());
            }
        }
        return result;
    }

    /**
     * Returns the invoice notes.
     * <p/>
     * If there are multiple notes, these will be concatenated, separated by newlines.
     *
     * @param invoice the invoice
     * @return the invoice note. May be <tt>null</tt>
     */
    protected String getNotes(InvoiceType invoice) {
        String result = null;
        List<NoteType> notes = invoice.getNote();
        if (notes != null && !notes.isEmpty()) {
            StringBuilder buffer = new StringBuilder();
            for (NoteType note : notes) {
                if (buffer.length() != 0) {
                    buffer.append('\n');
                }
                if (!StringUtils.isEmpty(note.getValue())) {
                    buffer.append(note.getValue());
                }
            }
            result = buffer.toString();
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
        return getSupplier(invoice.getAccountingSupplierParty(), "AccountingSupplierParty", "Invoice", invoiceId);
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
        CustomerPartyType customerType = getRequired(invoice.getAccountingCustomerParty(), "AccountingCustomerParty",
                                                     "Invoice", invoiceId);
        CustomerAssignedAccountIDType accountId = customerType.getCustomerAssignedAccountID();
        Party location = (Party) getObject(STOCK_LOCATION, accountId,
                                           "AccountingCustomerParty/CustomerAssignedAccountID", "Invoice", invoiceId);
        if (location == null) {
            Message message = ESCIAdapterMessages.invoiceInvalidStockLocation(
                    "AccountingCustomerParty/CustomerAssignedAccountID", invoiceId, accountId.getValue());
            throw new ESCIException(message.toString());
        }
        return location;
    }

    /**
     * Maps an <tt>OrderReferenceType</tt> to its corresponding <em>act.supplierOrder</em>.
     * <p/>
     * This verifies that the original order was submitted by the supplier.
     *
     * @param order     the order reference
     * @param supplier  the suppplier
     * @param invoiceId the invoice identifier
     * @return the order
     * @throws ESCIException if the order wasn't submitted by the supplier
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *                       for any archetype service error
     */
    protected ActBean mapOrderReference(OrderReferenceType order, Party supplier, String invoiceId) {
        IMObjectReference ref = getReference(ORDER, order.getID(), "OrderReference", "Invoice", invoiceId);
        FinancialAct result = getOrder(ref, supplier, "Invoice", invoiceId);
        return factory.createActBean(result);
    }

    /**
     * Maps an <tt>InvoiceLineType</tt> to an <em>act.supplierDeliveryItem</em>.
     *
     * @param line             the invoice line
     * @param startTime        the invoice start time
     * @param practiceCurrency the practice currency. All amounts must be expressed in this currency
     * @param supplier         the supplier
     * @param order            the original order. May be <tt>null</tt>
     * @param invoiceLineId    the invoice line identifier
     * @return a new <em>act.supplierDeliveryItem</em> corresponding to the invoice line
     * @throws ESCIException if the order wasn't submitted by the supplier
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *                       for any archetype service error
     */
    protected FinancialAct mapInvoiceLine(InvoiceLineType line, Date startTime, String practiceCurrency, Party supplier,
                                          ActBean order, String invoiceLineId) {
        ActBean deliveryItem = factory.createActBean(SupplierArchetypes.DELIVERY_ITEM);
        InvoicedQuantityType invoicedQuantity = line.getInvoicedQuantity();

        BigDecimal quantity = getQuantity(invoicedQuantity, "InvoicedQuantity", "InvoiceLine", invoiceLineId);
        String invoicedUnitCode = getRequired(invoicedQuantity.getUnitCode(), "InvoicedQuantity@unitCode",
                                              "InvoiceLine", invoiceLineId);

        Product product = getProduct(line, supplier, invoiceLineId);
        BigDecimal lineExtensionAmount = getAmount(line.getLineExtensionAmount(), practiceCurrency,
                                                   "LineExtensionAmount", "InvoiceLine", invoiceLineId);
        ItemType item = getRequired(line.getItem(), "Item", "InvoiceLine", invoiceLineId);
        String reorderCode = getSellerItemId(item);
        String reorderDescription = getSellerItemName(item);
        PriceType price = getRequired(line.getPrice(), "Price", "InvoiceLine", invoiceLineId);
        BigDecimal unitPrice = getAmount(price.getPriceAmount(), practiceCurrency, "Price/PriceAmount",
                                         "InvoiceLine", invoiceLineId);
        BigDecimal listPrice = getListPrice(line, practiceCurrency, invoiceLineId);
        BigDecimal tax = getTax(line, practiceCurrency, invoiceLineId);
        String packageUnits = UBLHelper.getUnitOfMeasure(invoicedUnitCode, lookupService, factory);
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
        deliveryItem.setValue("listPrice", listPrice);
        deliveryItem.setValue("tax", tax);
        deliveryItem.setValue("packageUnits", packageUnits);
        if (product != null) {
            deliveryItem.addNodeParticipation("product", product);
        }
        deliveryItem.setValue("reorderCode", reorderCode);
        deliveryItem.setValue("reorderDescription", reorderDescription);

        FinancialAct orderItem = mapOrderItem(order, line, invoiceLineId);
        if (orderItem != null) {
            deliveryItem.addNodeRelationship("order", orderItem);
        }

        getArchetypeService().deriveValues(deliveryItem.getObject());
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
        ItemType item = getRequired(line.getItem(), "Item", "InvoiceLine", invoiceLineId);
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
     * Returns the list price.
     * <p/>
     * This looks for a single PricingReference/AlternativeConditionPrice. If present, it must have a PriceTypeCode
     * of "WS" (wholesale).
     *
     * @param line             the invoice line
     * @param practiceCurrency the practice currency
     * @param invoiceLineId    the invoice line identifier
     * @return the list price, or <tt>0.0</tt> if no wholesale price is specified
     */
    protected BigDecimal getListPrice(InvoiceLineType line, String practiceCurrency, String invoiceLineId) {
        BigDecimal result = BigDecimal.ZERO;
        PricingReferenceType pricing = line.getPricingReference();
        if (pricing != null) {
            List<PriceType> prices = pricing.getAlternativeConditionPrice();
            if (!prices.isEmpty()) {
                PriceType price = prices.get(0);
                result = getAmount(price.getPriceAmount(), practiceCurrency,
                                   "PricingReference/AlternativeConditionPrice/PriceAmount", "InvoiceLine",
                                   invoiceLineId);
                PriceTypeCodeType code = getRequired(
                        price.getPriceTypeCode(), "PricingReference/AlternativeConditionPrice/PriceTypeCode",
                        "InvoiceLine", invoiceLineId);
                if (!WHOLESALE.equals(code.getValue())) {
                    Message message = ESCIAdapterMessages.ublInvalidValue(
                            "PricingReference/AlternativeConditionPrice/PriceTypeCode", "InvoiceLine", invoiceLineId,
                            WHOLESALE, code.getValue());
                    throw new ESCIException(message.toString());
                }
            }
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
        checkUBLVersion(invoice.getUBLVersionID(), "Invoice", invoiceId);
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
        IssueDateType issueDate = getRequired(invoice.getIssueDate(), "IssueDate", "Invoice", invoiceId);
        XMLGregorianCalendar calendar = issueDate.getValue();
        checkRequired(calendar, "IssueDate", "Invoice", invoiceId);

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
     * Returns an author to associate with the delivery.
     * <p/>
     * This returns the author of the original order, if present. If not, it returns that from the stock location.
     *
     * @param order         the order. May be <tt>null</tt>
     * @param stockLocation the stock location
     * @return the author reference, or <tt>null</tt> if none is available
     */
    private Entity getAuthor(ActBean order, Party stockLocation) {
        Entity result = null;
        if (order != null) {
            result = order.getNodeParticipant("author");
        }
        if (result == null) {
            EntityBean bean = factory.createEntityBean(stockLocation);
            result = bean.getNodeTargetEntity("defaultAuthor");
        }
        return result;
    }

}
