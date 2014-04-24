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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.joda.time.DateTime;
import org.openvpms.archetype.i18n.time.DateDurationFormatter;
import org.openvpms.archetype.i18n.time.DurationFormatter;
import org.openvpms.archetype.rules.workflow.Slot;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractTableBrowser;
import org.openvpms.web.component.im.query.IterableBackedResultSet;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A browser for free appointment slots.
 *
 * @author Tim Anderson
 */
public class FreeAppointmentSlotBrowser extends AbstractTableBrowser<Slot> {

    /**
     * The query.
     */
    private final FreeAppointmentSlotQuery query;

    /**
     * The free slot duration formatter.
     */
    private static DurationFormatter formatter = DateDurationFormatter.create(true, true, true, true, true, true);

    /**
     * Constructs a {@link FreeAppointmentSlotBrowser}.
     *
     * @param layoutContext the layout context
     */
    public FreeAppointmentSlotBrowser(FreeAppointmentSlotQuery query, LayoutContext layoutContext) {
        super(new SlotTableModel(), layoutContext);
        this.query = query;
        query.setListener(new QueryListener() {
            public void query() {
                onQuery();
            }
        });
    }

    /**
     * Returns the schedule for a slot.
     *
     * @param slot the slot
     * @return the corresponding schedule or {@code null} if none is found
     */
    public Entity getSchedule(Slot slot) {
        for (Entity schedule : query.getViewSchedules()) {
            if (slot.getSchedule() == schedule.getId()) {
                return schedule;
            }
        }
        return null;
    }

    /**
     * Query using the specified criteria, and populate the browser with matches.
     */
    @Override
    public void query() {
        Iterable<Slot> iterable = new Iterable<Slot>() {
            @Override
            public Iterator<Slot> iterator() {
                return query.query();
            }
        };
        ((SlotTableModel) getTableModel()).setSchedules(query.getSelectedSchedules());
        ResultSet<Slot> set = new IterableBackedResultSet<Slot>(iterable, 20);

        Component component = getComponent();

        doLayout(component, hasResults(set));

        PagedIMTable<Slot> table = getTable();
        table.setResultSet(set);
        setFocusOnResults();
    }

    /**
     * Lays out this component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
    }

    /**
     * Performs a query and notifies registered listeners.
     */
    private void onQuery() {
        query();
        notifyBrowserListeners();
    }

    private static class SlotTableModel extends AbstractIMTableModel<Slot> {

        /**
         * The schedule names, keyed on schedule identifier.
         */
        private Map<Long, String> names = Collections.emptyMap();

        /**
         * The schedule name column.
         */
        private static final int SCHEDULE = 0;

        /**
         * The slot start date column.
         */
        private static final int DATE = 1;

        /**
         * The slot start time column.
         */
        private static final int TIME = 2;

        /**
         * The slot duration column.
         */
        private static final int DURATION = 3;

        public SlotTableModel() {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(SCHEDULE, "workflow.scheduling.query.schedule"));
            model.addColumn(createTableColumn(DATE, "table.act.date"));
            model.addColumn(createTableColumn(TIME, "table.act.time"));
            model.addColumn(createTableColumn(DURATION, "workflow.scheduling.appointment.find.duration"));
            setTableColumnModel(model);
        }

        /**
         * Sets the schedules being viewed.
         *
         * @param schedules the schedules
         */
        public void setSchedules(List<Entity> schedules) {
            names = new HashMap<Long, String>();
            for (Entity schedule : schedules) {
                names.put(schedule.getId(), schedule.getName());
            }
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
         * @return the sort criteria, or {@code null} if the column isn't sortable
         */
        @Override
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            return null;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param slot   the slot
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(Slot slot, TableColumn column, int row) {
            Object result = null;
            Date startTime = slot.getStartTime();
            switch (column.getModelIndex()) {
                case SCHEDULE:
                    result = names.get(slot.getSchedule());
                    break;
                case DATE:
                    DateTime dt = new DateTime(startTime);
                    DateFormat format;
                    if (dt.getYear() == new DateTime().getYear()) {
                        format = DateFormatter.getDayMonthDateFormat();
                    } else {
                        format = DateFormatter.getFullDateFormat();
                    }
                    result = format.format(startTime);
                    break;
                case TIME:
                    result = DateFormatter.formatTime(startTime, false);
                    break;
                case DURATION:
                    result = formatter.format(startTime, slot.getEndTime());
                    break;
            }
            return result;
        }
    }
}
