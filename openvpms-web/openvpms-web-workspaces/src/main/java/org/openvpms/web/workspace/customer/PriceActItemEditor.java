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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.edit.act.TemplateProduct;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.charge.PriceActEditContext;

import java.math.BigDecimal;
import java.util.Date;

import static org.openvpms.archetype.rules.product.ProductArchetypes.MEDICATION;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MERCHANDISE;


/**
 * An editor for {@link Act}s that have fixed and unit prices.
 *
 * @author Tim Anderson
 */
public abstract class PriceActItemEditor extends ActItemEditor {

    /**
     * Minimum quantity node name.
     */
    public static final String MINIMUM_QUANTITY = "minQuantity";

    /**
     * The service ratio node name.
     */
    protected static final String SERVICE_RATIO = "serviceRatio";

    /**
     * The edit context.
     */
    private final PriceActEditContext editContext;

    /**
     * The service ratio editor.
     */
    private final ServiceRatioEditor serviceRatioEditor;

    /**
     * Listener for service ratio updates.
     */
    private final ModifiableListener serviceRatioListener;

    /**
     * The available service ratio, or {@code null} if no service ratio is available.
     */
    private BigDecimal availableServiceRatio;

    /**
     * Fixed price node editor.
     */
    private FixedPriceEditor fixedEditor;

    /**
     * The unit price.
     */
    private ProductPrice unitProductPrice;


    /**
     * Constructs a {@link PriceActItemEditor}.
     *
     * @param act           the act to edit
     * @param parent        the parent act. May be {@code null}
     * @param context       the context
     * @param layoutContext the layout context
     */
    public PriceActItemEditor(Act act, Act parent, PriceActEditContext context, LayoutContext layoutContext) {
        super(act, parent, layoutContext);
        this.editContext = context;

        Product product = getProduct();
        Property fixedPrice = getProperty("fixedPrice");

        availableServiceRatio = getServiceRatio(product);
        fixedEditor = new FixedPriceEditor(fixedPrice, context.getPricingContext());
        fixedEditor.setProduct(product, getServiceRatio());
        Property serviceRatio = getProperty(SERVICE_RATIO);
        serviceRatioEditor = new ServiceRatioEditor(serviceRatio);
        serviceRatioListener = modifiable -> serviceRatioModified();
        serviceRatio.addModifiableListener(serviceRatioListener);

        addEditor(fixedEditor);
        addEditor(serviceRatioEditor);
    }

    /**
     * Returns the minimum quantity.
     *
     * @return the minimum quantity
     */
    public BigDecimal getMinimumQuantity() {
        return getProperty(MINIMUM_QUANTITY).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the minimum quantity.
     *
     * @param quantity the minimum quantity
     */
    public void setMinimumQuantity(BigDecimal quantity) {
        getProperty(MINIMUM_QUANTITY).setValue(quantity);
    }

    /**
     * Returns the fixed price.
     *
     * @return the fixed price
     */
    public BigDecimal getFixedPrice() {
        return getProperty("fixedPrice").getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Returns the service ratio.
     *
     * @return the service ratio, or {@code null} if none is applied.
     */
    public BigDecimal getServiceRatio() {
        return getProperty(SERVICE_RATIO).getBigDecimal();
    }

    /**
     * Sets the service ratio.
     *
     * @param serviceRatio the service ratio. May be {@code null}
     */
    public void setServiceRatio(BigDecimal serviceRatio) {
        getProperty(SERVICE_RATIO).setValue(serviceRatio);
    }

    /**
     * Returns the available service ratio.
     *
     * @return the available service ratio, or {@code null} if none is available.
     */
    public BigDecimal getAvailableServiceRatio() {
        return availableServiceRatio;
    }

    /**
     * Sets a product included from a template.
     * <p>
     * This updates the minimum quantity to that of the low quantity from the template.
     *
     * @param product  the product. May be {@code null}
     * @param template the template that included the product. May be {@code null}
     */
    @Override
    public void setProduct(TemplateProduct product, Product template) {
        setMinimumQuantity(null);
        super.setProduct(product, template);
        setMinimumQuantity(product != null ? product.getLowQuantity() : null);
    }

    /**
     * Moves the focus to the product editor, if one exists.
     */
    public void moveFocusToProduct() {
        ProductParticipationEditor productEditor = getProductEditor();
        if (productEditor != null) {
            FocusGroup group = productEditor.getFocusGroup();
            if (group != null) {
                group.setFocus();
            }
        }
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean result = super.doValidation(validator);
        if (result && editContext.useMinimumQuantities()) {
            result = validateMinimumQuantity(validator);
        }
        return result;
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
        if (!MathRules.isZero(minQuantity) && getQuantity().compareTo(minQuantity) < 0) {
            Product product = getProduct();
            String name = (product != null) ? product.getName() : null;
            // product should never be null, due to validation
            Property property = getProperty(QUANTITY);
            String message = Messages.format("customer.charge.minquantity", name, minQuantity);
            validator.add(property, new ValidatorError(property, message));
            result = false;
        }
        return result;
    }

    /**
     * Save any edits.
     * <p>
     * This implementation saves the current object before children, to ensure deletion of child acts
     * don't result in StaleObjectStateException exceptions.
     * <p>
     * This implementation will throw an exception if the product is an <em>product.template</em>.
     * Ideally, the act would be flagged invalid if this is the case, but template expansion only works for valid
     * acts. TODO
     *
     * @throws OpenVPMSException     if the save fails
     * @throws IllegalStateException if the product is a template
     */
    @Override
    protected void doSave() {
        if (TypeHelper.isA(getProductRef(), ProductArchetypes.TEMPLATE)) {
            Product product = getProduct();
            String name = product != null ? product.getName() : null;
            throw new IllegalStateException("Cannot save with product template: " + name);
        }
        saveObject();
        saveChildren();
    }

    /**
     * Invoked when the product is changed.
     *
     * @param product the product. May be {@code null}
     */
    @Override
    protected void productModified(Product product) {
        super.productModified(product);
        BigDecimal serviceRatio = updateServiceRatio(product);
        if (!TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            fixedEditor.setProduct(product, serviceRatio);
        } else {
            fixedEditor.setProduct(null, serviceRatio);
        }
    }

    /**
     * Invoked when the service ratio is modified.
     */
    protected void serviceRatioModified() {
        fixedEditor.setServiceRatio(getServiceRatio());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return createLayoutStrategy(fixedEditor, serviceRatioEditor);
    }

    /**
     * Creates the layout strategy.
     *
     * @param fixedPrice         the fixed price editor
     * @param serviceRatioEditor the service ratio editor
     * @return a new layout strategy
     */
    protected IMObjectLayoutStrategy createLayoutStrategy(FixedPriceEditor fixedPrice,
                                                          ServiceRatioEditor serviceRatioEditor) {
        return new PriceItemLayoutStrategy(fixedPrice, serviceRatioEditor);
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        restrictProductSelection();
    }

    /**
     * Returns the edit context.
     *
     * @return the edit context
     */
    protected PriceActEditContext getEditContext() {
        return editContext;
    }

    /**
     * Returns the dose of a product for a patient, based on the patient's weight.
     *
     * @param product the product
     * @param patient the patient
     * @return the dose, or {@code 0} if no dose exists for the patient weight
     */
    protected BigDecimal getDose(Product product, Party patient) {
        return editContext.getDose(product, patient);
    }

    /**
     * Returns the fixed cost.
     *
     * @return the fixed cost
     */
    protected BigDecimal getFixedCost() {
        return getProperty("fixedCost").getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Returns the unit cost.
     *
     * @return the unit cost
     */
    protected BigDecimal getUnitCost() {
        return getProperty("unitCost").getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Determines if discounting has been disabled.
     *
     * @return {@code true} if discounts are disabled
     */
    protected boolean disableDiscounts() {
        return editContext.disableDiscounts();
    }

    /**
     * Returns the maximum discount allowed on the fixed price.
     *
     * @return the maximum discount
     */
    protected BigDecimal getFixedPriceMaxDiscount() {
        return getFixedPriceMaxDiscount(BigDecimal.ZERO);
    }

    /**
     * Returns the maximum discount allowed on the fixed price.
     *
     * @param defaultValue the default value, if there is no fixed price
     * @return the maximum discount, or {@code defaultValue} if there is no fixed price
     */
    protected BigDecimal getFixedPriceMaxDiscount(BigDecimal defaultValue) {
        Product product = getProduct();
        BigDecimal result;
        if (product != null) {
            ProductPrice price = getFixedProductPrice(product);
            result = getMaxDiscount(price);
        } else {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Returns the maximum discount allowed on the unit price.
     *
     * @return the maximum discount
     */
    protected BigDecimal getUnitPriceMaxDiscount() {
        return getUnitPriceMaxDiscount(BigDecimal.ZERO);
    }

    /**
     * Returns the maximum discount allowed on the unit price.
     *
     * @param defaultValue the default value, if there is no unit price
     * @return the maximum discount, or {@code defaultValue} if there is no unit price
     */
    protected BigDecimal getUnitPriceMaxDiscount(BigDecimal defaultValue) {
        Product product = getProduct();
        BigDecimal result;
        if (product != null) {
            ProductPrice price = getUnitProductPrice(product);
            result = getMaxDiscount(price);
        } else {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Calculates the discount amount, updating the 'discount' node.
     * <p>
     * If discounts are disabled, any existing discount will be set to {@code 0}.
     *
     * @return {@code true} if the discount was updated
     */
    protected boolean updateDiscount() {
        boolean result = false;
        try {
            BigDecimal amount = calculateDiscount();
            // If discount amount calculates to zero don't update any existing value as may have been manually modified
            // unless discounts are disabled (in which case amount should be zero).
            if (disableDiscounts() || amount.compareTo(BigDecimal.ZERO) != 0) {
                Property discount = getProperty("discount");
                result = discount.setValue(amount);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

    /**
     * Calculates the discount.
     *
     * @return the discount or {@code BigDecimal.ZERO} if the discount can't be calculated or discounts are disabled
     */
    protected BigDecimal calculateDiscount() {
        BigDecimal unitPrice = getUnitPrice();
        BigDecimal quantity = getQuantity();
        return calculateDiscount(unitPrice, quantity);
    }

    /**
     * Calculates the discount.
     *
     * @param unitPrice the unit price
     * @param quantity  the quantity
     * @return the discount or {@code BigDecimal.ZERO} if the discount can't be calculated or discounts are disabled
     */
    protected BigDecimal calculateDiscount(BigDecimal unitPrice, BigDecimal quantity) {
        BigDecimal amount = BigDecimal.ZERO;
        if (!disableDiscounts()) {
            Party customer = getCustomer();
            Party patient = getPatient();
            Product product = getProduct();

            if (customer != null && product != null && !TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
                BigDecimal fixedCost = getFixedCost();
                BigDecimal unitCost = getUnitCost();
                BigDecimal fixedPrice = getFixedPrice();
                BigDecimal fixedPriceMaxDiscount = getFixedPriceMaxDiscount();
                BigDecimal unitPriceMaxDiscount = getUnitPriceMaxDiscount();
                Date startTime = getStartTime();
                if (startTime == null) {
                    Act parent = (Act) getParent();
                    startTime = parent.getActivityStartTime();
                }
                DiscountRules rules = editContext.getDiscountRules();
                amount = rules.calculateDiscount(startTime, editContext.getPractice(), customer, patient, product,
                                                 fixedCost, unitCost, fixedPrice, unitPrice, quantity,
                                                 fixedPriceMaxDiscount, unitPriceMaxDiscount);
            }
        }
        return amount;
    }

    /**
     * Returns the default fixed product price for the specified product.
     *
     * @param product the product
     * @return the product price, or {@code null} if none is found
     */
    protected ProductPrice getDefaultFixedProductPrice(Product product) {
        return getProductPrice(ProductArchetypes.FIXED_PRICE, product);
    }

    /**
     * Returns the fixed product price for the specified product.
     *
     * @param product the product
     * @return the product price, or {@code null} if none is found
     */
    protected ProductPrice getFixedProductPrice(Product product) {
        ProductPrice result = fixedEditor.getProductPrice();
        result = getProductPrice(product, ProductArchetypes.FIXED_PRICE, result, getFixedPrice(), getServiceRatio());
        fixedEditor.setProductPrice(result);
        return result;
    }

    /**
     * Returns the default unit product price for the specified product.
     *
     * @param product the product
     * @return the product price, or {@code null} if none is found
     */
    protected ProductPrice getDefaultUnitProductPrice(Product product) {
        return getProductPrice(ProductArchetypes.UNIT_PRICE, product);
    }

    /**
     * Returns the unit product price for the specified product.
     *
     * @param product the product
     * @return the product price, or {@code null} if none is found
     */
    protected ProductPrice getUnitProductPrice(Product product) {
        unitProductPrice = getProductPrice(product, ProductArchetypes.UNIT_PRICE, unitProductPrice, getUnitPrice(),
                                           getServiceRatio());
        return unitProductPrice;
    }

    /**
     * Returns the price of a product.
     * <p>
     * This:
     * <ul>
     * <li>applies any service ratio to the price</li>
     * <li>subtracts any tax exclusions the customer may have</li>
     * </ul>
     *
     * @param price        the price
     * @param serviceRatio the service ratio. May be {@code null}
     * @return the price, minus any tax exclusions
     */
    protected BigDecimal getPrice(Product product, ProductPrice price, BigDecimal serviceRatio) {
        return editContext.getPrice(product, price, serviceRatio);
    }

    /**
     * Calculate the amount of tax for the act using tax type information for the product, product type, organisation
     * and customer associated with the act.
     * The tax amount will be calculated and stored in the tax node for the act.
     *
     * @param customer the customer
     * @return the amount of tax for the act
     */
    protected BigDecimal calculateTax(Party customer) {
        FinancialAct act = (FinancialAct) getObject();
        return editContext.getTaxRules().calculateTax(act, customer);
    }

    /**
     * Updates the service ratio.
     *
     * @param product the product. May be {@code null}
     * @return the service ratio. May be {@code null}
     */
    protected BigDecimal updateServiceRatio(Product product) {
        BigDecimal serviceRatio = getServiceRatio(product);
        availableServiceRatio = serviceRatio;
        Property property = getProperty(SERVICE_RATIO);
        property.removeModifiableListener(serviceRatioListener);
        property.setValue(serviceRatio);
        property.addModifiableListener(serviceRatioListener);
        return serviceRatio;
    }

    /**
     * Returns the first product price with the specified short name and price, active as of the date.
     *
     * @param shortName    the price short name
     * @param price        the tax-inclusive price
     * @param serviceRatio the service ratio, or {@link BigDecimal#ONE} if no ratio applies
     * @param product      the product
     * @return the product price, or {@code null} if none is found
     */
    protected ProductPrice getProductPrice(String shortName, BigDecimal price, BigDecimal serviceRatio,
                                           Product product) {
        return editContext.getPricingContext().getProductPrice(shortName, price, serviceRatio, product, getStartTime());
    }

    /**
     * Restricts product selection:
     * <ul>
     * <li>to exclude template-only products</li>
     * <li>if the item has a minimum quantity<br/>
     * When a minimum quantity is in place, this only allows a product to be replaced with one of the same type
     * <br/>
     * This is to handle the case where the preferred product is out of stock.
     * </li>
     * </ul>
     */
    protected void restrictProductSelection() {
        ProductParticipationEditor editor = getProductEditor(false);
        if (editor != null) {
            // register the location in order to determine service ratios, and restrict products if useLocationProducts
            // is true. Note that registering the location and stock location shouldn't be required, as these should
            // be inherited from the context.
            Party location = getLocation();
            boolean useLocationProducts = editContext.useLocationProducts();
            editor.setUseLocationProducts(useLocationProducts);
            editor.setLocation(location);
            editor.setExcludeTemplateOnlyProducts(true);
            if (useLocationProducts) {
                editor.setStockLocation(editContext.getStockLocation());
            }

            if (editContext.useMinimumQuantities() && !editContext.overrideMinimumQuantities()) {
                if (!MathRules.isZero(getMinimumQuantity())) {
                    IMObjectReference product = editor.getEntityRef();
                    if (TypeHelper.isA(product, MEDICATION, MERCHANDISE)) {
                        // doesn't apply to services - these should be read-only
                        editor.setShortNames(product.getArchetypeId().getShortName());
                    } else {
                        editor.resetShortNames();
                    }
                } else {
                    editor.resetShortNames();
                }
            }
        }
    }

    /**
     * Determines if the product is read-only.
     *
     * @return {@code true} if minimum quantities are enforced, and the product is a service with a minimum quantity
     */
    protected boolean isProductReadOnly() {
        boolean result = false;
        if (editContext.useMinimumQuantities() && !editContext.overrideMinimumQuantities()
            && TypeHelper.isA(getProductRef(), ProductArchetypes.SERVICE)) {
            BigDecimal minQuantity = getMinimumQuantity();
            result = !MathRules.isZero(minQuantity);
        }
        return result;
    }

    /**
     * Determines if the product is currently editable, but a read-only product is required.
     *
     * @return {@code true} if the product is editable and read-only is required
     */
    protected boolean needsReadOnlyProduct() {
        return getProductEditor(false) != null && isProductReadOnly();
    }

    /**
     * Returns the service ratio for a product.
     *
     * @param product the product. May be {@code null}
     * @return the service ratio, or {@code null} if none exists
     */
    protected BigDecimal getServiceRatio(Product product) {
        BigDecimal result = null;
        if (product != null) {
            result = editContext.getPricingContext().getServiceRatio(product, getStartTime());
            if (result != null && result.compareTo(BigDecimal.ONE) == 0) {
                // ignore serviceRatio == 1
                result = null;
            }
        }
        return result;
    }

    /**
     * Determines if the service ratio should be displayed.
     *
     * @return {@code true} if the service ratio should be displayed
     */
    protected boolean showServiceRatio() {
        return getServiceRatio() != null || getAvailableServiceRatio() != null;
    }

    /**
     * Helper to return a product price for a product.
     *
     * @param product      the product
     * @param shortName    the product price archetype short name
     * @param current      the current product price. May be {@code null}
     * @param price        the current price
     * @param serviceRatio the service ratio, or {@link BigDecimal#ONE} if no ratio applies
     * @return {@code current} if it matches the specified product and price;
     * or the first matching product price associated with the product,
     * or {@code null} if none is found
     */
    private ProductPrice getProductPrice(Product product, String shortName, ProductPrice current, BigDecimal price,
                                         BigDecimal serviceRatio) {
        ProductPrice result = null;
        if (current != null && current.getProduct().equals(product)) {
            BigDecimal defaultValue = getPrice(product, current, serviceRatio);
            if (price.compareTo(defaultValue) == 0) {
                result = current;
            }
        }
        if (result == null) {
            if (price.compareTo(BigDecimal.ZERO) == 0) {
                result = getProductPrice(shortName, product);
            } else {
                result = getProductPrice(shortName, price, serviceRatio, product);
            }
        }
        return result;
    }

    /**
     * Layout strategy that includes the fixed price editor.
     */
    protected class PriceItemLayoutStrategy extends LayoutStrategy {

        public PriceItemLayoutStrategy(FixedPriceEditor editor, PropertyEditor serviceRatio) {
            addComponent(new ComponentState(editor));
            addComponent(new ComponentState(serviceRatio));
        }

        /**
         * Apply the layout strategy.
         * <p>
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
            if (isProductReadOnly()) {
                addComponent(createComponent(createReadOnly(properties.get(PRODUCT)), parent, context));
            }
            return super.apply(object, properties, parent, context);
        }
    }

    protected class ServiceRatioEditor extends AbstractPropertyEditor {

        private final TextField field;

        private final CheckBox apply;

        private final Component component;

        private final FocusGroup group = new FocusGroup(ServiceRatioEditor.class.getSimpleName());

        private final ModifiableListener listener;

        /**
         * Constructs an {@link AbstractPropertyEditor}.
         *
         * @param property the property being edited
         */
        public ServiceRatioEditor(Property property) {
            super(property);
            field = BoundTextComponentFactory.createNumeric(property, 10);
            field.setEnabled(false);
            field.setStyleName(Styles.EDIT);
            apply = CheckBoxFactory.create(hasRatio());
            listener = modifiable -> onRatioChanged();
            property.addModifiableListener(listener);
            apply.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    property.removeModifiableListener(listener);
                    if (apply.isSelected()) {
                        property.setValue(getAvailableServiceRatio());
                    } else {
                        property.setValue(null);
                    }
                    property.addModifiableListener(listener);
                }
            });
            component = RowFactory.create(Styles.CELL_SPACING, field, apply);
        }

        /**
         * Returns the edit component.
         *
         * @return the edit component
         */
        @Override
        public Component getComponent() {
            return component;
        }

        /**
         * Returns the focus group.
         *
         * @return the focus group, or {@code null} if the editor hasn't been rendered
         */
        @Override
        public FocusGroup getFocusGroup() {
            return group;
        }

        private void onRatioChanged() {
            apply.setSelected(hasRatio());
        }

        private boolean hasRatio() {
            return getProperty().getBigDecimal(BigDecimal.ONE).compareTo(BigDecimal.ONE) != 0;
        }
    }

}
