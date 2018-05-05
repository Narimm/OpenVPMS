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

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.product.ProductPriceUpdater;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.EditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.MultipleEntityLinkCollectionEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.openvpms.web.component.im.product.ProductDoseEditor.MAX_WEIGHT;
import static org.openvpms.web.component.im.product.ProductDoseEditor.MIN_WEIGHT;
import static org.openvpms.web.component.im.product.ProductDoseEditor.WEIGHT_UNITS;


/**
 * A {@link Product} editor that recalculates prices when
 * {@link ProductSupplier} relationships change.
 *
 * @author Tim Anderson
 */
public class ProductEditor extends AbstractIMObjectEditor {

    /**
     * The product price updater.
     */
    private ProductPriceUpdater updater;

    /**
     * Concentration node.
     */
    private static final String CONCENTRATION = "concentration";

    /**
     * Prices node.
     */
    private static final String PRICES = "prices";

    /**
     * Doses node.
     */
    private static final String DOSES = "doses";

    /**
     * Stock locations node.
     */
    private static final String STOCK_LOCATIONS = "stockLocations";

    /**
     * Locations nodes.
     */
    private static final String LOCATIONS = "locations";

    /**
     * Suppliers node.
     */
    private static final String SUPPLIERS = "suppliers";

    /**
     * Pricing groups node.
     */
    private static final String PRICING_GROUPS = "pricingGroups";

    /**
     * Preferred supplier node.
     */
    private static final String PREFERRED = "preferred";

    /**
     * Constructs a {@link ProductEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context. May be {@code null}.
     */
    public ProductEditor(Product object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        CollectionProperty suppliers = getCollectionProperty(SUPPLIERS);
        CollectionProperty stock = getCollectionProperty(STOCK_LOCATIONS);
        CollectionProperty locations = getCollectionProperty(LOCATIONS);
        if (suppliers != null && stock != null) {
            RelationshipCollectionEditor stockLocations
                    = new MultipleEntityLinkCollectionEditor(stock, object, getLayoutContext()) {
                @Override
                protected IMObjectEditor createEditor(IMObject object, LayoutContext context) {
                    IMObjectEditor editor = super.createEditor(object, context);
                    if (editor instanceof ProductStockLocationEditor) {
                        ((ProductStockLocationEditor) editor).setProductEditor(ProductEditor.this);
                    }
                    return editor;
                }
            };
            getEditors().add(stockLocations);
        }
        if (locations != null) {
            getEditors().add(new ProductLocationCollectionEditor(locations, object, getLayoutContext()));
        }
        updater = new ProductPriceUpdater(ServiceHelper.getBean(ProductPriceRules.class),
                                          ServiceHelper.getBean(PracticeRules.class),
                                          ServiceHelper.getArchetypeService());
    }

    /**
     * Returns the prices editor.
     *
     * @return the prices, or {@code null} if the product doesn't support prices, or the component has not been created
     */
    public EditableIMObjectCollectionEditor getPricesEditor() {
        return (EditableIMObjectCollectionEditor) getEditor(PRICES, false);
    }

    /**
     * Returns the suppliers editor.
     *
     * @return the prices, or {@code null} if the product doesn't support prices, or the component has not been created
     */
    public EditableIMObjectCollectionEditor getSuppliersEditor() {
        return (EditableIMObjectCollectionEditor) getEditor(SUPPLIERS, false);
    }

    /**
     * Returns the product supplier references.
     *
     * @return the product supplier references
     */
    public List<IMObjectReference> getSuppliers() {
        List<IMObjectReference> result = new ArrayList<>();
        EditableIMObjectCollectionEditor suppliers = getSuppliersEditor();
        if (suppliers != null) {
            for (IMObject object : suppliers.getCurrentObjects()) {
                IMObjectRelationship relationship = (IMObjectRelationship) object;
                IMObjectReference target = relationship.getTarget();
                if (target != null) {
                    result.add(target);
                }
            }
        }
        return result;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = super.doValidation(validator);
        if (valid) {
            valid = validateUnitPrices(validator) && validateDoses(validator) && validateSuppliers(validator);
        }
        return valid;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        RelationshipCollectionEditor stockLocations = (RelationshipCollectionEditor) getEditor(STOCK_LOCATIONS, false);
        if (stockLocations != null) {
            strategy.addComponent(new ComponentState(stockLocations));
        }
        ProductLocationCollectionEditor locations = (ProductLocationCollectionEditor) getEditor(LOCATIONS, false);
        if (locations != null) {
            strategy.addComponent(new ComponentState(locations));
        }
        return strategy;
    }

    /**
     * Invoked when layout has completed. This can be used to perform
     * processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        EditableIMObjectCollectionEditor editor = getSuppliersEditor();
        if (editor != null) {
            editor.addModifiableListener(modifiable -> onSupplierChanged());
        }
    }

    /**
     * Invoked when a product-supplier relationship changes. This recalculates product prices if required.
     */
    private void onSupplierChanged() {
        updateUnitPrices();
        updatePreferred();
    }

    /**
     * Updates unit prices if one or more product-supplier relationships have autoPriceUpdate set {@code true}.
     */
    private void updateUnitPrices() {
        EditableIMObjectCollectionEditor suppliers = getSuppliersEditor();
        EditableIMObjectCollectionEditor prices = getPricesEditor();
        if (prices != null) {
            IMObjectEditor current = prices.getCurrentEditor();
            if (current != null && !prices.getCollection().getValues().contains(current.getObject())) {
                prices.add(current.getObject());
            }
            Collection<IMObjectEditor> currentPrices = prices.getEditors();
            Collection<IMObjectEditor> editors = suppliers.getEditors();
            for (IMObjectEditor editor : editors) {
                IMObjectRelationship rel = (IMObjectRelationship) editor.getObject();
                ProductSupplier ps = new ProductSupplier(rel, ServiceHelper.getArchetypeService());
                List<ProductPrice> updated = updater.update((Product) getObject(), ps, false);
                for (ProductPrice price : updated) {
                    updatePriceEditor(price, currentPrices);
                }
            }
            prices.refresh();
        }
    }

    /**
     * Refreshes the price editor associated with a product price.
     *
     * @param price   the price
     * @param editors the price editors
     */
    private void updatePriceEditor(ProductPrice price, Collection<IMObjectEditor> editors) {
        for (IMObjectEditor editor : editors) {
            if (editor.getObject().equals(price) && (editor instanceof ProductPriceEditor)) {
                ((ProductPriceEditor) editor).refresh();
                break;
            }
        }
    }

    /**
     * Verifies that:
     * <ul>
     * <li>unit prices with no pricing groups don't overlap other unit prices with no pricing groups on date
     * range</li>
     * <li>unit prices with pricing groups that overlap on date range have different pricing groups</li>
     * </ul>
     *
     * @param validator the validator
     * @return {@code true} if the prices are valid
     */
    private boolean validateUnitPrices(Validator validator) {
        boolean valid = true;
        Product product = (Product) getObject();
        List<ProductPrice> unitPrices = new ArrayList<>();
        for (org.openvpms.component.model.product.ProductPrice p : product.getProductPrices()) {
            ProductPrice price = (ProductPrice) p;
            if (price.isA(ProductArchetypes.UNIT_PRICE)) {
                unitPrices.add(price);
            }
        }

        while (unitPrices.size() > 1) {
            ProductPrice price = unitPrices.remove(0);
            for (ProductPrice other : unitPrices) {
                if (DateRules.intersects(price.getFromDate(), price.getToDate(), other.getFromDate(),
                                         other.getToDate())) {
                    if (DateRules.dateEquals(price.getToDate(), other.getFromDate())) {
                        // intersection is on time only. Make them them so that they don't intersect
                        setToDate(price, other.getFromDate());
                    } else if (DateRules.dateEquals(price.getFromDate(), other.getToDate())) {
                        setToDate(other, price.getFromDate());
                    } else {
                        IMObjectBean priceBean = new IMObjectBean(price);
                        IMObjectBean otherBean = new IMObjectBean(other);
                        List<Lookup> priceGroups = priceBean.getValues(PRICING_GROUPS, Lookup.class);
                        List<Lookup> otherGroups = otherBean.getValues(PRICING_GROUPS, Lookup.class);
                        if (priceGroups.isEmpty() && otherGroups.isEmpty()) {
                            validator.add(getPriceEditor(price), new ValidatorError(
                                    Messages.format("product.price.dateOverlap", formatPrice(price),
                                                    formatPrice(other))));
                            valid = false;
                            break;
                        } else if (priceGroups.removeAll(otherGroups)) {
                            validator.add(getPriceEditor(price), new ValidatorError(
                                    Messages.format("product.price.groupOverlap", formatPrice(price),
                                                    formatPrice(other))));
                            valid = false;
                            break;
                        }
                    }
                }
            }
        }
        return valid;
    }

    /**
     * Sets the to-date of a price.
     *
     * @param price the price
     * @param date  the date
     */
    private void setToDate(ProductPrice price, Date date) {
        IMObjectEditor editor = getPriceEditor(price);
        if (editor instanceof ProductPriceEditor) {
            ((ProductPriceEditor) editor).setToDate(date);
        } else {
            price.setToDate(date);
        }
    }

    /**
     * Verifies that if there are doses, a concentration is required and that doses don't overlap on weight range.
     *
     * @param validator the validator
     * @return {@code true} if the doses are valid, or the product has no doses
     */
    private boolean validateDoses(Validator validator) {
        boolean result = true;
        EditableIMObjectCollectionEditor editor = (EditableIMObjectCollectionEditor) getEditor(DOSES);
        if (editor != null) {
            result = validateConcentration(editor, validator) && validateDoses(editor, validator);
        }
        return result;
    }

    /**
     * Verifies a concentration has been specified if there are doses.
     *
     * @param doses     the doses editor
     * @param validator the validator
     * @return {@code true} if a concentration has been specified no concentration is required, otherwise {@code false}
     */
    private boolean validateConcentration(EditableIMObjectCollectionEditor doses, Validator validator) {
        boolean result = true;
        if (!doses.getCurrentObjects().isEmpty()) {
            Property property = getProperty(CONCENTRATION);
            BigDecimal concentration = property.getBigDecimal(BigDecimal.ZERO);
            if (MathRules.isZero(concentration)) {
                result = false;
                validator.add(this, new ValidatorError(Messages.format("product.concentration.required")));
            }
        }
        return result;
    }

    /**
     * Verifies that doses don't overlap on weight range.
     *
     * @param editor    the dose editor
     * @param validator the validator
     * @return {@code true} if the doses are valid, otherwise {@code false}
     */
    private boolean validateDoses(EditableIMObjectCollectionEditor editor, Validator validator) {
        Lookup all = new Lookup(); // dummy to indicate the dose applies to all species.
        Map<Lookup, List<IMObject>> dosesBySpecies = new LinkedHashMap<>();
        for (IMObject dose : editor.getCurrentObjects()) {
            IMObjectBean bean = new IMObjectBean(dose);
            List<Lookup> values = bean.getValues("species", Lookup.class);
            Lookup species = !values.isEmpty() ? values.get(0) : all;
            List<IMObject> doses = dosesBySpecies.get(species);
            if (doses == null) {
                doses = new ArrayList<>();
                dosesBySpecies.put(species, doses);
            }
            doses.add(dose);
        }
        for (Map.Entry<Lookup, List<IMObject>> entry : dosesBySpecies.entrySet()) {
            List<IMObject> doses = new ArrayList<>(entry.getValue());
            while (doses.size() > 1) {
                IMObjectBean dose = new IMObjectBean(doses.remove(0));
                BigDecimal minWeight = dose.getBigDecimal(MIN_WEIGHT, BigDecimal.ZERO);
                BigDecimal maxWeight = dose.getBigDecimal(MAX_WEIGHT, BigDecimal.ZERO);
                WeightUnits units = WeightUnits.fromString(dose.getString(WEIGHT_UNITS));
                for (IMObject otherDose : doses) {
                    IMObjectBean other = new IMObjectBean(otherDose);
                    BigDecimal otherMinWeight = other.getBigDecimal(MIN_WEIGHT, BigDecimal.ZERO);
                    BigDecimal otherMaxWeight = other.getBigDecimal(MAX_WEIGHT, BigDecimal.ZERO);
                    WeightUnits otherUnits = WeightUnits.fromString(other.getString(WEIGHT_UNITS));
                    if (units != null && otherUnits != null && !units.equals(otherUnits)) {
                        otherMinWeight = MathRules.convert(otherMinWeight, otherUnits, units);
                        otherMaxWeight = MathRules.convert(otherMaxWeight, otherUnits, units);
                    }
                    if (MathRules.intersects(minWeight, maxWeight, otherMinWeight, otherMaxWeight)) {
                        validator.add(this, new ValidatorError(Messages.format("product.dose.weightOverlap",
                                                                               dose.getObject().getName(),
                                                                               otherDose.getName())));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Invoked when a product-supplier relationship changes to ensure that only one is preferred.
     */
    private void updatePreferred() {
        EditableIMObjectCollectionEditor suppliers = getSuppliersEditor();
        if (suppliers != null) {
            IMObjectEditor currentEditor = suppliers.getCurrentEditor();
            if (currentEditor != null && currentEditor.getProperty(PREFERRED).getBoolean()) {
                // current relationship is preferred. Ensure other suppliers are not preferred
                for (IMObject object : suppliers.getCurrentObjects()) {
                    if (!object.equals(currentEditor.getObject())) {
                        IMObjectEditor editor = suppliers.getEditor(object);
                        if (editor != null) {
                            Property preferred = editor.getProperty(PREFERRED);
                            if (preferred.getBoolean()) {
                                preferred.setValue(false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Ensures that a product has only one preferred supplier.
     *
     * @param validator the validator
     * @return {@code true} if there is at most one preferred supplier, otherwise {@code false}
     */
    private boolean validateSuppliers(Validator validator) {
        boolean valid = true;
        EditableIMObjectCollectionEditor editor = getSuppliersEditor();
        if (editor != null) {
            IMObjectReference preferred = null;
            for (IMObject object : editor.getCurrentObjects()) {
                IMObjectRelationship relationship = (IMObjectRelationship) object;
                IMObjectBean bean = new IMObjectBean(relationship);
                if (bean.getBoolean(PREFERRED)) {
                    if (preferred != null) {
                        IMObject first = getObject(preferred);
                        IMObject second = getObject(relationship.getTarget());
                        String firstName = (first != null) ? first.getName() : null;
                        String secondName = (second != null) ? second.getName() : null;
                        // shouldn't fail to retrieve suppliers...
                        String message = Messages.format("product.supplier.multiplepreferred", firstName, secondName);
                        validator.add(editor.getProperty(), new ValidatorError(editor.getProperty(), message));
                        valid = false;
                        break;
                    } else {
                        preferred = relationship.getTarget();
                    }
                }
            }
        }
        return valid;
    }

    /**
     * Formats a price for error reporting.
     *
     * @param price the price
     * @return the price text
     */
    private String formatPrice(ProductPrice price) {
        String amount = NumberFormatter.formatCurrency(price.getPrice());
        if (price.getFromDate() != null) {
            String date = DateFormatter.formatDate(price.getFromDate(), false);
            return Messages.format("product.price.priceWithStartDate", amount, date);
        }
        return Messages.format("product.price.priceWithNoStartDate", amount);
    }

    /**
     * Returns an editor for a price.
     *
     * @param price the price
     * @return the price editor
     */
    private IMObjectEditor getPriceEditor(ProductPrice price) {
        EditableIMObjectCollectionEditor prices = getPricesEditor();
        return prices.getEditor(price);
    }

}
