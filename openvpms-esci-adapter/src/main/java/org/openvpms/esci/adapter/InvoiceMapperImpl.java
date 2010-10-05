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
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.InvoiceLineType;
import org.oasis.ubl.common.aggregate.ItemIdentificationType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.MonetaryTotalType;
import org.oasis.ubl.common.aggregate.PriceType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.aggregate.TaxTotalType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.InvoicedQuantityType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.IssueTimeType;
import org.oasis.ubl.common.basic.NameType;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
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

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.ArrayList;
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
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * Stock location archetype id.
     */
    private static final ArchetypeId STOCK_LOCATION = new ArchetypeId(StockArchetypes.STOCK_LOCATION);


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
     */
    public List<FinancialAct> map(InvoiceType invoice) {
        String supplierInvoiceId = getInvoiceId(invoice);
        Date issueDatetime = getIssueDatetime(invoice);
        Party supplier = getSupplier(invoice);
        Party stockLocation = getStockLocation(invoice);
        MonetaryTotalType monetaryTotal = invoice.getLegalMonetaryTotal();
        if (monetaryTotal == null) {
            throw new IllegalArgumentException("LegalMonetaryTotal not provided");
        }
        String practiceCurrency = UBLHelper.getCurrencyCode(practiceRules, factory);
        BigDecimal total = getAmount(monetaryTotal.getPayableAmount(), practiceCurrency,
                                     "LegalMonetaryTotal/PayableAmount");
        BigDecimal invoiceLineExtensionAmount = getAmount(monetaryTotal.getLineExtensionAmount(), practiceCurrency,
                                                          "LegalMonetaryTotal/LineExtensionAmount");
        BigDecimal tax = getTax(invoice, practiceCurrency);

        ActBean delivery = factory.createActBean(SupplierArchetypes.DELIVERY);
        delivery.setValue("startTime", issueDatetime);
        delivery.setValue("amount", total);
        delivery.setValue("tax", tax);
        delivery.setValue("supplierInvoiceId", supplierInvoiceId);
        delivery.addNodeParticipation("supplier", supplier);
        delivery.addNodeParticipation("stockLocation", stockLocation);

        List<FinancialAct> result = new ArrayList<FinancialAct>();
        result.add((FinancialAct) delivery.getAct());
        List<InvoiceLineType> lines = invoice.getInvoiceLine();
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("No line items provided");
        }
        BigDecimal itemTotal = BigDecimal.ZERO;
        BigDecimal itemTax = BigDecimal.ZERO;
        BigDecimal itemLineExtensionAmount = BigDecimal.ZERO;
        for (InvoiceLineType line : lines) {
            BigDecimal amount =
                    getAmount(line.getLineExtensionAmount(), practiceCurrency, "InvoiceLine/LineExtensionAmount");
            FinancialAct item = map(line, issueDatetime, practiceCurrency);
            service.deriveValues(item);
            result.add(item);
            itemLineExtensionAmount = itemLineExtensionAmount.add(amount);
            itemTax = itemTax.add(item.getTaxAmount());
            itemTotal = itemTotal.add(item.getTotal());
        }
        if (tax.compareTo(itemTax) != 0) {
            throw new IllegalArgumentException("Invoice tax: " + tax + " doesn't match InvoiceLine tax: " + itemTax);
        }
        if (itemLineExtensionAmount.compareTo(invoiceLineExtensionAmount) != 0) {
            throw new IllegalArgumentException("Invoice LineExtensionAmount: " + invoiceLineExtensionAmount
                                               + " doesn't match InvoiceLine totals: " + itemLineExtensionAmount);
        }
        if (total.compareTo(itemTotal) != 0) {
            throw new IllegalArgumentException("Invoice total: " + total + " doesn't match InvoiceLine totals: "
                                               + itemTotal);
        }
        return result;
    }

    private FinancialAct map(InvoiceLineType line, Date startTime, String practiceCurrency) {
        ActBean item = factory.createActBean(SupplierArchetypes.DELIVERY_ITEM);
        InvoicedQuantityType quantityType = line.getInvoicedQuantity();
        if (quantityType == null) {
            throw new IllegalArgumentException("InvoiceLine/InvoiceQuantity not provided");
        }
        BigDecimal quantity = quantityType.getValue();
        Product product = getProduct(line);
        BigDecimal lineExtensionAmount = getAmount(line.getLineExtensionAmount(), practiceCurrency,
                                                   "InvoiceLine/LineExtensionAmount");
        String reorderCode = getReorderCode(line);
        String reorderDescription = getReorderDescription(line);
        PriceType price = line.getPrice();
        if (price == null) {
            throw new IllegalArgumentException("InvoiceLine/Price not provided");
        }
        BigDecimal unitPrice = getAmount(price.getPriceAmount(), practiceCurrency, "InvoiceLine/Price/PriceAmount");
        BigDecimal tax = getTax(line, practiceCurrency);
        String packageUnits = UBLHelper.getUnitOfMeasure(price.getBaseQuantity(), lookupService, factory);
        BigDecimal expectedLineExtensionAmount = unitPrice.multiply(quantity);
        if (expectedLineExtensionAmount.compareTo(lineExtensionAmount) != 0) {
            throw new IllegalArgumentException("InvoiceLine/LineExtensionAmount. Got " + lineExtensionAmount
                                               + ", but expected " + expectedLineExtensionAmount);
        }

        item.setValue("startTime", startTime);
        item.setValue("quantity", quantity);
        item.setValue("unitPrice", unitPrice);
        item.setValue("tax", tax);
        item.setValue("packageUnits", packageUnits);
        item.addNodeParticipation("product", product);
        item.setValue("reorderCode", reorderCode); // TODO - check re-order code against that provided by the supplier
        item.setValue("reorderDescription", reorderDescription);
        service.deriveValues(item.getObject());
        return (FinancialAct) item.getAct();
    }

    private BigDecimal getTax(InvoiceType invoice, String practiceCurrency) {
        BigDecimal result = BigDecimal.ZERO;
        List<TaxTotalType> tax = invoice.getTaxTotal();
        if (tax != null) {
            if (tax.size() != 1) {
                throw new IllegalArgumentException("Expected 1 TaxTotal but got " + tax.size());
            }
            TaxTotalType total = tax.get(0);
            result = getAmount(total.getTaxAmount(), practiceCurrency, "Invoice/TaxTotal/TaxAmount");
        }
        return result;
    }

    private BigDecimal getTax(InvoiceLineType line, String practiceCurrency) {
        BigDecimal result = BigDecimal.ZERO;
        if (line.getTaxTotal() != null) {
            if (line.getTaxTotal().size() > 1) {
                throw new IllegalArgumentException("Expected 0..1 TaxTotal but got " + line.getTaxTotal().size());
            } else if (line.getTaxTotal().size() == 1) {
                TaxTotalType total = line.getTaxTotal().get(0);
                result = getAmount(total.getTaxAmount(), practiceCurrency, "InvoiceLine/TaxTotal/TaxAmount");
            }
        }
        return result;
    }

    private Product getProduct(InvoiceLineType line) {
        ItemType item = line.getItem();
        if (item == null) {
            throw new IllegalArgumentException("InvoiceLine/Item not provided");
        }
        ItemIdentificationType buyerId = item.getBuyersItemIdentification();
        if (buyerId == null) {
            throw new IllegalArgumentException("InvoiceLine/Item/BuyersItemIdentification not provided");
        }
        IDType id = buyerId.getID();
        if (id == null) {
            throw new IllegalArgumentException("InvoiceLine/Item/BuyersItemIdentification/ID not provided");
        }
        long productId = NumberUtils.toLong(id.getValue(), -1);
        if (productId == -1) {
            throw new IllegalArgumentException("InvoiceLine/Item/BuyersItemIdentification/ID is invalid: " + productId);
        }
        Product product = (Product) getObject(productId, ProductArchetypes.MEDICATION,
                                              ProductArchetypes.MERCHANDISE,
                                              ProductArchetypes.SERVICE);
        if (product == null) {
            throw new IllegalArgumentException("InvoiceLine/Item/BuyersItemIdentification/ID is not a valid product: "
                                               + productId);
        }
        return product;
    }

    private String getReorderCode(InvoiceLineType line) {
        ItemType item = line.getItem();
        if (item != null) {
            ItemIdentificationType sellerId = item.getSellersItemIdentification();
            if (sellerId != null) {
                IDType id = sellerId.getID();
                if (id != null) {
                    return id.getValue();
                }
            }
        }
        return null;
    }

    private String getReorderDescription(InvoiceLineType line) {
        ItemType item = line.getItem();
        if (item != null) {
            NameType name = item.getName();
            if (name != null) {
                return name.getValue();
            }
        }
        return null;
    }

    private String getInvoiceId(InvoiceType invoice) {
        IDType id = invoice.getID();
        if (id == null) {
            throw new IllegalArgumentException("ID not provided");
        }
        String value = id.getValue();
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("ID is invalid");
        }
        return value;
    }

    /**
     * Returns the invoice issue date/time.
     *
     * @param invoice the invoice
     * @return the issue date/time
     */
    private Date getIssueDatetime(InvoiceType invoice) {
        IssueDateType issueDate = invoice.getIssueDate();
        if (issueDate == null) {
            throw new IllegalArgumentException("IssueDate not provided");
        }
        XMLGregorianCalendar calendar = issueDate.getValue();
        if (calendar == null) {
            throw new IllegalArgumentException("IssueDate is invalid");
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

    private Party getSupplier(InvoiceType invoice) {
        SupplierPartyType supplierType = invoice.getAccountingSupplierParty();
        if (supplierType == null) {
            throw new IllegalArgumentException("AccountingSupplierParty not provided");
        }
        CustomerAssignedAccountIDType accountId = supplierType.getCustomerAssignedAccountID();
        if (accountId == null) {
            throw new IllegalArgumentException("AccountingSupplierParty/CustomerAssignedAccountID not provided");
        }
        long id = NumberUtils.toLong(accountId.getValue(), -1);
        if (id == -1) {
            throw new IllegalArgumentException("AccountingSupplierParty/CustomerAssignedAccountID is invalid: " + id);
        }

        Party result = (Party) getObject(id, "party.supplier*");
        if (result == null) {
            throw new IllegalArgumentException(
                    "AccountingSupplierParty/CustomerAssignedAccountID is not a valid supplier: " + id);
        }
        return result;
    }

    private Party getStockLocation(InvoiceType invoice) {
        CustomerPartyType customerType = invoice.getAccountingCustomerParty();
        if (customerType == null) {
            throw new IllegalArgumentException("AccountingCustomerParty not provided");
        }
        CustomerAssignedAccountIDType accountId = customerType.getCustomerAssignedAccountID();
        if (accountId == null) {
            throw new IllegalArgumentException("AccountingCustomerParty/CustomerAssignedAccountID not provided");
        }
        long id = NumberUtils.toLong(accountId.getValue(), -1);
        if (id == -1) {
            throw new IllegalArgumentException("AccountingCustomerParty/CustomerAssignedAccountID is invalid: " + id);
        }
        IMObjectReference ref = new IMObjectReference(STOCK_LOCATION, id);
        Party result = (Party) service.get(ref);
        if (result == null) {
            throw new IllegalArgumentException(
                    "AccountingCustomerParty/CustomerAssignedAccountID is not a valid stock location: " + id);
        }
        return result;
    }

    private BigDecimal getAmount(AmountType amount, String practiceCurrency, String path) {
        if (amount == null) {
            throw new IllegalArgumentException(path + " not provided");

        }
        CurrencyCodeContentType currency = amount.getCurrencyID();
        if (currency == null || StringUtils.isEmpty(currency.value())) {
            throw new IllegalArgumentException(path + "/CurrencyID not provided");
        }
        if (!ObjectUtils.equals(practiceCurrency, currency.value())) {
            throw new IllegalArgumentException(path + "/CurrencyID invalid. Expected "
                                               + practiceCurrency + ", but got " + currency);
        }
        return amount.getValue();
    }

    private IMObject getObject(long id, String... shortNames) {
        IMObject result = null;
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        query.add(Constraints.eq("id", id));
        IPage<IMObject> page = service.get(query);
        if (page.getResults().size() == 1) {
            result = page.getResults().get(0);
        }
        return result;
    }
}
