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

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityLinkEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;

/**
 * Editor for <em>entityLink.productIncludes</em>.
 *
 * @author Tim Anderson
 */
public class ProductIncludesEditor extends EntityLinkEditor {

    /**
     * The minimum weight node.
     */
    private static final String MIN_WEIGHT = "minWeight";

    /**
     * The maximum weight node.
     */
    private static final String MAX_WEIGHT = "maxWeight";

    /**
     * The weight units node.
     */
    private static final String WEIGHT_UNITS = "weightUnits";

    /**
     * The low quantity node.
     */
    private static final String LOW_QUANTITY = "lowQuantity";

    /**
     * The high quantity node.
     */
    private static final String HIGH_QUANTITY = "highQuantity";

    /**
     * Constructs a {@link ProductIncludesEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public ProductIncludesEditor(EntityLink object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        getArchetypeNodes().exclude(MAX_WEIGHT, WEIGHT_UNITS);

        getProperty(LOW_QUANTITY).addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onLowQuantityChanged();
            }
        });

        getProperty(HIGH_QUANTITY).addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onHighQuantityChanged();
            }
        });
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validateWeight(validator);
    }

    /**
     * Validates that {@code minWeight < maxWeight}, if both are non-zero, and that the units are set.
     *
     * @param validator the validator
     * @return {@code true} if the weights are valid
     */
    private boolean validateWeight(Validator validator) {
        boolean valid = true;
        Property minWeight = getProperty(MIN_WEIGHT);
        Property maxWeight = getProperty(MAX_WEIGHT);
        BigDecimal min = minWeight.getBigDecimal(BigDecimal.ZERO);
        BigDecimal max = maxWeight.getBigDecimal(BigDecimal.ZERO);
        if (min.compareTo(BigDecimal.ZERO) != 0 || max.compareTo(BigDecimal.ZERO) != 0) {
            if (min.compareTo(max) >= 0) {
                validator.add(this, new ValidatorError(Messages.format("product.template.weighterror", min, max)));
                valid = false;
            } else if (getProperty(WEIGHT_UNITS).getString() == null) {
                validator.add(this, new ValidatorError(Messages.format("product.template.noweightunits", min, max)));
                valid = false;
            }
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
        return new ProductIncludesLayoutStrategy();
    }

    /**
     * Invoked when the low quantity changes.
     */
    private void onLowQuantityChanged() {
        Property highQuantity = getProperty(HIGH_QUANTITY);
        Property lowQuantity = getProperty(LOW_QUANTITY);
        BigDecimal low = lowQuantity.getBigDecimal(BigDecimal.ZERO);
        BigDecimal high = highQuantity.getBigDecimal(BigDecimal.ZERO);
        if (low.compareTo(high) > 0) {
            highQuantity.setValue(low);
        }
    }

    /**
     * Invoked when the high quantity changes.
     */
    private void onHighQuantityChanged() {
        Property highQuantity = getProperty(HIGH_QUANTITY);
        Property lowQuantity = getProperty(LOW_QUANTITY);
        BigDecimal low = lowQuantity.getBigDecimal(BigDecimal.ZERO);
        BigDecimal high = highQuantity.getBigDecimal(BigDecimal.ZERO);
        if (low.compareTo(high) > 0) {
            lowQuantity.setValue(high);
        }
    }

    private class ProductIncludesLayoutStrategy extends LayoutStrategy {

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
            ComponentState minWeight = createComponent(properties.get(MIN_WEIGHT), parent, context);
            ComponentState maxWeight = createComponent(properties.get(MAX_WEIGHT), parent, context);
            ComponentState weightUnits = createComponent(properties.get(WEIGHT_UNITS), parent, context);
            Label label = LabelFactory.create();
            label.setText("-");
            Row row = RowFactory.create(Styles.CELL_SPACING, minWeight.getComponent(), label, maxWeight.getComponent(),
                                        weightUnits.getComponent());
            FocusGroup weight = new FocusGroup("weight", minWeight.getComponent(), maxWeight.getComponent(),
                                               weightUnits.getComponent());
            String displayName = Messages.get("product.weight");
            addComponent(new ComponentState(row, minWeight.getProperty(), weight, displayName));
            return super.apply(object, properties, parent, context);
        }

    }
}
