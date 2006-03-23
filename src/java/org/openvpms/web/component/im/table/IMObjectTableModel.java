package org.openvpms.web.component.im.table;

import java.util.List;

import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.table.SortableTableModel;


/**
 * Table model for {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectTableModel extends SortableTableModel {

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    List<IMObject> getObjects();

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    void setObjects(List<IMObject> objects);

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    TableColumnModel getColumnModel();

    /**
     * Returns the node name associated with a column.
     *
     * @param column the column
     * @return the name of the node associated with the column, or
     *         <code>null</code>
     */
    String getNode(int column);

}
