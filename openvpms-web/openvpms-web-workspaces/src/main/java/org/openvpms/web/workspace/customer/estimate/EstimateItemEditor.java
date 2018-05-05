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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.bound.BoundProperty;
import org.openvpms.web.component.im.edit.act.TemplateProduct;
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
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.PriceActItemEditor;
import org.openvpms.web.workspace.customer.charge.ChargeEditContext;
import org.openvpms.web.workspace.customer.charge.Quantity;

import java.math.BigDecimal;
import java.util.Date;

import static java.math.BigDecimal.ZERO;
import static org.openvpms.archetype.rules.math.MathRules.isZero;


/**
 * An editor for {@link Act}s which have an archetype of <em>act.customerEstimationItem</em>.
 *
 * @author Tim Anderson
 */
public class EstimateItemEditor extends PriceActItemEditor {

    /**
     * The low quantity.
     */
    private final Quantity lowQuantity;

    /**
     * The high quantity.
     */
    private final Quantity highQuantity;

    /**
     * Listener for low and high total updates.
     */
    private final ModifiableListener totalListener;

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
     * Low total node name.
     */
    private static final String LOW_TOTAL = "lowTotal";

    /**
     * High total node name.
     */
    private static final String HIGH_TOTAL = "highTotal";

    /**
     * Nodes to display when a product template is selected.
     */
    private static final ArchetypeNodes TEMPLATE_NODES = new ArchetypeNodes().exclude(
            LOW_QTY, HIGH_QTY, FIXED_PRICE, LOW_UNIT_PRICE, HIGH_UNIT_PRICE, LOW_DISCOUNT, HIGH_DISCOUNT,
            PRINT, LOW_TOTAL, HIGH_TOTAL);


    /**
     * Constructs an {@link EstimateItemEditor}.
     *
     * @param act           the act to edit
     * @param parent        the parent act
     * @param context       the edit context
     * @param layoutContext the layout context
     */
    public EstimateItemEditor(Act act, Act parent, ChargeEditContext context, LayoutContext layoutContext) {
        super(act, parent, context, layoutContext);
        if (!TypeHelper.isA(act, EstimateArchetypes.ESTIMATE_ITEM)) {
            throw new IllegalArgumentException("Invalid act type:" + act.getArchetypeId().getShortName());
        }
        if (act.isNew()) {
            // default the act start time to today
            act.setActivityStartTime(new Date());
        }

        lowQuantity = new Quantity(getProperty(LOW_QTY), act, getLayoutContext());
        if (context.overrideMinimumQuantities()) {
            lowQuantity.getProperty().addModifiableListener(new ModifiableListener() {
                @Override
                public void modified(Modifiable modifiable) {
                    onLowQuantityChanged();
                }
            });
        }
        highQuantity = new Quantity(getProperty(HIGH_QTY), act, getLayoutContext());

        Product product = getProduct();
        boolean showPrint = updatePrint(product);
        ArchetypeNodes nodes = getFilterForProduct(product, showPrint);
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
        lowQuantity.getProperty().addModifiableListener(lowListener);
        getProperty(HIGH_UNIT_PRICE).addModifiableListener(highListener);
        highQuantity.getProperty().addModifiableListener(highListener);

        // add a listener to update the tax amount when the total changes
        totalListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onTotalChanged();
            }
        };
        getProperty(LOW_TOTAL).addModifiableListener(totalListener);
        getProperty(HIGH_TOTAL).addModifiableListener(totalListener);
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
        return lowQuantity.getValue(ZERO);
    }

    /**
     * Sets the low quantity.
     *
     * @param quantity the low quantity
     */
    public void setLowQuantity(BigDecimal quantity) {
        lowQuantity.setValue(quantity);
    }

    /**
     * Returns the high quantity.
     *
     * @return the high quantity
     */
    public BigDecimal getHighQuantity() {
        return highQuantity.getValue(ZERO);
    }

    /**
     * Sets the high quantity.
     *
     * @param quantity the high quantity
     */
    public void setHighQuantity(BigDecimal quantity) {
        highQuantity.setValue(quantity);
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
        return getProperty(LOW_UNIT_PRICE).getBigDecimal(ZERO);
    }

    /**
     * Returns the high unit price.
     *
     * @return the high unit price
     */
    public BigDecimal getHighUnitPrice() {
        return getProperty(HIGH_UNIT_PRICE).getBigDecimal(ZERO);
    }

    /**
     * Sets the discount.
     *
     * @param discount the discount
     */
    @Override
    public void setDiscount(BigDecimal discount) {
        setLowDiscount(discount);
        setHighDiscount(discount);
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
     * Sets a product included from a template.
     *
     * @param product  the product. May be {@code null}
     * @param template the template that included the product. May be {@code null}
     */
    @Override
    public void setProduct(TemplateProduct product, Product template) {
        setMinimumQuantity(null);
        setTemplate(template);  // NB: template must be set before product
        if (product != null) {
            // clear the quantity. If the quantity changes after the product is set, don't overwrite with that
            // from the template, as it is the dose quantity for the patient weight, unless the low quantity is zero
            setQuantity(ZERO);
            setProduct(product.getProduct());
            // need to set the minimum quantity after the product, as it can mark the product read-only.
            // If done before the product, the productModified() callback is never invoked. TODO - brittle
            setMinimumQuantity(product.getLowQuantity());
            if (isZero(getHighQuantity())) {
                setLowQuantity(product.getLowQuantity());
                setHighQuantity(product.getHighQuantity());
            } else if (isZero(product.getLowQuantity())) {
                setLowQuantity(ZERO);
            }
            if (!product.getPrint()) {
                BigDecimal low = getLowTotal();
                BigDecimal high = getHighTotal();
                if (isZero(low) && isZero(high)) {
                    setPrint(false);
                }
            }
            if (product.getZeroPrice()) {
                setFixedPrice(ZERO);
                setUnitPrice(ZERO);
                setDiscount(ZERO);
            }
        } else {
            setProduct(null);
        }
    }

    /**
     * Returns the low total.
     *
     * @return the low total
     */
    public BigDecimal getLowTotal() {
        return getProperty(LOW_TOTAL).getBigDecimal(ZERO);
    }

    /**
     * Returns the high total.
     *
     * @return the high total
     */
    public BigDecimal getHighTotal() {
        return getProperty(HIGH_TOTAL).getBigDecimal(ZERO);
    }

    /**
     * Ensures that the quantity isn't less than the minimum quantity.
     *
     * @param validator the validator
     * @return {@code true} if the quantity isn't less than the minimum quantity, otherwise {@code false}
     */
    protected boolean validateMinimumQuantity(Validator validator) {
        boolean result = true;
        BigDecimal minQuantity = getMinimumQuantity();
        if (!MathRules.isZero(minQuantity) && getLowQuantity().compareTo(minQuantity) < 0) {
            Product product = getProduct();
            String name = (product != null) ? product.getName() : null;
            // product should never be null, due to validation
            Property property = getProperty(LOW_QTY);
            String message = Messages.format("customer.charge.minquantity", name, minQuantity);
            validator.add(property, new ValidatorError(property, message));
            result = false;
        }
        return result;
    }

    /**
     * Invoked when the product is changed, to update prices.
     *
     * @param product the product. May be {@code null}
     */
    @Override
    protected void productModified(Product product) {
        getProperty(LOW_TOTAL).removeModifiableListener(totalListener);
        getProperty(HIGH_TOTAL).removeModifiableListener(totalListener);
        super.productModified(product);

        Property lowDiscount = getProperty(LOW_DISCOUNT);
        Property highDiscount = getProperty(HIGH_DISCOUNT);
        lowDiscount.setValue(ZERO);
        highDiscount.setValue(ZERO);
        boolean showPrint = false;

        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            // zero out the fixed, low and high prices.
            Property fixedPrice = getProperty(FIXED_PRICE);
            Property lowUnitPrice = getProperty(LOW_UNIT_PRICE);
            Property highUnitPrice = getProperty(HIGH_UNIT_PRICE);
            fixedPrice.setValue(ZERO);
            lowUnitPrice.setValue(ZERO);
            highUnitPrice.setValue(ZERO);
            updateSellingUnits(null);
        } else {
            boolean clearDefault = true;
            if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                Party patient = getPatient();
                if (patient != null) {
                    BigDecimal dose = getDose(product, patient);
                    if (!isZero(dose)) {
                        lowQuantity.setValue(dose, true);
                        highQuantity.setValue(dose, true);
                        clearDefault = false;
                    }
                }
            }
            if (clearDefault) {
                // the quantity is not a default for the product, so turn off any highlighting
                lowQuantity.clearDefault();
                highQuantity.clearDefault();
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
                fixedPrice.setValue(ZERO);
            }
            if (unit != null) {
                BigDecimal price = getPrice(product, unit);
                lowUnitPrice.setValue(price);
                highUnitPrice.setValue(price);
            } else {
                lowUnitPrice.setValue(ZERO);
                highUnitPrice.setValue(ZERO);
            }
            showPrint = updatePrint(product);
            updateSellingUnits(product);
        }

        ProductParticipationEditor productEditor = getProductEditor();
        if (productEditor != null) {
            // check if the product has been marked read-only by setting the minimum quantity
            boolean readOnly = needsReadOnlyProduct();
            productEditor.setReadOnly(readOnly);
        }

        setPrint(true);
        updateLayout(product, showPrint);

        notifyProductListener(product);
        getProperty(LOW_TOTAL).addModifiableListener(totalListener);
        getProperty(HIGH_TOTAL).addModifiableListener(totalListener);
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
     * Invoked when the low quantity changes and the user can override the minimum quantity.
     * <p/>
     * This ensures that the minimum quantity is set to the low quantity if it is less, in order for
     * the estimate to be valid.
     * <p/>
     // Setting the low quantity to zero disables the minimum.
     */
    private void onLowQuantityChanged() {
        BigDecimal minQuantity = getMinimumQuantity();
        BigDecimal quantity = getLowQuantity();
        if (lowQuantity.getProperty().isValid()
            && (quantity.compareTo(minQuantity) < 0 || MathRules.isZero(quantity))) {
            setMinimumQuantity(quantity);
        }
    }

    /**
     * Updates the layout if required.
     *
     * @param product   the product. May be {@code null}
     * @param showPrint if {@code true} show the print node
     */
    private void updateLayout(Product product, boolean showPrint) {
        ArchetypeNodes currentNodes = getArchetypeNodes();
        ArchetypeNodes expectedFilter = getFilterForProduct(product, showPrint);
        if (!ObjectUtils.equals(currentNodes, expectedFilter)) {
            Component focus = FocusHelper.getFocus();
            Property focusProperty = null;
            if (focus instanceof BoundProperty) {
                focusProperty = ((BoundProperty) focus).getProperty();
            }
            changeLayout(expectedFilter);
            if (focusProperty != null) {
                if (!setFocus(focusProperty)) {
                    moveFocusToProduct();
                }
            } else {
                moveFocusToProduct();
            }
        }
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
            // If discount amount calculates to zero don't update any existing value as may have been manually
            // modified unless discounts are disabled or quantity is zero (in which case amount should be zero).
            if (disableDiscounts() || !isZero(amount) || isZero(quantity)) {
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
            // If discount amount calculates to zero don't update any existing value as may have been manually
            // modified unless discounts are disabled or quantity is zero (in which case amount should be zero).
            if (disableDiscounts() || !isZero(amount) || isZero(quantity)) {
                Property discount = getProperty(HIGH_DISCOUNT);
                result = discount.setValue(amount);
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

        /**
         * Apply the layout strategy.
         * <p/>
         * This renders an object in a {@code Component}, using a factory to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component containing the rendered {@code object}
         */
        @Override
        public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
            addComponent(createQuantity(lowQuantity, lowQtySellingUnits));
            addComponent(createQuantity(highQuantity, highQtySellingUnits));
            return super.apply(object, properties, parent, context);
        }

        /**
         * Creates a component containing a quantity and their selling units.
         *
         * @param quantity the quantity
         * @param units    the selling units
         * @return a new component
         */
        protected ComponentState createQuantity(Quantity quantity, Label units) {
            Row row = RowFactory.create(Styles.CELL_SPACING, quantity.getComponent(), units);
            return new ComponentState(row, quantity.getProperty());
        }
    }

    /**
     * Invoked when the total changes.
     */
    private void onTotalChanged() {
        Product product = getProduct();
        boolean showPrint = updatePrint(product);
        updateLayout(product, showPrint);
    }

    /**
     * Updates the print flag when the product or total changes.
     *
     * @param product the product. May be {@code null}
     * @return {@code true} if the print flag should be shown
     */
    private boolean updatePrint(Product product) {
        boolean result = false;
        if (product != null) {
            BigDecimal lowTotal = getLowTotal();
            BigDecimal highTotal = getHighTotal();
            result = MathRules.equals(lowTotal, ZERO) && MathRules.equals(highTotal, ZERO);
            if (result) {
                Product template = getTemplate();
                if (template != null) {
                    IMObjectBean bean = new IMObjectBean(template);
                    result = !bean.getBoolean("printAggregate");
                }
            } else {
                setPrint(true);
            }
        }
        return result;
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
     * @param product   a reference to the product. May be {@code null}
     * @param showPrint if {@code true}, show the print node
     * @return a node filter for the product. If {@code null}, no nodes require filtering
     */
    private ArchetypeNodes getFilterForProduct(Product product, boolean showPrint) {
        ArchetypeNodes result = null;
        if (TypeHelper.isA(product, TEMPLATE)) {
            result = TEMPLATE_NODES;
        } else {
            if (disableDiscounts()) {
                result = new ArchetypeNodes().exclude(LOW_DISCOUNT, HIGH_DISCOUNT);
            }
            if (showPrint) {
                if (result == null) {
                    result = new ArchetypeNodes();
                }
                result.simple(PRINT);
                result.order(PRINT, LOW_TOTAL);
            }
        }
        return result;
    }

}