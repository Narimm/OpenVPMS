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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.workflow.CalendarBlock;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dialog to display overlapping calendar events.
 *
 * @author Tim Anderson
 */
class OverlappingCalendarEventDialog extends ModalDialog {

    /**
     * The dialog message.
     */
    private final String message;

    /**
     * The overlaps. A list of appointment times and calendar blocks.
     */
    private final List<Object> overlaps = new ArrayList<>();

    /**
     * Constructs an {@link OverlappingCalendarEventDialog}.
     *
     * @param title   the dialog title
     * @param message the dialog message
     * @param event   the event
     * @param buttons the dialog buttons
     */
    public OverlappingCalendarEventDialog(String title, String message, Times event, String[] buttons) {
        super(title, "MediumWidthHeightDialog", buttons);
        this.message = message;
        overlaps.add(event);
    }

    /**
     * Constructs an {@link OverlappingCalendarEventDialog}.
     *
     * @param title        the dialog title
     * @param message      the dialog message
     * @param appointments the overlapped appointments
     * @param blocks       the overlapped calendar blocks
     */
    public OverlappingCalendarEventDialog(String title, String message, List<Times> appointments,
                                          List<CalendarBlock> blocks) {
        super(title, "MediumWidthHeightDialog", OK_CANCEL);
        this.message = message;
        overlaps.addAll(appointments);
        overlaps.addAll(blocks);
    }

    /**
     * Lays out the component prior to display.
     * This implementation is a no-op.
     */
    @Override
    protected void doLayout() {
        Label label = LabelFactory.create(true);
        label.setText(message);
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, label, getTable());
        Column inset = ColumnFactory.create(Styles.LARGE_INSET, column);
        getLayout().add(inset);
    }

    /**
     * Renders the component.
     *
     * @return the component
     */
    private Component getTable() {
        ResultSet<Object> set = new ListResultSet<>(overlaps, 20);
        OverlapModel model = new OverlapModel();
        PagedIMTable<Object> table = new PagedIMTable<>(model, set);
        return table.getComponent();
    }

    private static class OverlapModel extends AbstractIMTableModel<Object> {

        /**
         * Archetype column index.
         */
        public static final int ARCHETYPE_INDEX = 0;

        /**
         * Start time column index.
         */
        private static final int START_INDEX = 1;

        /**
         * End time column index.
         */
        private static final int END_INDEX = 2;

        /**
         * Description column index.
         */
        private static final int DESCRIPTION_INDEX = 3;

        /**
         * Constructs a {@link OverlapModel}.
         */
        public OverlapModel() {
            TableColumn startTime = new TableColumn(START_INDEX);
            TableColumn endTime = new TableColumn(END_INDEX);
            startTime.setHeaderValue(DescriptorHelper.getDisplayName(ScheduleArchetypes.APPOINTMENT, "startTime"));
            endTime.setHeaderValue(DescriptorHelper.getDisplayName(ScheduleArchetypes.APPOINTMENT, "endTime"));

            DefaultTableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(ARCHETYPE_INDEX, ARCHETYPE));
            model.addColumn(startTime);
            model.addColumn(endTime);
            model.addColumn(createTableColumn(DESCRIPTION_INDEX, DESCRIPTION));
            setTableColumnModel(model);
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if {@code true} sort in ascending order; otherwise
         *                  sort in {@code descending} order
         * @return the sort criteria, or {@code null} if the column isn't
         * sortable
         */
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            return null;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        protected Object getValue(Object object, TableColumn column, int row) {
            int index = column.getModelIndex();
            Object result = null;
            switch (index) {
                case ARCHETYPE_INDEX:
                    result = getArchetypeName(object);
                    break;
                case START_INDEX:
                    result = getStartTime(object);
                    break;
                case END_INDEX:
                    result = getEndTime(object);
                    break;
                case DESCRIPTION_INDEX:
                    result = getDescription(object);
                    break;
            }
            return result;
        }

        private String getArchetypeName(Object object) {
            if (object instanceof Times) {
                return DescriptorHelper.getDisplayName(ScheduleArchetypes.APPOINTMENT);
            }
            return DescriptorHelper.getDisplayName(ScheduleArchetypes.CALENDAR_BLOCK);
        }

        private String getStartTime(Object object) {
            Date startTime;
            if (object instanceof Times) {
                startTime = ((Times) object).getStartTime();
            } else {
                startTime = ((CalendarBlock) object).getStartTime();
            }
            return DateFormatter.formatDateTimeAbbrev(startTime);
        }

        private String getEndTime(Object object) {
            Date startTime;
            if (object instanceof Times) {
                startTime = ((Times) object).getEndTime();
            } else {
                startTime = ((CalendarBlock) object).getEndTime();
            }
            return DateFormatter.formatDateTimeAbbrev(startTime);
        }

        private String getDescription(Object object) {
            String result = null;
            if (object instanceof CalendarBlock) {
                result = ((CalendarBlock) object).getName();
            }
            return result;
        }
    }

}
