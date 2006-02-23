package org.openvpms.web.component.util;

import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableModel;


/**
 * Factory for {@link Table}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public final class TableFactory extends ComponentFactory {

    /**
     * Create a new table.
     *
     * @param columns the initial column count
     * @param rows    the initial row count
     * @return a new table
     */
    public static Table create(int columns, int rows) {
        Table table = new Table(columns, rows);
        setDefaults(table);
        return table;
    }

    /**
     * Create a new table.
     *
     * @param model the table model
     * @return a new table
     */
    public static Table create(TableModel model) {
        Table table = new Table(model);
        setDefaults(table);
        return table;
    }

}
