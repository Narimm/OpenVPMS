package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableCellRenderer;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Renders the day column headers for {@link MultiDayTableModel}.
 *
 * @author Tim Anderson
 */
class MultiDayTableHeaderRenderer implements TableCellRenderer {

    /**
     * The singleton instance.
     */
    public static final MultiDayTableHeaderRenderer INSTANCE = new MultiDayTableHeaderRenderer();

    /**
     * Returns a component that will be displayed at the specified coordinate in the table.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value
     */
    @Override
    public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
        Component result;
        if (value instanceof Date) {
            Label label = LabelFactory.create();
            Date date = (Date) value;
            String text;
            if (DateRules.compareDateToToday(date) == 0) {
                label.setStyleName("MultiDayTable.Today");
                text = Messages.format("workflow.scheduling.appointment.column.today", date);
            } else {
                label.setStyleName("Table.Header");
                text = Messages.format("workflow.scheduling.appointment.column.day", date);
            }
            label.setText(text);
            result = label;
        } else {
            result = new Label();
        }
        return result;
    }
}
