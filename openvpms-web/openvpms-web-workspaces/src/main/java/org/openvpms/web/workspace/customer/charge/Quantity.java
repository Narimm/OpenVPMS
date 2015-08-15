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

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.style.Styles;

import java.math.BigDecimal;

/**
 * An act quantity that highlights the value if the quantity represents a default.
 *
 * @author Tim Anderson
 */
public class Quantity {

    /**
     * The quantity property.
     */
    private final Property property;

    /**
     * The component.
     */
    private ComponentState state;

    /**
     * The default quantity, or {@code null} if the quantity isn't a default
     */
    private BigDecimal defaultQuantity;

    /**
     * Constructs a {@link Quantity}.
     *
     * @param property the quantity property
     * @param object   the object
     * @param context  the layout context
     */
    public Quantity(Property property, IMObject object, LayoutContext context) {
        this.property = property;
        state = context.getComponentFactory().create(property, object);
        property.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onQuantityChanged();
            }
        });
    }

    /**
     * Returns the quantity.
     *
     * @return the quantity. May be {@code null}
     */
    public BigDecimal getValue() {
        return getProperty().getBigDecimal();
    }

    /**
     * Returns the property.
     *
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Sets the quantity.
     *
     * @param quantity  the quantity
     * @param isDefault if {@code true} the quantity is a default
     */
    public void setQuantity(BigDecimal quantity, boolean isDefault) {
        defaultQuantity = isDefault ? quantity : null;
        property.setValue(quantity);
        setStyle(isDefault);
    }

    /**
     * Clears the quantity.
     */
    public void clear() {
        setQuantity(null, false);
    }

    /**
     * Determines if the quantity is a default that should be highlight.
     *
     * @return {@code true} if the quantity is a default
     */
    public boolean isDefault() {
        BigDecimal value = property.getBigDecimal();
        return (defaultQuantity != null && value != null && MathRules.equals(value, defaultQuantity));
    }

    /**
     * Returns the component state.
     *
     * @return the component state
     */
    public ComponentState getState() {
        return state;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return state.getComponent();
    }

    /**
     * Invoked when the quantity changes.
     */
    private void onQuantityChanged() {
        boolean defaultValue = isDefault();
        if (!defaultValue) {
            defaultQuantity = null;
        }
        setStyle(defaultValue);
    }

    /**
     * Sets the field style.
     *
     * @param isDefault if {@code true}, highlight the field, otherwise use the edit style
     */
    private void setStyle(boolean isDefault) {
        String style = (isDefault) ? "highlight" : Styles.DEFAULT;
        getComponent().setStyleName(style);
    }

}
