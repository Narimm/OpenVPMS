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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.type;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.doc.DocumentTemplateReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityLinkEditor;
import org.openvpms.web.component.property.Property;

/**
 * An editor for <em>entityLink.reminderConfigurationTemplateCustomer</em>
 * and <em>entityLink.reminderConfigurationTemplatePatient</em>archetypes, that constrains templates to those of
 * GROUPED_REMINDERS type.
 *
 * @author Tim Anderson
 */
public class ReminderConfigurationGroupTemplateEditor extends EntityLinkEditor {

    /**
     * Constructs an {@link ReminderConfigurationGroupTemplateEditor}.
     *
     * @param relationship the link
     * @param parent       the parent object
     * @param context      the layout context
     */
    public ReminderConfigurationGroupTemplateEditor(EntityLink relationship, IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
    }

    /**
     * Creates a new reference editor.
     *
     * @param property the reference property
     * @param context  the layout context
     * @return a new reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createReferenceEditor(Property property, LayoutContext context) {
        DocumentTemplateReferenceEditor editor = new DocumentTemplateReferenceEditor(property, getParent(), context,
                                                                                     true);
        editor.setTypes("GROUPED_REMINDERS");
        return editor;
    }
}
