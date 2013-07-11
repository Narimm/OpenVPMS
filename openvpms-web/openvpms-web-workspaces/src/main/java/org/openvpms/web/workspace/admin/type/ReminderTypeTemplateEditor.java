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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.type;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;

/**
 * An editor for <em>entityRelationship.reminderTypeTemplate</em> relationships.
 * <p/>
 * This ensures that one of the <em>list</em> and <em>export</em> nodes may be true, but not both.
 *
 * @author Tim Anderson
 */
public class ReminderTypeTemplateEditor extends EntityRelationshipEditor {

    /**
     * Constructs a {@link ReminderTypeTemplateEditor}.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param context      the layout context
     */
    public ReminderTypeTemplateEditor(EntityRelationship relationship, IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
        getProperty("list").addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onListChanged();
            }
        });
        getProperty("export").addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onExportChanged();
            }
        });
    }

    /**
     * Invoked when the list node changes.
     * <p/>
     * If true, ensures that the export node is {@code false}
     */
    private void onListChanged() {
        Boolean list = (Boolean) getProperty("list").getValue();
        if (list != null && list) {
            getProperty("export").setValue(false);
        }
    }

    /**
     * Invoked when the export node changes.
     * <p/>
     * If true, ensures that the list node is {@code false}
     */
    private void onExportChanged() {
        Boolean export = (Boolean) getProperty("export").getValue();
        if (export != null && export) {
            getProperty("list").setValue(false);
        }
    }
}
