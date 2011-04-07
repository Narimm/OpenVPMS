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
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.map.ErrorContext;
import org.openvpms.esci.adapter.map.UBLFinancialType;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.common.aggregate.InvoiceLineType;
import org.openvpms.esci.ubl.common.aggregate.ItemIdentificationType;
import org.openvpms.esci.ubl.common.aggregate.ItemType;
import org.openvpms.esci.ubl.common.aggregate.OrderLineReferenceType;
import org.openvpms.esci.ubl.common.aggregate.OrderReferenceType;
import org.openvpms.esci.ubl.common.aggregate.PriceType;
import org.openvpms.esci.ubl.common.aggregate.PricingReferenceType;
import org.openvpms.esci.ubl.common.basic.BaseQuantityType;
import org.openvpms.esci.ubl.common.basic.InvoicedQuantityType;
import org.openvpms.esci.ubl.common.basic.LineIDType;
import org.openvpms.esci.ubl.common.basic.PackQuantityType;
import org.openvpms.esci.ubl.common.basic.PackSizeNumericType;
import org.openvpms.esci.ubl.common.basic.PriceTypeCodeType;

import java.math.BigDecimal;
import java.util.List;


/**
 * Wrapper for the <tt>InvoiceLineType</tt> class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UBLInvoiceLine extends UBLFinancialType {

    /**
     * The invoice line.
     */
    private final InvoiceLineType line;

    /**
     * Supplier rules.
     */
    private final SupplierRules supplierRules;

    /**
     * Order archetype id.
     */
    private static final ArchetypeId ORDER = new ArchetypeId(SupplierArchetypes.ORDER);

    /**
     * Order item archetype id.
     */
    private static final ArchetypeId ORDER_ITEM = new ArchetypeId(SupplierArchetypes.ORDER_ITEM);

    /**
     * UN/CEFACT wholesale price type code identifier.
     * See http://www.unece.org/uncefact/codelist/standard/UNECE_PriceTypeCode_D09B.xsd
     */
    private static final String WHOLESALE = "WH";

    /**
     * Path to the alternative condidition price.
     */
    private static final String ALTERNATIVE_CONDITION_PRICE = "PricingReference/AlternativeConditionPrice";

    /**
     * Path to the alternative condidition price type code.
     */
    private static final String ALTERNATIVE_CONDITION_PRICE_TYPE_CODE = ALTERNATIVE_CONDITION_PRICE + "/PriceTypeCode";


    /**
     * Constructs an <tt>UBLInvoiceLine</tt>.
     *
     * @param line             the invoice line
     * @param parent           the parent invoice
     * @param expectedCurrency the expected currency for all amounts
     * @param service          the archetype service
     * @param rules            supplier rules
     */
    public UBLInvoiceLine(InvoiceLineType line, UBLInvoice parent, String expectedCurrency, IArchetypeService service,
                          SupplierRules rules) {
        super(parent, expectedCurrency, service);
        this.line = line;
        this.supplierRules = rules;
    }

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    public String getType() {
        return "InvoiceLine";
    }

    /**
     * Returns the identifier for an invoice line.
     *
     * @return the identifier
     * @throws ESCIAdapterException if the identifier isn't set
     */
    public String getID() {
        return getId(line.getID(), "ID");
    }

    /**
     * Determines if the {@link #getType type} and {@link #getID identifier} of this should be used for
     * error reporting. If not, then the parent should be used.
     *
     * @return true
     */
    @Override
    public boolean useForErrorReporting() {
        return true;
    }

    /**
     * Returns the order reference.
     *
     * @return the order reference, or <tt>null</tt> if there is no associated order
     */
    public IMObjectReference getOrderReference() {
        IMObjectReference result = null;
        OrderLineReferenceType orderLineRef = getOrderLineReference();
        if (orderLineRef != null) {
            OrderReferenceType orderRef = orderLineRef.getOrderReference();
            if (orderRef != null) {
                result = getReference(ORDER, orderRef.getID(), "OrderLineReference/OrderReference");
            }
        }
        return result;
    }

    /**
     * Returns the order item reference.
     * <p/>
     * <em>NOTE:</em> an item reference with a id of <tt>-1</tt> indicates that only the order is known, and that
     * the item must be matched on product and quantity.
     *
     * @return the order item reference, or <tt>null</tt> if there is no associated order item
     */
    public IMObjectReference getOrderItemReference() {
        IMObjectReference result = null;
        OrderLineReferenceType ref = getOrderLineReference();
        if (ref != null) {
            LineIDType id = ref.getLineID();
            result = getReference(ORDER_ITEM, id, "OrderLineReference/LineID");
        }
        return result;
    }

    /**
     * Returns the order line reference.
     *
     * @return the order line reference, or <tt>null</tt> none is present
     * @throws ESCIAdapterException if the reference is incorrectly specified
     */
    public OrderLineReferenceType getOrderLineReference() {
        OrderLineReferenceType result = null;
        List<OrderLineReferenceType> list = line.getOrderLineReference();
        if (!list.isEmpty()) {
            if (list.size() != 1) {
                throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidCardinality(
                        "OrderLineReference", getType(), getID(), "1", list.size()));
            }
            result = list.get(0);
        }
        return result;
    }

    /**
     * Returns the associated order, if one is explicitly reference.
     *
     * @return the order, or <tt>null</tt> if the invoice line isn't associated with an order
     * @throws ESCIAdapterException      if the order was specified, but could not be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct getOrder() {
        FinancialAct result = null;
        IMObjectReference ref = getOrderReference();
        if (ref != null) {
            result = (FinancialAct) getArchetypeService().get(ref);
            if (result == null) {
                throw new ESCIAdapterException(ESCIAdapterMessages.invalidOrder(
                        getType(), getID(), Long.toString(ref.getId())));
            }
        }
        return result;
    }

    /**
     * Returns the associated order item.
     *
     * @return the order item, or <tt>null</tt> if there is no associated order item
     * @throws ESCIAdapterException if the order item is specified incorrectly, or the referenced order item cannot be
     *                              found
     */
    public FinancialAct getOrderItem() {
        FinancialAct result = null;
        IMObjectReference ref = getOrderItemReference();
        if (ref != null && ref.getId() != -1) {
            result = (FinancialAct) getArchetypeService().get(ref);
            if (result == null) {
                throw new ESCIAdapterException(ESCIAdapterMessages.invoiceInvalidOrderItem(
                        getID(), Long.toString(ref.getId())));
            }
        }
        return result;
    }

    /**
     * Returns the line extension amount.
     *
     * @return the line extension amount
     * @throws ESCIAdapterException if the amount isn't present, is invalid, or has a currency the doesn't match that
     *                              expected
     */
    public BigDecimal getLineExtensionAmount() {
        return getAmount(line.getLineExtensionAmount(), "LineExtensionAmount");
    }

    /**
     * Returns the invoiced quantity.
     *
     * @return the invoiced quantity
     * @throws ESCIAdapterException if the quantity doesn't exist or is &lt;= zero
     */
    public BigDecimal getInvoicedQuantity() {
        return getQuantity(line.getInvoicedQuantity(), "InvoicedQuantity");
    }

    /**
     * Returns the invoiced quantity unit code.
     * <p/>
     * This corresponds to <em>InvoiceLine/InvoicedQuantity@unitCode</em>
     *
     * @return the invoiced quantity unit code
     * @throws ESCIAdapterException if the unit code doesn't exist or is invalid
     */
    public String getInvoicedQuantityUnitCode() {
        InvoicedQuantityType quantity = getRequired(line.getInvoicedQuantity(), "InvoicedQuantity");
        return getRequired(quantity.getUnitCode(), "InvoicedQuantity@unitCode");
    }

    /**
     * Returns the product referenced by an invoice line.
     * <p/>
     * This implementation requires that one of Item/BuyersItemIdentification and Item/SellersItemIdentification is
     * provided. It will use BuyersItemIdentification in preference to SellersItemIdentification.
     *
     * @param supplier the supplier
     * @return the corresponding product, or <tt>null</tt> if the reference product is not found
     * @throws ESCIAdapterException      if the product is incorrectly specified
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Product getProduct(Party supplier) {
        Product result = null;
        long productId = getBuyersItemID();
        String sellerId = getSellersItemID();
        if (productId == -1 && sellerId == null) {
            throw new ESCIAdapterException(ESCIAdapterMessages.invoiceNoProduct(getID()));
        }
        if (productId != -1) {
            result = getProduct(productId);
        }
        if (result == null && sellerId != null) {
            // try and find the product by seller id.
            result = getProduct(sellerId, supplier);
        }
        return result;
    }

    /**
     * Returns the buyer's item identifier.
     * <p/>
     * This corresponds to <em>InvoiceLine/Item/BuyersItemIdentification/ID</em>.
     *
     * @return the buyer's item identifier, or <tt>-1</tt> if none was specified
     * @throws ESCIAdapterException if there is no item or the identifier was incorrectly specified
     */
    public long getBuyersItemID() {
        long result = -1;
        ItemType item = getItem();
        ItemIdentificationType id = item.getBuyersItemIdentification();
        if (id != null) {
            result = getNumericId(id.getID(), "Item/BuyersItemIdentification/ID");
        }
        return result;
    }

    /**
     * Returns the seller's item identifier.
     * </p>
     * This corresponds to <em>InvoiceLine/Item/SellersItemIdentification/ID</em>.
     *
     * @return the sellers's item identifier, or <tt>null</tt> if none was specified
     * @throws ESCIAdapterException if there is no item or the identifier is incorrectly specified
     */
    public String getSellersItemID() {
        String result = null;
        ItemType item = getItem();
        ItemIdentificationType sellerId = item.getSellersItemIdentification();
        if (sellerId != null) {
            result = getId(sellerId.getID(), "Item/SellersItemIdentification/ID");
        }
        return result;
    }

    /**
     * Returns the item's name.
     *
     * @return the item name, or <tt>null</tt> if one was not specified
     */
    public String getItemName() {
        ItemType item = getItem();
        return (item.getName() != null) ? StringUtils.trimToNull(item.getName().getValue()) : null;
    }

    /**
     * Returns the package size.
     *
     * @return the package size, or <tt>0.0</tt> if none was specified
     */
    public BigDecimal getPackSizeNumeric() {
        PackSizeNumericType packSize = getItem().getPackSizeNumeric();
        return (packSize != null) ? packSize.getValue() : BigDecimal.ZERO;
    }

    /**
     * Returns the package quantity.
     *
     * @return the package quantity, or <tt>null</tt> if none was specified
     */
    public BigDecimal getPackQuantity() {
        PackQuantityType quantity = getItem().getPackQuantity();
        return (quantity != null) ? quantity.getValue() : null;
    }

    /**
     * Returns the package quantity unit code.
     *
     * @return the package quantity unit code, or <tt>null</tt> if none was specified
     * @throws ESCIAdapterException if the PackQuantity is specified without the unitCode
     */
    public String getPackQuantityUnitCode() {
        PackQuantityType quantity = getItem().getPackQuantity();
        return (quantity != null) ? getRequired(quantity.getUnitCode(), "PackQuantity@unitCode") : null;
    }

    /**
     * Returns the price.
     * <p/>
     * This corresponds to <em>InvoiceLine/Price/PriceAmount</em>.
     *
     * @return the price amount
     * @throws ESCIAdapterException if the price isn't present or is incorrectly specified
     */
    public BigDecimal getPriceAmount() {
        PriceType price = getRequired(line.getPrice(), "Price");
        return getAmount(price.getPriceAmount(), "Price/PriceAmount");
    }

    /**
     * Returns the wholesale (or list) price.
     * <p/>
     * This looks for a single PricingReference/AlternativeConditionPrice. If present, it must have a PriceTypeCode
     * of "WH" (wholesale).
     *
     * @return the list price, <tt>null</tt> if no list price is specified
     */
    public BigDecimal getWholesalePrice() {
        BigDecimal result = null;
        PricingReferenceType pricing = line.getPricingReference();
        if (pricing != null) {
            List<PriceType> prices = pricing.getAlternativeConditionPrice();
            if (!prices.isEmpty()) {
                PriceType price = prices.get(0);
                result = getAmount(price.getPriceAmount(), ALTERNATIVE_CONDITION_PRICE + "/PriceAmount");
                PriceTypeCodeType code = getRequired(price.getPriceTypeCode(), ALTERNATIVE_CONDITION_PRICE_TYPE_CODE);
                if (!WHOLESALE.equals(code.getValue())) {
                    ErrorContext context = new ErrorContext(this, ALTERNATIVE_CONDITION_PRICE_TYPE_CODE);
                    throw new ESCIAdapterException(ESCIAdapterMessages.ublInvalidValue(
                            context.getPath(), context.getType(), context.getID(), WHOLESALE, code.getValue()));
                }
            }
        }
        return result;
    }

    /**
     * Returns the base quantity.
     *
     * @return the base quantity. May be <tt>null</tt>
     */
    public BigDecimal getBaseQuantity() {
        BaseQuantityType quantity = line.getPrice().getBaseQuantity();
        return (quantity != null) ? quantity.getValue() : null;
    }

    /**
     * Returns the base quantity unit code.
     *
     * @return the base quantity unit code. May be <tt>null</tt>
     */
    public String getBaseQuantityUnitCode() {
        BaseQuantityType quantity = line.getPrice().getBaseQuantity();
        return (quantity != null) ? quantity.getUnitCode() : null;
    }

    /**
     * Returns the tax for the invoice line.
     *
     * @return the invoice line tax
     * @throws ESCIAdapterException if the tax is incorrectly specified
     */
    public BigDecimal getTaxAmount() {
        return getTaxAmount(line.getTaxTotal());
    }

    /**
     * Returns the tax subtotal for the invoice line.
     * <p/>
     * This corresponds to <em>TaxTotal/TaxSubtotal</em> (i.e only one TaxTotal with one TaxSubtotal is
     * supported).
     *
     * @return the tax sub total, or <tt>null</tt> if no tax is specified
     * @throws ESCIAdapterException if the tax is incorrectly specified
     */
    public UBLTaxSubtotal getTaxSubtotal() {
        return getTaxSubtotal(line.getTaxTotal());
    }

    /**
     * Returns a product given its id.
     *
     * @param id the product identifier
     * @return the corresponding product, or <tt>null</tt> if it can't be found
     * @throws ArchetypeServiceException for any archetype service error
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
     * @throws ArchetypeServiceException for any archetype service error
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
     * Returns the item associated with the invoice line.
     *
     * @return the item
     * @throws ESCIAdapterException if the item isn't specified
     */
    protected ItemType getItem() {
        return getRequired(line.getItem(), "Item");
    }

}
