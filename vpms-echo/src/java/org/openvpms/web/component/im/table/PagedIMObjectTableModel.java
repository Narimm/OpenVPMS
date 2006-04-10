package org.openvpms.web.component.im.table;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.table.PageableTableModel;
import org.openvpms.web.component.table.SortableTableModel;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PagedIMObjectTableModel
        extends AbstractTableModel
        implements PageableTableModel, SortableTableModel, IMObjectTableModel {

    /**
     * The result set.
     */
    private ResultSet _set;

    /**
     * The model to delegate to.
     */
    private final IMObjectTableModel _model;

    /**
     * The current page.
     */
    private int _page;

    /**
     * The sort column.
     */
    private int _sortColumn;


    /**
     * Construct a new <code>PagedIMObjectTableModel</code>.
     *
     * @param model the underlying table model.
     */
    public PagedIMObjectTableModel(IMObjectTableModel model) {
        _model = model;
        _model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                notifyListeners(event);
            }
        });
    }

    /**
     * Sets the result set.
     *
     * @param set the result set
     */
    public void setResultSet(ResultSet set) {
        _set = set;
        String node = set.getSortNode();
        _sortColumn = (node != null) ? getNodeColumn(node) : -1;
        setPage(0);
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return _model.getColumnCount();
    }

    /**
     * Returns <code>Object.class</code>
     *
     * @see TableModel#getColumnClass(int)
     */
    @Override
    public Class getColumnClass(int column) {
        return _model.getColumnClass(column);
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return _model.getColumnName(column);
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    public int getRowCount() {
        return _model.getRowCount();
    }

    /**
     * Sets the current page.
     *
     * @param page the page to set
     */
    public void setPage(int page) {
        List<IMObject> objects = Collections.emptyList();
        IPage<IMObject> result = _set.getPage(page);
        if (result != null) {
            int rows = result.getTotalNumOfRows();
            if (rows > 0) {
                objects = result.getRows();
            }
        }
        _page = page;
        _model.setObjects(objects);
    }

    /**
     * Returns the current page.
     *
     * @return the current page
     */
    public int getPage() {
        return _page;
    }

    /**
     * Returns the total number of pages.
     *
     * @return the total number of pages
     */
    public int getPages() {
        return _set.getPages();
    }

    /**
     * Returns the number of rows per page.
     *
     * @return the number. of rows per page
     */
    public int getRowsPerPage() {
        return _set.getRowsPerPage();
    }

    /**
     * Returns the total number of rows. <em>NOTE: </em> the {@link
     * #getRowCount} method returns the number of visible rows.
     *
     * @return the total number of rows
     */
    public int getRows() {
        return _set.getRows();
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     * @return the value at the given coordinate.
     */
    public Object getValueAt(int column, int row) {
        return _model.getValueAt(column, row);
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    public List<IMObject> getObjects() {
        return _model.getObjects();
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return _model.getColumnModel();
    }

    /**
     * Sort the table rows.
     *
     * @param column    the column to sort on
     * @param ascending if <code>true</code> sort the column ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    public void sort(int column, boolean ascending) {
        String node = getNode(column);
        if (node != null) {
            _sortColumn = column;
            _set.sort(node, ascending);
            setPage(0);
        } else {
            _sortColumn = -1;
        }
    }

    /**
     * Returns the sort column.
     *
     * @return the sort column
     */
    public int getSortColumn() {
        return _sortColumn;
    }

    /**
     * Determines if the sort column is sorted ascending or descending.
     *
     * @return <code>true</code> if the column is sorted ascending;
     *         <code>false</code> if it is sorted descending
     */
    public boolean isSortedAscending() {
        return _set.isSortedAscending();
    }

    /**
     * Returns the node name associated with a column.
     *
     * @param column the column
     * @return the name of the node associated with the column, or
     *         <code>null</code>
     */
    public String getNode(int column) {
        return _model.getNode(column);
    }

    /**
     * Determines if selection should be enabled.
     *
     * @return <code>true</code> if selection should be enabled; otherwise
     *         <code>false</code>
     */
    public boolean getEnableSelection() {
        return _model.getEnableSelection();
    }

    /**
     * Returns the column associated witha node.
     *
     * @param node the node
     * @return the column associated with <code>node</code> or <code>-1</code>
     *         ifthe node isn't associated with any column
     */
    protected int getNodeColumn(String node) {
        int result = -1;
        Iterator iterator = _model.getColumnModel().getColumns();
        while (iterator.hasNext()) {
            TableColumn column = (TableColumn) iterator.next();
            String modelNode = _model.getNode(column.getModelIndex());
            if (node.equals(modelNode)) {
                result = column.getModelIndex();
                break;
            }
        }
        return result;
    }

    /**
     * Notify listeners of an update to the underlying table.
     *
     * @param event the event
     */
    protected void notifyListeners(TableModelEvent event) {
        if (event.getType() == TableModelEvent.STRUCTURE_CHANGED) {
            fireTableStructureChanged();
        } else {
            fireTableDataChanged();
        }
    }

}
