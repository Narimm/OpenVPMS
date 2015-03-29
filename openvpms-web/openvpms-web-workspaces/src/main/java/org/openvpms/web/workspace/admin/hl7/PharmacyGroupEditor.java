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

package org.openvpms.web.workspace.admin.hl7;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * An editor for <em>entity.HL7ServicePharmacyGroup</em> archetypes.
 * <p/>
 * This ensures that pharmacies have different practice locations.
 *
 * @author Tim Anderson
 */
public class PharmacyGroupEditor extends AbstractIMObjectEditor {

    /**
     * The pharmacies.
     */
    private final Pharmacies pharmacies;

    /**
     * Constructs a {@link {PharmacyGroupEditor}}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public PharmacyGroupEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        this.pharmacies = ServiceHelper.getBean(Pharmacies.class);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validatePharmacies(validator);
    }

    /**
     * Ensures that each pharmacy has a different practice location.
     *
     * @param validator the validator
     * @return {@code true} if each pharmacy has a different practice location
     */
    private boolean validatePharmacies(Validator validator) {
        boolean result = true;
        Map<IMObjectReference, Entity> locations = new HashMap<IMObjectReference, Entity>();
        CollectionProperty property = getCollectionProperty("pharmacies");
        for (Object value : property.getValues()) {
            IMObjectRelationship relationship = (IMObjectRelationship) value;
            Entity pharmacy = (relationship.getTarget() != null) ?
                              pharmacies.getPharmacy(relationship.getTarget()) : null;
            if (pharmacy != null) {
                EntityBean bean = new EntityBean(pharmacy);
                IMObjectReference location = bean.getNodeTargetObjectRef("location");
                if (location != null) {
                    Entity existing = locations.get(location);
                    if (existing != null) {
                        String message = Messages.format("admin.hl7.pharmacyGroup.duplicateLocation",
                                                         existing.getName(), pharmacy.getName(),
                                                         IMObjectHelper.getName(location));
                        validator.add(this, new ValidatorError(property, message));
                        result = false;
                        break;
                    } else {
                        locations.put(location, pharmacy);
                    }
                }
            }
        }
        return result;
    }
}


