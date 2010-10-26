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
import org.oasis.ubl.common.aggregate.InvoiceLineType;
import org.oasis.ubl.common.aggregate.ItemIdentificationType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.OrderLineReferenceType;
import org.oasis.ubl.common.aggregate.PriceType;
import org.oasis.ubl.common.aggregate.PricingReferenceType;
import org.oasis.ubl.common.basic.InvoicedQuantityType;
import org.oasis.ubl.common.basic.LineIDType;
import org.oasis.ubl.common.basic.PriceTypeCodeType;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.UBLFinancialType;
import org.openvpms.esci.exception.ESCIException;

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
     * Order item archetype id.
     */
    private static final ArchetypeId ORDER_ITEM = new ArchetypeId(SupplierArchetypes.ORDER_ITEM);

    /**
     * UN/CEFACT wholesale price type code identifier.
     * See http://www.unece.org/uncefact/codelist/standard/UNECE_PriceTypeCode_D09B.xsd
     */
    private static final String WHOLESALE = "WS";


    public UBLInvoiceLine(InvoiceLineType line, String expectedCurrency, IArchetypeService service,
                          SupplierRules rules) {
        super(expectedCurrency, service);
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
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the identifier isn't set
     */
    public String getID() {
        return getId(line.getID(), "ID", getType(), null);
    }

    /**
     * Returns the order item reference.
     *
     * @return the order item reference, or <tt>null</tt> if there is no associated order item
     */
    public IMObjectReference getOrderItemRef() {
        IMObjectReference result = null;
        List<OrderLineReferenceType> list = line.getOrderLineReference();
        if (!list.isEmpty()) {
            if (list.size() != 1) {
                Message message = ESCIAdapterMessages.ublInvalidCardinality("OrderLineReference", getType(),
                                                                            getID(), "1", list.size());
                throw new ESCIException(message.toString());
            }
            LineIDType id = list.get(0).getLineID();
            result = getReference(ORDER_ITEM, id, "OrderLineReference/LineID");
        }
        return result;
    }

    /**
     * Returns the associated order item.
     *
     * @return the order item, or <tt>null</tt> if there is no associated order item
     * @throws ESCIException if the order item is specified incorrectly, or the referenced order item cannot be found
     */
    public FinancialAct getOrderItem() {
        FinancialAct result = null;
        IMObjectReference ref = getOrderItemRef();
        if (ref != null) {
            result = (FinancialAct) getArchetypeService().get(ref);
            if (result == null) {
                Message message = ESCIAdapterMessages.invoiceInvalidOrderItem(getID(), Long.toString(ref.getId()));
                throw new ESCIException(message.toString());
            }
        }
        return result;
    }

    /**
     * Returns the line extension amount.
     *
     * @return the line extension amount
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the amount isn't present, is invalid, or has a currency the
     *          doesn't match that expected
     */
    public BigDecimal getLineExtensionAmount() {
        return getAmount(line.getLineExtensionAmount(), "LineExtensionAmount");
    }

    /**
     * Returns the invoiced quantity.
     *
     * @return the invoiced quantity
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the quantity doesn't exist or is &lt;= zero
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
     * @throws ESCIException if the unit code doesn't exist or is invalid
     */
    public String getInvoicedQuantityUnitCode() {
        InvoicedQuantityType quantity = line.getInvoicedQuantity();
        checkRequired(quantity, "InvoicedQuantity", "InvoiceLine", getID());
        return getRequired(quantity.getUnitCode(), "InvoicedQuantity@unitCode", "InvoiceLine", getID());
    }

    /**
     * Returns the product referenced by an invoice line.
     * <p/>
     * This implementation requires that one of Item/BuyersItemIdentification and Item/SellersItemIdentification is
     * provided. It will use BuyersItemIdentification in preference to SellersItemIdentification.
     *
     * @param supplier the supplier
     * @return the corresponding product, or <tt>null</tt> if the reference product is not found
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the product is incorrectly specified
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    public Product getProduct(Party supplier) {
        Product result = null;
        long productId = getBuyersItemID();
        String sellerId = getSellersItemID();
        if (productId == -1 && sellerId == null) {
            Message message = ESCIAdapterMessages.invoiceNoProduct(getID());
            throw new ESCIException(message.toString());
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
     * @throws ESCIException if there is no item or the identifier was incorrectly specified
     */
    public long getBuyersItemID() {
        long result = -1;
        ItemType item = getItem();
        ItemIdentificationType id = item.getBuyersItemIdentification();
        if (id != null) {
            result = getNumericId(id.getID(), "Item/BuyersItemIdentification/ID", "InvoiceLine", getID());
        }
        return result;
    }

    /**
     * Returns the seller's item identifier.
     * </p>
     * This corresponds to <em>InvoiceLine/Item/SellersItemIdentification/ID</em>.
     *
     * @return the sellers's item identifier, or <tt>null</tt> if none was specified
     * @throws ESCIException if there is no item or the identifier is incorrectly specified
     */
    public String getSellersItemID() {
        String result = null;
        ItemType item = getItem();
        ItemIdentificationType sellerId = item.getSellersItemIdentification();
        if (sellerId != null) {
            result = getId(sellerId.getID(), "Item/SellersItemIdentification/ID", "InvoiceLine", getID());
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
     * Returns the price.
     * <p/>
     * This corresponds to <em>InvoiceLine/Price/PriceAmount</em>.
     *
     * @return the price amount
     * @throws ESCIException if the price isn't present or is incorrectly specified
     */
    public BigDecimal getPriceAmount() {
        PriceType price = getRequired(line.getPrice(), "Price", "InvoiceLine", getID());
        return getAmount(price.getPriceAmount(), "Price/PriceAmount", "InvoiceLine", getID());
    }

    /**
     * Returns the wholesale (or list) price.
     * <p/>
     * This looks for a single PricingReference/AlternativeConditionPrice. If present, it must have a PriceTypeCode
     * of "WS" (wholesale).
     *
     * @return the list price, or <tt>0.0</tt> if no wholesale price is specified
     */
    public BigDecimal getWholesalePrice() {
        BigDecimal result = BigDecimal.ZERO;
        PricingReferenceType pricing = line.getPricingReference();
        if (pricing != null) {
            List<PriceType> prices = pricing.getAlternativeConditionPrice();
            if (!prices.isEmpty()) {
                PriceType price = prices.get(0);
                result = getAmount(price.getPriceAmount(),
                                   "PricingReference/AlternativeConditionPrice/PriceAmount", "InvoiceLine", getID());
                PriceTypeCodeType code = getRequired(
                        price.getPriceTypeCode(), "PricingReference/AlternativeConditionPrice/PriceTypeCode",
                        "InvoiceLine", getID());
                if (!WHOLESALE.equals(code.getValue())) {
                    Message message = ESCIAdapterMessages.ublInvalidValue(
                            "PricingReference/AlternativeConditionPrice/PriceTypeCode", "InvoiceLine", getID(),
                            WHOLESALE, code.getValue());
                    throw new ESCIException(message.toString());
                }
            }
        }
        return result;
    }

    /**
     * Returns the tax for the invoice line.
     *
     * @return the invoice line tax
     * @throws ESCIException if the tax is incorrectly specified
     */
    public BigDecimal getTax() {
        return getTax(line.getTaxTotal(), "InvoiceLine", getID());
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
     * Returns the item associated with the invoice line.
     *
     * @return the item
     * @throws ESCIException if the item isn't specified
     */
    protected ItemType getItem() {
        return getRequired(line.getItem(), "Item", "InvoiceLine", getID());
    }

}
