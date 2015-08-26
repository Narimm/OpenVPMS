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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;

/**
 * Editor for <em>entity.productDose</em>.
 *
 * @author Tim Anderson
 */
public class ProductDoseEditor extends AbstractIMObjectEditor {

    /**
     * The species node.
     */
    public static final String SPECIES = "species";

    /**
     * The minimum weight node.
     */
    public static final String MIN_WEIGHT = "minWeight";

    /**
     * The maximum weight node.
     */
    public static final String MAX_WEIGHT = "maxWeight";

    /**
     * The weight units node.
     */
    public static final String WEIGHT_UNITS = "weightUnits";

    /**
     * Constructs a {@link ProductDoseEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public ProductDoseEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
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
        if (min.compareTo(max) >= 0) {
            validator.add(this, new ValidatorError(Messages.format("product.weighterror", min, max)));
            valid = false;
        }
        return valid;
    }

}
