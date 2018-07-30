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

package org.openvpms.web.workspace.admin.calendar;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.EditSeriesDialog;
import org.openvpms.web.workspace.workflow.appointment.repeat.ScheduleEventSeriesState;

import java.util.Date;

/**
 * An editor for <em>entity.calendar*</em> calendars.
 * <p>
 * This supports editing both the entity and the calendar events.
 *
 * @author Tim Anderson
 */
public class CalendarEditor extends AbstractIMObjectEditor {

    /**
     * The calendar viewer.
     */
    private final CalendarEventViewer viewer;


    /**
     * Constructs an {@link CalendarEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public CalendarEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        viewer = new CalendarEventViewer(object);
        viewer.setListener(new CalendarListener() {
            @Override
            public void create(Date date) {
                onCreate(date);
            }

            @Override
            public void edit(Act event) {
                onEdit(event);
            }
        });
    }

    public CalendarEventViewer getViewer() {
        return viewer;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new CalendarLayoutStrategy(viewer);
    }

    /**
     * Invoked to create a new event at the specified date/time.
     *
     * @param date the date/time
     */
    protected void onCreate(Date date) {
        if (getObject().isNew()) {
            save();
        }
        Act event = (Act) IMObjectCreator.create("act.calendarEvent");
        event.setActivityStartTime(date);
        IMObjectBean bean = ServiceHelper.getArchetypeService().getBean(event);
        bean.addTarget("schedule", getObject());

        onEdit(event);
    }

    /**
     * Invoked to edit an event.
     *
     * @param event the event to edit
     */
    private void onEdit(Act event) {
        final ScheduleEventSeriesState state = new ScheduleEventSeriesState(event,
                                                                            ServiceHelper.getArchetypeService());
        if (state.hasSeries()) {
            if (state.canEditFuture()) {
                EditSeriesDialog dialog = new EditSeriesDialog(state, getHelpContext().subtopic("editseries"));
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        if (dialog.single()) {
                            edit(event, false);
                        } else if (dialog.future()) {
                            edit(event, true);
                        } else if (dialog.all()) {
                            edit(state.getFirst(), true);
                        }
                    }
                });
                dialog.show();
            } else {
                // can't edit the future events, so disable series editing
                edit(event, false);
            }
        } else {
            // not part of a series, so enable series editing
            edit(event, true);
        }
    }

    /**
     * Edit an event.
     *
     * @param event      the event to edit
     * @param editSeries if {@code true} enable series editing
     */
    private void edit(Act event, boolean editSeries) {
        HelpContext help = getHelpContext().topic(event, "edit");
        Context context = getLayoutContext().getContext();
        LayoutContext layout = new DefaultLayoutContext(context, help);
        IMObjectEditor editor = new CalendarEventEditor(event, null, editSeries, layout);
        EditDialog dialog = EditDialogFactory.create(editor, context);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onClose(WindowPaneEvent event) {
                viewer.refresh();
            }
        });
        dialog.show();
    }

}
