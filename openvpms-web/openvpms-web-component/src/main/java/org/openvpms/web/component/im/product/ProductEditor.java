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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceUpdater;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.EditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.MultipleEntityRelationshipCollectionEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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
     * Constructs a {@link ProductEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context. May be {@code null}.
     */
    public ProductEditor(Product object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        CollectionProperty suppliers = getCollectionProperty("suppliers");
        CollectionProperty stock = getCollectionProperty("stockLocations");
        if (suppliers != null && stock != null) {
            RelationshipCollectionEditor stockLocations
                    = new MultipleEntityRelationshipCollectionEditor(stock, object, getLayoutContext()) {
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
        updater = new ProductPriceUpdater(ServiceHelper.getCurrencies(), ServiceHelper.getArchetypeService(),
                                          ServiceHelper.getLookupService());
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
            valid = validateUnitPrices(validator);
        }
        return valid;
    }

    /**
     * Returns the product supplier references.
     *
     * @return the product supplier references
     */
    public List<IMObjectReference> getSuppliers() {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        EditableIMObjectCollectionEditor suppliers = (EditableIMObjectCollectionEditor) getEditor("suppliers");
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
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        RelationshipCollectionEditor stockLocations = (RelationshipCollectionEditor) getEditor("stockLocations", false);
        if (stockLocations != null) {
            strategy.addComponent(new ComponentState(stockLocations));
        }
        return strategy;
    }

    /**
     * Invoked when layout has completed. This can be used to perform
     * processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        IMObjectCollectionEditor editor = (IMObjectCollectionEditor) getEditor("suppliers");
        if (editor != null) {
            editor.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onSupplierChanged();
                }
            });
        }
    }

    /**
     * Invoked when a product-supplier relationship changes. This recalculates product prices if required.
     */
    private void onSupplierChanged() {
        EditableIMObjectCollectionEditor suppliers = (EditableIMObjectCollectionEditor) getEditor("suppliers");
        EditableIMObjectCollectionEditor prices = (EditableIMObjectCollectionEditor) getEditor("prices");
        Collection<IMObjectEditor> currentPrices = prices.getEditors();
        Collection<IMObjectEditor> editors = suppliers.getEditors();
        for (IMObjectEditor editor : editors) {
            EntityRelationship rel = (EntityRelationship) editor.getObject();
            ProductSupplier ps = new ProductSupplier(rel);
            List<ProductPrice> updated = updater.update((Product) getObject(), ps, false);
            for (ProductPrice price : updated) {
                updatePriceEditor(price, currentPrices);
            }
        }
        prices.refresh();
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
        List<ProductPrice> unitPrices = new ArrayList<ProductPrice>();
        for (ProductPrice price : product.getProductPrices()) {
            if (TypeHelper.isA(price, ProductArchetypes.UNIT_PRICE)) {
                unitPrices.add(price);
            }
        }

        while (unitPrices.size() > 1) {
            ProductPrice price = unitPrices.remove(0);
            for (ProductPrice other : unitPrices) {
                if (DateRules.intersects(price.getFromDate(), price.getToDate(), other.getFromDate(),
                                         other.getToDate())) {
                    IMObjectBean priceBean = new IMObjectBean(price);
                    IMObjectBean otherBean = new IMObjectBean(other);
                    List<Lookup> priceGroups = priceBean.getValues("pricingGroups", Lookup.class);
                    List<Lookup> otherGroups = otherBean.getValues("pricingGroups", Lookup.class);
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
        EditableIMObjectCollectionEditor prices = (EditableIMObjectCollectionEditor) getEditor("prices");
        return prices.getEditor(price);
    }

}
