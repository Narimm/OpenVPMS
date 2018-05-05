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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundTextArea;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.ParticipationCollectionEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientActEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.history.PatientHistoryDatingPolicy;

import java.util.List;


/**
 * And editor that combines parts of an <em>act.patientClinicalEvent</em> and an <em>act.patientClinicalNote</em>
 *
 * @author Tim Anderson
 */
public class PatientVisitNoteEditor extends AbstractPatientClinicalActEditor {

    /**
     * The note editor.
     */
    private PatientActEditor noteEditor;

    /**
     * The clinician editor.
     */
    private ParticipationCollectionEditor clinicianEditor;

    /**
     * Constructs a new {@link PatientVisitNoteEditor}.
     *
     * @param event   the event to edit
     * @param context the layout context
     */
    public PatientVisitNoteEditor(Act event, LayoutContext context) {
        super(event, null, ActStatus.COMPLETED, context);
        initParticipant("location", context.getContext().getLocation());

        // create an editor for the clinical note
        ActRelationshipCollectionEditor items = createItemsEditor(event, getCollectionProperty("items"));
        IMObject object = items.create(PatientArchetypes.CLINICAL_NOTE);
        if (object == null) {
            throw new IllegalStateException("Failed to create " + PatientArchetypes.CLINICAL_NOTE);
        }
        items.add(object);
        noteEditor = (PatientActEditor) items.getEditor(object);
        getEditors().add(noteEditor);

        // update the event clinician when the note clinician changes
        clinicianEditor = new ParticipationCollectionEditor(noteEditor.getCollectionProperty("clinician"),
                                                            noteEditor.getObject(), context);
        clinicianEditor.addModifiableListener(modifiable -> setParticipant("clinician", noteEditor.getClinicianRef()));

        // update the event start time when the note start time changes
        Property noteStartTime = noteEditor.getProperty("startTime");
        noteStartTime.addModifiableListener(modifiable -> setStartTime(noteEditor.getStartTime()));
    }

    /**
     * Returns a display name for the object being edited.
     *
     * @return a display name for the object
     */
    @Override
    public String getDisplayName() {
        return Messages.get("patient.record.summary.visitandnote");
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    private class LayoutStrategy extends ActLayoutStrategy {
        public LayoutStrategy() {
            super(false);
        }

        /**
         * Apply the layout strategy.
         * <p>
         * This renders an object in a {@code Component}, using a factory to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component containing the rendered {@code object}
         */
        @Override
        public ComponentState apply(IMObject object, PropertySet properties, IMObject parent,
                                    LayoutContext context) {
            ArchetypeNodes nodes = ArchetypeNodes.none().simple("reason");
            setArchetypeNodes(nodes);
            return super.apply(object, properties, parent, context);
        }

        /**
         * Lays out components in a grid.
         *
         * @param object     the object to lay out
         * @param properties the properties
         * @param context    the layout context
         */
        @Override
        protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context) {
            ComponentSet set = createComponentSet(object, properties, context);
            if (noteEditor != null) {
                Act act = noteEditor.getObject();
                Property startTime = noteEditor.getProperty("startTime");
                PatientHistoryDatingPolicy policy = ServiceHelper.getBean(PatientHistoryDatingPolicy.class);
                if (!startTime.isReadOnly() && !policy.canEditStartTime(act)) {
                    startTime = createReadOnly(startTime);
                }
                set.add(0, createComponent(startTime, act, context));
                Property note = noteEditor.getProperty("note");
                TextComponent textArea = new BoundTextArea(note);
                if (note.getMaxLength() != -1) {
                    textArea.setMaximumLength(note.getMaxLength());
                }
                textArea.setStyleName("PatientClinicalNote.note");
                set.add(new ComponentState(ColumnFactory.create("PatientClinicalNote.inset", textArea), note));
                set.add(new ComponentState(clinicianEditor));
            }

            ComponentGrid grid = new ComponentGrid();
            grid.add(set, 1);
            return grid;
        }

        /**
         * Lays out child components in a grid.
         *
         * @param object     the object to lay out
         * @param parent     the parent object. May be {@code null}
         * @param properties the properties
         * @param container  the container to use
         * @param context    the layout context
         */
        @Override
        protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties,
                                      Component container, LayoutContext context) {
            ComponentGrid grid = createGrid(object, properties, context);
            Grid component = createGrid(grid);
            component.setWidth(Styles.FULL_WIDTH);
            component.setColumnWidth(1, new Extent(90, Extent.PERCENT));
            container.add(ColumnFactory.create(Styles.INSET, component));
        }
    }
}
