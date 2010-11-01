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
import org.openvpms.archetype.rules.math.MathRules;
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
import org.openvpms.esci.adapter.map.ErrorContext;
import org.openvpms.esci.adapter.map.UBLDocument;
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
        TaxRates rates = new TaxRates(lookupService, factory);
        UBLInvoice wrapper = new UBLInvoice(invoice, practiceCurrency, getArchetypeService(), supplierRules);
        String invoiceId = wrapper.getID();
        checkUBLVersion(wrapper);
        Date issueDatetime = wrapper.getIssueDatetime();
        String notes = wrapper.getNotes();
        Party supplier = wrapper.getSupplier();
        checkSupplier(supplier, user, factory);
        Party stockLocation = wrapper.getStockLocation();

        ActBean orderBean = null;
        FinancialAct order = wrapper.getOrder();
        if (order != null) {
            checkOrder(order, supplier, wrapper);
            result.setOrder(order);
            orderBean = factory.createActBean(order);
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
        Entity author = getAuthor(orderBean, stockLocation);
        if (author != null) {
            delivery.addNodeParticipation("author", author);
        }
        if (order != null) {
            delivery.addNodeRelationship("order", order);
        }
        result.setDelivery((FinancialAct) delivery.getAct());
        List<UBLInvoiceLine> lines = wrapper.getInvoiceLines();
        if (lines.isEmpty()) {
            Message message = ESCIAdapterMessages.ublInvalidCardinality("InvoiceLine", "Invoice", invoiceId, "1..*", 0);
            throw new ESCIException(message.toString());
        }
        BigDecimal itemTaxIncTotal;
        BigDecimal itemTaxExTotal = BigDecimal.ZERO;
        BigDecimal itemTax = BigDecimal.ZERO;
        BigDecimal itemLineExtensionAmount = BigDecimal.ZERO;
        BigDecimal itemCharge = BigDecimal.ZERO;
        for (UBLInvoiceLine line : lines) {
            BigDecimal amount = line.getLineExtensionAmount();
            FinancialAct item = mapInvoiceLine(line, issueDatetime, supplier, rates, orderBean);
            getArchetypeService().deriveValues(item);
            delivery.addNodeRelationship("items", item);
            result.addDeliveryItem(item);

            itemLineExtensionAmount = itemLineExtensionAmount.add(amount);
            itemTax = itemTax.add(item.getTaxAmount());
            itemTaxExTotal = itemTaxExTotal.add(item.getTotal().subtract(item.getTaxAmount()));
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
            Message message = ESCIAdapterMessages.invoiceInvalidChargeTotal(invoiceId, chargeTotal, itemCharge);
            throw new ESCIException(message.toString());
        }

        itemTaxExTotal = itemTaxExTotal.add(itemCharge);
        itemTaxIncTotal = itemTaxExTotal.add(itemTax);

        if (taxTotal.compareTo(itemTax) != 0) {
            Message message = ESCIAdapterMessages.invoiceInvalidTax(invoiceId, taxTotal, itemTax);
            throw new ESCIException(message.toString());
        }
        if (itemLineExtensionAmount.compareTo(invoiceLineExtensionAmount) != 0) {
            Message message = ESCIAdapterMessages.invoiceInvalidLineExtensionAmount(
                    invoiceId, invoiceLineExtensionAmount, itemLineExtensionAmount);
            throw new ESCIException(message.toString());
        }
        if (itemTaxExTotal.compareTo(taxExclusiveAmount) != 0) {
            Message message = ESCIAdapterMessages.invoiceInvalidTaxExclusiveAmount(
                    invoiceId, taxExclusiveAmount, itemTaxExTotal);
            throw new ESCIException(message.toString());
        }
        if (payableAmount.compareTo(itemTaxIncTotal) != 0) {
            Message message = ESCIAdapterMessages.invoiceInvalidPayableAmount(invoiceId, payableAmount,
                                                                              itemTaxIncTotal);
            throw new ESCIException(message.toString());
        }
        return result;
    }

    /**
     * Verifies that the TaxTotal/TaxAmount corresponds to that of the TaxTotal/TaxSubtotal.
     * <p/>
     * This implementation only supports a single TaxTotal/Subtotal and expects the TaxTotal/Subtotal/TaxableAmount
     * to match that supplied.
     *
     * @param line  the invoice line
     * @param rates the tax rates
     * @throws ESCIException if the tax is incorrectly specified
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
     * @throws ESCIException if the subtotal is invalid
     */
    protected void checkTax(UBLTaxSubtotal subtotal, BigDecimal expectedTaxAmount, BigDecimal amount, TaxRates rates) {
        if (subtotal != null) {
            BigDecimal taxAmount = subtotal.getTaxAmount();
            if (expectedTaxAmount.compareTo(taxAmount) != 0) {
                ErrorContext context = new ErrorContext(subtotal, "TaxAmount");
                Message message = ESCIAdapterMessages.ublInvalidValue(context.getPath(), context.getType(),
                                                                      context.getID(), expectedTaxAmount.toString(),
                                                                      taxAmount.toString());
                throw new ESCIException(message.toString());
            }
            UBLTaxCategory category = subtotal.getTaxCategory();
            BigDecimal rate = checkTaxCategory(category, rates);
            BigDecimal divisor = BigDecimal.valueOf(100);
            if (taxAmount.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal calc = MathRules.divide(amount.multiply(rate), divisor, 2);
                if (calc.compareTo(expectedTaxAmount) != 0) {
                    ErrorContext context = new ErrorContext(subtotal, "TaxTotal/TaxAmount");
                    Message message = ESCIAdapterMessages.ublInvalidValue(context.getPath(), context.getType(),
                                                                          context.getID(), calc.toString(),
                                                                          expectedTaxAmount.toString());
                    throw new ESCIException(message.toString());
                }
            }
        } else if (expectedTaxAmount.compareTo(BigDecimal.ZERO) != 0) {
            ErrorContext context = new ErrorContext(subtotal.getParent(), "TaxTotal");
            Message message = ESCIAdapterMessages.ublElementRequired(context.getPath(), context.getType(),
                                                                     context.getID());
            throw new ESCIException(message.toString());
        }
    }

    /**
     * Verifies that a tax category matches that expected.
     *
     * @param category the tax category
     * @param rates    the tax rates
     * @return the tax rate used
     * @throws ESCIException if the category is invalid
     */
    protected BigDecimal checkTaxCategory(UBLTaxCategory category, TaxRates rates) {
        String taxCategory = category.getID();
        BigDecimal rate = category.getTaxRate();
        String taxScheme = category.getTaxSchemeID();
        BigDecimal expectedRate = rates.getTaxRate(taxScheme, taxCategory);
        if (expectedRate == null) {
            ErrorContext context = new ErrorContext(category);
            Message message = ESCIAdapterMessages.invalidTaxSchemeAndCategory(context.getPath(), context.getType(),
                                                                              context.getID(), taxScheme, taxCategory);
            throw new ESCIException(message.toString());
        }
        if (expectedRate.compareTo(rate) != 0) {
            ErrorContext context = new ErrorContext(category, "Percent");
            Message message = ESCIAdapterMessages.ublInvalidValue(context.getPath(), context.getType(), context.getID(),
                                                                  expectedRate.toString(), rate.toString());
            throw new ESCIException(message.toString());
        }
        return rate;
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
     * @param rates     the tax rates
     * @param order     the original order. May be <tt>null</tt>
     * @return a new <em>act.supplierDeliveryItem</em> corresponding to the invoice line
     * @throws ESCIException             if the order wasn't submitted by the supplier
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected FinancialAct mapInvoiceLine(UBLInvoiceLine line, Date startTime, Party supplier, TaxRates rates,
                                          ActBean order) {
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
            Message message = ESCIAdapterMessages.invoiceLineInvalidLineExtensionAmount(
                    line.getID(), lineExtensionAmount, calcLineExtensionAmount);
            throw new ESCIException(message.toString());
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

        FinancialAct orderItem = mapOrderItem(order, line);
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
     * @throws ESCIException if the allowance/charge cannot be mapped
     */
    protected FinancialAct mapCharge(UBLAllowanceCharge charge, Date startTime, String invoiceId, TaxRates rates) {
        if (!charge.isCharge()) {
            Message message = ESCIAdapterMessages.invoiceAllowanceNotSupported(invoiceId);
            throw new ESCIException(message.toString());
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
                Message message = ESCIAdapterMessages.ublInvalidValue(context.getPath(), context.getType(),
                                                                      context.getID(), expectedTax.toString(),
                                                                      tax.toString());
                throw new ESCIException(message.toString());
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
            Message message = ESCIAdapterMessages.ublInvalidValue("UBLVersionID", "Invoice", invoice.getID(),
                                                                  UBL_VERSION, invoice.getUBLVersionID());
            throw new ESCIException(message.toString());
        }
    }

    /**
     * Verifies that an order has a relationship to the expected supplier, and that the order isn't already associated
     * with a delivery.
     *
     * @param order    the order
     * @param supplier the suppplier
     * @param document the invoice
     * @throws ESCIException             if the order wasn't submitted by the supplier or is associated with a delivery
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    protected void checkOrder(FinancialAct order, Party supplier, UBLDocument document) {
        super.checkOrder(order, supplier, document);
        ActBean bean = factory.createActBean(order);
        if (!bean.getRelationships("actRelationship.supplierDeliveryOrder").isEmpty()) {
            Message message = ESCIAdapterMessages.duplicateInvoice(document.getID(), order.getId());
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
