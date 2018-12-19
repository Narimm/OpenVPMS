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

package org.openvpms.web.workspace.workflow.checkin;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.edit.act.SingleParticipationCollectionEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.worklist.TaskActEditor;

import java.util.Date;

import static org.openvpms.web.echo.style.Styles.BOLD;

/**
 * Check-in task panel.
 *
 * @author Tim Anderson
 */
class TaskPanel {

    /**
     * The task editor
     */
    private final TaskEditor editor;

    /**
     * The listener to notify of work list changes.
     */
    private Runnable listener;

    /**
     * Constructs a {@link TaskPanel}.
     *
     * @param arrivalTime   the arrival time
     * @param appointment   the appointment. May be {@code null}
     * @param layoutContext the layout context
     * @param service       the archetype service
     */
    TaskPanel(Date arrivalTime, Act appointment, LayoutContext layoutContext, IArchetypeService service) {
        Act task = (Act) service.create(ScheduleArchetypes.TASK);
        task.setActivityStartTime(arrivalTime);
        editor = new TaskEditor(task, appointment, layoutContext);
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        editor.setPatient(patient);
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician. May be {@code null}
     */
    public void setClinician(User clinician) {
        editor.setClinician(clinician);
    }

    /**
     * Returns the task act created by Check-In.
     *
     * @return the task, or {@code null} if none was saved
     */
    public Act getTask() {
        Act act = editor.getObject();
        return act != null && !act.isNew() ? act : null;
    }

    /**
     * Validates the task.
     *
     * @param validator the validator
     * @return {@code true} if the editor is valid, otherwise {@code false}
     */
    public boolean validate(Validator validator) {
        return editor.isEmpty() || editor.validate(validator);
    }

    /**
     * Saves the task, if one is required.
     */
    public void save() {
        if (!editor.isEmpty() && editor.isModified()) {
            editor.save();
        }
    }

    /**
     * Lays out the panel in the specified grid.
     *
     * @param grid the grid
     * @return the focus group
     */
    public FocusGroup layout(ComponentGrid grid) {
        editor.getComponent();
        IMObjectCollectionEditor workListEditor = editor.getWorkListEditor();
        workListEditor.addModifiableListener(modifiable -> onWorkListChanged());
        IMObjectCollectionEditor taskTypeEditor = editor.getTypeEditor();
        String taskTypeDisplayName = taskTypeEditor.getProperty().getDisplayName();

        grid.add(TableHelper.createSpacer());
        grid.add(LabelFactory.text(editor.getDisplayName(), BOLD));
        grid.add(LabelFactory.text(workListEditor.getProperty().getDisplayName()),
                 workListEditor.getComponent(),
                 LabelFactory.text(taskTypeDisplayName),
                 taskTypeEditor.getComponent());

        FocusGroup result = new FocusGroup("TaskPanel");
        result.add(workListEditor.getFocusGroup());
        result.add(taskTypeEditor.getFocusGroup());
        return result;
    }

    /**
     * Registers a listener to be notified of work list changes.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setWorkListListener(Runnable listener) {
        this.listener = listener;
    }

    /**
     * Returns the work list.
     *
     * @return the work list. May be {@code null}
     */
    public Entity getWorkList() {
        return editor.getWorkList();
    }

    /**
     * Sets the work list.
     *
     * @param worklist the work list. May be {@code null}
     */
    public void setWorkList(Party worklist) {
        editor.setWorkList(worklist);
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    Editor getWorkListEditor() {
        return editor.getWorkListEditor();
    }

    /**
     * Invoked when the work list changes. Notifies any registered listener.
     */
    private void onWorkListChanged() {
        if (listener != null) {
            try {
                listener.run();
            } catch (Throwable exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Editor for the task.
     */
    private static class TaskEditor extends TaskActEditor {

        /**
         * Constructs a {@link TaskEditor}.
         *
         * @param task        the task
         * @param appointment the current appointment. May be {@code null}
         * @param context the context
         */
        TaskEditor(Act task, Act appointment, LayoutContext context) {
            super(task, null, context);
            ArchetypeServiceFunctions functions = ServiceHelper.getBean(ArchetypeServiceFunctions.class);
            if (appointment != null) {
                String reason = functions.lookup(appointment, "reason", "Appointment");
                String notes = appointment.getDescription();
                if (notes == null) {
                    notes = "";
                }
                String description = Messages.format("workflow.checkin.task.description", reason, notes);
                Property property = getProperty("description");
                int maxLength = property.getMaxLength();
                property.setValue(StringUtils.abbreviate(description, maxLength));
            }

            SingleParticipationCollectionEditor workListEditor = createWorkListEditor();
            addEditor(workListEditor);
            workListEditor.addModifiableListener(modifiable -> getTaskTypeEditor().setWorkList(getWorkList()));
        }

        public boolean isEmpty() {
            return isNull("worklist") && isNull("taskType");
        }

        /**
         * Invoked when layout has completed. All editors have been created.
         */
        @Override
        protected void onLayoutCompleted() {
            super.onLayoutCompleted();
            // need to remove the parent component layout data as the components are being used outside of the editor
            getWorkListEditor().getComponent().setLayoutData(null);
            getTaskTypeEditor().getComponent().setLayoutData(null);
        }

        IMObjectCollectionEditor getWorkListEditor() {
            return (IMObjectCollectionEditor) getEditor("worklist");
        }

        IMObjectCollectionEditor getTypeEditor() {
            return ((IMObjectCollectionEditor) getEditor("taskType"));
        }

        private boolean isNull(String name) {
            ParticipationEditor editor = getParticipationEditor(name, true);
            return editor == null || editor.isNull();
        }

        /**
         * Creates the work list editor.
         *
         * @return a new work-list editor
         */
        private SingleParticipationCollectionEditor createWorkListEditor() {
            CollectionProperty property = getCollectionProperty("worklist");
            return new SingleParticipationCollectionEditor(property, getObject(), getLayoutContext());
        }
    }

}
