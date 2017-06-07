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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.SelectFieldIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.AllNoneListCellRenderer;
import org.openvpms.web.component.im.list.StyledListCell;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.alert.Alert;

import java.util.List;

/**
 * Editor for <em>participation.patientAlertType</em> participations.
 *
 * @author Tim Anderson
 */
public class AlertTypeParticipationEditor extends ParticipationEditor<Entity> {

    /**
     * Constructs an {@link AlertTypeParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context
     */
    public AlertTypeParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
    }

    /**
     * Creates a new object reference editor for the participation entity.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createEntityEditor(Property property) {
        SelectFieldIMObjectReferenceEditor<Entity> editor
                = new SelectFieldIMObjectReferenceEditor<Entity>(property, getAlertTypes(), false) {
            @Override
            protected void modified() {
                super.modified();
                refresh(this);
            }
        };
        editor.setListCellRenderer(new AlertTypeCellRenderer());
        refresh(editor);
        return editor;
    }

    /**
     * Updates the colour of the alert type field with that of the alert type.
     *
     * @param editor the alert type field editor
     */
    private void refresh(SelectFieldIMObjectReferenceEditor<Entity> editor) {
        IMObject selected = editor.getObject();
        if (selected != null) {
            Alert alert = new Alert(selected);
            Color background = alert.getColour();
            Color foreground = alert.getTextColour();
            editor.getComponent().setBackground(background);
            editor.getComponent().setForeground(foreground);
        }
    }

    /**
     * Returns the alert types.
     *
     * @return the alert types
     */
    @SuppressWarnings("unchecked")
    private List<Entity> getAlertTypes() {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.ALERT_TYPE, true, true);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        return (List<Entity>) (List) ServiceHelper.getArchetypeService().get(query).getResults();
    }

    /**
     * Renders the alert types cell background with that from the <em>entity.patientAlertType</em>.
     */
    private static class AlertTypeCellRenderer extends AllNoneListCellRenderer<IMObject> {

        /**
         * Constructs an {@link AlertTypeCellRenderer}.
         */
        public AlertTypeCellRenderer() {
            super(IMObject.class);
        }

        /**
         * Renders an object.
         *
         * @param list   the list component
         * @param object the object to render. May be {@code null}
         * @param index  the object index
         * @return the rendered object
         */
        @Override
        protected Object getComponent(Component list, IMObject object, int index) {
            if (object != null) {
                Alert alert = new Alert(object);
                Color background = alert.getColour();
                Color foreground = alert.getTextColour();
                return new StyledListCell(object.getName(), background, foreground);
            }
            return null;
        }
    }

}