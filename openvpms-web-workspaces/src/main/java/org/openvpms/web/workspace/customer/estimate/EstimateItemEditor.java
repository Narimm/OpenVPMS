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
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.PriceActItemEditor;

import java.math.BigDecimal;
import java.util.Date;

import static org.openvpms.web.echo.style.Styles.CELL_SPACING;


/**
 * An editor for {@link Act}s which have an archetype of <em>act.customerEstimationItem</em>.
 *
 * @author Tim Anderson
 */
public class EstimateItemEditor extends PriceActItemEditor {

    /**
     * Tax rules.
     */
    private final CustomerTaxRules taxRules;

    /**
     * Low quantity selling units.
     */
    private Label lowQtySellingUnits = LabelFactory.create();

    /**
     * High quantity selling units.
     */
    private Label highQtySellingUnits = LabelFactory.create();

    /**
     * Nodes to display when a product template is selected.
     */
    private static final ArchetypeNodes TEMPLATE_NODES = new ArchetypeNodes().exclude(
            "lowQty", "highQty", "fixedPrice", "lowUnitPrice", "highUnitPrice", "lowTotal", "highTotal");


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
        if (act.isNew()) {
            // default the act start time to today
            act.setActivityStartTime(new Date());
        }
        taxRules = new CustomerTaxRules(context.getContext().getPractice(), ServiceHelper.getArchetypeService(),
                                        ServiceHelper.getLookupService());

        // add a listener to update the discount when the fixed, high unit price
        // or quantity, changes
        ModifiableListener listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateDiscount();
            }
        };
        getProperty("fixedPrice").addModifiableListener(listener);
        getProperty("highUnitPrice").addModifiableListener(listener);
        getProperty("highQty").addModifiableListener(listener);
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
     * Sets the low quantity.
     *
     * @param quantity the low quantity
     */
    public void setLowQuantity(BigDecimal quantity) {
        getProperty("lowQty").setValue(quantity);
    }

    /**
     * Sets the high quantity.
     *
     * @param quantity the high quantity
     */
    public void setHighQuantity(BigDecimal quantity) {
        getProperty("highQty").setValue(quantity);
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
        BigDecimal value = (BigDecimal) getProperty("highQty").getValue();
        return (value != null) ? value : BigDecimal.ZERO;
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
        BigDecimal value = (BigDecimal) getProperty("highUnitPrice").getValue();
        return (value != null) ? value : BigDecimal.ZERO;
    }

    /**
     * Invoked when the product is changed, to update prices.
     *
     * @param product the product. May be {@code null}
     */
    @Override
    protected void productModified(Product product) {
        super.productModified(product);

        Property discount = getProperty("discount");
        discount.setValue(BigDecimal.ZERO);

        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            if (getArchetypeNodes() != TEMPLATE_NODES) {
                changeLayout(TEMPLATE_NODES);
            }
            // zero out the fixed, low and high prices.
            Property fixedPrice = getProperty("fixedPrice");
            Property lowUnitPrice = getProperty("lowUnitPrice");
            Property highUnitPrice = getProperty("highUnitPrice");
            fixedPrice.setValue(BigDecimal.ZERO);
            lowUnitPrice.setValue(BigDecimal.ZERO);
            highUnitPrice.setValue(BigDecimal.ZERO);
            updateSellingUnits(null);
        } else {
            if (getArchetypeNodes() != null) {
                changeLayout(null);
            }
            Property fixedPrice = getProperty("fixedPrice");
            Property lowUnitPrice = getProperty("lowUnitPrice");
            Property highUnitPrice = getProperty("highUnitPrice");
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
     * Returns the price of a product.
     * <p/>
     * This subtracts any tax exclusions the customer may have.
     *
     * @param price the price
     * @return the price, minus any tax exclusions
     */
    private BigDecimal getPrice(Product product, ProductPrice price) {
        BigDecimal amount = price.getPrice();
        return taxRules.getTaxExAmount(amount, product, getCustomer());
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

    protected class EstimateItemLayoutStrategy extends PriceItemLayoutStrategy {
        public EstimateItemLayoutStrategy(FixedPriceEditor fixedPrice) {
            super(fixedPrice);
        }

        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            ComponentState state = super.createComponent(property, parent, context);
            if ("lowQty".equals(property.getName())) {
                Component component = RowFactory.create(CELL_SPACING, state.getComponent(), lowQtySellingUnits);
                state = new ComponentState(component, property);
            } else if ("highQty".equals(property.getName())) {
                Component component = RowFactory.create(CELL_SPACING, state.getComponent(), highQtySellingUnits);
                state = new ComponentState(component, property);
            }
            return state;
        }
    }
}
