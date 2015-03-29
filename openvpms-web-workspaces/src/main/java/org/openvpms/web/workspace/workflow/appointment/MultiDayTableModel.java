package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.util.Date;

/**
 * Appointment table model for appointments that span multiple days.
 *
 * @author Tim Anderson
 */
public class MultiDayTableModel extends ScheduleTableModel {

    /**
     * Constructs a {@link MultiDayTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     */
    public MultiDayTableModel(MultiDayScheduleGrid grid, Context context) {
        super(grid, context, false);
    }

    /**
     * Returns the slot of the specified event.
     *
     * @param schedule the schedule
     * @param eventRef the event reference
     * @return the slot, or {@code -1} if the event is not found
     */
    @Override
    public int getSlot(Schedule schedule, IMObjectReference eventRef) {
        PropertySet event = schedule.getEvent(eventRef);
        if (event != null) {
            return getGrid().getSlot(event.getDate(ScheduleEvent.ACT_START_TIME));
        }
        return -1;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param column the column
     * @param row    the row
     * @return the cell value
     */
    @Override
    public Object getValueAt(int column, int row) {
        Object result = null;
        if (column == 0) {
            Schedule schedule = getSchedule(column, row);
            result = (schedule != null) ? schedule.getName() : null;
        } else {
            PropertySet set = getEvent(column, row);
            MultiDayScheduleGrid grid = (MultiDayScheduleGrid) getGrid();
            if (set != null) {
                result = getEvent(set);
                int span = grid.getSlots(set, column - 1);
                if (span > 1) {
                    if (column + span > getColumnCount()) {
                        Label next = LabelFactory.create();
                        next.setFont(new Font(Font.SANS_SERIF, Font.BOLD, new Extent(15)));
                        next.setText(">");
                        RowLayoutData newValue = new RowLayoutData();
                        newValue.setAlignment(Alignment.ALIGN_RIGHT);
                        newValue.setWidth(Styles.FULL_WIDTH);
                        newValue.setInsets(new Insets(5, 0));
                        next.setLayoutData(newValue);
                        result = RowFactory.create((Component) result, next);
                    }
                    setColumnSpan((Component) result, span);
                }
            }
        }
        return result;
    }

    /**
     * Returns the event at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the event, or {@code null} if none is found
     */
    @Override
    public PropertySet getEvent(int column, int row) {
        if (column == 0) {
            return null;
        }
        Schedule schedule = getSchedule(column, row);
        return (schedule != null) ? getGrid().getEvent(schedule, column - 1) : null;
    }

    /**
     * Returns a component representing an event.
     *
     * @param event the event
     * @return a new component
     */
    private Component getEvent(PropertySet event) {
        String text = evaluate(event);
        if (text == null) {
            String customer = event.getString(ScheduleEvent.CUSTOMER_NAME);
            String patient = event.getString(ScheduleEvent.PATIENT_NAME);
            String status = AppointmentTableModel.getStatus(event);
            String reason = event.getString(ScheduleEvent.ACT_REASON_NAME);
            if (reason == null) {
                // fall back to the code
                reason = event.getString(ScheduleEvent.ACT_REASON);
            }

            if (patient == null) {
                text = Messages.format(
                        "workflow.scheduling.appointment.table.customer",
                        customer, reason, status);
            } else {
                text = Messages.format(
                        "workflow.scheduling.appointment.table.customerpatient",
                        customer, patient, reason, status);
            }
        }

        String notes = event.getString(ScheduleEvent.ACT_DESCRIPTION);
        return createLabelWithNotes(text, notes);
    }

    /**
     * Creates a column model to display a list of schedules.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(ScheduleEventGrid grid) {
        DefaultTableColumnModel result = new DefaultTableColumnModel();
        Date start = grid.getDate();
        int modelIndex = 0;
        result.addColumn(new Column(modelIndex++, "Schedule"));
        for (int i = 0; i < grid.getSlots(); ++i) {
            result.addColumn(new DateColumn(modelIndex++, DateRules.getDate(start, i, DateUnits.DAYS)));
        }
        return result;
    }

}
