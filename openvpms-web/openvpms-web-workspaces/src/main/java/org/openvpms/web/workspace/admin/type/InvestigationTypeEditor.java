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

package org.openvpms.web.workspace.admin.type;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.EditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityLinkEditor;
import org.openvpms.web.component.property.Validator;

/**
 * Editor for <em>entity.investigationType</em>.
 * <p>
 * This ensures that both <em>universalServiceIdentifier</em> and </em><em>laboratory</em> are required if one is
 * specified.
 *
 * @author Tim Anderson
 */
public class InvestigationTypeEditor extends AbstractIMObjectEditor {

    /**
     * Universal service identifier node name.
     */
    public static final String SERVICE_IDENTIFIER = "universalServiceIdentifier";

    /**
     * Laboratory node name.
     */
    public static final String LABORATORY = "laboratory";

    /**
     * Constructs an {@link AbstractIMObjectEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public InvestigationTypeEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Returns the laboratory.
     *
     * @return the laboratory. May be {@code null}
     */
    public Entity getLaboratory() {
        Editor editor = getEditor(LABORATORY);
        if (editor instanceof EditableIMObjectCollectionEditor) {
            EditableIMObjectCollectionEditor collectionEditor = (EditableIMObjectCollectionEditor) editor;
            EntityLinkEditor linkEditor = (EntityLinkEditor) collectionEditor.getFirstEditor(false);
            if (linkEditor != null) {
                EntityLink link = (EntityLink) linkEditor.getObject();
                return (Entity) getObject(link.getTarget());
            }
        }
        return null;
    }

    /**
     * Sets the laboratory.
     *
     * @param entity the laboratory. May be {@code null}
     */
    public void setLaboratory(Entity entity) {
        Editor editor = getEditor(LABORATORY);
        if (editor instanceof EditableIMObjectCollectionEditor) {
            boolean create = entity != null;
            EditableIMObjectCollectionEditor collectionEditor = (EditableIMObjectCollectionEditor) editor;
            EntityLinkEditor linkEditor = (EntityLinkEditor) collectionEditor.getFirstEditor(create);
            if (linkEditor != null) {
                linkEditor.setTarget(entity);
            }
        }
    }

    /**
     * Returns the universal service identifier, used by HL7 services.
     *
     * @return the universal service identifier. May be {@code null}
     */
    public String getUniversalServiceIdentifier() {
        return getProperty(SERVICE_IDENTIFIER).getString();
    }

    /**
     * Sets the universal service identifier, used by HL7 services.
     *
     * @param id the universal service identifier. May be {@code null}
     */
    public void setUniversalServiceIdentifier(String id) {
        getProperty(SERVICE_IDENTIFIER).setValue(id);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validateLaboratory(validator);
    }

    /**
     * Ensures that both <em>universalServiceIdentifier</em> and </em><em>laboratory</em> are required if one is
     * specified.
     *
     * @param validator the validator
     * @return {@code true} if neither are set, or both are set, otherwise {@code false}
     */
    private boolean validateLaboratory(Validator validator) {
        boolean valid = true;
        String id = getUniversalServiceIdentifier();
        Entity laboratory = getLaboratory();
        if (id != null && laboratory == null) {
            valid = reportRequired(LABORATORY, validator);
        } else if (id == null && laboratory != null) {
            valid = reportRequired(SERVICE_IDENTIFIER, validator);
        }
        return valid;
    }

}
