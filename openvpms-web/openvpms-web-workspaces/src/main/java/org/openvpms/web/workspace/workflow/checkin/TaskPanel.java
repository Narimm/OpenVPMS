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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkin;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.edit.act.SingleParticipationCollectionEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.worklist.RestrictedWorkListParticipationEditor;
import org.openvpms.web.workspace.workflow.worklist.RestrictedWorkListTaskEditor;
import org.openvpms.web.workspace.workflow.worklist.ScheduleWorkListQuery;

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
        IMObjectCollectionEditor workListEditor = editor.getWorkListCollectionEditor();
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
    public void setWorkList(Entity worklist) {
        editor.setWorkList(worklist);
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    Editor getWorkListEditor() {
        return editor.getWorkListCollectionEditor();
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
    private static class TaskEditor extends RestrictedWorkListTaskEditor {

        /**
         * The schedule associated with the appointment, if an appointment was supplied.
         */
        private final Entity schedule;

        /**
         * Constructs a {@link TaskEditor}.
         *
         * @param task        the task
         * @param appointment the current appointment. May be {@code null}
         * @param context     the context
         */
        TaskEditor(Act task, Act appointment, LayoutContext context) {
            super(task, null, context);
            ArchetypeServiceFunctions functions = ServiceHelper.getBean(ArchetypeServiceFunctions.class);
            if (appointment != null) {
                IMObjectBean bean = getBean(appointment);
                schedule = bean.getTarget("schedule", Entity.class);
                String reason = functions.lookup(appointment, "reason", "Appointment");
                String notes = appointment.getDescription();
                if (notes == null) {
                    notes = "";
                }
                String description = Messages.format("workflow.checkin.task.description", reason, notes);
                Property property = getProperty("description");
                int maxLength = property.getMaxLength();
                property.setValue(StringUtils.abbreviate(description, maxLength));
            } else {
                schedule = null;
            }
            initWorkListEditor();
        }

        /**
         * Returns a default work list.
         * <p/>
         * This returns the default work list associated with the schedule, if check-in was launched from an
         * appointment and the schedule has {@code useAllWorkLists = true}.
         *
         * @return a default work list, or {@code null} if there is no default
         */
        @Override
        protected Entity getDefaultWorkList() {
            Entity result = null;
            if (schedule != null) {
                IMObjectBean bean = getBean(schedule);
                boolean useAllWorkLists = bean.getBoolean("useAllWorkLists", true);
                if (!useAllWorkLists) {
                    IArchetypeService service = ServiceHelper.getArchetypeService();
                    result = EntityRelationshipHelper.getDefaultTarget(schedule, "workLists", false, service);
                }
            }
            return result;
        }

        /**
         * Returns the work list editor.
         *
         * @return the work list editor
         */
        @Override
        public SingleParticipationCollectionEditor getWorkListCollectionEditor() {
            return super.getWorkListCollectionEditor();
        }

        public boolean isEmpty() {
            return isNull("worklist") && isNull("taskType");
        }

        /**
         * Creates an editor to edit a work list participation.
         *
         * @param participation the participation to edit
         * @return a new editor
         */
        @Override
        protected ParticipationEditor<Entity> createWorkListEditor(Participation participation) {
            final LayoutContext layoutContext = getLayoutContext();
            return new RestrictedWorkListParticipationEditor(participation, getObject(), layoutContext) {
                @Override
                protected Query<Entity> createWorkListQuery(String name) {
                    Party location = layoutContext.getContext().getLocation();
                    ScheduleWorkListQuery query = new ScheduleWorkListQuery(schedule, location);
                    return new EntityQuery<>(query, layoutContext.getContext());
                }
            };
        }

        /**
         * Invoked when layout has completed. All editors have been created.
         */
        @Override
        protected void onLayoutCompleted() {
            super.onLayoutCompleted();
            // need to remove the parent component layout data as the components are being used outside of the editor
            getWorkListCollectionEditor().getComponent().setLayoutData(null);
            getTaskTypeEditor().getComponent().setLayoutData(null);
        }


        IMObjectCollectionEditor getTypeEditor() {
            return ((IMObjectCollectionEditor) getEditor("taskType"));
        }

        private boolean isNull(String name) {
            ParticipationEditor editor = getParticipationEditor(name, true);
            return editor == null || editor.isNull();
        }

    }

}
