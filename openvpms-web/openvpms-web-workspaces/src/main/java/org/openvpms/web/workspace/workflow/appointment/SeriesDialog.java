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

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.MessageDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.appointment.repeat.ScheduleEventSeriesState;

import java.util.List;
import java.util.Map;

/**
 * Dialog that prompts to perform an operation on a calendar series.
 *
 * @author Tim Anderson
 */
public abstract class SeriesDialog extends MessageDialog {

    /**
     * The series state.
     */
    private final ScheduleEventSeriesState series;

    /**
     * If selected, indicates to work on a single event.
     */
    private RadioButton single;

    /**
     * If selected, indicates to work on future events.
     */
    private RadioButton future;

    /**
     * If selected, indicates to work on all events.
     */
    private RadioButton all;

    /**
     * Constructs a {@link SeriesDialog}.
     *
     * @param title   the dialog title
     * @param message the dialog message
     * @param series  the series
     * @param help    the help context
     */
    public SeriesDialog(String title, String message, ScheduleEventSeriesState series, HelpContext help) {
        super(title, message, OK_CANCEL, help);
        this.series = series;
        ButtonGroup group = new ButtonGroup();
        ActionListener listener = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onOK();
            }
        };
        single = ButtonFactory.create(null, group, listener);
        single.setText(Messages.format("workflow.scheduling.appointment.series.single", series.getDisplayName()));
        single.setSelected(true);
        if (series.canEditFuture()) {
            future = ButtonFactory.create(null, group, listener);
            future.setText(Messages.format("workflow.scheduling.appointment.series.future",
                                           series.getDisplayName()));
        }
        if (series.canEditSeries()) {
            all = ButtonFactory.create("workflow.scheduling.appointment.series.all", group, listener);
        }
    }

    /**
     * Determines if a single event should be operated on.
     *
     * @return {@code true} if a single event should be operated on
     */
    public boolean single() {
        return single.isSelected();
    }

    /**
     * Determines if a future events should be operated on.
     *
     * @return {@code true} if future events should be operated on
     */
    public boolean future() {
        return future != null && future.isSelected();
    }

    /**
     * Determines if a all events should be operated on.
     *
     * @return {@code true} if all events should be operated on
     */
    public boolean all() {
        return all != null && all.isSelected();
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        if (future()) {
            List<String> statuses = series.getFutureNonPendingStatuses();
            if (!statuses.isEmpty()) {
                confirm(statuses);
            } else {
                super.onOK();
            }
        } else if (all()) {
            List<String> statuses = series.getNonPendingStatuses();
            if (!statuses.isEmpty()) {
                confirm(statuses);
            } else {
                super.onOK();
            }
        } else {
            super.onOK();
        }
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create(true, true);
        message.setText(getMessage());
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, message, single);
        if (future != null) {
            column.add(future);
        }
        if (all != null) {
            column.add(all);
        }
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }


    /**
     * Confirms an operation if there are appointments with non-Pending status.
     *
     * @param statuses the statuses
     */
    private void confirm(List<String> statuses) {
        Map<String, String> names = LookupNameHelper.getLookupNames(ScheduleArchetypes.APPOINTMENT, "status");

        String message;
        if (statuses.size() == 1) {
            message = Messages.format("workflow.scheduling.appointment.series.nonpending1",
                                      names.get(statuses.get(0)));
        } else {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < statuses.size() - 1; ++i) {
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(names.get(statuses.get(i)));
            }
            String last = names.get(statuses.get(statuses.size() - 1));
            message = Messages.format("workflow.scheduling.appointment.series.nonpending2", buffer, last);
        }
        ConfirmationDialog dialog = new ConfirmationDialog(getTitle(), message);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                SeriesDialog.super.onOK();
            }

            @Override
            public void onCancel() {
                SeriesDialog.super.onCancel();
            }
        });
        dialog.show();
    }
}
