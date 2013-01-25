package org.openvpms.archetype.rules.supplier;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;

import java.math.BigDecimal;

/**
 * Used by {@link OrderGenerator} to manage stock information.
 *
 * @author Tim Anderson
 */
class Stock {

    /**
     * The product.
     */
    private final Product product;

    /**
     * The stock location.
     */
    private final Party stockLocation;

    /**
     * The supplier.
     */
    private final Party supplier;

    /**
     * The quantity.
     */
    private final BigDecimal quantity;

    /**
     * The ideal quantity.
     */
    private final BigDecimal idealQty;

    /**
     * The stock on order.
     */
    private final BigDecimal onOrder;

    /**
     * The stock to order.
     */
    private final BigDecimal toOrder;

    /**
     * The reorder code.
     */
    private final String reorderCode;

    /**
     * The reorder description.
     */
    private final String reorderDescription;

    /**
     * The package size.
     */
    private final int packageSize;

    /**
     * The package units.
     */
    private final String packageUnits;

    /**
     * The unit price.
     */
    private final BigDecimal unitPrice;

    /**
     * The list price.
     */
    private final BigDecimal listPrice;

    /**
     * Constructs a {@code Stock}.
     *
     * @param product            the product
     * @param stockLocation      the stock location
     * @param supplier           the supplier
     * @param quantity           the quantity
     * @param idealQty           the ideal quantity
     * @param onOrder            the stock on order
     * @param toOrder            the stock to order
     * @param reorderCode        the reorder code. May be {@code null}
     * @param reorderDescription the reorder description. May be {@code null}
     * @param packageSize        the package size
     * @param packageUnits       the package units
     * @param unitPrice          the unit price
     * @param listPrice          the list price
     */
    public Stock(Product product, Party stockLocation, Party supplier, BigDecimal quantity,
                 BigDecimal idealQty, BigDecimal onOrder, BigDecimal toOrder, String reorderCode,
                 String reorderDescription, int packageSize, String packageUnits, BigDecimal unitPrice,
                 BigDecimal listPrice) {
        this.product = product;
        this.stockLocation = stockLocation;
        this.supplier = supplier;
        this.quantity = quantity;
        this.idealQty = idealQty;
        this.onOrder = onOrder;
        this.toOrder = toOrder;
        this.reorderCode = reorderCode;
        this.reorderDescription = reorderDescription;
        this.packageSize = packageSize;
        this.packageUnits = packageUnits;
        this.unitPrice = unitPrice;
        this.listPrice = listPrice;
    }

    /**
     * Returns the product.
     *
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location
     */
    public Party getStockLocation() {
        return stockLocation;
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier
     */
    public Party getSupplier() {
        return supplier;
    }

    /**
     * Returns the quantity on hand.
     *
     * @return the quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Returns the ideal quantity.
     *
     * @return the ideal quantity
     */
    public BigDecimal getIdealQty() {
        return idealQty;
    }

    /**
     * Returns the quantity on order.
     *
     * @return the quantity on order
     */
    public BigDecimal getOnOrder() {
        return onOrder;
    }

    /**
     * Returns the quantity to order.
     *
     * @return the quantity to order
     */
    public BigDecimal getToOrder() {
        return toOrder;
    }

    /**
     * Returns the reorder code.
     *
     * @return the reorder code
     */
    public String getReorderCode() {
        return reorderCode;
    }

    /**
     * Returns the reorder description.
     *
     * @return the reorder description
     */
    public String getReorderDescription() {
        return reorderDescription;
    }

    /**
     * Returns the package size.
     *
     * @return the package size
     */
    public int getPackageSize() {
        return packageSize;
    }

    /**
     * Returns the package units
     *
     * @return the package units
     */
    public String getPackageUnits() {
        return packageUnits;
    }

    /**
     * Returns the unit price.
     *
     * @return the unit price
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Returns the list price.
     *
     * @return the list price
     */
    public BigDecimal getListPrice() {
        return listPrice;
    }
}
