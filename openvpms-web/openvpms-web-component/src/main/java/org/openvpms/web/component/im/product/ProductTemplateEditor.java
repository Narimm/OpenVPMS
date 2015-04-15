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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

/**
 * An editor for <em>product.template</em> archetypes.
 *
 * @author Tim Anderson
 */
public class ProductTemplateEditor extends ProductEditor {


    /**
     * Constructs a {@link ProductTemplateEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context. May be {@code null}.
     */
    public ProductTemplateEditor(Product object, IMObject parent, LayoutContext layoutContext) {
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
        return super.doValidation(validator) && validateIncludes(validator);
    }

    /**
     * Verifies that the template doesn't include itself.
     *
     * @param validator the validator
     * @return {@code true} if the template is valid
     */
    protected boolean validateIncludes(Validator validator) {
        boolean valid = true;
        IMObject object = getObject();
        if (!object.isNew()) {
            IMObjectReference reference = object.getObjectReference();
            CollectionProperty property = getCollectionProperty("includes");
            for (Object value : property.getValues()) {
                IMObjectRelationship relationship = (IMObjectRelationship) value;
                if (ObjectUtils.equals(reference, relationship.getTarget())) {
                    String message = Messages.format("product.template.includeself", object.getName());
                    validator.add(property, new ValidatorError(property, message));
                    valid = false;
                }
            }
        }
        return valid;
    }
}
