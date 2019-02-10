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

package org.openvpms.web.workspace.admin.calendar;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.workflow.CalendarService;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.delete.AbstractIMObjectDeletionListener;
import org.openvpms.web.component.im.delete.ConfirmingDeleter;
import org.openvpms.web.component.im.delete.IMObjectDeleter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.AbstractCalendarEventEditDialog;
import org.openvpms.web.workspace.workflow.appointment.AbstractCalendarEventEditor;
import org.openvpms.web.workspace.workflow.appointment.DeleteSeriesDialog;
import org.openvpms.web.workspace.workflow.appointment.repeat.ScheduleEventSeriesState;

import java.util.List;

/**
 * Dialog for <em>act.calendarEvent</em> acts.
 *
 * @author Tim Anderson
 */
public class CalendarEventEditDialog extends AbstractCalendarEventEditDialog {

    /**
     * Delete button identifier.
     */
    private static final String DELETE_ID = "button.delete";

    /**
     * Constructs a {@link AbstractCalendarEventEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public CalendarEventEditDialog(AbstractCalendarEventEditor editor, Context context) {
        super(editor, context);
        ButtonSet buttons = getButtons();
        buttons.add(DELETE_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onDelete();
            }
        });
        enableDelete();
    }

    /**
     * Saves the current object, if saving is enabled.
     *
     * @return {@code true} if the object was saved
     */
    @Override
    public boolean save() {
        boolean save = super.save();
        enableDelete();
        return save;
    }

    /**
     * Verifies that the event or events in the series don't overlap existing events.
     * <p>
     * If they do, and double scheduling is allowed, a confirmation dialog is shown prompting to save or continue
     * editing.
     * <br/>
     * If double scheduling is not allowed, an error dialog is shown and no save is performed.
     *
     * @param times the event and series times
     * @param close determines if the dialog should close if the user OKs overlapping appointments
     * @return {@code true} if the event and series is valid, otherwise {@code false}
     */
    @Override
    protected boolean checkEventTimes(List<Times> times, boolean close) {
        boolean result = true;
        AbstractCalendarEventEditor editor = getEditor();
        Entity schedule = (Entity) editor.getSchedule();
        if (schedule != null) {
            CalendarService service = ServiceHelper.getBean(CalendarService.class);
            List<Times> overlaps = service.getOverlappingEvents(times, schedule, 1);
            Times overlap = (overlaps != null && !overlaps.isEmpty()) ? overlaps.get(0) : null;
            if (overlap != null) {
                displayOverlapError(overlap);
                result = false;
            }
        }
        return result;
    }

    /**
     * Prompts to confirm deletion of the event series.
     * <p>
     * If confirmed, deletes it and closes the dialog.
     *
     * @param state the event series state
     */
    @SuppressWarnings("unchecked")
    protected void deleteSeries(ScheduleEventSeriesState state) {
        DeleteSeriesDialog dialog = new DeleteSeriesDialog(state, getHelpContext().subtopic("deleteseries"));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                boolean deleted = false;
                if (dialog.single()) {
                    deleted = state.delete();
                } else if (dialog.future()) {
                    deleted = state.deleteFuture();
                } else if (dialog.all()) {
                    deleted = state.deleteSeries();
                }
                if (deleted) {
                    close(DELETE_ID);
                }
            }
        });
        dialog.show();
    }

    /**
     * Prompts to confirm deletion of the event.
     * <p>
     * If confirmed, deletes it and closes the dialog.
     *
     * @param event the event
     */
    @SuppressWarnings("unchecked")
    protected void delete(Act event) {
        IMObjectDeleter<Act> deleter = (ConfirmingDeleter<Act>) ServiceHelper.getBean(ConfirmingDeleter.class);
        HelpContext delete = getHelpContext().subtopic("delete");
        Context local = new LocalContext(getContext());
        // nest the context so the global context isn't updated. See OVPMS-2046
        deleter.delete(event, local, delete, new AbstractIMObjectDeletionListener<Act>() {
            public void deleted(Act object) {
                close(DELETE_ID);
            }

            public void deactivated(Act object) {
                close(DELETE_ID);
            }
        });

    }

    /**
     * Invoked to delete the current event.
     * <p>
     * If the event is associated with a series
     */
    private void onDelete() {
        Act event = getEvent();
        event = IMObjectHelper.reload(event);
        if (event != null) {
            ScheduleEventSeriesState state = new ScheduleEventSeriesState(event, ServiceHelper.getArchetypeService());
            if (state.hasSeries() && state.canEditFuture()) {
                deleteSeries(state);
            } else {
                delete(event);
            }
        } else {
            close(DELETE_ID);
        }
    }

    /**
     * Enables the delete button if the event has been saved.
     */
    private void enableDelete() {
        ButtonSet buttons = getButtons();
        buttons.setEnabled(DELETE_ID, !getEvent().isNew());
    }
}