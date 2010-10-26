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

import org.oasis.ubl.InvoiceType;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.AbstractUBLMapper;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.exception.ESCIException;

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
     * @param invoice the invoice to map
     * @param user    the ESCI user that submitted the invoice
     * @return the acts produced in the mapping. The first element is always the <em>act.supplierDelivery</em>
     * @throws ESCIException     if the invoice cannot be mapped
     * @throws OpenVPMSException for any OpenVPMS error
     */
    public Delivery map(InvoiceType invoice, User user) {
        Delivery result = new Delivery();
        String practiceCurrency = UBLHelper.getCurrencyCode(practiceRules, factory);
        UBLInvoice facade = new UBLInvoice(invoice, practiceCurrency, getArchetypeService(), supplierRules);
        String invoiceId = facade.getID();
        checkUBLVersion(facade);
        Date issueDatetime = facade.getIssueDatetime();
        String notes = facade.getNotes();
        Party supplier = facade.getSupplier();
        checkSupplier(supplier, user, factory);
        Party stockLocation = facade.getStockLocation();

        ActBean orderBean = null;
        FinancialAct order = facade.getOrder();
        if (order != null) {
            checkOrder(order, supplier, facade);
            result.setOrder(order);
            orderBean = factory.createActBean(order);
        }

        BigDecimal payableAmount = facade.getPayableAmount();
        BigDecimal invoiceLineExtensionAmount = facade.getLineExtensionAmount();
        BigDecimal tax = facade.getTaxTotal();
        BigDecimal charge = facade.getChargeTotal();

        ActBean delivery = factory.createActBean(SupplierArchetypes.DELIVERY);
        delivery.setValue("startTime", issueDatetime);
        delivery.setValue("supplierNotes", notes);
        delivery.setValue("amount", payableAmount);
        delivery.setValue("tax", tax);
        delivery.setValue("supplierInvoiceId", invoiceId);
        delivery.addNodeParticipation("supplier", supplier);
        delivery.addNodeParticipation("stockLocation", stockLocation);
        Entity author = getAuthor(orderBean, stockLocation);
        if (author != null) {
            delivery.addNodeParticipation("author", author);
        }
        result.setDelivery((FinancialAct) delivery.getAct());
        List<UBLInvoiceLine> lines = facade.getInvoiceLines();
        if (lines.isEmpty()) {
            Message message = ESCIAdapterMessages.ublInvalidCardinality("InvoiceLine", "Invoice", invoiceId, "1..*", 0);
            throw new ESCIException(message.toString());
        }
        BigDecimal itemTotal = BigDecimal.ZERO;
        BigDecimal itemTax = BigDecimal.ZERO;
        BigDecimal itemLineExtensionAmount = BigDecimal.ZERO;
        for (UBLInvoiceLine line : lines) {
            BigDecimal amount = line.getLineExtensionAmount();
            FinancialAct item = mapInvoiceLine(line, issueDatetime, supplier, orderBean);
            getArchetypeService().deriveValues(item);
            delivery.addNodeRelationship("items", item);
            result.addDeliveryItem(item);
            itemLineExtensionAmount = itemLineExtensionAmount.add(amount);
            itemTax = itemTax.add(item.getTaxAmount());
            itemTotal = itemTotal.add(item.getTotal());
        }

        for (UBLAllowanceCharge allowanceCharge : facade.getAllowanceCharges()) {
            FinancialAct item = mapAllowanceCharge(allowanceCharge, issueDatetime, invoiceId);
            delivery.addNodeRelationship("items", item);
            result.addDeliveryItem(item);
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
     * @param order the order. May be <tt>null</tt>
     * @param line  the invoice line
     * @return the corresponding order item, or <tt>null</tt> if none is present
     * @throws ESCIException if the order reference was inccrrectly specified
     */
    protected FinancialAct mapOrderItem(ActBean order, UBLInvoiceLine line) {
        FinancialAct result = null;
        IMObjectReference reference = line.getOrderItemRef();
        if (reference != null) {
            if (order == null) {
                // no order, but an order item specified. Expected 0 cardinality
                Message message = ESCIAdapterMessages.ublInvalidCardinality("OrderLineReference", "InvoiceLine",
                                                                            line.getID(), "0", 1);
                throw new ESCIException(message.toString());
            }
            result = line.getOrderItem();
        }
        return result;
    }

    /**
     * Maps an <tt>InvoiceLineType</tt> to an <em>act.supplierDeliveryItem</em>.
     *
     * @param line      the invoice line
     * @param startTime the invoice start time
     * @param supplier  the supplier
     * @param order     the original order. May be <tt>null</tt>
     * @return a new <em>act.supplierDeliveryItem</em> corresponding to the invoice line
     * @throws ESCIException             if the order wasn't submitted by the supplier
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected FinancialAct mapInvoiceLine(UBLInvoiceLine line, Date startTime, Party supplier, ActBean order) {
        ActBean deliveryItem = factory.createActBean(SupplierArchetypes.DELIVERY_ITEM);
        BigDecimal quantity = line.getInvoicedQuantity();
        String invoicedUnitCode = line.getInvoicedQuantityUnitCode();

        Product product = line.getProduct(supplier);
        BigDecimal lineExtensionAmount = line.getLineExtensionAmount();
        String reorderCode = line.getSellersItemID();
        String reorderDescription = line.getItemName();
        BigDecimal unitPrice = line.getPriceAmount();
        BigDecimal listPrice = line.getWholesalePrice();
        BigDecimal tax = line.getTax();
        String packageUnits = UBLHelper.getUnitOfMeasure(invoicedUnitCode, lookupService, factory);
        BigDecimal calcLineExtensionAmount = unitPrice.multiply(quantity);
        if (calcLineExtensionAmount.compareTo(lineExtensionAmount) != 0) {
            Message message = ESCIAdapterMessages.invoiceLineInvalidLineExtensionAmount(
                    line.getID(), lineExtensionAmount, calcLineExtensionAmount);
            throw new ESCIException(message.toString());
        }

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

        FinancialAct orderItem = mapOrderItem(order, line);
        if (orderItem != null) {
            deliveryItem.addNodeRelationship("order", orderItem);
        }

        getArchetypeService().deriveValues(deliveryItem.getObject());
        return (FinancialAct) deliveryItem.getAct();
    }

    protected FinancialAct mapAllowanceCharge(UBLAllowanceCharge allowanceCharge, Date startTime, String invoiceId) {
        if (!allowanceCharge.isCharge()) {
            Message message = ESCIAdapterMessages.invoiceAllowanceNotSupported(invoiceId);
            throw new ESCIException(message.toString());
        }
        BigDecimal amount = allowanceCharge.getAmount();
        ActBean deliveryItem = factory.createActBean(SupplierArchetypes.DELIVERY_ITEM);
        BigDecimal unitPrice = amount;
        BigDecimal tax = allowanceCharge.getTaxTotal();
        if (tax.compareTo(BigDecimal.ZERO) != 0) {
            unitPrice = amount.subtract(tax);
        }
        deliveryItem.setValue("startTime", startTime);
        deliveryItem.setValue("quantity", BigDecimal.ONE);
        deliveryItem.setValue("unitPrice", unitPrice);
        deliveryItem.setValue("tax", tax);

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
            Message message = ESCIAdapterMessages.ublInvalidValue("UBLVersionID", "Invoice", invoice.getID(),
                                                                  UBL_VERSION, invoice.getUBLVersionID());
            throw new ESCIException(message.toString());
        }
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
