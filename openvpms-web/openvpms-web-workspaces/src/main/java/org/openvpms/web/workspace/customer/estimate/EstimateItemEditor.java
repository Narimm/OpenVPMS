/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.workspace.customer.PriceActItemEditor;

import java.math.BigDecimal;
import java.util.Date;

import static org.openvpms.archetype.rules.product.ProductArchetypes.TEMPLATE;
import static org.openvpms.web.echo.style.Styles.CELL_SPACING;


/**
 * An editor for {@link Act}s which have an archetype of <em>act.customerEstimationItem</em>.
 *
 * @author Tim Anderson
 */
public class EstimateItemEditor extends PriceActItemEditor {

    /**
     * Low quantity selling units.
     */
    private Label lowQtySellingUnits = LabelFactory.create();

    /**
     * High quantity selling units.
     */
    private Label highQtySellingUnits = LabelFactory.create();

    /**
     * Fixed price node name.
     */
    private static final String FIXED_PRICE = "fixedPrice";

    /**
     * Low unit price node name.
     */
    private static final String LOW_UNIT_PRICE = "lowUnitPrice";

    /**
     * High unit price node name.
     */
    private static final String HIGH_UNIT_PRICE = "highUnitPrice";

    /**
     * Low quantity node name.
     */
    private static final String LOW_QTY = "lowQty";

    /**
     * High quantity node name.
     */
    private static final String HIGH_QTY = "highQty";

    /**
     * Low discount node name.
     */
    private static final String LOW_DISCOUNT = "lowDiscount";

    /**
     * High discount node name.
     */
    private static final String HIGH_DISCOUNT = "highDiscount";

    /**
     * Nodes to display when a product template is selected.
     */
    private static final ArchetypeNodes TEMPLATE_NODES = new ArchetypeNodes().exclude(
            LOW_QTY, HIGH_QTY, FIXED_PRICE, LOW_UNIT_PRICE, HIGH_UNIT_PRICE, LOW_DISCOUNT, HIGH_DISCOUNT,
            "lowTotal", "highTotal");


    /**
     * Constructs an {@link EstimateItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public EstimateItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, EstimateArchetypes.ESTIMATE_ITEM)) {
            throw new IllegalArgumentException("Invalid act type:" + act.getArchetypeId().getShortName());
        }
        setDisableDiscounts(getDisableDiscounts(getLocation()));
        if (act.isNew()) {
            // default the act start time to today
            act.setActivityStartTime(new Date());
        }

        ArchetypeNodes nodes = getFilterForProduct(getProductRef());
        setArchetypeNodes(nodes);

        // add a listener to update the discount when the fixed, high unit price
        // or quantity, changes
        ModifiableListener listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateDiscount();
            }
        };
        ModifiableListener lowListener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                updateLowDiscount();
            }
        };
        ModifiableListener highListener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                updateHighDiscount();
            }
        };
        getProperty(FIXED_PRICE).addModifiableListener(listener);
        getProperty(LOW_UNIT_PRICE).addModifiableListener(lowListener);
        getProperty(LOW_QTY).addModifiableListener(lowListener);
        getProperty(HIGH_UNIT_PRICE).addModifiableListener(highListener);
        getProperty(HIGH_QTY).addModifiableListener(highListener);
    }

    /**
     * Sets the product quantity.
     *
     * @param quantity the product quantity
     */
    public void setQuantity(BigDecimal quantity) {
        setLowQuantity(quantity);
        setHighQuantity(quantity);
    }

    /**
     * Returns the quantity.
     * <p/>
     * This implementation returns the high quantity.
     *
     * @return the quantity
     */
    @Override
    public BigDecimal getQuantity() {
        return getHighQuantity();
    }

    /**
     * Returns the low quantity.
     *
     * @return the low quantity
     */
    public BigDecimal getLowQuantity() {
        return getProperty(LOW_QTY).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the low quantity.
     *
     * @param quantity the low quantity
     */
    public void setLowQuantity(BigDecimal quantity) {
        getProperty(LOW_QTY).setValue(quantity);
    }

    /**
     * Returns the high quantity.
     *
     * @return the high quantity
     */
    public BigDecimal getHighQuantity() {
        return getProperty(HIGH_QTY).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the high quantity.
     *
     * @param quantity the high quantity
     */
    public void setHighQuantity(BigDecimal quantity) {
        getProperty(HIGH_QTY).setValue(quantity);
    }

    /**
     * Sets the unit price.
     * <p/>
     * This implementation updates both the lowUnitPrice and highUnitPrice.
     *
     * @param unitPrice the unit price
     */
    @Override
    public void setUnitPrice(BigDecimal unitPrice) {
        getProperty(LOW_UNIT_PRICE).setValue(unitPrice);
        getProperty(HIGH_UNIT_PRICE).setValue(unitPrice);
    }

    /**
     * Returns the unit price.
     * <p/>
     * This implementation returns the high unit price.
     *
     * @return the unit price
     */
    @Override
    public BigDecimal getUnitPrice() {
        return getHighUnitPrice();
    }

    /**
     * Returns the low unit price.
     *
     * @return the low unit price
     */
    public BigDecimal getLowUnitPrice() {
        return getProperty(LOW_UNIT_PRICE).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Returns the high unit price.
     *
     * @return the high unit price
     */
    public BigDecimal getHighUnitPrice() {
        return getProperty(HIGH_UNIT_PRICE).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the low discount.
     *
     * @param lowDiscount the low discount
     */
    public void setLowDiscount(BigDecimal lowDiscount) {
        getProperty(LOW_DISCOUNT).setValue(lowDiscount);
    }

    /**
     * Sets the high discount.
     *
     * @param highDiscount the high discount
     */
    public void setHighDiscount(BigDecimal highDiscount) {
        getProperty(HIGH_DISCOUNT).setValue(highDiscount);
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        ProductParticipationEditor product = getProductEditor();
        if (product != null) {
            // register the location in order to determine service ratios
            product.setLocation(getLocation());
        }
    }

    /**
     * Invoked when the product is changed, to update prices.
     *
     * @param product the product. May be {@code null}
     */
    @Override
    protected void productModified(Product product) {
        super.productModified(product);

        Property lowDiscount = getProperty(LOW_DISCOUNT);
        Property highDiscount = getProperty(HIGH_DISCOUNT);
        lowDiscount.setValue(BigDecimal.ZERO);
        highDiscount.setValue(BigDecimal.ZERO);

        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            if (getArchetypeNodes() != TEMPLATE_NODES) {
                changeLayout(TEMPLATE_NODES);
            }
            // zero out the fixed, low and high prices.
            Property fixedPrice = getProperty(FIXED_PRICE);
            Property lowUnitPrice = getProperty(LOW_UNIT_PRICE);
            Property highUnitPrice = getProperty(HIGH_UNIT_PRICE);
            fixedPrice.setValue(BigDecimal.ZERO);
            lowUnitPrice.setValue(BigDecimal.ZERO);
            highUnitPrice.setValue(BigDecimal.ZERO);
            updateSellingUnits(null);
        } else {
            ArchetypeNodes currentNodes = getArchetypeNodes();
            IMObjectReference productRef = (product != null) ? product.getObjectReference() : null;
            ArchetypeNodes expectedFilter = getFilterForProduct(productRef);
            if (!ObjectUtils.equals(currentNodes, expectedFilter)) {
                changeLayout(expectedFilter);
            }
            Property fixedPrice = getProperty(FIXED_PRICE);
            Property lowUnitPrice = getProperty(LOW_UNIT_PRICE);
            Property highUnitPrice = getProperty(HIGH_UNIT_PRICE);
            ProductPrice fixed = null;
            ProductPrice unit = null;
            if (product != null) {
                fixed = getDefaultFixedProductPrice(product);
                unit = getDefaultUnitProductPrice(product);
            }

            if (fixed != null) {
                fixedPrice.setValue(getPrice(product, fixed));
            } else {
                fixedPrice.setValue(BigDecimal.ZERO);
            }
            if (unit != null) {
                BigDecimal price = getPrice(product, unit);
                lowUnitPrice.setValue(price);
                highUnitPrice.setValue(price);
            } else {
                lowUnitPrice.setValue(BigDecimal.ZERO);
                highUnitPrice.setValue(BigDecimal.ZERO);
            }
            updateSellingUnits(product);
        }
        notifyProductListener(product);
    }

    /**
     * Returns the fixed cost.
     * <p/>
     * TODO - estimates lose the fixed cost if the fixed price is changed
     *
     * @return the fixed cost
     */
    @Override
    protected BigDecimal getFixedCost() {
        ProductPrice price = getFixedProductPrice(getProduct());
        return getCostPrice(price);
    }

    /**
     * Returns the unit cost.
     * <p/>
     * TODO - estimates lose the unit cost if the unit price is changed
     *
     * @return the unit cost
     */
    @Override
    protected BigDecimal getUnitCost() {
        ProductPrice price = getUnitProductPrice(getProduct());
        return getCostPrice(price);
    }

    /**
     * Calculates the discount amounts.
     *
     * @return {@code true} if a discount was updated
     */
    @Override
    protected boolean updateDiscount() {
        boolean updated = updateLowDiscount();
        updated |= updateHighDiscount();
        return updated;
    }

    /**
     * Creates the layout strategy.
     *
     * @param fixedPrice the fixed price editor
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy(FixedPriceEditor fixedPrice) {
        return new EstimateItemLayoutStrategy(fixedPrice);
    }

    /**
     * Updates the selling units label.
     *
     * @param product the product. May be {@code null}
     */
    private void updateSellingUnits(Product product) {
        String units = "";
        if (product != null) {
            IMObjectBean bean = new IMObjectBean(product);
            String node = "sellingUnits";
            if (bean.hasNode(node)) {
                units = LookupNameHelper.getName(product, node);
            }
        }
        lowQtySellingUnits.setText(units);
        highQtySellingUnits.setText(units);
    }

    /**
     * Updates the low discount.
     *
     * @return {@code true} if the discount was updated
     */
    private boolean updateLowDiscount() {
        boolean result = false;
        try {
            BigDecimal unitPrice = getLowUnitPrice();
            BigDecimal quantity = getLowQuantity();
            BigDecimal amount = calculateDiscount(unitPrice, quantity);
            // If discount amount calculates to zero don't update any existing value as may have been manually modified.
            if (getDisableDiscounts() || amount.compareTo(BigDecimal.ZERO) != 0) {
                Property discount = getProperty(LOW_DISCOUNT);
                result = discount.setValue(amount);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

    /**
     * Updates the high discount.
     *
     * @return {@code true} if the discount was updated
     */
    private boolean updateHighDiscount() {
        boolean result = false;
        try {
            BigDecimal unitPrice = getHighUnitPrice();
            BigDecimal quantity = getHighQuantity();
            BigDecimal amount = calculateDiscount(unitPrice, quantity);
            // If discount amount calculates to zero don't update any existing value as may have been manually modified.
            if (getDisableDiscounts() || amount.compareTo(BigDecimal.ZERO) != 0) {
                Property discount = getProperty(HIGH_DISCOUNT);
                discount.setValue(amount);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

    protected class EstimateItemLayoutStrategy extends PriceItemLayoutStrategy {
        public EstimateItemLayoutStrategy(FixedPriceEditor fixedPrice) {
            super(fixedPrice);
        }

        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            ComponentState state = super.createComponent(property, parent, context);
            if (LOW_QTY.equals(property.getName())) {
                Component component = RowFactory.create(CELL_SPACING, state.getComponent(), lowQtySellingUnits);
                state = new ComponentState(component, property);
            } else if (HIGH_QTY.equals(property.getName())) {
                Component component = RowFactory.create(CELL_SPACING, state.getComponent(), highQtySellingUnits);
                state = new ComponentState(component, property);
            }
            return state;
        }
    }

    /**
     * Returns a node filter for the specified product reference.
     * <p/>
     * This excludes:
     * <ul>
     * <li>the price and discount nodes for <em>product.template</em>
     * <li>the discount node, if discounts are disabled</li>
     * </ul>
     *
     * @param product a reference to the product. May be {@code null}
     * @return a node filter for the product. If {@code null}, no nodes require filtering
     */
    private ArchetypeNodes getFilterForProduct(IMObjectReference product) {
        ArchetypeNodes result = null;
        if (TypeHelper.isA(product, TEMPLATE)) {
            result = TEMPLATE_NODES;
        } else if (getDisableDiscounts()) {
            result = new ArchetypeNodes().exclude(LOW_DISCOUNT, HIGH_DISCOUNT);
        }
        return result;
    }
}