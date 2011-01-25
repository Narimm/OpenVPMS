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
import org.oasis.ubl.InvoiceType;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
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
import org.openvpms.esci.adapter.map.UBLDocument;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

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
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

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
        String practiceCurrency = UBLHelper.getCurrencyCode(practiceRules, factory);
        TaxRates rates = new TaxRates(lookupService, factory);
        UBLInvoice wrapper = new UBLInvoice(invoice, practiceCurrency, getArchetypeService(), supplierRules);
        String invoiceId = wrapper.getID();
        checkUBLVersion(wrapper);
        Date issueDatetime = wrapper.getIssueDatetime();
        String notes = wrapper.getNotes();
        wrapper.checkSupplier(supplier, accountId);
        wrapper.checkStockLocation(stockLocation, accountId);

        checkDuplicateInvoice(supplier, invoiceId, issueDatetime);
        FinancialAct order = wrapper.getOrder();
        if (order != null) {
            checkOrder(order, supplier, stockLocation, wrapper);
            result.setOrder(order);
        }

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
        List<UBLInvoiceLine> lines = wrapper.getInvoiceLines();
        if (lines.isEmpty()) {
            throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                    "InvoiceLine", "Invoice", invoiceId, "1..*", 0));
        }
        BigDecimal itemTaxIncTotal;
        BigDecimal itemTaxExTotal = BigDecimal.ZERO;
        BigDecimal itemTax = BigDecimal.ZERO;
        BigDecimal itemLineExtensionAmount = BigDecimal.ZERO;
        BigDecimal itemCharge = BigDecimal.ZERO;
        for (UBLInvoiceLine line : lines) {
            BigDecimal amount = line.getLineExtensionAmount();
            FinancialAct item = mapInvoiceLine(line, issueDatetime, supplier, stockLocation, rates, wrapper, result);
            getArchetypeService().deriveValues(item);
            delivery.addNodeRelationship("items", item);
            result.addDeliveryItem(item);

            itemLineExtensionAmount = itemLineExtensionAmount.add(amount);
            itemTax = itemTax.add(item.getTaxAmount());
            itemTaxExTotal = itemTaxExTotal.add(item.getTotal().subtract(item.getTaxAmount()));
        }
        for (FinancialAct relatedOrder : result.getOrders()) {
            delivery.addNodeRelationship("orders", relatedOrder);
        }
        Entity author = getAuthor(result, stockLocation);
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
        String taxScheme = category.getTaxSchemeID();
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
     * Returns the associated order item for an invoice line.
     * <p/>
     * If no order is supplied, this verifies that there are no references to order items.
     *
     * @param line          the invoice line
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @param delivery      the delivery
     * @param invoice       the invoice
     * @return the corresponding order item, or <tt>null</tt> if none is present
     * @throws ESCIAdapterException if the order reference was inccrrectly specified
     */
    protected FinancialAct mapOrderItem(UBLInvoiceLine line, Party supplier, Party stockLocation, Delivery delivery,
                                        UBLInvoice invoice) {
        FinancialAct result = null;
        FinancialAct order = delivery.getOrder();
        IMObjectReference orderRef = line.getOrderReference();
        IMObjectReference itemRef = line.getOrderItemReference();
        if (itemRef != null) {
            // invoice line is referring to an order line
            if (orderRef == null) {
                // no order reference specified, so must be working with the document level order
                if (order == null) {
                    // no order was specified in the invoice line, and no document level order specified
                    // Expected 0 cardinality
                    throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                            "OrderLineReference", "InvoiceLine", line.getID(), "0", 1));
                }
            } else {
                // referencing an order. Make sure it can be retrieved
                FinancialAct childOrder = delivery.getOrder(orderRef);
                if (childOrder == null) {
                    // get the order and ensure it was submitted to the supplier from the same stock location
                    childOrder = line.getOrder();
                    checkOrder(order, supplier, stockLocation, invoice);
                    delivery.addOrder(childOrder);
                }
                if (order != null && !ObjectUtils.equals(order.getObjectReference(), childOrder.getObjectReference())) {
                    // top-level order specified, but the child order is different.
                    throw new ESCIAdapterException(ESCIAdapterMessages.invalidOrder(invoice.getType(), invoice.getID(),
                                                                                    Long.toString(childOrder.getId())));
                }
                order = childOrder;
            }

            result = line.getOrderItem();

            // make sure there is a relationship between the order and the order item
            ActBean bean = factory.createActBean(order);
            if (!bean.hasRelationship(SupplierArchetypes.ORDER_ITEM_RELATIONSHIP, result)) {
                throw new ESCIAdapterException(ESCIAdapterMessages.invoiceInvalidOrderItem(
                        line.getID(), Long.toString(result.getId())));
            }
        }
        return result;
    }

    /**
     * Maps an <tt>InvoiceLineType</tt> to an <em>act.supplierDeliveryItem</em>.
     *
     * @param line          the invoice line
     * @param startTime     the invoice start time
     * @param supplier      the supplier
     * @param stockLocation the stock location
     * @param rates         the tax rates
     * @param invoice       the parent invoice
     * @param delivery      the delivery
     * @return a new <em>act.supplierDeliveryItem</em> corresponding to the invoice line
     * @throws ESCIAdapterException      if the order wasn't submitted by the supplier
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected FinancialAct mapInvoiceLine(UBLInvoiceLine line, Date startTime, Party supplier, Party stockLocation,
                                          TaxRates rates, UBLInvoice invoice, Delivery delivery) {
        ActBean deliveryItem = factory.createActBean(SupplierArchetypes.DELIVERY_ITEM);
        BigDecimal quantity = line.getInvoicedQuantity();
        String invoicedUnitCode = line.getInvoicedQuantityUnitCode();

        Product product = line.getProduct(supplier);
        BigDecimal lineExtensionAmount = line.getLineExtensionAmount();
        String reorderCode = line.getSellersItemID();
        String reorderDescription = line.getItemName();
        BigDecimal unitPrice = line.getPriceAmount();
        BigDecimal listPrice = line.getWholesalePrice();
        BigDecimal tax = line.getTaxAmount();
        String packageUnits = UBLHelper.getUnitOfMeasure(invoicedUnitCode, lookupService, factory);
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
        if (product != null) {
            deliveryItem.addNodeParticipation("product", product);
        }
        deliveryItem.setValue("reorderCode", reorderCode);
        deliveryItem.setValue("reorderDescription", reorderDescription);

        FinancialAct orderItem = mapOrderItem(line, supplier, stockLocation, delivery, invoice);
        if (orderItem != null) {
            deliveryItem.addNodeRelationship("order", orderItem);
        }

        getArchetypeService().deriveValues(deliveryItem.getObject());
        return (FinancialAct) deliveryItem.getAct();
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
     * @param delivery      the delivery
     * @param stockLocation the stock location
     * @return the author reference, or <tt>null</tt> if none is available
     */
    private Entity getAuthor(Delivery delivery, Party stockLocation) {
        Entity result = null;
        FinancialAct order = delivery.getOrder();
        if (order == null) {
            // no primary order. If there is ony one secondary order, use that (which is essentially the primary order)
            List<FinancialAct> orders = delivery.getOrders();
            if (orders.size() == 1) {
                order = orders.get(0);
            }
        }

        if (order != null) {
            ActBean bean = factory.createActBean(order);
            result = bean.getNodeParticipant("author");
        }
        if (result == null) {
            EntityBean bean = factory.createEntityBean(stockLocation);
            result = bean.getNodeTargetEntity("defaultAuthor");
        }
        return result;
    }

    /**
     * Verifies that an order has a relationship to the expected supplier and stock location and is not
     * already associated with the invoice.
     *
     * @param order         the order
     * @param supplier      the suppplier
     * @param stockLocation the stock location
     * @param invoice       the invoice
     * @throws ESCIAdapterException      if the order wasn't submitted by the supplier or the invoice is a duplicate
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    protected void checkOrder(FinancialAct order, Party supplier, Party stockLocation, UBLDocument invoice) {
        super.checkOrder(order, supplier, stockLocation, invoice);
        String invoiceId = invoice.getID();
        ActBean orderBean = factory.createActBean(order);
        List<FinancialAct> deliveries = orderBean.getNodeActs("deliveries", FinancialAct.class);
        for (FinancialAct delivery : deliveries) {
            ActBean deliveryBean = factory.createActBean(delivery);
            String supplierInvoiceId = deliveryBean.getString("supplierInvoiceId");
            if (ObjectUtils.equals(invoiceId, supplierInvoiceId)) {
                throw new ESCIAdapterException(ESCIAdapterMessages.duplicateInvoiceForOrder(invoiceId, order.getId()));
            }
        }
    }

}
