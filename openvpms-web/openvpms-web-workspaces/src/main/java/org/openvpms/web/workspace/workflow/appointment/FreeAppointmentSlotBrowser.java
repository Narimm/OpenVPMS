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
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.i18n.time.DateDurationFormatter;
import org.openvpms.archetype.i18n.time.DurationFormatter;
import org.openvpms.archetype.rules.workflow.Slot;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractTableBrowser;
import org.openvpms.web.component.im.query.IterableBackedResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Collections;
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
     * Query button id.
     */
    private static final String QUERY_ID = "button.query";

    /**
     * Constructs a {@link FreeAppointmentSlotBrowser}.
     *
     * @param layoutContext the layout context
     */
    public FreeAppointmentSlotBrowser(LayoutContext layoutContext) {
        super(new SlotTableModel(), layoutContext);
        Context context = layoutContext.getContext();
        query = new FreeAppointmentSlotQuery(context.getLocation(), context.getScheduleView(), context.getSchedule(),
                                             context.getScheduleDate());
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
        Component component = query.getComponent();

        ButtonRow row = new ButtonRow(getFocusGroup());
        row.add(component);
        row.addButton(QUERY_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        container.add(row);
    }

    /**
     * Invoked when the query button is pressed. Performs the query and notifies
     * any listeners.
     */
    private void onQuery() {
        query();
        notifyBrowserListeners();
    }


    private static class SlotTableModel extends AbstractIMTableModel<Slot> {

        private Map<Long, String> schedules = Collections.emptyMap();
        private static final int SCHEDULE = 0;
        private static final int FROM = 1;
        private static final int TO = 2;
        private static final int DURATION = 3;

        public SlotTableModel() {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(SCHEDULE, "workflow.scheduling.query.schedule"));
            model.addColumn(createTableColumn(FROM, "workflow.scheduling.appointment.find.from"));
            model.addColumn(createTableColumn(TO, "workflow.scheduling.appointment.find.to"));
            model.addColumn(createTableColumn(DURATION, "workflow.scheduling.appointment.find.duration"));
            setTableColumnModel(model);
        }

        public void setSchedules(List<Entity> schedules) {
            this.schedules = new HashMap<Long, String>();
            for (Entity schedule : schedules) {
                this.schedules.put(schedule.getId(), schedule.getName());
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
            switch (column.getModelIndex()) {
                case SCHEDULE:
                    result = schedules.get(slot.getSchedule());
                    break;
                case FROM:
                    result = DateFormatter.formatDateTime(slot.getStartTime(), false);
                    break;
                case TO:
                    result = DateFormatter.formatDateTime(slot.getEndTime(), false);
                    break;
                case DURATION:
                    result = formatter.format(slot.getStartTime(), slot.getEndTime());
                    break;
            }
            return result;
        }
    }
}
